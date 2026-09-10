package app.hifis.shared.work

/**
 * 업무 화면 문구 — 두 플랫폼이 **같은 말**을 한다
 *
 * 업무 탭에는 **공통 업무와 내 업무만** 든다 (2026-09-10 대표 결정).
 * 동료 평가·회원 친절도·센터 기여도는 V2 에서 여기 탭으로 같이 있었는데,
 * 매일 하는 일과 가끔 보는 것이 한 줄에 서 있어서 매일 하는 사람이 매일 한 번 더 골랐다.
 * 수업 개수는 PT 라 TeamFIS 로 간다 — 이 앱에 없다.
 */
object WorkBoard {
    const val TITLE = "업무"

    /** 위 두 칸 — 왼쪽이 공통, 오른쪽이 내 것이다 */
    const val COMMON = "공통 업무"
    const val MINE = "내 업무"

    // ── 공통 업무 ──

    /** 칩 격자의 머리말 */
    const val TODAY_ITEMS = "오늘 점검 항목"

    /** 그 머리말 오른쪽 — 누르면 오늘 수행 내역이 열린다 */
    fun totalLabel(total: Int): String = "총 ${total}회"

    /** 점검 항목이 하나도 없다 — 지점이 아직 안 정했다 */
    const val EMPTY_ITEMS = "점검 항목이 아직 없어요."

    /** 오늘 몇 번 했는지 다 더한다 — 머리말 오른쪽 숫자 */
    fun total(counts: Map<String, Int>): Int = counts.values.sum()

    // ── 내 업무 ──

    /** 내 업무 목록의 머리말 */
    const val MY_TODAY = "오늘 할 일"

    /** 다 했다 — 숫자 대신 이 말이 뜬다 */
    const val DONE_ALL = "완료"

    /** 오늘 할 일이 하나도 없다 */
    const val EMPTY_TASKS = "오늘 할 일이 없어요."

    /** 체크한 줄 수 */
    fun doneCount(tasks: List<MyTask>): Int = tasks.count { it.checked }

    /** 머리말 오른쪽 — 다 했으면 [DONE_ALL], 아니면 `3/5` */
    fun progressLabel(tasks: List<MyTask>): String {
        val done = doneCount(tasks)
        return if (tasks.isNotEmpty() && done == tasks.size) DONE_ALL else "$done/${tasks.size}"
    }

    /** 진행 막대가 찰 비율 — 할 일이 없으면 0 이다 (0 으로 나누지 않는다) */
    fun progress(tasks: List<MyTask>): Float =
        if (tasks.isEmpty()) 0f else doneCount(tasks).toFloat() / tasks.size
}
