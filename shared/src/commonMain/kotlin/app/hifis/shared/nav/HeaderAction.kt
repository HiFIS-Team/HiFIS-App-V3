package app.hifis.shared.nav

/**
 * 헤더 오른쪽에 서는 단추들 — **제품마다 다르다**
 *
 * 왼쪽 지점은 늘 있고, 오른쪽만 갈린다. 목록이 플랫폼 코드에 흩어져 있으면
 * 한쪽만 고쳐져서 두 앱의 헤더가 달라진다 ([MainTab] 이 V2 에서 겪은 일이다).
 *
 * **차례를 안 바꾼다.** 자리를 외운 사람에게 순서가 바뀌면 못 찾는다.
 * 제품별 목록은 [all] 의 차례를 지키는 **부분집합**이어야 한다 (`HeaderActionTest` 가 막는다).
 *
 * @property icon 아이콘 이름 — 양 플랫폼이 같은 이름을 쓴다
 */
enum class HeaderAction(val label: String, val icon: String) {
    SEARCH("검색", "ic_search"),
    SCAN("출퇴근 스캔", "ic_scan"),
    CHAT("사내톡", "ic_chat"),
    NOTIFICATION("알림", "ic_bell"),
    PROFILE("마이", "ic_person"),
    ;

    companion object {
        /** 서는 **차례**. 제품별 목록이 이 순서를 지킨다 */
        val all: List<HeaderAction> = entries.toList()

        /**
         * **어느 제품에서도 빠지지 않는 것** — 알림과 마이
         *
         * 알림은 눌러서 갈 곳이 생기는 유일한 입구고, 마이는 로그아웃·설정으로 가는 길이다.
         * 둘 중 하나라도 빠지면 그 제품에 들어간 사람이 갇힌다.
         */
        val always: List<HeaderAction> = listOf(NOTIFICATION, PROFILE)

        /** 안드로이드 헤더 — 검색이 선다 */
        fun android(product: Product): List<HeaderAction> = when (product) {
            // TeamFIS 는 출퇴근도 사내톡도 HiFIS 쪽 일이라 안 세운다 (2026-09-11 대표)
            Product.TEAMFIS -> listOf(SEARCH, NOTIFICATION, PROFILE)
            Product.HIFIS, Product.WEFIS -> all
        }

        /** iOS 헤더 — TeamFIS 에서는 **검색도 뺀다** (2026-09-11 대표) */
        fun ios(product: Product): List<HeaderAction> = when (product) {
            Product.TEAMFIS -> listOf(NOTIFICATION, PROFILE)
            Product.HIFIS, Product.WEFIS -> all
        }
    }
}
