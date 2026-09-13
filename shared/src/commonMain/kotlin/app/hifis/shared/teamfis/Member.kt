package app.hifis.shared.teamfis

/** 회원 목록을 가르는 **세 갈래** */
enum class MemberStatus { ACTIVE, HOLDING, EXPIRED }

/**
 * 회원 한 명 — 트레이너가 들고 있는 사람
 *
 * 목록에 무엇을 어디에 적는지는 **TeamFIS 것과 같다** (2026-09-14 대표가 그 레포를 지목).
 */
data class Member(
    val id: String,
    val name: String,
    val status: MemberStatus,
    /** `12/30회차` — 진행한 회차 / 등록 회차 */
    val progress: String,
    /** 이름 밑 한 줄 — 마지막 수업일·홀딩 시작일 같은 것 */
    val detail: String,
) {
    /** `김수현 회원님` */
    val label: String get() = MemberBoard.nameLabel(name)
}

/**
 * 회원 목록 문구·갈래 — 두 플랫폼이 **같은 말**을 한다
 *
 * 값은 아직 [demo] 다 — **서버를 안 붙였다.**
 */
object MemberBoard {
    /**
     * `김수현 회원님` — **수업 카드와 같은 말이다**
     *
     * 두 자리에서 따로 지으면 한쪽만 `김수현 님` 이 된다 ([TeamClass.memberLabel] 이 여기를 쓴다).
     */
    fun nameLabel(name: String): String = "$name 회원님"

    /** 필터 칩과 상태 배지가 같이 쓴다 */
    fun statusLabel(status: MemberStatus): String = when (status) {
        MemberStatus.ACTIVE -> "활성"
        MemberStatus.HOLDING -> "홀딩"
        MemberStatus.EXPIRED -> "만료"
    }

    /** 갈래 차례 — 화면이 새로 세우지 않는다 */
    val statuses: List<MemberStatus> = MemberStatus.entries.toList()

    /** 고른 갈래가 없으면 **보유 회원 전체**다 — 그게 이 화면의 기본이다 */
    fun shown(members: List<Member>, filter: MemberStatus?): List<Member> =
        if (filter == null) members else members.filter { it.status == filter }

    /** 칩에 달리는 숫자 — 고르지 않고도 갈래별 규모가 보이게 한다 */
    fun count(members: List<Member>, status: MemberStatus): Int =
        members.count { it.status == status }

    /** 고른 갈래에 아무도 없다 */
    const val EMPTY = "회원이 없어요."

    /**
     * 자리 표시자 — 서버가 회원 목록을 주면 **통째로 걷어낸다**
     *
     * 홀딩·만료를 섞어 둔다. 다 활성이면 배지가 한 번도 안 서서
     * 목록이 제대로 그려지는지 눈으로 못 본다.
     */
    val demo: List<Member> = listOf(
        Member("m1", "김수현", MemberStatus.ACTIVE, "12/30회차", "마지막 9/5"),
        Member("m2", "박승규", MemberStatus.ACTIVE, "3/20회차", "마지막 9/6"),
        Member("m3", "정민준", MemberStatus.ACTIVE, "8/10회차", "마지막 9/4"),
        Member("m4", "이건주", MemberStatus.ACTIVE, "27/30회차", "마지막 9/6"),
        Member("m5", "김서준", MemberStatus.HOLDING, "14/40회차", "9/1부터 홀딩"),
        Member("m6", "한지우", MemberStatus.ACTIVE, "1/50회차", "마지막 9/2"),
        Member("m7", "오세영", MemberStatus.EXPIRED, "20/20회차", "8/28 만료"),
        Member("m8", "장태현", MemberStatus.ACTIVE, "19/30회차", "마지막 9/3"),
        Member("m9", "윤가람", MemberStatus.HOLDING, "6/20회차", "8/20부터 홀딩"),
        Member("m10", "배준호", MemberStatus.EXPIRED, "30/30회차", "8/11 만료"),
        Member("m11", "신다은", MemberStatus.ACTIVE, "5/10회차", "마지막 9/6"),
        Member("m12", "고윤성", MemberStatus.EXPIRED, "10/10회차", "7/30 만료"),
    )
}
