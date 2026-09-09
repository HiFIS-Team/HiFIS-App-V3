package app.hifis.shared.nav

/**
 * 하단바에 서는 탭 — **양 플랫폼이 이 목록 하나를 읽는다**
 *
 * V2 는 이 목록이 플랫폼마다 따로 있었다. 아이폰은 2단 바(메인 5 / 서브 5),
 * 안드로이드는 한 줄 8개, 데스크톱은 사이드바 13개. 그래서 알림을 화면으로
 * 보내는 `_go()` 가 switch 세 개가 됐고, 주석에 이렇게 남아 있다 —
 * *"같은 화면이라도 자리가 셋 다 다르다."*
 *
 * 그러다 **화면이 목록에서 새어 나갔다.** 일정·조직도는 폰에 탭이 없어서
 * 알림을 눌러도 아무 일이 안 일어났고, 전자결재는 같은 이유로 버그가 났다
 * (2026-09-07). V3 는 그 목록을 여기 한 곳에만 둔다.
 *
 * **지금은 센터 근무자 기준이다.** 본사는 자주 쓰는 것이 달라서
 * (프로젝트·회의록·전자결재) 나중에 소속에 따라 다른 목록을 세우게 된다.
 * 그때도 목록이 늘어날 뿐 **읽는 자리는 여기 하나여야 한다.**
 *
 * @property label 하단바에 찍히는 글자
 * @property icon 안 고른 칸 — **선으로만** 그린 아이콘
 * @property iconFilled 고른 칸 — **속을 채운** 아이콘
 *
 * 아이콘 이름은 양 플랫폼이 **같은 것**을 쓴다. 안드로이드는 `R.drawable.<이름>`,
 * iOS 는 같은 이름의 에셋이다 (`tools/icons/sync_ios_icons.py` 가 맞춰 준다).
 */
enum class MainTab(val label: String, val icon: String, val iconFilled: String) {
    HOME("홈", "ic_home", "ic_home_fill"),
    WORK("업무", "ic_work", "ic_work_fill"),
    SCHEDULE("일정", "ic_schedule", "ic_schedule_fill"),
    ATTENDANCE("근태", "ic_attendance", "ic_attendance_fill"),

    /**
     * 나머지를 전부 담는 목록 — **하단바가 변신하지 않는다**
     *
     * 5칸짜리 바를 2단으로 뒤집는 방식(V2 아이폰)은 한 칸을 '뒤로'에 쓰느라
     * 실제로 8개밖에 못 담는다. 넣을 것이 10개라 처음부터 안 맞는다.
     */
    MORE("전체", "ic_more", "ic_more_fill"),
    ;

    companion object {
        /** 하단바에 서는 **차례**. 순서를 바꾸면 양 플랫폼이 같이 바뀐다 */
        val all: List<MainTab> = entries.toList()
    }
}
