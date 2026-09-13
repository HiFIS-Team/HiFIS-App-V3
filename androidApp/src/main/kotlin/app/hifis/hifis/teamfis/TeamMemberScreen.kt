package app.hifis.hifis.teamfis

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import app.hifis.hifis.R
import app.hifis.hifis.shell.TabPage
import app.hifis.hifis.ui.tap
import app.hifis.hifis.ui.theme.Dimens
import app.hifis.hifis.ui.theme.HifisTheme
import app.hifis.hifis.ui.theme.HifisType
import app.hifis.shared.teamfis.Member
import app.hifis.shared.teamfis.MemberBoard
import app.hifis.shared.teamfis.MemberStatus

/**
 * TeamFIS 회원 — **보유 회원 전체가 기본**이고 필터가 그 위에서 갈래를 좁힌다
 *
 * 짜임은 **TeamFIS 것과 같다** (2026-09-14 대표가 그 레포를 지목). 값은 HiFIS 토큰으로
 * 옮겨 심었다 — 그쪽은 모서리가 4 로 각진 벌인데, 그대로 가져오면 이 화면만 앱에서 튄다.
 *
 * **필터 줄은 헤더와 함께 붙어 있다.** 같이 흘러가면 목록 아래에서 갈래를 바꾸려고
 * 맨 위까지 되돌아가야 한다.
 *
 * 값은 아직 [MemberBoard.demo] 다 — **서버를 안 붙였다.**
 */
@Composable
fun TeamMemberScreen(
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
        val members = remember { MemberBoard.demo }
        // 화면을 돌려도 보던 갈래에 그대로 있는다
        var filter by rememberSaveable { mutableStateOf<MemberStatus?>(null) }
        val shown = MemberBoard.shown(members, filter)

        FilterBar(
            members = members,
            filter = filter,
            // 고른 것을 다시 누르면 풀려서 전체로 돌아온다
            onPick = { filter = if (filter == it) null else it },
        )

        if (shown.isEmpty()) {
            Text(
                MemberBoard.EMPTY,
                style = HifisType.body,
                color = HifisTheme.colors.inkSecondary,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = EMPTY_PAD),
            )
            return@TabPage
        }

        // 수십 줄로 길어지는 자리라 **보이는 줄만 만든다**
        LazyColumn {
            itemsIndexed(shown, key = { _, it -> it.id }) { index, member ->
                if (index > 0) RowDivider()
                MemberRow(member)
            }
        }
    }
}

/**
 * 필터 줄 — 활성 · 홀딩 · 만료
 *
 * **`전체` 칸을 안 둔다.** 아무것도 안 고른 상태가 곧 보유 회원 전체이고 그게 기본이다.
 * 칩마다 숫자를 달아 **고르지 않고도 갈래별 규모**가 보이게 한다.
 *
 * 오른쪽 끝이 **회원 추가**다. TeamFIS 는 안드로이드에서 FAB 을 쓰지만 우리는
 * 양 플랫폼을 같은 자리에 둔다 — 이 앱에 FAB 관습이 없고, 안드로이드 오른쪽 아래는
 * 떠 있는 AI 단추 자리다 (제품이 바뀌어도 자리를 비워 둔다).
 */
@Composable
private fun FilterBar(
    members: List<Member>,
    filter: MemberStatus?,
    onPick: (MemberStatus) -> Unit,
) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(
                start = Dimens.screenEdge,
                end = Dimens.screenEdge,
                top = BAR_TOP,
                bottom = BAR_BOTTOM,
            ),
        horizontalArrangement = Arrangement.spacedBy(CHIP_GAP),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        MemberBoard.statuses.forEach { status ->
            CountChip(
                label = MemberBoard.statusLabel(status),
                count = MemberBoard.count(members, status),
                picked = filter == status,
            ) { onPick(status) }
        }
        Spacer(Modifier.weight(1f))
        AddButton()
    }
}

/** 필터 칩 하나 — 이름 + 숫자. 고르면 제품색으로 찬다 */
@Composable
private fun CountChip(label: String, count: Int, picked: Boolean, onClick: () -> Unit) {
    val colors = HifisTheme.colors
    val spec = tween<Color>(CHIP_MS)
    val fill by animateColorAsState(if (picked) colors.brand else colors.surface, spec, label = "chip-fill")
    val tint by animateColorAsState(if (picked) Color.White else colors.inkSecondary, spec, label = "chip-tint")
    // 고른 칩은 제품색 위라 흐린 회색이 안 보인다 — 흰색을 반투명하게 깐다
    val numberTint by animateColorAsState(
        if (picked) Color.White.copy(alpha = 0.7f) else colors.inkTertiary, spec, label = "chip-num",
    )

    Row(
        Modifier
            .height(CHIP_HEIGHT)
            .background(fill, RoundedCornerShape(CHIP_RADIUS))
            .tap(label = label, onClick = onClick)
            .padding(horizontal = CHIP_PAD),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(label, style = HifisType.label, color = tint)
        Text(
            count.toString(),
            style = HifisType.label.copy(fontFeatureSettings = HifisType.TABULAR),
            color = numberTint,
        )
    }
}

/**
 * 회원 추가 — 칩과 같은 높이의 네모에 **플러스만 제품색**이다
 *
 * 바탕까지 채우지 않는다. 고른 필터 칩이 이미 제품색 **면**이라, 같은 줄에 찬 면이
 * 둘이면 어느 것이 고른 것인지 흐려진다.
 *
 * > **아직 갈 화면이 없다.** 붙으면 여기서 연다.
 */
@Composable
private fun AddButton() {
    val colors = HifisTheme.colors
    Box(
        Modifier
            .size(CHIP_HEIGHT)
            .background(colors.surface, RoundedCornerShape(CHIP_RADIUS))
            .tap(label = "회원 추가") {},
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            painterResource(R.drawable.ic_plus),
            contentDescription = null,
            tint = colors.brand,
            modifier = Modifier.size(ADD_ICON),
        )
    }
}

/**
 * 회원 한 줄
 *
 * ```
 * 김수현 회원님  [홀딩]              12/30회차
 * 마지막 9/5
 * ```
 */
@Composable
private fun MemberRow(member: Member) {
    val colors = HifisTheme.colors
    Row(
        Modifier
            .fillMaxWidth()
            .tap(label = member.label) {}
            .padding(horizontal = Dimens.screenEdge, vertical = ROW_PAD),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    member.label,
                    style = HifisType.body.copy(fontWeight = FontWeight.SemiBold),
                    color = colors.ink,
                )
                StatusBadge(member.status)
            }
            Spacer(Modifier.height(4.dp))
            Text(member.detail, style = HifisType.caption, color = colors.inkTertiary)
        }
        Text(
            member.progress,
            // 회차는 자릿수가 바뀌어도 오른쪽 끝이 안 흔들려야 한다
            style = HifisType.label.copy(fontFeatureSettings = HifisType.TABULAR),
            color = colors.inkSecondary,
        )
    }
}

/**
 * 상태 배지 — **활성에는 안 붙는다**
 *
 * 대부분이 활성이라 다 붙이면 목록이 배지로 뒤덮인다. 눈에 걸려야 할 예외에만 붙인다.
 * **만료는 제품색**이다 — 재등록을 붙여야 할 자리다.
 */
@Composable
private fun StatusBadge(status: MemberStatus) {
    if (status == MemberStatus.ACTIVE) return
    val colors = HifisTheme.colors
    Text(
        MemberBoard.statusLabel(status),
        style = HifisType.caption,
        color = if (status == MemberStatus.EXPIRED) colors.brand else colors.inkTertiary,
        modifier = Modifier
            .padding(start = BADGE_GAP)
            .background(colors.fieldFill, RoundedCornerShape(BADGE_RADIUS))
            .padding(horizontal = BADGE_H, vertical = BADGE_V),
    )
}

/** 줄 사이 얇은 선 — **바깥까지 안 간다** (화면 가장자리에 선이 붙으면 답답하다) */
@Composable
private fun RowDivider() {
    Box(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = Dimens.screenEdge)
            .height(1.dp)
            .background(HifisTheme.colors.line),
    )
}

/** 필터 줄 위아래 여백 */
private val BAR_TOP = 8.dp
private val BAR_BOTTOM = 12.dp

/** 필터 칩 — 높이·모서리·안쪽 여백·사이 */
private val CHIP_HEIGHT = 36.dp
private val CHIP_RADIUS = 14.dp
private val CHIP_PAD = 14.dp
private val CHIP_GAP = 8.dp

/** 칩이 물드는 빠르기 — 업무 요일 줄과 같다 (일곱 칸은 아니지만 같은 결이다) */
private const val CHIP_MS = 140

/** 회원 추가 단추 안의 플러스 */
private val ADD_ICON = 20.dp

/** 회원 한 줄의 위아래 여백 */
private val ROW_PAD = 16.dp

/** 상태 배지 — 이름과 사이·모서리·안쪽 여백 (수업 카드 배지와 같은 값) */
private val BADGE_GAP = 8.dp
private val BADGE_RADIUS = 8.dp
private val BADGE_H = 8.dp
private val BADGE_V = 2.dp
