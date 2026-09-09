package app.hifis.hifis.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import app.hifis.hifis.ui.theme.Dimens
import app.hifis.hifis.ui.theme.HifisColors
import app.hifis.hifis.ui.theme.HifisTheme
import app.hifis.hifis.ui.theme.HifisType
import app.hifis.shared.home.TodayWork
import app.hifis.shared.home.Tone
import kotlinx.coroutines.delay
import java.time.LocalTime

/**
 * 오늘 근무 카드 — 홈 첫 장. 실시간 시계 + 서버가 판정한 오늘 근태
 *
 * V2 의 `_HeroStatusCard` 를 그대로 옮겼다 (`home_status.dart`).
 * 짜임과 간격은 V2 그대로고, **색과 글꼴만 V3 토큰**이다.
 *
 * 진행률 계산은 `shared` 의 [TodayWork.rateAt] 이 한다 — 두 플랫폼이 같은 답을 내야 한다.
 */
@Composable
fun TodayWorkCard(work: TodayWork, modifier: Modifier = Modifier) {
    val colors = HifisTheme.colors

    // 매초 다시 그린다. **다른 탭으로 가면 이 화면이 사라져 자동으로 멈춘다** —
    // V2 는 IndexedStack 이 탭을 살려 둬서 멈추는 장치를 따로 달아야 했다
    var now by remember { mutableStateOf(LocalTime.now()) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(1_000)
            now = LocalTime.now()
        }
    }

    val rate = work.rateAt(now.hour * 60 + now.minute)

    Column(
        modifier
            .fillMaxWidth()
            .shadow(6.dp, RoundedCornerShape(Dimens.cardRadius), clip = false)
            .background(colors.surface, RoundedCornerShape(Dimens.cardRadius))
            .border(1.dp, colors.line, RoundedCornerShape(Dimens.cardRadius))
            .padding(Dimens.cardPadding),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                "오늘 근무",
                style = HifisType.label,
                color = colors.inkSecondary,
                modifier = Modifier.weight(1f),
            )
            StatusBadge(work.status.label, toneColor(work.status.tone, colors))
        }

        Spacer(Modifier.height(16.dp))

        Text(
            text = "%02d:%02d:%02d".format(now.hour, now.minute, now.second),
            style = HifisType.display.copy(fontFeatureSettings = HifisType.TABULAR),
            color = colors.ink,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
        )

        Spacer(Modifier.height(20.dp))

        WorkGauge(rate)

        Spacer(Modifier.height(10.dp))

        // 시작 — 진행률 — 종료
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text(work.shiftStartText, style = HifisType.caption, color = colors.inkTertiary)
            Spacer(Modifier.weight(1f))
            Text(
                "${(rate * 100).toInt()}%",
                style = HifisType.label.copy(fontWeight = androidx.compose.ui.text.font.FontWeight.Bold),
                color = colors.brand,
            )
            Spacer(Modifier.weight(1f))
            Text(work.shiftEndText, style = HifisType.caption, color = colors.inkTertiary)
        }

        Spacer(Modifier.height(18.dp))

        // 실제 출퇴근 스캔 기록
        Row(Modifier.fillMaxWidth()) {
            ScanRecord("출근", work.checkInText)
            Spacer(Modifier.weight(1f))
            ScanRecord("퇴근", work.checkOutText)
        }
    }
}

/** 상태 배지 — 색은 뜻([WorkTone])에서 온다. 여기서 상태별로 새로 고르지 않는다 */
private fun toneColor(tone: Tone, colors: HifisColors): Color = when (tone) {
    Tone.NEUTRAL -> colors.inkSecondary
    Tone.GOOD -> colors.success
    Tone.CAUTION -> colors.warning
    Tone.BAD -> colors.danger
    Tone.INFO -> colors.brand
}

@Composable
private fun StatusBadge(label: String, color: Color) {
    Text(
        text = label,
        style = HifisType.caption.copy(fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold),
        color = color,
        modifier = Modifier
            .background(color.copy(alpha = 0.12f), CircleShape)
            .padding(horizontal = 12.dp, vertical = 6.dp),
    )
}

/** 스캔 기록 한 짝 — 안 찍힌 시각은 흐리게 둔다 */
@Composable
private fun ScanRecord(label: String, time: String) {
    val colors = HifisTheme.colors
    val recorded = time != TodayWork.NO_TIME
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(label, style = HifisType.caption, color = colors.inkTertiary)
        Spacer(Modifier.width(8.dp))
        Text(
            time,
            style = HifisType.body.copy(
                fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold,
                fontFeatureSettings = HifisType.TABULAR,
            ),
            color = if (recorded) colors.ink else colors.inkTertiary,
        )
    }
}

/**
 * 근무 진행 게이지 — 트랙 + 채움 + 지금 자리를 짚는 손잡이
 *
 * 손잡이가 트랙보다 커서 줄 높이([Dimens.gaugeRow])를 트랙보다 크게 잡는다.
 */
@Composable
private fun WorkGauge(rate: Float) {
    val colors = HifisTheme.colors
    BoxWithConstraints(
        Modifier
            .fillMaxWidth()
            .height(Dimens.gaugeRow),
        contentAlignment = Alignment.CenterStart,
    ) {
        // 손잡이가 양 끝에서 트랙 밖으로 안 나가게 지름만큼 뺀 거리 위를 움직인다
        val thumbX = (maxWidth - Dimens.gaugeThumb) * rate

        Box(
            Modifier
                .fillMaxWidth()
                .height(Dimens.gaugeTrack)
                .background(colors.line, CircleShape),
        )
        Box(
            Modifier
                .fillMaxWidth(rate)
                .height(Dimens.gaugeTrack)
                .background(
                    Brush.horizontalGradient(
                        listOf(colors.brandGradientStart, colors.brandGradientEnd),
                    ),
                    CircleShape,
                ),
        )
        Box(
            Modifier
                .offset(x = thumbX)
                .size(Dimens.gaugeThumb)
                .shadow(3.dp, CircleShape)
                .background(colors.surface, CircleShape)
                .border(3.dp, colors.brand, CircleShape),
        )
    }
}
