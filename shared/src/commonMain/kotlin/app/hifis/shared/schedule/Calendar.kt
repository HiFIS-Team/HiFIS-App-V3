package app.hifis.shared.schedule

import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.plus

/**
 * 달력 한 칸
 *
 * **날짜를 미리 다 풀어서 담는다.** 화면 쪽에서 `LocalDate` 를 꺼내 쓰면
 * 안드로이드와 iOS 가 각자의 날짜 API 로 요일·일자를 다시 계산하게 되고,
 * 그러면 같은 달인데 첫 칸이 다른 날이 되는 일이 생긴다.
 *
 * @property inMonth 이 달의 날인가 — 아니면 화면에 **빈 칸**으로 둔다
 * @property weekday 0 = 일 … 6 = 토
 */
data class CalendarCell(
    val date: LocalDate,
    val day: Int,
    val inMonth: Boolean,
    val isToday: Boolean,
    val weekday: Int,
) {
    /** 목록 키 — `2026-07-22` */
    val key: String get() = date.toString()
}

/** 하루치 일정 묶음 — `다가오는 일정` 한 덩어리 */
data class DaySchedule(
    val date: LocalDate,
    val day: Int,
    /** `수` */
    val weekdayLabel: String,
    /** 0 = 일 … 6 = 토 */
    val weekday: Int,
    val events: List<ScheduleEvent>,
) {
    val key: String get() = date.toString()
}

/**
 * 달력 계산 — **두 플랫폼이 같은 답을 내야 해서 여기 둔다**
 *
 * 오늘 날짜를 여기서 읽지 않고 **밖에서 받는다.** 읽으면 테스트가 오늘에 따라
 * 결과가 달라져서 못 박을 수가 없다.
 */
object Calendar {
    /**
     * 년·월·일로 날짜를 만든다 — **화면 쪽에서 날짜 타입을 직접 짓지 않게** 하는 자리
     *
     * 두 플랫폼이 각자 자기 날짜 API 로 만들면 판이 올라갈 때 한쪽만 깨진다.
     */
    fun dateOf(year: Int, month: Int, day: Int): LocalDate = LocalDate(year, month, day)

    /**
     * 이전/다음으로 옮긴다 — **달 보기는 한 달씩, 주 보기는 한 주씩**
     *
     * 이걸 화면에 두면 안드로이드는 한 달, iOS 는 30일 하는 식으로 갈린다.
     */
    fun step(from: LocalDate, monthMode: Boolean, back: Boolean): LocalDate {
        val n = if (back) -1 else 1
        return if (monthMode) {
            from.plus(n, DateTimeUnit.MONTH)
        } else {
            from.plus(n * 7, DateTimeUnit.DAY)
        }
    }

    /** 요일 머리말 — **일요일부터**다 (V2 달력과 같다) */
    val weekdayLabels = listOf("일", "월", "화", "수", "목", "금", "토")

    /** `2026년 7월` */
    fun monthLabel(anyDayInMonth: LocalDate): String =
        "${anyDayInMonth.year}년 ${anyDayInMonth.monthOfYear}월"

    /**
     * 한 달 격자 — 주 단위로 자른다. **일요일 시작**
     *
     * 줄 수는 달마다 다르다 (4~6). 늘 6줄로 두면 짧은 달에 빈 줄이 남는다.
     * 이 달이 아닌 날도 자리를 채우되 `inMonth = false` 로 표시한다 —
     * null 을 담으면 iOS 로 건너갈 때 다루기가 번거롭다.
     */
    fun monthGrid(anyDayInMonth: LocalDate, today: LocalDate): List<List<CalendarCell>> {
        val first = LocalDate(anyDayInMonth.year, anyDayInMonth.month, 1)
        val start = first.minusDays(sundayIndex(first))
        val last = first.plus(1, DateTimeUnit.MONTH).minusDays(1)
        val end = last.plus(6 - sundayIndex(last), DateTimeUnit.DAY)

        val weeks = mutableListOf<List<CalendarCell>>()
        var cursor = start
        while (cursor <= end) {
            weeks += (0..6).map { offset ->
                cell(cursor.plus(offset, DateTimeUnit.DAY), anyDayInMonth.monthOfYear, today)
            }
            cursor = cursor.plus(7, DateTimeUnit.DAY)
        }
        return weeks
    }

    /** 한 주 격자 — 그 날이 든 주 하나. **일요일 시작** */
    fun weekGrid(anyDayInWeek: LocalDate, today: LocalDate): List<List<CalendarCell>> {
        val start = anyDayInWeek.minusDays(sundayIndex(anyDayInWeek))
        return listOf(
            (0..6).map { offset ->
                // 주 보기에서는 이 주의 날이 다 '이 달'이다 — 흐리게 만들 이유가 없다
                cell(start.plus(offset, DateTimeUnit.DAY), month = -1, today = today)
            },
        )
    }

    /**
     * `다가오는 일정` — **오늘부터** 앞으로 [days] 일치를 날짜별로 묶는다
     *
     * 일정이 없는 날은 **빼 버린다.** 빈 날까지 세우면 목록이 대부분 빈 줄이 된다.
     */
    fun upcoming(
        events: List<ScheduleEvent>,
        today: LocalDate,
        days: Int = 14,
    ): List<DaySchedule> {
        val until = today.plus(days, DateTimeUnit.DAY)
        return events
            .filter { it.date >= today && it.date <= until }
            .groupBy { it.date }
            .toList()
            .sortedBy { (date, _) -> date.toString() }
            .map { (date, list) ->
                DaySchedule(
                    date = date,
                    day = date.day,
                    weekdayLabel = weekdayLabels[sundayIndex(date)],
                    weekday = sundayIndex(date),
                    // 시각이 없는 것(종일)이 먼저, 그 다음 이른 시각부터
                    events = list.sortedBy { it.start ?: "" },
                )
            }
    }

    /** 그 날에 걸린 일정들 — 달력 점을 찍을 때 쓴다 */
    fun eventsOn(events: List<ScheduleEvent>, date: LocalDate): List<ScheduleEvent> =
        events.filter { it.date == date }

    /** 0 = 일 … 6 = 토. `dayOfWeek` 는 월요일이 1 이라 한 칸 돌린다 */
    private fun sundayIndex(date: LocalDate): Int = (date.dayOfWeek.ordinal + 1) % 7

    private fun cell(date: LocalDate, month: Int, today: LocalDate) = CalendarCell(
        date = date,
        day = date.day,
        inMonth = month < 0 || date.monthOfYear == month,
        isToday = date == today,
        weekday = sundayIndex(date),
    )

    private fun LocalDate.minusDays(n: Int): LocalDate = plus(-n, DateTimeUnit.DAY)

    /**
     * 1~12 월
     *
     * `Month` 는 enum 이라 `ordinal` 이 0(1월)부터다. 판마다 `number`·`monthNumber`
     * 이름이 오가서 **서수로 직접 센다** — 라이브러리를 올려도 안 깨진다.
     */
    private val LocalDate.monthOfYear: Int get() = month.ordinal + 1
}
