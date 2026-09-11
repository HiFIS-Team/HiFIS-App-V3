package app.hifis.hifis.shell

import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.Modifier
import app.hifis.hifis.ui.ModeSwitch
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
 *
 * ## 알약이 안 미끄러진다 (`slide = false`)
 *
 * 제품을 옮기면 셸이 통째로 새로 서고, **이 고르개도 같이 새로 선다.**
 * 새로 선 알약에는 미끄러져 올 자리가 없어서 그냥 제자리에 나타난다.
 *
 * 그런데 TeamFIS ↔ WeFIS 는 둘 다 자리 화면이라 화면이 안 바뀌어서 **거기서만
 * 미끄러졌다.** 셋 중 하나만 다르게 움직이니 그게 더 이상하게 보였다
 * (대표가 봤다 — "하이피스랑 팀피스는 딱딱 바로, 팀피스랑 위피스는 자연스럽고").
 *
 * 그래서 **셋 다 즉시 옮긴다.** 앱이 통째로 갈리는 자리라 알약만 천천히 따라가는 것도
 * 어차피 맞지 않는다. 미끄러지는 것은 화면이 그대로 있는 칸 고르개(전체/안읽음 ·
 * 공통/개인)의 몫이다.
 */
@Composable
fun ProductSwitch(modifier: Modifier = Modifier) {
    val scope = LocalProduct.current
    ModeSwitch(
        segments = Product.labels,
        selected = Product.all.indexOf(scope.product),
        onSelect = { scope.select(Product.all[it]) },
        modifier = modifier,
        slide = false,
    )
}
