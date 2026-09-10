package app.hifis.hifis.notification

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.hifis.hifis.R
import app.hifis.hifis.ui.component.HeaderIconButton
import app.hifis.hifis.ui.component.ScreenTitle
import app.hifis.hifis.ui.tap
import app.hifis.hifis.ui.theme.Dimens
import app.hifis.hifis.ui.theme.HifisTheme
import app.hifis.hifis.ui.theme.HifisType
import app.hifis.hifis.ui.theme.tone
import app.hifis.hifis.ui.theme.toneFillAlpha
import app.hifis.shared.notification.AppNotification
import app.hifis.shared.notification.NotificationBox
import app.hifis.shared.notification.NotificationKind
import kotlinx.datetime.toKotlinLocalDateTime
import java.time.LocalDateTime as JavaDateTime

/**
 * 알림함 — **옆에서 밀려 들어오는 잎** (헤더의 종이 연다)
 *
 * 전체 / 안읽음을 전환하며 **오늘·이전**으로 묶어 보여준다. V2 알림 화면을 그대로
 * 옮겼다 — 제목 줄 오른쪽에 새로고침·설정, 그 아래 전환 스위치, 비었으면 빈 카드.
 *
 * 눌러서 읽음 처리한다. **갈 곳으로 넘어가는 것은 아직 없다** — 갈 화면이 없다.
 * 새로고침·설정도 아직 아무 일도 안 한다 (서버도 설정 화면도 없다).
 *
 * 값은 [AppNotification.demo] 다 — **서버를 안 붙였다.**
 */
@Composable
fun NotificationScreen(onBack: () -> Unit) {
    // 시스템 뒤로가기도 이 화면을 닫는다 — 액티비티가 하나라 안 잡으면 앱이 통째로 나간다
    BackHandler(onBack = onBack)

    val colors = HifisTheme.colors
    // 시각은 한 번만 잡는다 — 매 프레임 흐르면 `12분 전` 이 보는 중에 바뀐다
    val now = remember { JavaDateTime.now().toKotlinLocalDateTime() }
    var items by remember { mutableStateOf(AppNotification.demo(now)) }
    var unreadOnly by rememberSaveable { mutableStateOf(false) }
    val unreadCount = items.count { !it.read }
    val sections = NotificationBox.sections(items, unreadOnly, now.date)

    Column(
        Modifier
            .fillMaxSize()
            .background(colors.background)
            // 셸 위에 얹힌 잎이라 빈 자리 터치가 아래 하단바로 샌다 — 이 층에서 멈춘다
            .pointerInput(Unit) {},
    ) {
        BackRow(onBack)
        Column(
            Modifier
                .verticalScroll(rememberScrollState())
                .padding(bottom = 24.dp),
        ) {
            Spacer(Modifier.height(TITLE_TOP_EXTRA))
            TitleRow()
            Spacer(Modifier.height(TITLE_SWITCH_GAP))
            ModeSwitch(
                left = NotificationBox.ALL,
                right = NotificationBox.unreadLabel(unreadCount),
                rightSelected = unreadOnly,
                onChange = { unreadOnly = it },
                modifier = Modifier.padding(horizontal = Dimens.screenEdge),
            )
            Spacer(Modifier.height(SWITCH_BODY_GAP))

            val open: (AppNotification) -> Unit = { picked ->
                // 읽음 — 화면을 먼저 바꾼다. 서버가 붙으면 그 뒤에 보낸다
                items = items.map { if (it.id == picked.id) it.markRead() else it }
            }
            Column(Modifier.padding(horizontal = Dimens.screenEdge)) {
                if (sections.isEmpty) {
                    EmptyCard(NotificationBox.emptyLabel(unreadOnly))
                } else {
                    if (sections.today.isNotEmpty()) {
                        SectionLabel(NotificationBox.TODAY)
                        NotificationCard(sections.today, now, open)
                    }
                    if (sections.today.isNotEmpty() && sections.earlier.isNotEmpty()) {
                        Spacer(Modifier.height(SECTION_GAP))
                    }
                    if (sections.earlier.isNotEmpty()) {
                        SectionLabel(NotificationBox.EARLIER)
                        NotificationCard(sections.earlier, now, open)
                    }
                }
            }
        }
    }
}

/** 왼쪽 위 뒤로가기 — 탭 화면 헤더와 같은 줄 높이·같은 자리다 */
@Composable
private fun BackRow(onBack: () -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .height(Dimens.headerHeight)
            .padding(horizontal = Dimens.screenEdge - Dimens.headerIconInset),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        HeaderIconButton(R.drawable.ic_chevron_left, "뒤로", onBack)
    }
}

/** 제목 줄 — 왼쪽 `알림`, 오른쪽 새로고침·설정 (V2 알림 화면과 같다) */
@Composable
private fun TitleRow() {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        ScreenTitle(NotificationBox.TITLE, Modifier.weight(1f))
        HeaderIconButton(R.drawable.ic_refresh, "새로고침", onClick = {
            // 아직 받을 곳이 없다 — 서버가 붙으면 다시 받는다
        })
        HeaderIconButton(R.drawable.ic_settings, "알림 설정", onClick = {
            // 아직 갈 곳이 없다 — 알림 설정 화면이 생기면 잇는다
        })
        // 그림이 화면 끝 `screenEdge` 에 서게 터치 여백만큼 뺀다 (헤더와 같은 계산)
        Spacer(Modifier.width(Dimens.screenEdge - Dimens.headerIconInset))
    }
}

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
private fun ModeSwitch(
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
    val spec = tween<Int>(SWITCH_SLIDE_MILLIS, easing = CubicBezierEasing(0.33f, 1f, 0.68f, 1f))
    val pillX by animateIntAsState(if (rightSelected) leftWidth else 0, spec, label = "pill-x")
    val pillWidth by animateIntAsState(if (rightSelected) rightWidth else leftWidth, spec, label = "pill-w")

    Box(
        modifier
            .height(SWITCH_HEIGHT)
            .clip(CircleShape)
            .background(colors.surface)
            .padding(SWITCH_PAD),
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
            .padding(horizontal = SEGMENT_PAD),
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

/** 목록이 비었을 때 자리를 채우는 카드 — 둥근 네모 아이콘과 한 줄 (V2 `EmptyCard`) */
@Composable
private fun EmptyCard(text: String) {
    val colors = HifisTheme.colors
    val shape = RoundedCornerShape(Dimens.cardRadius)
    Column(
        Modifier
            .fillMaxWidth()
            .shadow(6.dp, shape, clip = false)
            .clip(shape)
            .background(colors.surface, shape)
            .border(1.dp, colors.line, shape)
            .padding(vertical = EMPTY_PAD),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            Modifier
                .size(Dimens.alertChip)
                .background(colors.fieldFill, RoundedCornerShape(Dimens.alertChipRadius)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                painterResource(R.drawable.ic_bell),
                contentDescription = null, // 바로 아래 글이 말한다
                tint = colors.inkTertiary,
                modifier = Modifier.size(Dimens.alertIcon),
            )
        }
        Spacer(Modifier.height(14.dp))
        Text(text, style = HifisType.body, color = colors.inkSecondary)
    }
}

/** 오늘 · 이전 — 묶음 머리말은 전체 화면과 같은 글자다 */
@Composable
private fun SectionLabel(text: String) {
    Text(
        text,
        style = HifisType.caption.copy(fontWeight = FontWeight.Medium),
        color = HifisTheme.colors.inkTertiary,
        modifier = Modifier.padding(start = 4.dp),
    )
    Spacer(Modifier.height(SECTION_TITLE_GAP))
}

/** 한 묶음의 카드 — 줄 사이는 1px 선으로 가른다 */
@Composable
private fun NotificationCard(
    items: List<AppNotification>,
    now: kotlinx.datetime.LocalDateTime,
    onOpen: (AppNotification) -> Unit,
) {
    val colors = HifisTheme.colors
    val shape = RoundedCornerShape(Dimens.cardRadius)
    Column(
        Modifier
            .fillMaxWidth()
            .shadow(6.dp, shape, clip = false)
            .clip(shape)
            .background(colors.surface, shape)
            .border(1.dp, colors.line, shape)
            .padding(horizontal = CARD_PAD_H, vertical = CARD_PAD_V),
    ) {
        items.forEachIndexed { i, item ->
            if (i > 0) Box(Modifier.fillMaxWidth().height(1.dp).background(colors.line))
            NotificationRow(item, now) { onOpen(item) }
        }
    }
}

/**
 * 알림 한 줄 — 종류별 색 원 + 제목 + 곁글 + 시각, 안 읽었으면 오른쪽에 점
 *
 * **안 읽은 줄만 진하다.** 읽은 줄은 글자도 원도 가라앉아서 훑을 때 새것만 튄다.
 */
@Composable
private fun NotificationRow(item: AppNotification, now: kotlinx.datetime.LocalDateTime, onClick: () -> Unit) {
    val colors = HifisTheme.colors
    val unread = !item.read
    val tint = colors.tone(item.kind.tone)
    Row(
        Modifier
            .fillMaxWidth()
            .tap(label = item.title, onClick = onClick)
            .padding(vertical = ROW_PAD_V),
        verticalAlignment = Alignment.Top,
    ) {
        Box(
            Modifier
                .size(ROW_ICON_CIRCLE)
                .background(
                    tint.copy(alpha = if (unread) colors.toneFillAlpha else colors.toneFillAlpha / 2),
                    CircleShape,
                ),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                painterResource(drawableOf(item.kind)),
                contentDescription = null, // 바로 옆에 제목이 있다
                tint = if (unread) tint else colors.inkTertiary,
                modifier = Modifier.size(ROW_ICON),
            )
        }
        Spacer(Modifier.width(14.dp))
        Column(Modifier.weight(1f)) {
            Text(
                item.title,
                style = HifisType.body.copy(
                    fontWeight = if (unread) FontWeight.SemiBold else FontWeight.Normal,
                ),
                color = if (unread) colors.ink else colors.inkSecondary,
            )
            // 곁글은 제목만으로 모자란 것을 채운다 (`· 사유: …` 같은 것)
            item.body?.takeIf { it.isNotEmpty() }?.let { body ->
                Spacer(Modifier.height(3.dp))
                Text(
                    body,
                    style = HifisType.caption,
                    color = colors.inkTertiary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Spacer(Modifier.height(3.dp))
            Text(
                NotificationBox.timeLabel(item.createdAt, now),
                style = HifisType.caption,
                color = colors.inkTertiary,
            )
        }
        if (unread) {
            Spacer(Modifier.width(8.dp))
            Box(
                Modifier
                    .padding(top = 6.dp)
                    .size(Dimens.badgeDot)
                    .background(colors.brand, CircleShape),
            )
        }
    }
}

/**
 * 종류 아이콘을 그림 자원으로 바꾼다 — **이미 있는 것을 다시 쓴다**
 *
 * **`when` 이 enum 을 다 덮어야 컴파일된다.** 종류를 추가하면 여기서 걸린다.
 */
private fun drawableOf(kind: NotificationKind): Int = when (kind) {
    NotificationKind.ATTENDANCE -> R.drawable.ic_attendance
    NotificationKind.LEAVE -> R.drawable.ic_sun
    NotificationKind.NOTICE -> R.drawable.ic_notice
    NotificationKind.CHAT -> R.drawable.ic_chat
    NotificationKind.APPROVAL -> R.drawable.ic_approval
    NotificationKind.PROJECT -> R.drawable.ic_project
    NotificationKind.PAYROLL -> R.drawable.ic_salary
    NotificationKind.SCHEDULE -> R.drawable.ic_schedule
    NotificationKind.RANKING -> R.drawable.ic_ranking
    NotificationKind.MEETING -> R.drawable.ic_meeting
    NotificationKind.STAFF -> R.drawable.ic_staff
    NotificationKind.MY_TASK_MISSING -> R.drawable.ic_work
    NotificationKind.PT_SURVEY -> R.drawable.ic_dumbbell
    NotificationKind.OTHER -> R.drawable.ic_bell
}

/** 제목 위에 더 붙이는 여백 — 전체 화면과 같은 값. 헤더에서 32 떨어진다 */
private val TITLE_TOP_EXTRA = 14.dp

/** 제목(아래 10)과 스위치 사이에 더 두는 것 — 합쳐서 16 */
private val TITLE_SWITCH_GAP = 6.dp

/** 스위치와 본문(카드) 사이 */
private val SWITCH_BODY_GAP = 20.dp

/** 전환 스위치 — 높이·안쪽 여백·칸 좌우 여백 */
private val SWITCH_HEIGHT = 36.dp
private val SWITCH_PAD = 4.dp
private val SEGMENT_PAD = 18.dp

/** 알약이 옮겨 가는 데 걸리는 시간 — 목록바가 도는 자리는 다 이 값이다 (V2) */
private const val SWITCH_SLIDE_MILLIS = 240

/** 빈 카드 위아래 여백 */
private val EMPTY_PAD = 52.dp

/** 묶음 사이 · 묶음 머리말과 카드 사이 */
private val SECTION_GAP = 24.dp
private val SECTION_TITLE_GAP = 10.dp

/** 목록 카드 안쪽 여백 — 줄이 제 위아래 여백을 가져서 세로는 얇다 */
private val CARD_PAD_H = 20.dp
private val CARD_PAD_V = 4.dp

/** 한 줄의 위아래 여백 · 종류 색 원 · 그 안의 그림 */
private val ROW_PAD_V = 12.dp
private val ROW_ICON_CIRCLE = 40.dp
private val ROW_ICON = 20.dp
