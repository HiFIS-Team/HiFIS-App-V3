package app.hifis.hifis.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import app.hifis.hifis.ui.theme.Dimens
import app.hifis.hifis.ui.theme.HifisTheme
import app.hifis.shared.home.Notice
import app.hifis.shared.home.TodayWork

/**
 * 홈 — 모든 직원이 처음 보는 화면
 *
 * 첫 장은 **오늘 근무 카드**다 (V2 와 같다).
 *
 * 값은 아직 [TodayWork.demo] 다 — **서버를 안 붙였다.** 붙이면 그 자리를 갈아 끼운다.
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

        // 카드가 셋이라 작은 화면에서는 넘친다 — 본문만 굴린다 (헤더는 붙어 있다)
        Column(
            Modifier
                .verticalScroll(rememberScrollState())
                .padding(horizontal = Dimens.screenEdge)
                .padding(top = 16.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            TodayWorkCard(TodayWork.demo)
            // 누르는 자리는 아직 아무 데도 안 간다 — 갈 화면이 없다
            HomeShortcuts(onOpen = {})
            TodayNewsCard(Notice.demo, onOpen = {})
        }
    }
}
