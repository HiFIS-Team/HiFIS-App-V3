package app.hifis.shared.nav

/**
 * 홈 바로가기 — **하단바로 못 가는 화면들**
 *
 * ## 목록이 플랫폼마다 다르다 — 하단바가 다르기 때문이다
 *
 * iOS 26 은 탭바 오른쪽에 **시스템이 그리는 동그라미** 자리를 하나 준다
 * (`UISearchTab`). 거기에 AI 를 앉혔는데, 그 자리가 다섯 칸 중 하나를 먹는다.
 * 그래서 iOS 하단바는 네 칸이고 **근태가 탭에서 내려온다.**
 * 안드로이드에는 그 자리가 없어서 다섯 칸을 그대로 쓴다.
 *
 * ```
 *            하단바                                   바로가기
 * Android    홈 · 업무 · 일정 · 근태 · 전체            프로젝트 회의록 전자결재 / 조직도 급여 공지
 * iOS        홈 · 업무 · 일정 · 전체  (+AI 동그라미)   프로젝트 회의록 전자결재 / 근태  급여 공지
 * ```
 *
 * **바로가기는 늘 "하단바로 못 가는 것"이다.** 그 규칙은 두 플랫폼이 같고,
 * 하단바가 다르니 **한 칸만 갈린다** — iOS 는 근태가 탭에서 내려와 조직도 자리를 받는다.
 * 여섯 개로 맞춰 격자가 양쪽 다 셋씩 두 줄이다.
 *
 * **여섯에 못 든 화면(조직도·랭킹 등)은 `전체` 목록에 있다.** 바로가기는 자주 쓰는 것을
 * 앞에 꺼내 두는 자리이지 전수 명단이 아니다 — 명단은 [MoreRow] 다.
 *
 * **`전체` 목록([MoreRow])과 어긋나면 안 된다.** V2 는 탭이 없는 화면이
 * 알림을 눌러도 안 열렸는데, 갈 수 있는 곳을 세는 자리가 여러 군데였기 때문이다.
 * `MoreRowTest` 가 여기 있는 것이 전부 `전체` 에도 있는지 본다.
 *
 * 지금은 **센터 근무자 기준**이다. 본사는 자주 쓰는 것이 달라
 * (프로젝트·회의록·전자결재가 하단바로 올라간다) 이 목록도 같이 갈린다.
 *
 * @property icon 아이콘 이름 — 양 플랫폼이 **같은 이름**을 쓴다
 *   (`tools/icons/sync_ios_icons.py` 가 맞춰 준다)
 */
enum class HomeShortcut(val label: String, val icon: String) {
    PROJECT("프로젝트", "ic_project"),
    MEETING("회의록", "ic_meeting"),
    APPROVAL("전자결재", "ic_approval"),
    ATTENDANCE("근태", "ic_attendance"),
    STAFF("조직도", "ic_staff"),
    SALARY("급여", "ic_salary"),
    NOTICE("공지", "ic_notice"),
    ;

    companion object {
        /** 모든 칸 — 색표([app.hifis.shared.nav.HomeShortcut])와 테스트가 읽는다 */
        val all: List<HomeShortcut> = entries.toList()

        /**
         * 안드로이드 홈에 놓이는 **차례** — 하단바가 다섯 칸이라 여섯 개
         *
         * 근태는 탭에 있고, 랭킹은 `전체` 에만 둔다.
         */
        val android: List<HomeShortcut> = listOf(PROJECT, MEETING, APPROVAL, STAFF, SALARY, NOTICE)

        /**
         * iOS 홈에 놓이는 **차례** — 여섯 개
         *
         * 하단바 다섯째 칸을 AI 동그라미가 써서 **근태가 여기로 내려온다.**
         * 대신 조직도가 빠져 개수는 안드로이드와 같다 — 격자가 셋씩 두 줄로 떨어진다.
         * 조직도는 `전체` 목록에 그대로 있다.
         */
        val ios: List<HomeShortcut> =
            listOf(PROJECT, MEETING, APPROVAL, ATTENDANCE, SALARY, NOTICE)
    }
}
