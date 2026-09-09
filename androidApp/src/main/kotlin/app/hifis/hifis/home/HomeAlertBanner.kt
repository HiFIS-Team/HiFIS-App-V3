package app.hifis.hifis.home

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import app.hifis.hifis.R
import app.hifis.hifis.ui.theme.Dimens
import app.hifis.hifis.ui.theme.HifisColors
import app.hifis.hifis.ui.theme.HifisTheme
import app.hifis.hifis.ui.theme.HifisType
import app.hifis.shared.home.AlertKind
import app.hifis.shared.home.HomeAlert
import app.hifis.shared.home.Tone
import kotlinx.coroutines.delay

/**
 * 홈 맨 위 알림 배너 — **한 장만 서고 몇 초마다 다음 것으로 바뀐다**
 *
 * 바뀔 때 **줄었다가 커지면서** 갈린다. 그냥 글자만 갈아 끼우면 바뀐 줄 모르고 지나간다.
 *
 * **닫기(X)를 두지 않는다.** 닫아 놓은 것을 언제 다시 띄울지가 또 정해야 할 일이 되고,
 * 어차피 몇 초 뒤면 다음 것으로 넘어간다.
 *
 * 알림이 없으면 **아무것도 안 그린다** — 빈 껍데기를 남기면 그만큼 홈이 밀린다.
 */
@Composable
fun HomeAlertBanner(
    alerts: List<HomeAlert>,
    onOpen: (HomeAlert) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (alerts.isEmpty()) return

    var index by remember { mutableIntStateOf(0) }

    // 한 장뿐이면 돌릴 것이 없다. 다른 탭으로 가면 이 화면이 사라져 저절로 멈춘다
    if (alerts.size > 1) {
        LaunchedEffect(alerts.size) {
            while (true) {
                delay(HomeAlert.ROTATE_MILLIS)
                index = (index + 1) % alerts.size
            }
        }
    }

    AnimatedContent(
        targetState = alerts[index % alerts.size],
        modifier = modifier.fillMaxWidth(),
        transitionSpec = {
            // 나가는 것은 줄어들며 사라지고, 들어오는 것은 줄어든 채로 나타나 커진다
            (scaleIn(tween(260), initialScale = 0.94f) + fadeIn(tween(260)))
                .togetherWith(scaleOut(tween(180), targetScale = 0.94f) + fadeOut(tween(180)))
        },
        label = "home-alert",
    ) { alert ->
        AlertCard(alert, onOpen)
    }
}

@Composable
private fun AlertCard(alert: HomeAlert, onOpen: (HomeAlert) -> Unit) {
    val colors = HifisTheme.colors
    val shape = RoundedCornerShape(Dimens.cardRadius)
    val tint = toneColor(alert.kind.tone, colors)

    Row(
        Modifier
            .fillMaxWidth()
            .shadow(6.dp, shape, clip = false)
            .clip(shape)
            .background(colors.surface, shape)
            .border(1.dp, colors.line, shape)
            .clickable(onClickLabel = alert.title) { onOpen(alert) }
            .padding(Dimens.alertPadding),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            Modifier
                .size(Dimens.alertChip)
                .background(
                    tint.copy(alpha = if (colors.isDark) 0.20f else 0.12f),
                    RoundedCornerShape(Dimens.alertChipRadius),
                ),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                painter = androidx.compose.ui.res.painterResource(drawableOf(alert.kind)),
                contentDescription = null, // 바로 옆에 글자가 있다
                tint = tint,
                modifier = Modifier.size(Dimens.alertIcon),
            )
        }

        Spacer(Modifier.width(14.dp))

        Column(Modifier.weight(1f)) {
            Text(
                alert.title,
                style = HifisType.body.copy(fontWeight = FontWeight.SemiBold),
                color = colors.ink,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                alert.detail,
                style = HifisType.caption,
                color = colors.inkTertiary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(top = 2.dp),
            )
        }

        Spacer(Modifier.width(12.dp))

        // 버튼은 카드 전체와 같은 곳으로 간다 — 따로 누를 자리를 만들지 않는다.
        // 갈 곳이 하나인데 누르는 자리를 둘로 나누면 어느 쪽이 무엇인지 설명해야 한다
        Text(
            alert.action,
            style = HifisType.label.copy(fontWeight = FontWeight.Bold),
            color = tint,
            modifier = Modifier
                .background(
                    tint.copy(alpha = if (colors.isDark) 0.20f else 0.12f),
                    RoundedCornerShape(10.dp),
                )
                .padding(horizontal = 14.dp, vertical = 8.dp),
        )
    }
}

/** 뜻([Tone])에서 색이 온다 — 갈래마다 새로 고르지 않는다 */
private fun toneColor(tone: Tone, colors: HifisColors): Color = when (tone) {
    Tone.NEUTRAL -> colors.inkSecondary
    Tone.GOOD -> colors.success
    Tone.CAUTION -> colors.warning
    Tone.BAD -> colors.danger
    Tone.INFO -> colors.brand
}

/**
 * 아이콘 이름을 그림 자원으로 바꾼다 — **이미 있는 것을 다시 쓴다**
 *
 * `when` 이 enum 을 다 덮어야 컴파일된다.
 */
private fun drawableOf(kind: AlertKind): Int = when (kind) {
    AlertKind.PROJECT_DUE -> R.drawable.ic_project
    AlertKind.TASK_LEFT -> R.drawable.ic_work
    AlertKind.APPROVED, AlertKind.REJECTED -> R.drawable.ic_approval
}
