package app.hifis.shared.nav

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * 헤더는 제품마다 다르지만 **아무렇게나 다르면 안 된다** — 그 그물을 여기서 짠다.
 */
class HeaderActionTest {

    private fun everyHeader(): List<Pair<String, List<HeaderAction>>> =
        Product.all.flatMap {
            listOf(
                "안드로이드 ${it.label}" to HeaderAction.android(it),
                "iOS ${it.label}" to HeaderAction.ios(it),
            )
        }

    /**
     * **알림·마이는 어디서도 안 빠진다.**
     *
     * 알림은 눌러서 갈 곳이 생기는 유일한 입구고, 마이는 로그아웃·설정으로 가는 길이다.
     * 둘 중 하나라도 빠지면 그 제품에 들어간 사람이 갇힌다.
     */
    @Test
    fun `알림과 마이는 모든 제품에 있다`() {
        everyHeader().forEach { (who, actions) ->
            HeaderAction.always.forEach {
                assertTrue(it in actions, "$who 헤더에 ${it.label} 이 없다: ${actions.map { a -> a.label }}")
            }
        }
    }

    /**
     * **차례를 안 바꾼다.** 자리를 외운 사람에게 순서가 바뀌면 못 찾는다 —
     * 제품을 옮겼더니 알림이 다른 자리에 있으면 같은 앱으로 안 읽힌다.
     */
    @Test
    fun `제품별 목록은 전체 차례를 지킨다`() {
        everyHeader().forEach { (who, actions) ->
            assertEquals(
                actions.sortedBy { HeaderAction.all.indexOf(it) },
                actions,
                "$who 헤더 차례가 뒤바뀌었다",
            )
        }
    }

    @Test
    fun `한 헤더에 같은 단추가 두 번 안 선다`() {
        everyHeader().forEach { (who, actions) ->
            assertEquals(actions.size, actions.toSet().size, "$who 헤더에 같은 단추가 둘 있다")
        }
    }

    @Test
    fun `아이콘과 이름이 겹치지 않는다`() {
        assertEquals(HeaderAction.all.size, HeaderAction.all.map { it.icon }.toSet().size)
        assertEquals(HeaderAction.all.size, HeaderAction.all.map { it.label }.toSet().size)
    }

    /** TeamFIS 는 출퇴근·사내톡을 안 세운다 — HiFIS 쪽 일이다 (2026-09-11 대표) */
    @Test
    fun `TeamFIS 헤더에는 스캔과 사내톡이 없다`() {
        listOf(
            HeaderAction.android(Product.TEAMFIS),
            HeaderAction.ios(Product.TEAMFIS),
        ).forEach {
            assertTrue(HeaderAction.SCAN !in it, "TeamFIS 헤더에 출퇴근 스캔이 있다")
            assertTrue(HeaderAction.CHAT !in it, "TeamFIS 헤더에 사내톡이 있다")
        }
        // 검색은 안드로이드만 선다
        assertTrue(HeaderAction.SEARCH in HeaderAction.android(Product.TEAMFIS))
        assertTrue(HeaderAction.SEARCH !in HeaderAction.ios(Product.TEAMFIS))
    }
}
