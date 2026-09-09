package app.hifis.hifis.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import app.hifis.hifis.ui.theme.HifisTheme

/**
 * 홈 — 모든 직원이 처음 보는 화면
 *
 * **지금은 헤더뿐이다.** 본문에 뭘 얹을지는 아직 안 정했다
 * (`.claude/SPEC.md` 에 정해지면 여기 붙인다).
 *
 * 누르는 자리는 아직 아무 데도 안 간다 — 갈 화면이 없다.
 */
@Composable
fun HomeScreen(modifier: Modifier = Modifier) {
    Column(
        modifier
            .fillMaxSize()
            .background(HifisTheme.colors.background),
    ) {
        HomeHeader(
            onBranch = {},
            onSearch = {},
            onChat = {},
            onNotification = {},
            onProfile = {},
        )
    }
}
