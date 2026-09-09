package app.hifis.shared.schedule

import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * 달력은 **두 플랫폼이 같은 격자를 그려야** 해서 여기서 못 박는다.
 *
 * 깨지면 고치기 전에 **의도한 변경인지 먼저 확인한다.**
 */
class CalendarTest {
    // 2026-07-01 은 수요일이다
    private val july = LocalDate(2026, 7, 15)
    private val today = LocalDate(2026, 7, 22)

    @Test
    fun `한 달 격자는 일요일에서 시작한다`() {
        val weeks = Calendar.monthGrid(july, today)
        assertEquals(0, weeks.first().first().weekday)
        assertEquals(6, weeks.first().last().weekday)
    }

    @Test
    fun `7월은 다섯 줄이다`() {
        // 1일이 수요일이고 31일까지라 3 + 31 = 34 칸 → 다섯 줄
        assertEquals(5, Calendar.monthGrid(july, today).size)
    }

    @Test
    fun `앞뒤로 삐져나온 날은 이 달이 아니라고 표시한다`() {
        val weeks = Calendar.monthGrid(july, today)
        val first = weeks.first()
        // 6월 28·29·30 이 앞에 붙는다
        assertFalse(first[0].inMonth)
        assertFalse(first[2].inMonth)
        assertTrue(first[3].inMonth)
        assertEquals(1, first[3].day)
    }

    @Test
    fun `오늘만 오늘로 표시된다`() {
        val flat = Calendar.monthGrid(july, today).flatten()
        assertEquals(1, flat.count { it.isToday })
        assertEquals(22, flat.first { it.isToday }.day)
    }

    @Test
    fun `한 주 격자는 일곱 칸 한 줄이다`() {
        val weeks = Calendar.weekGrid(today, today)
        assertEquals(1, weeks.size)
        assertEquals(7, weeks[0].size)
        assertEquals(19, weeks[0][0].day) // 그 주의 일요일
    }

    @Test
    fun `다가오는 일정은 오늘부터 날짜순으로 묶인다`() {
        val events = ScheduleEvent.demo(today)
        val groups = Calendar.upcoming(events, today, days = 14)

        // 오늘 것 둘이 한 덩어리
        assertEquals(2, groups.first().events.size)
        assertEquals(22, groups.first().day)
        assertEquals("수", groups.first().weekdayLabel)

        // 날짜가 앞에서 뒤로
        val days = groups.map { it.date.toString() }
        assertEquals(days.sorted(), days)
    }

    @Test
    fun `지난 일정과 기간 밖은 안 나온다`() {
        val events = ScheduleEvent.demo(today)
        val groups = Calendar.upcoming(events, today, days = 2)
        assertTrue(groups.all { it.date >= today })
        assertEquals(3, groups.size) // 오늘 · +1 · +2
    }

    @Test
    fun `종일 일정이 시각 있는 것보다 먼저 선다`() {
        val date = LocalDate(2026, 7, 22)
        val events = listOf(
            ScheduleEvent("a", "오후 회의", EventKind.MEETING, date, "15:00"),
            ScheduleEvent("b", "월차", EventKind.OFF, date, null),
        )
        assertEquals("b", Calendar.upcoming(events, date).first().events.first().id)
    }

    @Test
    fun `모르는 종류는 기타로 떨어진다`() {
        assertEquals(EventKind.LESSON, EventKind.parse("수업"))
        assertEquals(EventKind.ETC, EventKind.parse("없는종류"))
        assertEquals(EventKind.ETC, EventKind.parse(null))
    }

    @Test
    fun `달 이름은 년월로 찍는다`() {
        assertEquals("2026년 7월", Calendar.monthLabel(july))
        assertEquals("2026년 12월", Calendar.monthLabel(LocalDate(2026, 12, 3)))
    }
}
