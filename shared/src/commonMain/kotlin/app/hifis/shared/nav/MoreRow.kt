package app.hifis.shared.nav

/**
 * 전체 목록의 **묶음**
 *
 * 판으로 싸지 않는다 — 머리말 하나와 그 아래 줄들이 전부다.
 * **묶음마다 머리말이 반드시 있다.** 판이 없어서 머리말이 빠지면
 * 그 줄들이 바로 위 묶음에 딸린 것처럼 보인다.
 */
enum class MoreGroup(val title: String) {
    WORK("업무"),
    MINE("내 근무"),
    COMPANY("회사"),
    ACCOUNT("계정"),
    ;

    companion object {
        /** 묶음이 서는 **차례**. 순서를 바꾸면 양 플랫폼이 같이 바뀐다 */
        val all: List<MoreGroup> = entries.toList()
    }
}

/**
 * 전체 목록의 한 줄 — **여기가 앱의 전수 명단이다**
 *
 * 하단바는 다섯 칸뿐이고 홈 바로가기는 여섯 개뿐이라, 둘 중 어디에도 못 서는
 * 화면이 생기면 **갈 방법이 사라진다.** V2 가 정확히 그랬다 — 일정·조직도·전자결재는
 * 폰에 탭이 없어서 알림을 눌러도 아무 일이 안 일어났다.
 *
 * 그래서 이 목록은 **빠짐없는 명단**이어야 한다. 화면을 새로 만들면 여기에 줄을
 * 추가하는 것이 마지막 단계다. `MoreRowTest` 가 [MainTab]·[HomeShortcut] 과
 * 대조해서 빠진 것을 잡는다 — 이름과 아이콘까지 같은지 본다.
 *
 * **홈만 예외다.** 하단바 첫 칸에 늘 있고 앱을 켜면 거기서 시작해서,
 * 목록에서 '홈으로 가기'를 찾을 일이 없다.
 *
 * @property icon 아이콘 이름 — 양 플랫폼이 **같은 이름**을 쓴다
 *   (`tools/icons/sync_ios_icons.py` 가 맞춰 준다)
 * @property tab 하단바에도 자리가 있는 화면이면 그 탭. **누르면 탭을 옮긴다** —
 *   화면을 새로 쌓지 않는다. 없으면 아직 화면이 없는 줄이다
 */
enum class MoreRow(
    val label: String,
    val icon: String,
    val group: MoreGroup,
    val tab: MainTab? = null,
) {
    WORK("업무", "ic_work", MoreGroup.WORK, tab = MainTab.WORK),
    PROJECT("프로젝트", "ic_project", MoreGroup.WORK),
    MEETING("회의록", "ic_meeting", MoreGroup.WORK),
    SCHEDULE("일정", "ic_schedule", MoreGroup.WORK, tab = MainTab.SCHEDULE),
    APPROVAL("전자결재", "ic_approval", MoreGroup.WORK),

    ATTENDANCE("근태", "ic_attendance", MoreGroup.MINE, tab = MainTab.ATTENDANCE),
    SALARY("급여", "ic_salary", MoreGroup.MINE),

    STAFF("조직도", "ic_staff", MoreGroup.COMPANY),
    NOTICE("공지", "ic_notice", MoreGroup.COMPANY),
    RANKING("랭킹", "ic_ranking", MoreGroup.COMPANY),

    SETTINGS("설정", "ic_settings", MoreGroup.ACCOUNT),
    LOGOUT("로그아웃", "ic_logout", MoreGroup.ACCOUNT),
    ;

    companion object {
        /** 줄이 서는 **차례**. 순서를 바꾸면 양 플랫폼이 같이 바뀐다 */
        val all: List<MoreRow> = entries.toList()

        /** 한 묶음에 들어가는 줄들 — 적힌 차례 그대로 나온다 */
        fun of(group: MoreGroup): List<MoreRow> = all.filter { it.group == group }
    }
}
