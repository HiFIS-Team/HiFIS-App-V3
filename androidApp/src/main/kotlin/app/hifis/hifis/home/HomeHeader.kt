package app.hifis.hifis.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import app.hifis.hifis.R
import app.hifis.hifis.ui.component.HeaderIconButton
import app.hifis.hifis.ui.theme.Dimens
import app.hifis.hifis.ui.theme.HifisTheme

/**
 * 홈 헤더 — 왼쪽 지점, 오른쪽 검색·사내톡·알림·마이
 *
 * 글자가 없고 아이콘만 선다.
 *
 * **오른쪽 넷은 순서가 고정이다.** 자리를 외운 사람에게 순서가 바뀌면 못 찾는다.
 * 새 버튼이 생겨도 이 넷 사이에 끼우지 말고 지점 옆(왼쪽)에 붙인다.
 *
 * 헤더는 `surface`, 본문은 `background` 라 **선을 안 그어도 층이 갈린다**.
 * 스크롤 경계선은 본문이 생긴 다음에 필요하면 그때 정한다.
 */
@Composable
fun HomeHeader(
    onBranch: () -> Unit,
    onSearch: () -> Unit,
    onChat: () -> Unit,
    onNotification: () -> Unit,
    onProfile: () -> Unit,
    modifier: Modifier = Modifier,
    /** 한 지점을 보고 있으면 true — 지점 아이콘이 브랜드색으로 바뀐다 */
    branchPicked: Boolean = false,
    /** 안 읽은 방이 있으면 true */
    chatUnread: Boolean = false,
    /** 안 읽은 알림이 있으면 true */
    notificationUnread: Boolean = false,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(HifisTheme.colors.surface)
            .statusBarsPadding()
            .height(Dimens.headerHeight)
            // 터치 자리가 그림보다 넓어서 그만큼 빼야 **그림**이 화면 끝 20 에 선다.
            // 9 라고 직접 적지 않는다 — 버튼 크기를 바꾸면 같이 따라와야 한다
            .padding(horizontal = Dimens.screenEdge - Dimens.headerIconInset),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        HeaderIconButton(
            icon = R.drawable.ic_branch,
            label = "지점",
            active = branchPicked,
            onClick = onBranch,
        )

        Spacer(Modifier.weight(1f))

        HeaderIconButton(
            icon = R.drawable.ic_search,
            label = "검색",
            onClick = onSearch,
        )
        HeaderIconButton(
            icon = R.drawable.ic_chat,
            label = "사내톡",
            badge = chatUnread,
            onClick = onChat,
        )
        HeaderIconButton(
            icon = R.drawable.ic_bell,
            label = "알림",
            badge = notificationUnread,
            onClick = onNotification,
        )
        HeaderIconButton(
            icon = R.drawable.ic_person,
            label = "마이",
            onClick = onProfile,
        )
    }
}
