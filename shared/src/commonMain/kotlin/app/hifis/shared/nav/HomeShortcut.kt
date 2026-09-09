package app.hifis.shared.nav

/**
 * 홈 바로가기 — **하단바로 못 가는 화면들**
 *
 * 하단바는 다섯 칸(홈·업무·일정·근태·전체)이고, 앞 넷만 화면이다.
 * 나머지 여섯이 여기 있다. 둘을 합치면 지금 정한 화면 열 개가 된다.
 *
 * ```
 * 하단바   홈 · 업무 · 일정 · 근태            (+ 전체)
 * 바로가기 프로젝트 · 회의록 · 전자결재 · 조직도 · 급여 · 공지
 * ```
 *
 * **`전체` 목록을 만들 때 이 목록과 어긋나면 안 된다.** V2 는 탭이 없는 화면이
 * 알림을 눌러도 안 열렸는데, 갈 수 있는 곳을 세는 자리가 여러 군데였기 때문이다.
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
    STAFF("조직도", "ic_staff"),
    SALARY("급여", "ic_salary"),
    NOTICE("공지", "ic_notice"),
    ;

    companion object {
        /** 홈에 놓이는 **차례**. 순서를 바꾸면 양 플랫폼이 같이 바뀐다 */
        val all: List<HomeShortcut> = entries.toList()
    }
}
