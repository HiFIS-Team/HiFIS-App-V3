package app.hifis.shared.teamfis

import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.plus

/** 수업이 지나갔나 — **둘뿐이다.** 결석·취소는 정해지면 그때 는다 */
enum class ClassStatus { SCHEDULED, DONE }

/**
 * TeamFIS 수업 한 칸 — **트레이너가 그날 맡은 PT**
 *
 * TeamFIS 는 트레이너 것이고 여기서 하는 일은 **수업과 세션 싸인**이다
 * (`.claude/DESIGN.md` 전체 목록). 그래서 칸에 드는 것은 상품·회차·회원·상태다 —
 * 참고 사진(팀버핏 코치 앱)의 `구역`·`정원` 은 그룹 수업 것이라 안 쓴다 (2026-09-13 대표).
 *
 * **시각은 글자로 든다.** `LocalTime` 을 쓰면 두 플랫폼이 각자 포맷을 짜게 되고,
 * 그러면 같은 수업이 한쪽에서만 `18:00`, 다른 쪽에서 `오후 6:00` 이 된다.
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
    /** 카드 가운데 큰 줄 */
    val timeLabel: String get() = "$start ~ $end"

    /** 머리말 오른쪽 알약 — `12/30회차` */
    val roundLabel: String get() = "$round/${rounds}회차"
}

/**
 * TeamFIS 일정 문구·목록 — 두 플랫폼이 **같은 말**을 한다
 *
 * 값은 아직 [demo] 다 — **서버를 안 붙였다.**
 */
object TeamSchedule {
    /** 카드 오른쪽 아래 — 지나간 수업은 조용히 물러난다 */
    fun statusLabel(status: ClassStatus): String =
        if (status == ClassStatus.DONE) "완료" else "예정"

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
        TeamClass("t3", today, "16:00", "17:00", "PT 20회", 3, 20, "정민준", ClassStatus.SCHEDULED),
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
