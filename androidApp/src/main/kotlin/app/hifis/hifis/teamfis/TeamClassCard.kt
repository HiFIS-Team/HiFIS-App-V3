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
import app.hifis.hifis.ui.theme.Dimens
import app.hifis.hifis.ui.theme.HifisColors
import app.hifis.hifis.ui.theme.HifisTheme
import app.hifis.hifis.ui.theme.HifisType
import app.hifis.shared.teamfis.ClassStatus
import app.hifis.shared.teamfis.TeamClass
import app.hifis.shared.teamfis.TeamSchedule

/**
 * 수업 카드 — 달력에서 고른 날의 수업 하나
 *
 * **무엇을 어디에 적는지는 TeamFIS 것과 같다** (2026-09-14 대표가 그 레포를 지목).
 *
 * ```
 * ● 김수현 회원님              [ 수업예정 ]   ← 상태 점 + 회원 · 상태 배지
 * 오후 2:00 ~ 3:00                           ← 눈이 먼저 닿는 줄
 * PT 30회                        12/30회차   ← 상품 · 회차
 * ```
 *
 * **시간이 제일 크다.** 하루를 시간 순으로 훑는 자리라 "누가"보다 "몇 시에"가 먼저 걸려야 한다.
 * 왼쪽 점은 **상태를 색으로만** 말한다 — 배지 글자를 안 읽고도 세로로 훑을 수 있다.
 */
@Composable
fun TeamClassCard(item: TeamClass, modifier: Modifier = Modifier) {
    val colors = HifisTheme.colors

    Column(
        modifier
            .fillMaxWidth()
            .background(colors.surface, RoundedCornerShape(Dimens.cardRadius))
            .padding(CARD_PADDING),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(DOT).background(dotColor(item.status, colors), CircleShape))
            Spacer(Modifier.size(DOT_GAP))
            Text(
                item.memberLabel,
                style = HifisType.label,
                color = colors.inkSecondary,
            )
            Spacer(Modifier.weight(1f))
            StatusBadge(item.status)
        }
        Spacer(Modifier.height(TIME_GAP))
        Text(
            item.timeLabel,
            // 시간은 자릿수가 바뀌어도 줄이 안 흔들려야 한다
            style = HifisType.title.copy(fontFeatureSettings = HifisType.TABULAR),
            color = colors.ink,
        )
        Spacer(Modifier.height(FOOT_GAP))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(item.product, style = HifisType.caption, color = colors.inkTertiary)
            Spacer(Modifier.weight(1f))
            Text(
                item.roundLabel,
                style = HifisType.caption.copy(fontFeatureSettings = HifisType.TABULAR),
                color = colors.inkTertiary,
            )
        }
    }
}

/**
 * 상태 배지 — **예정만 채운다**
 *
 * 지나간 것(완료·노쇼)을 채우면 눈이 거기 멈추는데, 봐야 하는 건 아직 안 한 수업이다.
 */
@Composable
private fun StatusBadge(status: ClassStatus) {
    val colors = HifisTheme.colors
    val filled = status == ClassStatus.SCHEDULED
    Text(
        TeamSchedule.statusLabel(status),
        style = HifisType.caption,
        color = when {
            filled -> Color.White
            status == ClassStatus.NO_SHOW -> colors.danger
            else -> colors.inkTertiary
        },
        modifier = Modifier
            .background(
                if (filled) colors.brand else colors.fieldFill,
                RoundedCornerShape(BADGE_RADIUS),
            )
            .padding(horizontal = BADGE_H, vertical = BADGE_V),
    )
}

/**
 * 훑을 때 쓰는 점 색 — 예정만 제품색이다
 *
 * 노쇼는 `danger` 다. TeamFIS 는 브랜드 레드를 노쇼에 쓰는데, 우리는 제품색이 곧
 * 그 레드라 그대로 쓰면 **예정과 노쇼가 같은 색**이 된다 (`DESIGN.md` 상태색 규칙).
 */
private fun dotColor(status: ClassStatus, colors: HifisColors): Color = when (status) {
    ClassStatus.SCHEDULED -> colors.brand
    ClassStatus.DONE -> colors.inkTertiary
    ClassStatus.NO_SHOW -> colors.danger
}

/** 카드 안쪽 여백 — 세 줄짜리라 카드 기본값(24)보다 좁다 */
private val CARD_PADDING = 16.dp

/** 머리말 왼쪽 상태 점과 그 뒤 사이 */
private val DOT = 8.dp
private val DOT_GAP = 8.dp

/** 상태 배지 — 모서리·안쪽 여백 */
private val BADGE_RADIUS = 8.dp
private val BADGE_H = 8.dp
private val BADGE_V = 3.dp

/** 머리말 → 시각 → 아래 줄 사이 */
private val TIME_GAP = 8.dp
private val FOOT_GAP = 12.dp

/** 카드 사이 */
internal val CARD_GAP = 10.dp

/** 카드 묶음 위 여백 — `펼쳐보기` 줄과 갈라 놓는다 */
internal val LIST_TOP = 14.dp

/** 그날 수업이 없을 때 그 자리의 위아래 여백 */
internal val EMPTY_PAD = 52.dp
