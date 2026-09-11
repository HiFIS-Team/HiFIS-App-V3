package app.hifis.hifis.shell

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
import app.hifis.shared.nav.HeaderAction
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
 * **헤더는 아이콘 줄뿐이다.** 화면 이름은 [ScreenTitle] 로 본문 **안에** 넣는다 —
 * 굴릴 때 같이 올라가야 한다. 여기서 그리면 붙어 있는 줄이 둘이 된다.
 *
 * **제품 고르개([ProductSwitch])는 여기 없다 — 홈에만 선다** (대표 결정, 2026-09-11).
 * 탭마다 세우면 업무 화면에서는 제품 알약과 공통/개인 알약이 위아래로 겹쳐 서서,
 * 모양이 같은데 무게가 다른 것이 둘 나란히 보인다.
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
            // 헤더 오른쪽은 제품이 정한다 — TeamFIS 는 출퇴근·사내톡이 없다
            actions = HeaderAction.android(LocalProduct.current.product),
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

/**
 * 화면 이름 — **본문 맨 위에 넣는다.** 헤더가 아니다
 *
 * 굴리면 같이 올라간다. 붙어 있는 것은 아이콘 줄(헤더)뿐이다.
 *
 * **홈에는 안 쓴다** — 첫 화면이라 어디인지 물을 일이 없고,
 * 그 자리는 알림 배너가 먼저 차지한다.
 */
@Composable
fun ScreenTitle(text: String, modifier: Modifier = Modifier) {
    Text(
        text,
        style = HifisType.title,
        color = HifisTheme.colors.ink,
        // 헤더와 붙으면 헤더의 일부처럼 보인다 — 위아래로 띄운다
        modifier = modifier.padding(
            start = Dimens.screenEdge,
            end = Dimens.screenEdge,
            top = 18.dp,
            bottom = 10.dp,
        ),
    )
}
