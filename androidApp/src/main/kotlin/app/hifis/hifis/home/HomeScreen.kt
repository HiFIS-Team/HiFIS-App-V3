package app.hifis.hifis.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import app.hifis.hifis.shell.ProductSwitch
import app.hifis.hifis.shell.TabPage
import app.hifis.hifis.ui.theme.Dimens
import app.hifis.shared.home.HomeAlert
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
fun HomeScreen(
    modifier: Modifier = Modifier,
    onSearch: () -> Unit = {},
    onScan: () -> Unit = {},
    onNotification: () -> Unit = {},
    onChat: () -> Unit = {},
) {
    TabPage(
        modifier,
        onSearch = onSearch,
        onScan = onScan,
        onNotification = onNotification,
        onChat = onChat,
    ) {
        // 카드가 셋이라 작은 화면에서는 넘친다 — 본문만 굴린다 (헤더는 붙어 있다)
        Column(
            Modifier
                .verticalScroll(rememberScrollState())
                .padding(horizontal = Dimens.screenEdge)
                .padding(top = 16.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            // **제품 고르개는 홈에만 선다** (대표 결정, 2026-09-11).
            // 카드 줄의 첫 칸이라 위 16 · 아래 12 를 카드와 같이 쓴다 — 따로 띄우지 않는다.
            // 가운데 선다. 카드는 폭을 다 쓰는데 이것만 짧아서, 왼쪽에 붙이면 줄이 시작하다 만 것처럼 보인다
            ProductSwitch(Modifier.align(Alignment.CenterHorizontally))
            HomeAlertBanner(HomeAlert.demo, onOpen = {})
            TodayWorkCard(TodayWork.demo)
            // 누르는 자리는 아직 아무 데도 안 간다 — 갈 화면이 없다
            HomeShortcuts(onOpen = {})
            TodayNewsCard(Notice.demo, onOpen = {})
        }
    }
}
