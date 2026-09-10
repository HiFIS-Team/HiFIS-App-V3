package app.hifis.hifis.search

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.hifis.hifis.R
import app.hifis.hifis.ui.tap
import app.hifis.hifis.ui.theme.Dimens
import app.hifis.hifis.ui.theme.HifisTheme
import app.hifis.hifis.ui.theme.HifisType
import app.hifis.shared.search.AppSearch

/**
 * 헤더 검색 — **헤더 아래로 내려오는 판**
 *
 * 화면을 갈아 끼우지 않는다. 하던 자리를 **덮기만** 하고 닫으면 그대로 돌아온다 —
 * 검색은 지금 보던 것을 버리고 가는 일이 아니다.
 *
 * 뒤는 [Scrim] 이 **옅게** 덮는다. 흐리게까지 하면 좋지만 **컴포즈의 흐림은 API 31 부터**라
 * 하한(26)에서는 조용히 아무 일도 안 한다 — 그래서 어둡게 까는 것만으로 층을 낸다.
 * 진하게 깔면 어디서 열었는지 잊는다 — **글자는 못 읽되 무엇이 있었는지는 보여야 한다.**
 *
 * 아직 **뒤질 것이 없다** (`AppSearch`). 서버도 색인도 안 붙여서 빈 상태만 뜬다.
 */
@Composable
fun SearchOverlay(onClose: () -> Unit) {
    val colors = HifisTheme.colors
    var query by remember { mutableStateOf("") }
    val focus = remember { FocusRequester() }

    // 열자마자 글쇠판이 올라온다 — 검색은 바로 치려고 여는 자리다
    LaunchedEffect(Unit) { focus.requestFocus() }

    Box(Modifier.fillMaxSize()) {
        Scrim(onClose)
        Column(
            Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .imePadding()
                .padding(horizontal = Dimens.screenEdge),
        ) {
            // 헤더 높이만큼 비워 둔다 — 판이 **헤더 아래에서** 내려온 것처럼 보여야 한다
            Spacer(Modifier.height(Dimens.headerHeight + PANEL_GAP))
            Panel(query, focus) { query = it }
        }
    }
}

/**
 * 뒤를 덮는 어두운 막 — **누르면 닫힌다**
 *
 * 판 밖을 누르는 것이 나가는 가장 빠른 길이다.
 */
@Composable
private fun Scrim(onClose: () -> Unit) {
    Box(
        Modifier
            .fillMaxSize()
            .background(HifisTheme.colors.background.copy(alpha = SCRIM_ALPHA))
            .tap(label = "검색 닫기", onClick = onClose),
    )
}

@Composable
private fun Panel(query: String, focus: FocusRequester, onChange: (String) -> Unit) {
    val colors = HifisTheme.colors
    val shape = RoundedCornerShape(PANEL_RADIUS)
    Column(
        Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(colors.surface, shape)
            .border(1.dp, colors.line, shape),
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .height(FIELD_HEIGHT)
                .padding(horizontal = 18.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                painterResource(R.drawable.ic_search),
                contentDescription = null, // 옆 칸이 무엇인지 말한다
                tint = colors.inkTertiary,
                modifier = Modifier.size(Dimens.headerIcon),
            )
            Spacer(Modifier.width(12.dp))
            Box(Modifier.fillMaxWidth()) {
                BasicTextField(
                    value = query,
                    onValueChange = onChange,
                    textStyle = TextStyle(fontSize = 16.sp, color = colors.ink),
                    cursorBrush = SolidColor(colors.brand),
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(focus),
                )
                if (query.isEmpty()) {
                    Text(
                        AppSearch.PLACEHOLDER,
                        style = TextStyle(fontSize = 16.sp),
                        color = colors.inkTertiary,
                    )
                }
            }
        }

        // 판 안을 가르는 줄 — 화면 끝까지 간다 (판이 이미 좁다)
        Box(
            Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(colors.line),
        )

        Box(
            Modifier
                .fillMaxWidth()
                .height(RESULT_HEIGHT),
            contentAlignment = Alignment.Center,
        ) {
            // 아직 뒤질 것이 없다 — 서버도 색인도 안 붙였다
            Text(
                AppSearch.EMPTY,
                style = HifisType.body,
                color = colors.inkSecondary,
            )
        }
    }
}

/** 헤더와 판 사이 — 헤더에 붙으면 헤더가 늘어난 것처럼 보인다 */
private val PANEL_GAP = 12.dp

/** 판 모서리 — 카드(24)보다 작다. 화면을 덮는 판이라 각이 덜 둥근 편이 단단해 보인다 */
private val PANEL_RADIUS = 20.dp

/** 입력 줄 높이 */
private val FIELD_HEIGHT = 54.dp

/** 결과 자리 — 아직 빈 상태 한 줄만 든다 */
private val RESULT_HEIGHT = 120.dp

/**
 * 뒤를 덮는 진하기 — **무엇이 있었는지는 보여야 한다**
 *
 * 진하게 깔면 어디서 열었는지 잊는다. iOS 는 이 자리에 흐림을 0.55 로 얹는데,
 * 우리 화면이 거의 검정이라 그쪽도 진하게 하면 아무것도 안 남았다.
 */
private const val SCRIM_ALPHA = 0.55f
