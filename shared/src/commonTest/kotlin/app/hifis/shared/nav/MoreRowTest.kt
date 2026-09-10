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
        val expected = MainTab.all - MainTab.HOME - MainTab.MORE

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
    fun `묶음마다 머리말이 있다`() {
        // 판으로 안 싸서 머리말이 유일한 경계다 — 비면 위 묶음에 딸린 것처럼 보인다
        MoreGroup.all.forEach { group ->
            assertTrue(group.title.isNotBlank(), "$group 에 머리말이 없다")
        }
    }
}
