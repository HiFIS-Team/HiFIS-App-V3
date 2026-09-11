package app.hifis.shared.work

import kotlinx.datetime.TimeZone
import kotlinx.datetime.isoDayNumber
import kotlinx.datetime.todayIn
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

/**
 * 업무 화면 문구 — 두 플랫폼이 **같은 말**을 한다
 *
 * 업무 탭에는 **공통 업무와 개인 업무만** 든다 (2026-09-10 대표 결정).
 * 동료 평가·회원 친절도·센터 기여도는 V2 에서 여기 탭으로 같이 있었는데,
 * 매일 하는 일과 가끔 보는 것이 한 줄에 서 있어서 매일 하는 사람이 매일 한 번 더 골랐다.
 * 수업 개수는 PT 라 TeamFIS 로 간다 — 이 앱에 없다.
 */
object WorkBoard {
    const val TITLE = "업무"

    /** 위 두 칸 — 왼쪽이 공통, 오른쪽이 내 것이다 */
    const val COMMON = "공통 업무"
    const val MINE = "개인 업무"

    // ── 공통 업무 ──

    /** 칩 격자의 머리말 */
    const val TODAY_ITEMS = "오늘 점검 항목"

    /** 그 머리말 오른쪽 — 누르면 오늘 수행 내역이 열린다 */
    fun totalLabel(total: Int): String = "총 ${total}회"

    /** 점검 항목이 하나도 없다 — 지점이 아직 안 정했다 */
    const val EMPTY_ITEMS = "점검 항목이 아직 없어요."

    /** 오늘 몇 번 했는지 다 더한다 — 머리말 오른쪽 숫자 */
    fun total(counts: Map<String, Int>): Int = counts.values.sum()

    // ── 개인 업무 ──

    /**
     * 요일 이름 — **ISO 차례다** (1=월 … 7=일)
     *
     * 일요일이 먼저 오는 차례(미국식)를 쓰지 않는다. 근무표가 월요일에 시작한다.
     */
    val DAY_NAMES: List<String> = listOf("월", "화", "수", "목", "금", "토", "일")

    /** 이레 — 요일 줄이 세우는 값 */
    val DAYS: List<Int> = (1..7).toList()

    /**
     * 오늘의 ISO 요일 (1=월 … 7=일)
     *
     * **여기서 한 번만 센다.** 플랫폼마다 세면 iOS 가 어긋난다 —
     * `Calendar` 의 `.weekday` 는 **일요일이 1** 이라 그대로 쓰면 하루씩 밀린다.
     */
    @OptIn(ExperimentalTime::class)
    fun today(): Int =
        Clock.System.todayIn(TimeZone.currentSystemDefault()).dayOfWeek.isoDayNumber

    /** `월` · `일` — 요일 줄의 한 글자 */
    fun dayName(day: Int): String = DAY_NAMES[day - 1]

    /** 보고 있는 날이 오늘인가 */
    fun isToday(day: Int, today: Int): Boolean = day == today

    /**
     * 목록 머리말 — 오늘이면 `오늘 할 일`, 아니면 `수요일 할 일`
     *
     * **줄마다 요일을 안 적는다** (V2 2026-08-20). 이 한 줄과 요일 줄이 이미
     * 어느 날을 보고 있는지 말한다 — 줄마다 `월·수·금` 을 달면 읽을 것만 는다.
     */
    fun dayTitle(day: Int, today: Int): String =
        if (isToday(day, today)) MY_TODAY else "${dayName(day)}요일 할 일"

    /** 오늘 목록의 머리말 */
    const val MY_TODAY = "오늘 할 일"

    /** 다 했다 — 숫자 대신 이 말이 뜬다 */
    const val DONE_ALL = "완료"

    /** 그 요일에 도는 것만 — 안 도는 날에는 아예 안 선다 */
    fun tasksOf(tasks: List<MyTask>, day: Int): List<MyTask> = tasks.filter { it.runsOn(day) }

    /**
     * 그날 것이 하나도 없다 — 오늘이면 `없어요`, 다른 날이면 `안 정했다`
     *
     * 지난 요일에 빈 것은 안 한 것이 아니라 **애초에 안 넣은 것**이라 말이 다르다.
     */
    fun emptyLabel(day: Int, today: Int): String =
        if (isToday(day, today)) "오늘 할 일이 없어요." else "${dayName(day)}요일에 정한 업무가 없어요."

    /**
     * 그 요일 것을 체크할 수 있는가 — **오늘만 된다**
     *
     * 체크는 늘 오늘 날짜로 찍힌다. 다른 요일을 보다 누르면 엉뚱한 날에 남는다
     * (V2 서버 `check_my_task` 가 오늘로 찍는다). 그래서 다른 날은 보기만 한다.
     */
    fun canCheck(day: Int, today: Int): Boolean = isToday(day, today)

    /** 그 요일에 체크한 줄 수 */
    fun doneCount(tasks: List<MyTask>, day: Int): Int = tasks.count { it.isChecked(day) }

    /** 머리말 오른쪽 — 다 했으면 [DONE_ALL], 아니면 `3/5` */
    fun progressLabel(tasks: List<MyTask>, day: Int): String {
        val done = doneCount(tasks, day)
        return if (tasks.isNotEmpty() && done == tasks.size) DONE_ALL else "$done/${tasks.size}"
    }

    /** 진행 막대가 찰 비율 — 할 일이 없으면 0 이다 (0 으로 나누지 않는다) */
    fun progress(tasks: List<MyTask>, day: Int): Float =
        if (tasks.isEmpty()) 0f else doneCount(tasks, day).toFloat() / tasks.size
}
