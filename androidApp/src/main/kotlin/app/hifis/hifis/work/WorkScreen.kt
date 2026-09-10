package app.hifis.hifis.work

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.hifis.hifis.R
import app.hifis.hifis.ui.component.ModeSwitch
import app.hifis.hifis.ui.component.ScreenTitle
import app.hifis.hifis.ui.component.TabPage
import app.hifis.hifis.ui.tap
import app.hifis.hifis.ui.theme.Dimens
import app.hifis.hifis.ui.theme.HifisTheme
import app.hifis.hifis.ui.theme.HifisType
import app.hifis.shared.work.EnvItem
import app.hifis.shared.work.MyTask
import app.hifis.shared.work.WorkBoard

/**
 * 업무 — **오늘 할 일 그 자체**다
 *
 * 하단바 탭이라 열자마자 오늘 점검할 것이 보여야 한다. V2 는 여기가 탭 다섯 개
 * (환경정비·동료 평가·회원 친절도·수업 개수·센터 기여도) 중 하나를 고르는 줄이었는데,
 * **매일 하는 일과 가끔 보는 것이 한 줄에 서 있어서** 매일 하는 사람이 매일 한 번 더 골랐다.
 * V3 는 공통 업무와 내 업무만 둔다 (2026-09-10 대표 결정).
 *
 * 제목 아래 두 칸으로 나뉜다. **둘은 도는 방식이 다르다** —
 * 공통 업무는 하루에 여러 번 해서 횟수가 늘고, 내 업무는 한 번씩 체크해서
 * 다 하면 완료·남으면 누락이다. 그래서 한쪽은 칩 격자, 한쪽은 체크 목록이다.
 */
@Composable
fun WorkScreen(
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
        // **지점이 정한 항목표다.** 서버가 붙으면 그 지점 것을 받아 쓴다
        val items = remember { EnvItem.base }
        // 오늘 몇 번 했는지 — **화면에만 있다.** 서버가 붙으면 그날 것을 받아 채운다
        val counts = remember { mutableStateMapOf<String, Int>() }
        val tasks = remember { mutableStateListOf(*MyTask.demo.toTypedArray()) }
        // 화면을 돌려도 보던 칸에 그대로 있는다
        var mine by rememberSaveable { mutableStateOf(false) }

        Column(
            Modifier
                .verticalScroll(rememberScrollState())
                // 떠 있는 AI 단추가 마지막 칩의 `+` 를 덮는다 — 그만큼 더 둬서 굴려 올릴 수 있게 한다
                .padding(bottom = Dimens.aiChatClear),
        ) {
            ScreenTitle(WorkBoard.TITLE)
            ModeSwitch(
                left = WorkBoard.COMMON,
                right = WorkBoard.MINE,
                rightSelected = mine,
                onChange = { mine = it },
                modifier = Modifier.padding(horizontal = Dimens.screenEdge),
            )
            Spacer(Modifier.height(SWITCH_BODY_GAP))
            if (mine) {
                MyTasks(tasks) { picked ->
                    // 화면을 먼저 바꾼다. 서버가 붙으면 그 뒤에 보낸다
                    val at = tasks.indexOfFirst { it.id == picked.id }
                    if (at >= 0) tasks[at] = tasks[at].check()
                }
            } else {
                Checklist(items, counts)
            }
        }
    }
}

/**
 * 내 업무 — **하루에 한 번씩 체크**한다
 *
 * **면을 안 깐다** (V2 와 같다). 회색 박스를 줄마다 두면 다섯 개짜리 목록이
 * 회색 덩어리 다섯으로 읽힌다. 줄 사이는 얇은 선이 가른다.
 */
@Composable
private fun MyTasks(tasks: List<MyTask>, onCheck: (MyTask) -> Unit) {
    val colors = HifisTheme.colors
    Column(Modifier.padding(horizontal = Dimens.screenEdge)) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                WorkBoard.MY_TODAY,
                style = HifisType.label.copy(fontWeight = FontWeight.Bold),
                color = colors.ink,
            )
            Spacer(Modifier.weight(1f))
            // 다 했으면 숫자 대신 `완료` 다 — 남은 것이 없다는 말이 숫자보다 빠르다
            Text(
                WorkBoard.progressLabel(tasks),
                style = HifisType.caption.copy(fontWeight = FontWeight.Bold),
                color = colors.brand,
            )
        }
        Spacer(Modifier.height(12.dp))

        if (tasks.isEmpty()) {
            Text(
                WorkBoard.EMPTY_TASKS,
                style = HifisType.body,
                color = colors.inkSecondary,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = EMPTY_PAD),
            )
            return@Column
        }

        // 진행 막대 — 머리말 숫자와 같은 말을 하지만 **눈이 먼저 닿는다**
        Box(
            Modifier
                .fillMaxWidth()
                .height(BAR_HEIGHT)
                .clip(RoundedCornerShape(BAR_HEIGHT / 2))
                .background(colors.fieldFill),
        ) {
            Box(
                Modifier
                    .fillMaxWidth(WorkBoard.progress(tasks))
                    .fillMaxHeight()
                    .background(colors.brand),
            )
        }
        Spacer(Modifier.height(6.dp))

        tasks.forEachIndexed { i, task ->
            if (i > 0) {
                Box(
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp)
                        .height(1.dp)
                        .background(colors.line),
                )
            }
            TaskRow(task) { onCheck(task) }
        }
    }
}

/**
 * 업무 한 줄 — 왼쪽 동그라미를 누르면 체크된다
 *
 * **다 한 줄은 잠근다** (V2 2026-08-20). 체크는 되돌릴 수 없어서 누를 자리가 아니다.
 * 눌리는 것처럼 보이는데 아무 일이 없으면 고장으로 읽힌다.
 *
 * **다 한 줄이 도드라지지 않는다.** 파란 면으로 띄우면 눈이 거기 멈추는데,
 * 봐야 하는 건 아직 안 한 줄이다 — 줄이 그어진 채로 조용히 물러난다.
 */
@Composable
private fun TaskRow(task: MyTask, onCheck: () -> Unit) {
    val colors = HifisTheme.colors
    val checked = task.checked
    Row(
        Modifier
            .fillMaxWidth()
            .then(if (checked) Modifier else Modifier.tap(label = task.content, onClick = onCheck))
            .padding(horizontal = 4.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            painterResource(
                if (checked) R.drawable.ic_check_circle_fill else R.drawable.ic_circle,
            ),
            contentDescription = null, // 바로 옆에 할 일이 적혀 있다
            tint = if (checked) colors.brand else colors.inkTertiary,
            modifier = Modifier.size(CHECK_SIZE),
        )
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(
                task.content,
                style = HifisType.body,
                // 다 한 줄은 **글자를 눕힌다** — 색만 바꾸면 남은 것과 한눈에 안 갈린다
                color = if (checked) colors.inkTertiary else colors.ink,
                textDecoration = if (checked) TextDecoration.LineThrough else null,
            )
            // 체크할 때 적어 넣은 값 — 아직 안 한 줄에는 안 붙는다
            task.value?.takeIf { it.isNotEmpty() }?.let { value ->
                Spacer(Modifier.height(3.dp))
                Text(
                    value,
                    style = HifisType.caption.copy(fontWeight = FontWeight.SemiBold),
                    color = colors.brand,
                )
            }
        }
    }
}

/**
 * 공통 업무 점검 — 항목이 **2열**로 내려가고, 칩 좌우 −/+ 로 오늘 횟수를 올린다
 *
 * **카드를 안 두른다** (V2 2026-09-01 대표 요청). 머리말은 바탕 위에 서고 칩이
 * 화면 폭을 다 쓴다 — 카드 여백이 좌우로 빠지면서 칩이 그만큼 넓어져 누르기 편하다.
 */
@Composable
private fun Checklist(items: List<EnvItem>, counts: MutableMap<String, Int>) {
    val colors = HifisTheme.colors
    val total = WorkBoard.total(counts)

    Column(Modifier.padding(horizontal = Dimens.screenEdge)) {
        // 머리말은 **칩 밖**이다 — 카드가 없으니 이 줄이 곧 묶음의 경계다
        Row(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                WorkBoard.TODAY_ITEMS,
                style = HifisType.label.copy(fontWeight = FontWeight.Bold),
                color = colors.ink,
            )
            Spacer(Modifier.weight(1f))
            // 누르면 오늘 수행 내역이 열린다 — **아직 그 판을 안 만들었다**
            Text(
                WorkBoard.totalLabel(total),
                style = HifisType.caption.copy(fontWeight = FontWeight.Bold),
                color = colors.brand,
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .tap(label = "오늘 내역") {}
                    .padding(horizontal = 8.dp, vertical = 4.dp),
            )
        }
        Spacer(Modifier.height(12.dp))

        if (items.isEmpty()) {
            Text(
                WorkBoard.EMPTY_ITEMS,
                style = HifisType.body,
                color = colors.inkSecondary,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = EMPTY_PAD),
            )
            return@Column
        }

        BoxWithConstraints(Modifier.fillMaxWidth()) {
            val chipWidth = (maxWidth - GRID_GAP) / COLUMNS
            val fontSize = chipFontSize(items.map { it.name }, chipWidth)
            Column {
                items.chunked(COLUMNS).forEachIndexed { row, pair ->
                    if (row > 0) Spacer(Modifier.height(GRID_GAP))
                    Row(Modifier.fillMaxWidth()) {
                        pair.forEachIndexed { col, item ->
                            if (col > 0) Spacer(Modifier.width(GRID_GAP))
                            CountChip(
                                label = item.name,
                                count = counts[item.id] ?: 0,
                                fontSize = fontSize,
                                modifier = Modifier.weight(1f),
                                onAdjust = { delta ->
                                    val next = (counts[item.id] ?: 0) + delta
                                    // 0 아래로는 안 내려간다 — 안 한 것을 덜 할 수는 없다
                                    if (next >= 0) counts[item.id] = next
                                },
                            )
                        }
                        // 항목이 홀수면 마지막 줄 오른쪽이 빈다 — 남은 칸을 채워
                        // 왼쪽 칩이 두 칸으로 늘어나지 않게 한다
                        repeat(COLUMNS - pair.size) {
                            Spacer(Modifier.width(GRID_GAP))
                            Spacer(Modifier.weight(1f))
                        }
                    }
                }
            }
        }
    }
}

/**
 * 모든 칩이 **함께 쓸** 글자 크기 — 제일 긴 이름이 들어가는 값으로 맞춘다
 *
 * 칩마다 알아서 줄이면 긴 이름(`화장실청소`)만 작아 보인다. 격자에서 그러면
 * 그 칸만 덜 중요한 것처럼 읽힌다.
 *
 * **굵은 글씨로 잰다.** 한 칩은 굵어지는데 보통 글씨로 재 두면 누른 순간 넘친다.
 */
@Composable
private fun chipFontSize(labels: List<String>, chipWidth: Dp): TextUnit {
    val measurer = rememberTextMeasurer()
    val density = androidx.compose.ui.platform.LocalDensity.current
    val available = chipWidth - BUTTON_WIDTH * 2 - 6.dp
    if (available <= 0.dp) return CHIP_FONT_BASE
    val availablePx = with(density) { available.toPx() }

    var size = CHIP_FONT_BASE.value
    labels.forEach { label ->
        val width = measurer.measure(
            AnnotatedString(label),
            style = TextStyle(fontSize = CHIP_FONT_BASE, fontWeight = FontWeight.Bold),
            maxLines = 1,
        ).size.width.toFloat()
        if (width > availablePx) {
            val fit = CHIP_FONT_BASE.value * availablePx / width
            if (fit < size) size = fit
        }
    }
    return size.coerceIn(CHIP_FONT_MIN, CHIP_FONT_BASE.value).sp
}

/**
 * 횟수 칩 — 왼쪽 −, 가운데 이름, 오른쪽 +
 *
 * **한 칩은 브랜드색으로 물든다.** 오늘 뭘 했는지가 격자를 훑을 때 한눈에 갈려야 한다.
 *
 * **횟수 숫자는 안 적는다.** V2 도 그랬다 — 칩에는 했는지 여부만 두고 몇 번인지는
 * 머리말의 `총 N회` 와 내역에서 본다. 좁은 칩에 숫자까지 넣으면 이름이 그만큼 줄어든다.
 */
@Composable
private fun CountChip(
    label: String,
    count: Int,
    fontSize: TextUnit,
    onAdjust: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = HifisTheme.colors
    val active = count > 0
    val shape = RoundedCornerShape(CHIP_RADIUS)
    // 눌린 뒤 색이 **스며들 듯** 바뀐다 — 하루에 수십 번 누르는 자리라 툭 튀면 피곤하다
    val spec = tween<Color>(180)
    val fill by animateColorAsState(
        if (active) colors.brand.copy(alpha = ACTIVE_FILL) else colors.surface,
        spec,
        label = "chip-fill",
    )
    val line by animateColorAsState(
        if (active) colors.brand.copy(alpha = ACTIVE_LINE) else colors.line,
        spec,
        label = "chip-line",
    )

    Row(
        modifier
            .height(CHIP_HEIGHT)
            .clip(shape)
            .background(fill, shape)
            .border(1.dp, line, shape),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        AdjustButton(
            icon = R.drawable.ic_minus,
            label = "$label 하나 줄이기",
            // 안 한 항목은 줄일 것이 없다 — 색으로 그렇다고 말한다
            tint = if (active) colors.danger else colors.inkTertiary,
        ) { onAdjust(-1) }

        Box(
            Modifier
                .weight(1f)
                .fillMaxHeight()
                // 가운데를 누르면 그 항목 내역이 열린다 — **아직 그 판을 안 만들었다**.
                // −/+ 는 제 자리가 따로 있어서 섞이지 않는다
                .tap(label = label) {},
            contentAlignment = Alignment.Center,
        ) {
            Text(
                label,
                fontSize = fontSize,
                fontWeight = if (active) FontWeight.Bold else FontWeight.Medium,
                color = if (active) colors.brand else colors.ink,
                maxLines = 1,
            )
        }

        AdjustButton(
            icon = R.drawable.ic_plus,
            label = "$label 하나 늘리기",
            tint = colors.brand,
        ) { onAdjust(1) }
    }
}

/**
 * 칩 좌우의 −/+ — **눌림 표시를 안 준다**
 *
 * 누른 결과가 칩 색으로 바로 보여서 따로 표시할 것이 없다 (V2 와 같다).
 * 아이콘 뒤에 면을 한 겹 깔아 단추처럼 보이게 한다.
 */
@Composable
private fun AdjustButton(icon: Int, label: String, tint: Color, onClick: () -> Unit) {
    Box(
        Modifier
            .width(BUTTON_WIDTH)
            .fillMaxHeight()
            .tap(label = label, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            Modifier
                .size(BUTTON_PLATE)
                .background(HifisTheme.colors.fieldFill, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                painterResource(icon),
                contentDescription = null, // 누르는 자리가 라벨을 들고 있다
                tint = tint,
                modifier = Modifier.size(BUTTON_ICON),
            )
        }
    }
}

/** 격자 칸 수 — 폰은 둘이다 */
private const val COLUMNS = 2

/** 칩 사이 (가로·세로 같다) */
private val GRID_GAP = 10.dp

/**
 * 칩 높이 — **44 보다 넉넉하게 준다.** 하루에 수십 번 누르는 자리다
 */
private val CHIP_HEIGHT = 56.dp

/** 칩 모서리 — 카드(24)보다 작다. 격자 안에 여럿이 서는 칸이다 */
private val CHIP_RADIUS = 14.dp

/**
 * −/+ 한 개의 폭 — **48 이다** (V2 2026-09-01 대표 요청, "누르기 편하게")
 *
 * 카드를 걷으면서 칩이 좌우로 넓어졌는데 그 자리를 글자만 쓰면 단추는 그대로 좁다.
 */
private val BUTTON_WIDTH = 48.dp

/** 그 안의 면과 아이콘 */
private val BUTTON_PLATE = 26.dp
private val BUTTON_ICON = 14.dp

/** 한 칩의 면·테두리 진하기 */
private const val ACTIVE_FILL = 0.16f
private const val ACTIVE_LINE = 0.45f

/** 칩 글자 — 기본과 하한. 이보다 줄면 읽을 수가 없어서 차라리 잘라 낸다 */
private val CHIP_FONT_BASE = 14.sp
private const val CHIP_FONT_MIN = 10f

/** 스위치와 본문 사이 */
private val SWITCH_BODY_GAP = 16.dp

/** 내 업무 진행 막대 두께 */
private val BAR_HEIGHT = 6.dp

/** 내 업무 줄 왼쪽 동그라미 */
private val CHECK_SIZE = 22.dp

/** 목록이 비었을 때 그 자리의 위아래 여백 */
private val EMPTY_PAD = 52.dp
