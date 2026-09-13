package app.hifis.hifis.teamfis

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.hifis.hifis.ui.theme.Dimens
import app.hifis.hifis.ui.theme.HifisColors
import app.hifis.hifis.ui.theme.HifisTheme
import app.hifis.hifis.ui.theme.HifisType
import app.hifis.hifis.ui.theme.eventColor
import app.hifis.shared.chat.ChatBox
import app.hifis.shared.teamfis.ClassStatus
import app.hifis.shared.teamfis.TeamClass
import app.hifis.shared.teamfis.TeamSchedule

/**
 * 수업 카드 — 달력에서 고른 날의 수업 하나 (2026-09-13 대표, 참고 사진의 짜임)
 *
 * ```
 * ● PT 30회                 [ 12/30회차 ]   ← 상태 점 + 상품 · 회차 알약
 * 18:00 ~ 19:00                             ← 눈이 먼저 닿는 줄
 * Ⓐ 김수현                          예정    ← 회원 · 상태
 * ```
 *
 * **지나간 수업은 조용히 물러난다** (업무 목록과 같은 규칙). 끝난 것을 색으로 띄우면
 * 눈이 거기 멈추는데, 봐야 하는 건 아직 안 한 수업이다.
 */
@Composable
fun TeamClassCard(item: TeamClass, modifier: Modifier = Modifier) {
    val colors = HifisTheme.colors
    val done = item.status == ClassStatus.DONE
    val mark = statusColor(item.status, colors)

    Column(
        modifier
            .fillMaxWidth()
            .background(colors.surface, RoundedCornerShape(Dimens.cardRadius))
            .padding(CARD_PADDING),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(DOT).background(mark, CircleShape))
            Spacer(Modifier.size(DOT_GAP))
            Text(
                item.product,
                style = HifisType.label.copy(fontWeight = FontWeight.SemiBold),
                color = if (done) colors.inkSecondary else colors.ink,
            )
            Spacer(Modifier.weight(1f))
            // 회차는 **알약에 담는다** — 상품 이름 옆에 그냥 두면 한 줄이 둘로 안 갈린다
            Text(
                item.roundLabel,
                style = HifisType.caption,
                color = colors.inkSecondary,
                modifier = Modifier
                    .background(colors.fieldFill, CircleShape)
                    .padding(horizontal = PILL_H, vertical = PILL_V),
            )
        }
        Spacer(Modifier.height(TIME_GAP))
        Text(
            item.timeLabel,
            // 시각이 카드에서 제일 큰 글자다 — 목록을 훑을 때 먼저 읽는 것이 시간이다
            style = HifisType.title.copy(fontFeatureSettings = HifisType.TABULAR),
            color = if (done) colors.inkTertiary else colors.ink,
        )
        Spacer(Modifier.height(FOOT_GAP))
        Row(verticalAlignment = Alignment.CenterVertically) {
            // **사진이 없다.** 이름 글자를 색 원에 넣는다 (사내톡과 같은 자리에서 뽑는다)
            Box(
                Modifier
                    .size(AVATAR)
                    .background(eventColor(ChatBox.colorIndex(item.member)), CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    ChatBox.initial(item.member),
                    fontSize = AVATAR_FONT,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                )
            }
            Spacer(Modifier.size(DOT_GAP))
            Text(
                item.member,
                style = HifisType.label,
                color = if (done) colors.inkTertiary else colors.inkSecondary,
            )
            Spacer(Modifier.weight(1f))
            Text(
                TeamSchedule.statusLabel(item.status),
                style = HifisType.caption.copy(fontWeight = FontWeight.Bold),
                color = mark,
            )
        }
    }
}

/** 아직 안 한 것만 색을 쓴다 — 끝난 것은 물러난다 */
private fun statusColor(status: ClassStatus, colors: HifisColors): Color =
    if (status == ClassStatus.DONE) colors.inkTertiary else colors.brand

/** 카드 안쪽 여백 — 세 줄짜리라 카드 기본값(24)보다 좁다 */
private val CARD_PADDING = 16.dp

/** 머리말 왼쪽 상태 점과 그 뒤 사이 */
private val DOT = 8.dp
private val DOT_GAP = 8.dp

/** 회차 알약 안쪽 여백 */
private val PILL_H = 10.dp
private val PILL_V = 4.dp

/** 머리말 → 시각 → 아래 줄 사이 */
private val TIME_GAP = 8.dp
private val FOOT_GAP = 10.dp

/**
 * 회원 아바타와 그 안 글자 — **두 글자가 들어간다**
 *
 * `ChatBox.initial` 은 이름에서 **두 글자**를 뽑는다 (사내톡과 같은 규칙이라 안 바꾼다).
 * 22 로 뒀더니 글자가 원에 꽉 차서 답답했다.
 */
private val AVATAR = 28.dp
private val AVATAR_FONT = 10.sp

/** 카드 사이 */
internal val CARD_GAP = 10.dp

/** 카드 묶음 위 여백 — `펼쳐보기` 줄과 갈라 놓는다 */
internal val LIST_TOP = 14.dp

/** 그날 수업이 없을 때 그 자리의 위아래 여백 */
internal val EMPTY_PAD = 52.dp
