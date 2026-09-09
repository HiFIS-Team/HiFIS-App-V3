package app.hifis.hifis.ui.theme

import androidx.compose.runtime.Composable
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
    // **지금은 늘 어둡게 간다.** 라이트 한 벌은 그대로 두었다 —
    // 설정에서 고르게 할 때 `isSystemInDarkTheme()` 로 되돌리면 된다
    dark: Boolean = true,
    content: @Composable () -> Unit,
) {
    CompositionLocalProvider(
        LocalHifisColors provides if (dark) DarkColors else LightColors,
        content = content,
    )
}

/** 화면에서 색을 집는 자리 — `HifisTheme.colors.ink` */
object HifisTheme {
    val colors: HifisColors
        @Composable get() = LocalHifisColors.current
}
