package app.hifis.shared.home

import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * 진행률은 **두 플랫폼이 같은 답을 내야 하는 계산**이라 여기서 못 박는다.
 *
 * 깨지면 고치기 전에 **의도한 변경인지 먼저 확인한다.**
 */
class TodayWorkTest {
    private val nineToSix = TodayWork(
        status = WorkStatus.IN_PROGRESS,
        shiftStart = "09:00",
        shiftEnd = "18:00",
        checkIn = "08:57",
        checkOut = null,
    )

    @Test
    fun `출근을 안 찍었으면 시간이 지나도 0 이다`() {
        val notIn = nineToSix.copy(checkIn = null)
        assertEquals(0f, notIn.rateAt(13 * 60))
    }

    @Test
    fun `근무 시간표가 없으면 0 에 머문다`() {
        assertEquals(0f, nineToSix.copy(shiftStart = null).rateAt(13 * 60))
        assertEquals(0f, nineToSix.copy(shiftEnd = null).rateAt(13 * 60))
    }

    @Test
    fun `한복판이면 절반이다`() {
        // 09:00~18:00 의 가운데는 13:30
        assertEquals(0.5f, nineToSix.rateAt(13 * 60 + 30))
    }

    @Test
    fun `퇴근을 찍었으면 지금이 아니라 그 시각까지 잰다`() {
        val done = nineToSix.copy(checkOut = "13:30")
        assertEquals(0.5f, done.rateAt(23 * 60))
    }

    @Test
    fun `시작 전과 종료 후는 0 과 1 로 잘린다`() {
        assertEquals(0f, nineToSix.rateAt(7 * 60))
        assertEquals(1f, nineToSix.rateAt(22 * 60))
    }

    @Test
    fun `시간표가 뒤집혀 있으면 0 이다`() {
        val broken = nineToSix.copy(shiftStart = "18:00", shiftEnd = "09:00")
        assertEquals(0f, broken.rateAt(13 * 60))
    }

    @Test
    fun `형식이 깨진 값은 없는 것과 같다`() {
        assertEquals(null, minutesOf("9시"))
        assertEquals(null, minutesOf("09:00:00"))
        assertEquals(540, minutesOf("09:00"))
    }
}
