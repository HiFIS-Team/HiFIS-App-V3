package app.hifis.hifis.shell

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
import app.hifis.hifis.ui.theme.Dimens
import app.hifis.hifis.ui.theme.HifisTheme

/**
 * 앱 헤더 — 왼쪽 지점, 오른쪽 검색·스캔·사내톡·알림·마이
 *
 * **홈 전용이 아니다.** 탭 화면(홈·업무·일정·근태)이 다 같은 줄을 쓴다 —
 * 사내톡·알림·마이가 홈에서만 열리면 다른 탭에서는 갈 방법이 없어진다.
 * V2 도 셸이 이 줄을 그리고 화면은 왼쪽 버튼만 끼워 넣었다 (`header_action.dart`).
 *
 * 글자가 없고 아이콘만 선다.
 *
 * **오른쪽 순서는 앱이 나간 뒤로는 안 바꾼다.** 자리를 외운 사람에게 순서가
 * 바뀌면 못 찾는다. 지금은 아직 안 나가서 사이에 끼워 넣어도 잃을 것이 없다.
 *
 * 헤더는 `surface`, 본문은 `background` 라 **선을 안 그어도 층이 갈린다**.
 * 스크롤 경계선은 본문이 생긴 다음에 필요하면 그때 정한다.
 */
@Composable
fun AppHeader(
    onBranch: () -> Unit,
    onSearch: () -> Unit,
    onScan: () -> Unit,
    onChat: () -> Unit,
    onNotification: () -> Unit,
    onProfile: () -> Unit,
    modifier: Modifier = Modifier,
    /** 한 지점을 보고 있으면 true — 지점 아이콘이 브랜드색으로 바뀐다 */
    branchPicked: Boolean = false,
    /**
     * 출퇴근 스캔 버튼을 세울지 — **`doesFieldWork` (점장·직원) 에게만 true**
     *
     * 대표·관리자는 출퇴근을 안 찍어서 눌러도 할 일이 없다.
     * V2 는 데스크톱만 그렇게 하고 **폰은 전원에게 띄워 놨었다.**
     * 로그인이 붙기 전이라 지금은 늘 true 지만, 자리는 여기다.
     */
    canScan: Boolean = true,
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
        if (canScan) {
            HeaderIconButton(
                icon = R.drawable.ic_scan,
                label = "출퇴근 스캔",
                onClick = onScan,
            )
        }
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
