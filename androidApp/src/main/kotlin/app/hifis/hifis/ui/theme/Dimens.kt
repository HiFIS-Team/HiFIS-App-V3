package app.hifis.hifis.ui.theme

import androidx.compose.ui.unit.dp

/**
 * 화면이 쓰는 크기·간격 — 숫자를 화면 파일에 직접 적지 않는다
 *
 * 여기 없는 값이 필요하면 `.claude/DESIGN.md` 에 먼저 적고 이름을 만든다.
 */
object Dimens {
    /** 화면 좌우 여백 — **모든 화면이 같다.** 화면마다 다르면 줄이 안 맞는다 */
    val screenEdge = 20.dp

    /** 헤더 높이 (상태바 **아래**) */
    val headerHeight = 56.dp

    /**
     * 헤더 아이콘의 **터치 자리** — 그림([headerIcon])보다 크다
     *
     * 손가락이 닿는 넓이라 22 로 두면 옆 아이콘을 누르게 된다.
     */
    val headerIconButton = 44.dp

    /** 헤더 아이콘 **그림** 크기 */
    val headerIcon = 22.dp

    /**
     * 터치 자리가 그림보다 넓어서 생기는 한쪽 여백 — `(44 - 22) / 2`
     *
     * 헤더 줄 여백을 [screenEdge] 에서 이만큼 빼야 **그림**이 화면 끝 20 에 선다.
     * 손으로 9 라고 적지 않는 이유는 버튼 크기를 바꿔도 따라오게 하기 위해서다.
     */
    val headerIconInset = (headerIconButton - headerIcon) / 2

    /** 안 읽음 점 지름 — 숫자는 안 쓴다 */
    val badgeDot = 7.dp

    /** 안 읽음 점을 두르는 바탕색 테두리 — 아이콘 선 위에 겹쳐도 점이 뭉개지지 않게 */
    val badgeRing = 1.5.dp

    /** 카드 모서리 — **카드는 전부 이 값이다.** 자리마다 다르면 줄이 안 맞는다 */
    val cardRadius = 24.dp

    /** 카드 안쪽 여백 */
    val cardPadding = 24.dp

    /** 게이지가 차지하는 줄 높이 — 트랙보다 큰 것은 손잡이가 트랙 밖으로 나오기 때문 */
    val gaugeRow = 18.dp

    /** 게이지 트랙 두께 */
    val gaugeTrack = 8.dp

    /** 게이지 손잡이 지름 */
    val gaugeThumb = 14.dp
}
