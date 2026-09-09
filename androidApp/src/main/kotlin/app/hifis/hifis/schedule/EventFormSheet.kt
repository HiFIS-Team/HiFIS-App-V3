package app.hifis.hifis.schedule

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import app.hifis.hifis.R
import app.hifis.hifis.ui.tap
import app.hifis.hifis.ui.theme.Dimens
import app.hifis.hifis.ui.theme.HifisTheme
import app.hifis.hifis.ui.theme.HifisType
import app.hifis.hifis.ui.theme.eventColor
import app.hifis.shared.schedule.EventKind
import app.hifis.shared.schedule.EventPalette
import app.hifis.shared.schedule.EventScope

/**
 * 일정 추가 — 아래에서 올라오는 판
 *
 * 담는 것: 제목 · 시작 · 종료 · 종류 · 공유 범위 · 색 · 메모.
 *
 * **아직 아무것도 저장되지 않는다.** 서버가 없어서 `저장` 을 누르면 그냥 닫힌다.
 * 날짜·시각도 지금은 글자로 받는다 — 고르개는 다음이다.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventFormSheet(onDismiss: () -> Unit) {
    val colors = HifisTheme.colors
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var title by remember { mutableStateOf("") }
    var startDate by remember { mutableStateOf("") }
    var startTime by remember { mutableStateOf("") }
    var endDate by remember { mutableStateOf("") }
    var endTime by remember { mutableStateOf("") }
    var kind by remember { mutableStateOf(EventKind.MEETING) }
    var scope by remember { mutableStateOf(EventScope.CENTER) }
    var colorIndex by remember { mutableIntStateOf(EventPalette.DEFAULT) }
    var memo by remember { mutableStateOf("") }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = colors.surface,
        dragHandle = {
            Box(Modifier.fillMaxWidth().padding(vertical = 10.dp), Alignment.Center) {
                Box(
                    Modifier
                        .size(width = 36.dp, height = 4.dp)
                        .background(colors.line, CircleShape),
                )
            }
        },
    ) {
        Column(
            Modifier
                .verticalScroll(rememberScrollState())
                .padding(horizontal = Dimens.screenEdge)
                .padding(bottom = 32.dp),
        ) {
            SheetHeader(onDismiss)

            Section("제목")
            Field(title, { title = it }, "무엇을 계획하고 있나요?")

            Section("시작")
            DateTimeRow(startDate, { startDate = it }, startTime, { startTime = it })

            Section("종료")
            DateTimeRow(endDate, { endDate = it }, endTime, { endTime = it })

            Section("카테고리")
            KindChips(kind) { kind = it }

            Section("공유 범위")
            ScopeCards(scope) { scope = it }

            Section("색상")
            ColorSwatches(colorIndex) { colorIndex = it }

            Section("메모", optional = true)
            Field(
                memo,
                { memo = it },
                "참석자·장소·준비물 등 상세 내용을 적어주세요",
                minHeight = 110.dp,
            )

            Spacer(Modifier.height(28.dp))
            SaveButton(enabled = title.isNotBlank(), onClick = onDismiss)
        }
    }
}

@Composable
private fun SheetHeader(onClose: () -> Unit) {
    val colors = HifisTheme.colors
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            Modifier
                .size(48.dp)
                .background(
                    colors.brand.copy(alpha = if (colors.isDark) 0.20f else 0.12f),
                    RoundedCornerShape(14.dp),
                ),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                painterResource(R.drawable.ic_schedule),
                contentDescription = null,
                tint = colors.brand,
                modifier = Modifier.size(24.dp),
            )
        }
        Spacer(Modifier.width(14.dp))
        Column(Modifier.weight(1f)) {
            Text("일정 추가", style = HifisType.title.copy(fontSize = 20.sp()), color = colors.ink)
            Text(
                "센터와 공유할 일정을 만들어요",
                style = HifisType.caption,
                color = colors.inkTertiary,
            )
        }
        Box(
            Modifier
                .size(36.dp)
                .clip(CircleShape)
                .tap(label = "닫기", onClick = onClose),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                painterResource(R.drawable.ic_close),
                contentDescription = "닫기",
                tint = colors.inkTertiary,
                modifier = Modifier.size(20.dp),
            )
        }
    }
}

/** `20.sp` 를 스타일 안에서 바꾸려고 쓰는 잔손질 */
private fun Int.sp() = androidx.compose.ui.unit.TextUnit(
    this.toFloat(),
    androidx.compose.ui.unit.TextUnitType.Sp,
)

@Composable
private fun Section(label: String, optional: Boolean = false) {
    val colors = HifisTheme.colors
    Spacer(Modifier.height(22.dp))
    Row(verticalAlignment = Alignment.Bottom) {
        Text(
            label,
            style = HifisType.label.copy(fontWeight = FontWeight.Bold),
            color = colors.ink,
        )
        if (optional) {
            Spacer(Modifier.width(5.dp))
            Text("(선택)", style = HifisType.caption, color = colors.inkTertiary)
        }
    }
    Spacer(Modifier.height(10.dp))
}

/** 글자를 받는 칸 — 면은 [HifisColors.fieldFill] 이다 */
@Composable
private fun Field(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    trailingIcon: Int? = null,
    minHeight: androidx.compose.ui.unit.Dp = 52.dp,
) {
    val colors = HifisTheme.colors
    val shape = RoundedCornerShape(14.dp)
    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        textStyle = HifisType.label.copy(color = colors.ink),
        cursorBrush = SolidColor(colors.brand),
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = minHeight)
            .background(colors.fieldFill, shape),
        decorationBox = { inner ->
            Row(
                Modifier.padding(horizontal = 16.dp, vertical = 15.dp),
                verticalAlignment = if (minHeight > 60.dp) Alignment.Top else Alignment.CenterVertically,
            ) {
                Box(Modifier.weight(1f)) {
                    if (value.isEmpty()) {
                        Text(placeholder, style = HifisType.label, color = colors.inkTertiary)
                    }
                    inner()
                }
                if (trailingIcon != null) {
                    Spacer(Modifier.width(10.dp))
                    Icon(
                        painterResource(trailingIcon),
                        contentDescription = null,
                        tint = colors.inkTertiary,
                        modifier = Modifier.size(18.dp),
                    )
                }
            }
        },
    )
}

/** 날짜 + 시각 — 날짜가 더 길어서 자리를 더 준다 */
@Composable
private fun DateTimeRow(
    date: String,
    onDate: (String) -> Unit,
    time: String,
    onTime: (String) -> Unit,
) {
    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        Field(date, onDate, "YYYY-MM-DD", Modifier.weight(1.7f), R.drawable.ic_schedule)
        Field(time, onTime, "--:--", Modifier.weight(1f), R.drawable.ic_attendance)
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun KindChips(picked: EventKind, onPick: (EventKind) -> Unit) {
    val colors = HifisTheme.colors
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        EventKind.all.forEach { kind ->
            val on = kind == picked
            Row(
                Modifier
                    .clip(CircleShape)
                    .background(
                        if (on) colors.brand.copy(alpha = if (colors.isDark) 0.18f else 0.10f)
                        else Color.Transparent,
                        CircleShape,
                    )
                    .border(1.dp, if (on) colors.brand else colors.line, CircleShape)
                    .tap(label = kind.label) { onPick(kind) }
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    painterResource(drawableOf(kind.icon)),
                    contentDescription = null,
                    tint = if (on) colors.brand else colors.inkSecondary,
                    modifier = Modifier.size(16.dp),
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    kind.label,
                    style = HifisType.caption.copy(fontWeight = FontWeight.SemiBold),
                    color = if (on) colors.brand else colors.ink,
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ScopeCards(picked: EventScope, onPick: (EventScope) -> Unit) {
    val colors = HifisTheme.colors
    val shape = RoundedCornerShape(14.dp)
    FlowRow(
        maxItemsInEachRow = 2,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        EventScope.all.forEach { scope ->
            val on = scope == picked
            Column(
                Modifier
                    .weight(1f)
                    .clip(shape)
                    .background(
                        if (on) colors.brand.copy(alpha = if (colors.isDark) 0.16f else 0.08f)
                        else Color.Transparent,
                        shape,
                    )
                    .border(1.dp, if (on) colors.brand else colors.line, shape)
                    .tap(label = scope.label) { onPick(scope) }
                    .padding(horizontal = 14.dp, vertical = 13.dp),
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        painterResource(drawableOf(scope.icon)),
                        contentDescription = null,
                        tint = if (on) colors.brand else colors.inkSecondary,
                        modifier = Modifier.size(17.dp),
                    )
                    Spacer(Modifier.width(7.dp))
                    Text(
                        scope.label,
                        style = HifisType.label.copy(fontWeight = FontWeight.Bold),
                        color = if (on) colors.brand else colors.ink,
                    )
                }
                Spacer(Modifier.height(3.dp))
                Text(scope.detail, style = HifisType.caption, color = colors.inkTertiary)
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ColorSwatches(picked: Int, onPick: (Int) -> Unit) {
    val colors = HifisTheme.colors
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        EventPalette.colors.indices.forEach { index ->
            val on = index == picked
            Box(
                Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(eventColor(index), CircleShape)
                    // 고른 것에만 바탕색 테를 둘러 띄운다 — 색끼리 붙어 있어 테가 없으면 못 찾는다
                    .then(if (on) Modifier.border(2.5.dp, colors.ink, CircleShape) else Modifier)
                    .tap(label = "색 ${index + 1}") { onPick(index) },
                contentAlignment = Alignment.Center,
            ) {
                if (on) {
                    Icon(
                        painterResource(R.drawable.ic_check),
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(16.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun SaveButton(enabled: Boolean, onClick: () -> Unit) {
    val colors = HifisTheme.colors
    val shape = RoundedCornerShape(14.dp)
    Box(
        Modifier
            .fillMaxWidth()
            .height(54.dp)
            .clip(shape)
            .background(if (enabled) colors.brand else colors.fieldFill, shape)
            .then(if (enabled) Modifier.tap(label = "저장", onClick = onClick) else Modifier),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            "저장",
            style = HifisType.body.copy(fontWeight = FontWeight.Bold),
            color = if (enabled) Color.White else colors.inkTertiary,
        )
    }
}

/**
 * 아이콘 이름을 그림 자원으로 — **`shared` 가 이름만 들고 있어서 여기서 잇는다**
 *
 * 여기 빠진 이름이 있으면 그 칩만 그림이 없다. `shared` 의 테스트가
 * 이름이 `ic_` 로 시작하는지까지는 보지만, 자원이 있는지는 이 표가 지킨다.
 */
private fun drawableOf(icon: String): Int = when (icon) {
    "ic_people" -> R.drawable.ic_people
    "ic_dumbbell" -> R.drawable.ic_dumbbell
    "ic_attendance" -> R.drawable.ic_attendance
    "ic_pin" -> R.drawable.ic_pin
    "ic_sun" -> R.drawable.ic_sun
    "ic_moon" -> R.drawable.ic_moon
    "ic_notice" -> R.drawable.ic_notice
    "ic_gift" -> R.drawable.ic_gift
    "ic_work" -> R.drawable.ic_work
    "ic_person_check" -> R.drawable.ic_person_check
    "ic_cap" -> R.drawable.ic_cap
    "ic_chat" -> R.drawable.ic_chat
    "ic_glass" -> R.drawable.ic_glass
    "ic_heart" -> R.drawable.ic_heart
    "ic_person" -> R.drawable.ic_person
    "ic_dots" -> R.drawable.ic_dots
    "ic_more" -> R.drawable.ic_more
    "ic_branch" -> R.drawable.ic_branch
    "ic_project" -> R.drawable.ic_project
    else -> R.drawable.ic_dots
}
