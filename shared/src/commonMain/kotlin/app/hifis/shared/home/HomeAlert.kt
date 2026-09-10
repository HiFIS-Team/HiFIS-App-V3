package app.hifis.shared.home

/**
 * 홈 맨 위에 **하나씩 번갈아 뜨는** 알림 한 건
 *
 * 헤더 바로 아래에 한 장만 서고, 몇 초마다 다음 것으로 바뀐다
 * ([ROTATE_MILLIS]). 여러 장을 쌓지 않는 이유는 자리 때문이다 —
 * 홈에서 제일 좋은 자리라 다 쌓으면 정작 `오늘 근무` 가 화면 밖으로 밀린다.
 *
 * **닫기(X)를 두지 않는다.** 닫아 놓은 것을 언제 다시 띄울지가 또 정해야 할 일이 되고,
 * 어차피 몇 초 뒤면 다음 것으로 넘어간다.
 *
 * **오른쪽 버튼도 두지 않는다.** 카드를 누르면 그리로 가는데 버튼이 또 있으면
 * 누르는 자리가 둘로 갈린다 — 어느 쪽이 무엇인지 설명해야 하고, 그만큼 글자 자리도 준다.
 */
data class HomeAlert(
    val kind: AlertKind,
    /** 굵은 한 줄 — 무슨 일인지 */
    val title: String,
    /** 아래 흐린 한 줄 — 어떤 것인지 */
    val detail: String,
) {
    companion object {
        /** 다음 알림으로 넘어가는 간격 — **양 플랫폼이 같은 박자로 돈다** */
        const val ROTATE_MILLIS = 4_000L

        /**
         * 서버를 붙이기 전에 화면을 보기 위한 값 — **진짜가 아니다**
         *
         * 서버가 붙으면 지운다. 이 값을 보고 서버에 칸을 만들지 않는다.
         */
        val demo = listOf(
            HomeAlert(
                AlertKind.PROJECT_DUE,
                "마감이 이틀 남았어요",
                "9월 센터 리뉴얼 · 아직 진행 중",
            ),
            HomeAlert(
                AlertKind.TASK_LEFT,
                "오늘 할 일 3개 남았어요",
                "환경정비 · 세션 싸인 · 회원 등록",
            ),
            HomeAlert(
                AlertKind.APPROVED,
                "월차가 승인됐어요",
                "9월 12일 (금)",
            ),
            HomeAlert(
                AlertKind.REJECTED,
                "지출 결의가 반려됐어요",
                "사유를 확인해 주세요",
            ),
        )
    }
}

/**
 * 알림 갈래 — **아이콘과 색을 여기서 정한다**
 *
 * 화면마다 갈래를 색에 이으면 두 플랫폼이 갈린다. 뜻([Tone])만 정하고
 * 색은 각 플랫폼이 한 번만 잇는다 (`오늘 근무` 배지와 같은 규칙).
 *
 * 아이콘은 **이미 있는 것을 다시 쓴다.** 알림마다 새로 그리면
 * 같은 뜻인데 자리마다 다른 그림이 된다 (승인·반려는 둘 다 결재판이고 색만 갈린다).
 */
enum class AlertKind(val icon: String, val tone: Tone) {
    /** 마감이 다가오는데 아직 안 끝난 프로젝트 */
    PROJECT_DUE("ic_project", Tone.CAUTION),

    /** 오늘 남은 업무 */
    TASK_LEFT("ic_work", Tone.INFO),

    /** 결재가 승인됐다 */
    APPROVED("ic_approval", Tone.GOOD),

    /** 결재가 반려됐다 */
    REJECTED("ic_approval", Tone.BAD),
}
