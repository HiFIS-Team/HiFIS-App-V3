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
 * **제품마다 다른 목록을 세운다** ([android] · [ios]). 제품이 바뀌면 하단바가
 * 통째로 바뀐다 — 그 목록도 여기 한 곳에서만 정한다.
 *
 * **지금은 센터 근무자 기준이다.** 본사는 자주 쓰는 것이 달라서
 * (프로젝트·회의록·전자결재) 나중에 소속에 따라 다른 목록을 세우게 된다.
 * 그때도 목록이 늘어날 뿐 **읽는 자리는 여기 하나여야 한다.**
 *
 * 아이콘 이름은 양 플랫폼이 **같은 것**을 쓴다. 안드로이드는 `R.drawable.<이름>`,
 * iOS 는 같은 이름의 에셋이다 (`tools/icons/sync_ios_icons.py` 가 맞춰 준다).
 *
 * **iOS 하단바만 예외로 애플 심볼을 쓴다** ([symbol], 2026-09-11 대표).
 * 유리가 덮는 동안 속을 채워 그리는 일을 iOS 가 해 주는데 **그것이 애플 심볼일 때만**
 * 된다 — 우리 그림으로는 어디에 걸어도 시점이 어긋났다 (`.claude/DEVLOG.md`).
 * 하단바 밖(전체 목록·바로가기·헤더)은 그대로 우리 그림이다.
 *
 * @property label 하단바에 찍히는 글자
 * @property icon 안 고른 칸 — **선으로만** 그린 아이콘
 * @property iconFilled 고른 칸 — **속을 채운** 아이콘
 * @property symbol iOS 하단바에 서는 애플 심볼. 채움 벌(`.fill`)은 iOS 가 알아서 쓴다
 */
enum class MainTab(
    val label: String,
    val icon: String,
    val iconFilled: String,
    val symbol: String,
) {
    HOME("홈", "ic_home", "ic_home_fill", "house"),
    WORK("업무", "ic_work", "ic_work_fill", "briefcase"),
    SCHEDULE("일정", "ic_schedule", "ic_schedule_fill", "calendar"),
    ATTENDANCE("근태", "ic_attendance", "ic_attendance_fill", "clock"),

    /**
     * 나머지를 전부 담는 목록 — **하단바가 변신하지 않는다**
     *
     * 5칸짜리 바를 2단으로 뒤집는 방식(V2 아이폰)은 한 칸을 '뒤로'에 쓰느라
     * 실제로 8개밖에 못 담는다. 넣을 것이 10개라 처음부터 안 맞는다.
     */
    MORE("전체", "ic_more", "ic_more_fill", "square.grid.2x2"),

    // ── TeamFIS ──
    MEMBER("회원", "ic_people", "ic_people_fill", "person.2"),
    LESSON("수업", "ic_dumbbell", "ic_dumbbell_fill", "dumbbell"),
    ;

    companion object {
        /** 모든 탭 — 테스트가 읽는 **명단**이다. 한 제품이 이걸 다 세우지는 않는다 */
        val all: List<MainTab> = entries.toList()

        /** HiFIS 안드로이드 — 다섯 칸 */
        private val hifisAndroid = listOf(HOME, WORK, SCHEDULE, ATTENDANCE, MORE)

        /**
         * HiFIS iOS — **네 칸이다**
         *
         * iOS 26 은 탭바 오른쪽에 시스템이 그리는 동그라미 자리를 하나 준다
         * (`Tab(role: .search)`). 거기에 AI 를 앉혔는데 **그 자리가 다섯 칸 중 하나를 먹는다** —
         * 여섯으로 세우면 iOS 가 넘치는 것을 `More(…)` 로 접어 버린다.
         *
         * 그래서 **근태가 탭에서 내려온다.** 대신 iOS 홈 바로가기가 근태를 받는다
         * ([HomeShortcut.ios]). 안드로이드에는 그 자리가 없어 그대로 다섯이다.
         */
        private val hifisIos = hifisAndroid - ATTENDANCE

        /**
         * TeamFIS — **양 플랫폼 네 칸으로 같다** (2026-09-11 대표)
         *
         * 트레이너가 보는 것은 오늘 수업과 내 회원이다. 업무·근태·전체는 HiFIS 쪽 일이라
         * 여기 안 선다 — 그래서 iOS 도 칸을 줄일 필요가 없었다.
         *
         * iOS 는 동그라미 자리에 **검색**이 앉는다 (애플뮤직과 같은 자리).
         * 그래서 iOS TeamFIS 헤더에는 검색이 없다 ([HeaderAction.ios]).
         */
        private val teamfis = listOf(HOME, SCHEDULE, MEMBER, LESSON)

        /**
         * 안드로이드 하단바 — 제품이 정한다
         *
         * **비면 하단바를 안 세운다.** 없는 화면으로 가는 칸을 세울 수는 없다.
         */
        fun android(product: Product): List<MainTab> = when (product) {
            Product.HIFIS -> hifisAndroid
            Product.TEAMFIS -> teamfis
            // 아직 화면이 하나도 없다 — 자리 문구만 뜬다
            Product.WEFIS -> emptyList()
        }

        /** iOS 하단바 — 제품이 정한다 */
        fun ios(product: Product): List<MainTab> = when (product) {
            Product.HIFIS -> hifisIos
            Product.TEAMFIS -> teamfis
            Product.WEFIS -> emptyList()
        }

        /**
         * iOS 탭바 오른쪽 **동그라미**에 앉는 것 — 제품마다 다르다
         *
         * 시스템이 탭바와 같은 유리로 그려 주는 자리다 (`Tab(role: .search)`).
         * 비면 그 자리를 안 쓴다.
         */
        fun iosSideSlot(product: Product): SideSlot? = when (product) {
            Product.HIFIS -> SideSlot.AI
            // 애플뮤직처럼 검색이 그 자리에 앉는다 (2026-09-11 대표)
            Product.TEAMFIS -> SideSlot.SEARCH
            Product.WEFIS -> null
        }
    }

    /** iOS 탭바 오른쪽 동그라미에 앉을 수 있는 것 */
    enum class SideSlot(val label: String, val icon: String) {
        /** 브랜드 마크를 제 색 그대로 세운다 — 아이콘 이름이 아니라 에셋 이름이다 */
        AI("AI", "brand_mark"),
        SEARCH("검색", "ic_search"),
    }
}
