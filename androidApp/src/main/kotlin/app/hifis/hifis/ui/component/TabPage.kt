package app.hifis.hifis.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import app.hifis.hifis.ui.theme.Dimens
import app.hifis.hifis.ui.theme.HifisTheme
import app.hifis.hifis.ui.theme.HifisType

/**
 * 탭 화면의 공용 껍데기 — **헤더 + 본문**
 *
 * 홈·업무·일정·근태가 다 이걸 쓴다. 화면마다 헤더를 따로 그리면 언젠가 한 화면만
 * 빠지고, 그 탭에서는 사내톡·알림으로 갈 방법이 없어진다.
 *
 * **본문 스크롤은 화면이 정한다.** 헤더는 붙어 있고 본문만 굴리는 화면도 있고
 * (홈), 통째로 굴리는 화면도 있다 (일정).
 *
 * [title] 을 주면 헤더 바로 아래에 화면 이름이 한 줄 선다. **홈은 안 준다** —
 * 첫 화면이라 어디인지 물을 일이 없고, 그 자리는 알림 배너가 먼저 차지한다.
 */
@Composable
fun TabPage(
    modifier: Modifier = Modifier,
    title: String? = null,
    onBranch: () -> Unit = {},
    onSearch: () -> Unit = {},
    onScan: () -> Unit = {},
    onChat: () -> Unit = {},
    onNotification: () -> Unit = {},
    onProfile: () -> Unit = {},
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier
            .fillMaxSize()
            .background(HifisTheme.colors.background),
    ) {
        AppHeader(
            onBranch = onBranch,
            onSearch = onSearch,
            onScan = onScan,
            onChat = onChat,
            onNotification = onNotification,
            onProfile = onProfile,
        )
        if (title != null) {
            Text(
                title,
                style = HifisType.title,
                color = HifisTheme.colors.ink,
                modifier = Modifier.padding(
                    start = Dimens.screenEdge,
                    end = Dimens.screenEdge,
                    top = 10.dp,
                    bottom = 2.dp,
                ),
            )
        }
        content()
    }
}
