package app.hifis.shared.teamfis

import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.plus

/** 수업이 어떻게 됐나 — TeamFIS 것과 같은 셋이다 */
enum class ClassStatus { SCHEDULED, DONE, NO_SHOW }

/**
 * TeamFIS 수업 한 칸 — **트레이너가 그날 맡은 PT**
 *
 * TeamFIS 는 트레이너 것이고 여기서 하는 일은 **수업과 세션 싸인**이다
 * (`.claude/DESIGN.md` 전체 목록). 그래서 칸에 드는 것은 상품·회차·회원·상태다 —
 * 참고 사진(팀버핏 코치 앱)의 `구역`·`정원` 은 그룹 수업 것이라 안 쓴다 (2026-09-13 대표).
 *
 * **시각은 글자로 든다.** `LocalTime` 을 쓰면 두 플랫폼이 각자 포맷을 짜게 되고,
 * 그러면 같은 수업이 한쪽에서만 `18:00`, 다른 쪽에서 `오후 6:00` 이 된다.
 *
 * 카드에 무엇을 어디에 적는지는 **TeamFIS 것과 같다** (2026-09-14 대표가 그 레포를 지목).
 */
data class TeamClass(
    val id: String,
    val date: LocalDate,
    /** `18:00` */
    val start: String,
    /** `19:00` */
    val end: String,
    /** 끊은 상품 — `PT 30회` */
    val product: String,
    /** 오늘이 몇 번째인가 */
    val round: Int,
    /** 상품이 다 몇 번인가 */
    val rounds: Int,
    val member: String,
    val status: ClassStatus,
) {
    /**
     * 카드 가운데 큰 줄 — `오후 2:00 ~ 3:00`
     *
     * 끝 시각에는 오전·오후를 **넘어갈 때만** 붙인다 (TeamFIS 와 같은 규칙).
     * 한 줄 안에서 같은 말을 두 번 하면 정작 다른 쪽인 시각이 안 보인다.
     */
    val timeLabel: String
        get() {
            val (sh, sm) = parse(start)
            val (eh, em) = parse(end)
            val tail = if ((sh < 12) == (eh < 12)) clock(eh, em) else "${ampm(eh)} ${clock(eh, em)}"
            return "${ampm(sh)} ${clock(sh, sm)} ~ $tail"
        }

    /** 시작 시각만 — 하단바 위 줄이 쓴다 */
    val startLabel: String get() = parse(start).let { (h, m) -> "${ampm(h)} ${clock(h, m)}" }

    /** 카드 아래 오른쪽 — `12/30회차` */
    val roundLabel: String get() = "$round/${rounds}회차"

    /** 카드 머리말 왼쪽 — `박승규 회원님`. **회원 목록과 같은 말이다** */
    val memberLabel: String get() = MemberBoard.nameLabel(member)

    /** `18:30` → (18, 30) */
    private fun parse(hhmm: String): Pair<Int, Int> {
        val at = hhmm.split(":")
        return (at.getOrNull(0)?.toIntOrNull() ?: 0) to (at.getOrNull(1)?.toIntOrNull() ?: 0)
    }

    private fun ampm(hour: Int): String = if (hour < 12) "오전" else "오후"

    /** `2:00` — 12시간제. 0 시와 12 시는 둘 다 `12` 다 */
    private fun clock(hour: Int, minute: Int): String {
        val h = hour % 12
        return "${if (h == 0) 12 else h}:${minute.toString().padStart(2, '0')}"
    }
}

/**
 * TeamFIS 일정 문구·목록 — 두 플랫폼이 **같은 말**을 한다
 *
 * 값은 아직 [demo] 다 — **서버를 안 붙였다.**
 */
object TeamSchedule {
    /** 카드 머리말 오른쪽 배지 — TeamFIS 와 같은 말이다 */
    fun statusLabel(status: ClassStatus): String = when (status) {
        ClassStatus.SCHEDULED -> "수업예정"
        ClassStatus.DONE -> "수업완료"
        ClassStatus.NO_SHOW -> "노쇼"
    }

    /**
     * 하단바 위 줄에 세울 수업 — **오늘 남은 것 중 가장 이른 예정**
     *
     * 없으면 줄을 안 세운다. 다 끝난 날에 `다음 수업` 이 떠 있으면 거짓말이 된다.
     */
    fun next(classes: List<TeamClass>, today: LocalDate): TeamClass? =
        of(classes, today).firstOrNull { it.status == ClassStatus.SCHEDULED }

    /** 그날 수업만 — **이른 시각부터** */
    fun of(classes: List<TeamClass>, date: LocalDate): List<TeamClass> =
        classes.filter { it.date == date }.sortedBy { it.start }

    /**
     * 그날 것이 하나도 없다 — 오늘이면 `오늘`, 다른 날이면 그냥 없다고만 한다
     *
     * 업무 화면과 같은 결이다 (`WorkBoard.emptyLabel`) — 지난 날이 빈 것은
     * 안 한 것이 아니라 **애초에 없던 것**이라 말이 달라야 한다.
     */
    fun emptyLabel(date: LocalDate, today: LocalDate): String =
        if (date == today) "오늘 수업이 없어요." else "이 날은 수업이 없어요."

    /**
     * 자리 표시자 — 서버가 수업을 주면 **통째로 걷어낸다**
     *
     * 오늘·내일·사흘 뒤에만 둔다. 오늘 것 중 이른 시각은 이미 끝난 것으로 둬서
     * `완료`와 `예정`이 한 화면에 같이 서게 한다.
     */
    fun demo(today: LocalDate): List<TeamClass> = listOf(
        TeamClass("t1", today, "10:00", "11:00", "얼리버드 20회", 12, 20, "박승규", ClassStatus.DONE),
        TeamClass("t2", today, "14:00", "15:00", "PT 30회", 12, 30, "김수현", ClassStatus.SCHEDULED),
        TeamClass("t3", today, "16:00", "17:00", "PT 20회", 3, 20, "정민준", ClassStatus.NO_SHOW),
        TeamClass("t4", today, "18:30", "19:30", "얼리버드 10회", 8, 10, "이건주", ClassStatus.SCHEDULED),
        TeamClass(
            "t5", today.plus(1, DateTimeUnit.DAY), "11:00", "12:00",
            "PT 30회", 13, 30, "김수현", ClassStatus.SCHEDULED,
        ),
        TeamClass(
            "t6", today.plus(1, DateTimeUnit.DAY), "19:00", "20:00",
            "PT 20회", 4, 20, "정민준", ClassStatus.SCHEDULED,
        ),
        TeamClass(
            "t7", today.plus(3, DateTimeUnit.DAY), "09:00", "10:00",
            "얼리버드 20회", 13, 20, "박승규", ClassStatus.SCHEDULED,
        ),
    )
}
