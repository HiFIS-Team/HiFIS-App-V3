package app.hifis.hifis.ui.component

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.hifis.hifis.ui.tap
import app.hifis.hifis.ui.theme.HifisTheme

/**
 * 전환 스위치 — 회색 트랙 위에 **알약 하나가 미끄러진다**
 *
 * 칸마다 따로 켜고 끄면 옮기는 동안 둘 다 켜져 보이거나 툭 튄다. 알약 하나가
 * 자리를 옮긴다 (V2 `ModeSwitch`, 240ms).
 *
 * **고른 칸이 굵어져도 폭이 안 변한다.** 폭은 늘 굵은 글자로 재 두고 안 고른 글자는
 * 그 안에서 가운데 선다 — 안 그러면 고를 때마다 옆 칸이 밀린다.
 */
@Composable
fun ModeSwitch(
    left: String,
    right: String,
    rightSelected: Boolean,
    onChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = HifisTheme.colors
    val density = LocalDensity.current
    var leftWidth by remember { mutableIntStateOf(0) }
    var rightWidth by remember { mutableIntStateOf(0) }
    val spec = tween<Int>(SLIDE_MILLIS, easing = CubicBezierEasing(0.33f, 1f, 0.68f, 1f))
    val pillX by animateIntAsState(if (rightSelected) leftWidth else 0, spec, label = "pill-x")
    val pillWidth by animateIntAsState(if (rightSelected) rightWidth else leftWidth, spec, label = "pill-w")

    Box(
        modifier
            .height(HEIGHT)
            .clip(CircleShape)
            .background(colors.surface)
            .padding(PAD),
    ) {
        // 첫 프레임에는 폭을 모른다 — 재고 나서 그린다
        if (leftWidth > 0 && rightWidth > 0) {
            Box(
                Modifier
                    .offset { IntOffset(pillX, 0) }
                    .width(with(density) { pillWidth.toDp() })
                    .fillMaxHeight()
                    .background(colors.fieldFill, CircleShape),
            )
        }
        Row {
            Segment(left, selected = !rightSelected, onWidth = { leftWidth = it }) { onChange(false) }
            Segment(right, selected = rightSelected, onWidth = { rightWidth = it }) { onChange(true) }
        }
    }
}

@Composable
private fun Segment(label: String, selected: Boolean, onWidth: (Int) -> Unit, onClick: () -> Unit) {
    val colors = HifisTheme.colors
    Box(
        Modifier
            .onSizeChanged { onWidth(it.width) }
            .fillMaxHeight()
            .tap(label = label, onClick = onClick)
            .padding(horizontal = SEGMENT_H),
        contentAlignment = Alignment.Center,
    ) {
        // 폭은 늘 굵은 글자가 정한다
        Text(label, fontSize = 14.sp, fontWeight = FontWeight.Bold, modifier = Modifier.alpha(0f))
        Text(
            label,
            fontSize = 14.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
            color = if (selected) colors.ink else colors.inkSecondary,
        )
    }
}

/** 전환 스위치 — 높이·안쪽 여백·칸 좌우 여백 */
private val HEIGHT = 36.dp
private val PAD = 4.dp
private val SEGMENT_H = 18.dp

/** 알약이 옮겨 가는 데 걸리는 시간 — 목록바가 도는 자리는 다 이 값이다 (V2) */
private const val SLIDE_MILLIS = 240
