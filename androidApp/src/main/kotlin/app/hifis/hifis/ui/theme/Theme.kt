package app.hifis.hifis.ui.theme

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import app.hifis.shared.nav.Product
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf

/**
 * 색을 화면까지 내려보내는 통로 — 직접 읽지 말고 [HifisTheme.colors] 를 쓴다
 *
 * 기본값을 라이트로 둔 것은 [HifisTheme] 밖에서 미리보기를 띄웠을 때
 * 까맣게 터지지 않게 하기 위해서다.
 */
private val LocalHifisColors = staticCompositionLocalOf { LightColors }

/**
 * 앱 전체를 감싸는 테마 — `MaterialTheme` 을 안 쓴다
 *
 * V3 는 머티리얼 색·타입 스케일을 따라가지 않는다. 반쯤 걸치면 어떤 색이
 * 어디서 오는지가 화면마다 달라져서, 아예 우리 토큰만 쓴다.
 * (물결 효과처럼 머티리얼 부품을 꺼내 쓸 때는 그 자리에서만 명시적으로 쓴다.)
 */
@Composable
fun HifisTheme(
    /**
     * 어느 제품에 들어와 있나 — **브랜드색이 여기서 갈린다**
     *
     * 제품을 옮기면 색이 **서서히 물든다** ([BRAND_FADE]). 툭 갈리면 앱이 튄 것처럼 보인다.
     *
     * 이 자리가 **셸 바깥**이어야 한다. 제품을 옮기면 셸이 통째로 새로 서는데,
     * 애니메이션이 그 안에 있으면 같이 새로 서서 물들 새가 없다.
     */
    product: Product = Product.default,
    // **지금은 늘 어둡게 간다.** 라이트 한 벌은 그대로 두었다 —
    // 설정에서 고르게 할 때 `isSystemInDarkTheme()` 로 되돌리면 된다
    dark: Boolean = true,
    content: @Composable () -> Unit,
) {
    val base = if (dark) DarkColors else LightColors
    val target = brandOf(product, dark)
    val spec = tween<Color>(BRAND_FADE)
    val brand by animateColorAsState(target.brand, spec, label = "brand")
    val gradientStart by animateColorAsState(target.gradientStart, spec, label = "brand-start")
    val gradientEnd by animateColorAsState(target.gradientEnd, spec, label = "brand-end")

    CompositionLocalProvider(
        LocalHifisColors provides base.copy(
            brand = brand,
            brandGradientStart = gradientStart,
            brandGradientEnd = gradientEnd,
        ),
        content = content,
    )
}

/**
 * 브랜드색이 옮겨 가는 데 걸리는 시간 — 알약(240)보다 길다
 *
 * 화면 전체가 물드는 일이라 알약 하나 미끄러지는 것보다 느긋해야 한다.
 * 더 길면 제품을 옮긴 뒤에도 한참 물들고 있어서 덜 끝난 것처럼 보인다.
 */
private const val BRAND_FADE = 420

/** 화면에서 색을 집는 자리 — `HifisTheme.colors.ink` */
object HifisTheme {
    val colors: HifisColors
        @Composable get() = LocalHifisColors.current
}
