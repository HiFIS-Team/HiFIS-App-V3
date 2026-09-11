package app.hifis.shared.nav

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * 전체 목록은 **빠짐없는 명단**이어야 한다 — 그 그물을 여기서 짠다.
 *
 * V2 는 갈 수 있는 곳을 세는 자리가 셋이었고(아이폰 2단바·안드로이드 8칸·데스크톱
 * 사이드바) 그 사이로 화면이 새어 나갔다. 일정·조직도·전자결재는 폰에서 자리를
 * 못 얻어 알림을 눌러도 안 열렸다.
 *
 * 화면을 새로 만들면 **여기서 먼저 깨진다.** 깨지면 [MoreRow] 에 줄을 추가한다 —
 * 테스트를 고치는 게 아니다.
 */
class MoreRowTest {

    @Test
    fun `하단바 화면은 홈만 빼고 전부 전체에 있다`() {
        // 홈은 첫 칸에 늘 있고 앱이 거기서 시작한다 — 목록에서 찾을 일이 없다.
        // 전체(MORE)는 자기 자신이라 뺀다
        // **HiFIS 탭만 본다.** `전체` 는 HiFIS 의 명단이라 TeamFIS 탭(회원·수업)은 안 든다
        val expected = MainTab.android(Product.HIFIS) - MainTab.HOME - MainTab.MORE

        expected.forEach { tab ->
            val row = MoreRow.all.firstOrNull { it.tab == tab }
            assertNotNull(row, "${tab.label} 탭이 전체 목록에 없다")
            assertEquals(tab.label, row.label, "${tab.label} — 이름이 하단바와 다르다")
            assertEquals(tab.icon, row.icon, "${tab.label} — 아이콘이 하단바와 다르다")
        }
    }

    @Test
    fun `홈 바로가기는 전부 전체에 있다`() {
        HomeShortcut.all.forEach { shortcut ->
            val row = MoreRow.all.firstOrNull { it.label == shortcut.label }
            assertNotNull(row, "${shortcut.label} 바로가기가 전체 목록에 없다")
            assertEquals(shortcut.icon, row.icon, "${shortcut.label} — 아이콘이 바로가기와 다르다")
        }
    }

    @Test
    fun `홈과 전체는 목록에 없다`() {
        // 홈은 시작 자리라서, 전체는 지금 보고 있는 화면이라서 뺀다.
        // 실수로 들어가면 목록 첫 줄이 '홈' 이 되어 이상해진다
        listOf(MainTab.HOME, MainTab.MORE).forEach { tab ->
            assertTrue(
                MoreRow.all.none { it.tab == tab },
                "${tab.label} 이 전체 목록에 들어 있다",
            )
        }
    }

    @Test
    fun `같은 이름이 두 번 나오지 않는다`() {
        val labels = MoreRow.all.map { it.label }
        assertEquals(labels.size, labels.toSet().size, "전체 목록에 같은 이름이 둘 있다: $labels")
    }

    @Test
    fun `같은 아이콘이 두 줄에 쓰이지 않는다`() {
        // 줄에 색도 구분선도 없어서 **아이콘이 줄을 알아보는 유일한 표시**다.
        // 둘이 같은 그림을 쓰면 훑을 때 같은 줄로 읽힌다
        val icons = MoreRow.all.map { it.icon }
        val dup = icons.groupBy { it }.filterValues { it.size > 1 }.keys
        assertTrue(dup.isEmpty(), "전체 목록에서 아이콘이 겹친다: $dup")
    }

    @Test
    fun `묶음은 전부 줄을 하나 이상 가진다`() {
        // 빈 판은 머리말만 뜬 빈 상자로 보인다
        MoreGroup.all.forEach { group ->
            assertTrue(MoreRow.of(group).isNotEmpty(), "$group 묶음이 비었다")
        }
    }

    @Test
    fun `묶음을 나누면 모든 줄이 한 번씩 나온다`() {
        // 화면은 묶음별로 그린다 — 어느 판에도 안 들어가는 줄이 있으면 안 보인다
        val drawn = MoreGroup.all.flatMap { MoreRow.of(it) }
        assertEquals(MoreRow.all, drawn)
    }

    @Test
    fun `iOS 하단바는 네 칸이다`() {
        // 다섯째 칸을 시스템 AI 동그라미가 쓴다. 여섯으로 세우면 iOS 가 `More(…)` 로 접는다
        val ios = MainTab.ios(Product.HIFIS)
        assertEquals(4, ios.size, "iOS 하단바가 네 칸이 아니다: $ios")
        assertEquals(5, MainTab.android(Product.HIFIS).size, "안드로이드 하단바가 다섯 칸이 아니다")
    }

    @Test
    fun `하단바에서 내려온 화면은 그 플랫폼 바로가기가 받는다`() {
        // iOS 는 근태가 탭에서 내려왔다 — 갈 방법이 사라지면 안 된다
        val dropped = MainTab.android(Product.HIFIS) - MainTab.ios(Product.HIFIS).toSet()
        dropped.forEach { tab ->
            assertTrue(
                HomeShortcut.ios.any { it.label == tab.label },
                "${tab.label} 이 iOS 탭에서 빠졌는데 바로가기에도 없다",
            )
        }
    }

    @Test
    fun `바로가기는 같은 플랫폼 하단바와 안 겹친다`() {
        // 바로가기는 **하단바로 못 가는 것**이다. 겹치면 같은 화면이 두 자리에 선다
        fun check(tabs: List<MainTab>, shortcuts: List<HomeShortcut>, who: String) {
            val labels = tabs.map { it.label }.toSet()
            shortcuts.forEach {
                assertTrue(it.label !in labels, "$who — ${it.label} 이 하단바에도 바로가기에도 있다")
            }
        }
        check(MainTab.android(Product.HIFIS), HomeShortcut.android, "안드로이드")
        check(MainTab.ios(Product.HIFIS), HomeShortcut.ios, "iOS")
    }

    /**
     * **TeamFIS 는 양 플랫폼이 같은 네 칸이다** — 홈·일정·회원·수업 (2026-09-11 대표).
     *
     * iOS 만 칸을 줄이던 이유(동그라미 자리가 한 칸을 먹는다)가 여기서는 안 걸린다.
     * 넷뿐이라 동그라미를 더해도 다섯이다.
     */
    @Test
    fun `TeamFIS 탭은 두 플랫폼이 같다`() {
        val android = MainTab.android(Product.TEAMFIS)
        assertEquals(android, MainTab.ios(Product.TEAMFIS))
        assertEquals(4, android.size, "TeamFIS 탭이 넷이 아니다: $android")
        // 업무·근태·전체는 HiFIS 쪽 일이다
        listOf(MainTab.WORK, MainTab.ATTENDANCE, MainTab.MORE).forEach {
            assertTrue(it !in android, "TeamFIS 탭에 ${it.label} 이 있다")
        }
    }

    /** 화면이 하나도 없는 제품은 하단바를 안 세운다 — 없는 곳으로 가는 칸이 선다 */
    @Test
    fun `WeFIS 는 아직 탭이 없다`() {
        assertTrue(MainTab.android(Product.WEFIS).isEmpty())
        assertTrue(MainTab.ios(Product.WEFIS).isEmpty())
        assertEquals(null, MainTab.iosSideSlot(Product.WEFIS))
    }

    /** iOS 동그라미 자리 — HiFIS 는 AI, TeamFIS 는 검색 (애플뮤직 자리) */
    @Test
    fun `iOS 동그라미는 제품마다 다른 것이 앉는다`() {
        assertEquals(MainTab.SideSlot.AI, MainTab.iosSideSlot(Product.HIFIS))
        assertEquals(MainTab.SideSlot.SEARCH, MainTab.iosSideSlot(Product.TEAMFIS))
    }

    /**
     * **AI 를 세우는 제품은 한 곳에서 정한다** ([Product.hasAi])
     *
     * 자리가 둘이라 갈리기 쉽다 — 안드로이드는 떠 있는 단추, iOS 는 탭바 동그라미다.
     * 한쪽만 고치면 딴 제품에서 답할 것 없는 AI 가 열린다.
     */
    @Test
    fun `AI 는 제품 하나만 세운다`() {
        Product.all.forEach { product ->
            assertEquals(
                product.hasAi,
                MainTab.iosSideSlot(product) == MainTab.SideSlot.AI,
                "${product.label} 의 AI 자리가 두 플랫폼에서 어긋난다",
            )
        }
        assertTrue(Product.HIFIS.hasAi)
        assertEquals(1, Product.all.count { it.hasAi })
    }

    /**
     * **어느 제품이든 첫 탭은 홈이다** — 제품 고르개가 거기 서기 때문이다.
     *
     * 고르개는 홈 화면에만 있다. 홈 탭이 없거나 첫 칸이 아니면, 그 제품에 들어간 사람이
     * **다른 제품으로 나올 길을 잃는다.** 실제로 TeamFIS 홈을 자리 문구로 바꿨다가
     * 고르개가 같이 사라져 갇혔다 (2026-09-11, 대표가 걸렸다).
     */
    @Test
    fun `제품마다 첫 탭은 홈이다`() {
        Product.all.forEach { product ->
            listOf("안드로이드" to MainTab.android(product), "iOS" to MainTab.ios(product))
                .filter { it.second.isNotEmpty() }
                .forEach { (who, tabs) ->
                    assertEquals(
                        MainTab.HOME,
                        tabs.first(),
                        "$who ${product.label} 의 첫 탭이 홈이 아니다 — 제품 고르개에 못 닿는다",
                    )
                }
        }
    }

    /** 한 하단바에 같은 칸이 두 번 서지 않는다 */
    @Test
    fun `제품별 탭 목록에 중복이 없다`() {
        Product.all.forEach { product ->
            listOf("안드로이드" to MainTab.android(product), "iOS" to MainTab.ios(product))
                .forEach { (who, tabs) ->
                    assertEquals(tabs.size, tabs.toSet().size, "$who ${product.label} 탭에 중복이 있다")
                    assertTrue(tabs.size <= 5, "$who ${product.label} 탭이 다섯을 넘는다: $tabs")
                }
        }
    }

    @Test
    fun `묶음마다 머리말이 있다`() {
        // 판으로 안 싸서 머리말이 유일한 경계다 — 비면 위 묶음에 딸린 것처럼 보인다
        MoreGroup.all.forEach { group ->
            assertTrue(group.title.isNotBlank(), "$group 에 머리말이 없다")
        }
    }
}
