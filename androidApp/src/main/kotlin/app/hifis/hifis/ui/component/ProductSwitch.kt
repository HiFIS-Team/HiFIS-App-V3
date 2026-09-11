package app.hifis.hifis.ui.component

import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.Modifier
import app.hifis.shared.nav.Product

/**
 * 지금 어느 제품에 들어와 있나 — **셸이 들고 화면은 읽기만 한다**
 *
 * 화면마다 `product` 를 손으로 넘기지 않는다. 제품은 홈·업무·일정이 신경 쓸 것이
 * 아니라 **그 화면들을 담고 있는 껍데기의 상태**라, 화면 signature 에 끼워 넣으면
 * 아무도 안 쓰는 인자가 다섯 군데에 는다.
 */
data class ProductScope(val product: Product, val select: (Product) -> Unit)

/** 기본값은 아무 일도 안 한다 — 셸이 안 감싸면 고르개가 서되 안 움직인다 */
val LocalProduct = compositionLocalOf { ProductScope(Product.default) {} }

/**
 * 제품 고르개 — **헤더 바로 아래**, 탭 화면 전부에 선다
 *
 * 칸 고르개(공통/개인 · 전체/안읽음)와 **같은 부품**이다 (대표 요청, 2026-09-11).
 * 트레이너는 출근을 HiFIS 에서 찍고 수업은 TeamFIS 에서 해서 하루에 여러 번 오간다 —
 * 한 탭에만 두면 그 탭을 거쳐 가야 한다.
 *
 * 제품이 바뀌면 **하단바까지 통째로 바뀐다.** 그래서 탭바 안이 아니라 그 위에 선다.
 */
@Composable
fun ProductSwitch(modifier: Modifier = Modifier) {
    val scope = LocalProduct.current
    ModeSwitch(
        segments = Product.labels,
        selected = Product.all.indexOf(scope.product),
        onSelect = { scope.select(Product.all[it]) },
        modifier = modifier,
    )
}
