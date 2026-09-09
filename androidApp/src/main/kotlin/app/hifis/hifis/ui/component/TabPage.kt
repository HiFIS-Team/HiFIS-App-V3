package app.hifis.hifis.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import app.hifis.hifis.ui.theme.HifisTheme

/**
 * 탭 화면의 공용 껍데기 — **헤더 + 본문**
 *
 * 홈·업무·일정·근태가 다 이걸 쓴다. 화면마다 헤더를 따로 그리면 언젠가 한 화면만
 * 빠지고, 그 탭에서는 사내톡·알림으로 갈 방법이 없어진다.
 *
 * **본문 스크롤은 화면이 정한다.** 헤더는 붙어 있고 본문만 굴리는 화면도 있고
 * (홈), 통째로 굴리는 화면도 있다 (일정).
 */
@Composable
fun TabPage(
    modifier: Modifier = Modifier,
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
        content()
    }
}
