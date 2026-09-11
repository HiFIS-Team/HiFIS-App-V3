package app.hifis.hifis.ui.theme

import androidx.compose.runtime.Composable
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
     * **색은 여기서 안 움직인다.** 제품을 옮길 때 셸이 통째로 녹아들면서
     * (`SHELL_FADE`) 두 겹이 겹쳐 보이므로 색도 저절로 옮겨 간다.
     */
    product: Product = Product.default,
    // **지금은 늘 어둡게 간다.** 라이트 한 벌은 그대로 두었다 —
    // 설정에서 고르게 할 때 `isSystemInDarkTheme()` 로 되돌리면 된다
    dark: Boolean = true,
    content: @Composable () -> Unit,
) {
    // **색을 따로 물들이지 않는다.** 셸이 통째로 녹아들면서(`SHELL_FADE`) 두 겹이
    // 겹쳐 보이므로 색도 저절로 옮겨 간다. 여기서 또 보간하면 **나가는 겹이
    // 중간색으로 칠해져** — HiFIS 가 빠지는 동안 보라색이 된다. 겹마다 제 색이어야 한다
    val base = if (dark) DarkColors else LightColors
    val target = brandOf(product, dark)

    CompositionLocalProvider(
        LocalHifisColors provides base.copy(
            brand = target.brand,
            brandGradientStart = target.gradientStart,
            brandGradientEnd = target.gradientEnd,
        ),
        content = content,
    )
}


/** 화면에서 색을 집는 자리 — `HifisTheme.colors.ink` */
object HifisTheme {
    val colors: HifisColors
        @Composable get() = LocalHifisColors.current
}
