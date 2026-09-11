package app.hifis.shared.nav

/**
 * 앱 안에 든 **제품** — 셸을 통째로 갈아 끼우는 층
 *
 * 한 앱에 셋이 들어간다. 로그인하면 하나를 고르고, 들어와서도 헤더 아래 고르개로 옮긴다.
 *
 * | | 누가 | 무엇을 |
 * |---|---|---|
 * | HiFIS | 직원 전원 | 출퇴근·환경정비·급여 — 센터와 본사 일 |
 * | TeamFIS | 트레이너 (대표·관리자는 **보기만**) | 수업·세션 싸인 |
 * | WeFIS | 회원 관리하는 사람 | 회원·등록권 — 브로제이·바디코디·다짐 자리를 대신한다 |
 *
 * **[MainTab] 보다 한 층 위다.** 탭은 한 제품 안의 칸이고, 제품이 바뀌면
 * 그 탭 목록이 통째로 바뀐다. 그래서 고르개가 탭바가 아니라 헤더 아래에 선다.
 *
 * 앱을 셋으로 가르지 않는 이유는 `.claude/멀티테넌트.md` 에 있다 — 트레이너도
 * 출퇴근을 찍고 환경정비를 하는 **직원**이라, 가르면 그 사람이 둘을 깔아야 한다.
 */
enum class Product(val label: String) {
    HIFIS("HiFIS"),
    TEAMFIS("TeamFIS"),
    WEFIS("WeFIS"),
    ;

    companion object {
        /** 고르개에 서는 **차례**. 순서를 바꾸면 양 플랫폼이 같이 바뀐다 */
        val all: List<Product> = entries.toList()

        /** 고르개가 읽는 이름들 — 화면이 `label` 을 따로 모으지 않게 */
        val labels: List<String> = all.map { it.label }

        /** 앱을 켜면 여기서 시작한다 — 직원이면 누구나 쓰는 자리다 */
        val default: Product = HIFIS

        /** 아직 화면이 없는 제품의 자리 문구 — 안 만든 탭과 같은 말을 쓴다 */
        fun comingSoon(product: Product): String = "${product.label} — 준비 중"
    }

    /**
     * AI 를 쓸 수 있는 제품인가 — **HiFIS 뿐이다** (2026-09-11 대표)
     *
     * AI 는 출퇴근·환경정비·급여를 아는 HiFIS 의 것이라, 딴 제품에서는 물어도 답할 것이 없다.
     * 안드로이드는 이 값이 **떠 있는 단추**를 세울지를 정하고
     * (`AiChatButton`), iOS 는 탭바 동그라미가 이걸 따른다 ([MainTab.iosSideSlot]).
     *
     * **자리가 둘이라 여기 한 곳에 둔다.** 화면마다 `product == HIFIS` 를 새로 적으면
     * 언젠가 한쪽만 고쳐진다 — 권한 판정을 한 곳에 모으는 것과 같은 이유다.
     */
    val hasAi: Boolean get() = this == HIFIS
}
