package app.hifis.hifis.chat

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.hifis.hifis.R
import app.hifis.hifis.shell.HeaderIconButton
import app.hifis.hifis.ui.tap
import app.hifis.hifis.ui.theme.Dimens
import app.hifis.hifis.ui.theme.HifisColors
import app.hifis.hifis.ui.theme.HifisTheme
import app.hifis.hifis.ui.theme.HifisType
import app.hifis.hifis.ui.theme.eventColor
import app.hifis.shared.chat.ChatBox
import app.hifis.shared.chat.ChatMate
import app.hifis.shared.chat.ChatPresence
import app.hifis.shared.chat.ChatRoom
import java.time.LocalDateTime as JavaDateTime
import kotlinx.datetime.toKotlinLocalDateTime

/**
 * 사내톡 목록 — **옆에서 밀려 들어오는 잎** (헤더의 말풍선이 연다)
 *
 * 디스코드 대화 목록을 옮겼다 (대표 지목, 2026-09-10). 위에서부터
 * 단추 줄(검색·직원 찾기·새 대화) · 접속 중인 동료 가로줄 · 대화방 세로 목록,
 * 그리고 화면 맨 아래에 **내 칸**이 붙는다.
 *
 * **왼쪽 세로 띠는 안 가져왔다.** 디스코드에서 그 자리는 서버 목록인데
 * 사내톡은 회사 하나라 넣을 것이 없다. 빈 띠를 세우면 화면 폭만 먹는다.
 *
 * **아직 서버가 없다.** 목록은 [ChatRoom.demo] 이고 눌러도 방이 안 열린다 — 방 화면이 없다.
 */
@Composable
fun ChatListScreen(onBack: () -> Unit) {
    // 시스템 뒤로가기도 이 화면을 닫는다 — 액티비티가 하나라 안 잡으면 앱이 통째로 나간다
    BackHandler(onBack = onBack)

    val colors = HifisTheme.colors
    // 시각은 한 번만 잡는다 — 매 프레임 흐르면 `40분` 이 보는 중에 바뀐다
    val now = remember { JavaDateTime.now().toKotlinLocalDateTime() }
    val rooms = remember { ChatRoom.demo(now) }
    val mates = remember { ChatRoom.demoMates() }
    val me = remember { ChatRoom.demoMe() }

    Column(
        Modifier
            .fillMaxSize()
            .background(colors.background)
            // 셸 위에 얹힌 잎이라 빈 자리 터치가 아래 하단바로 샌다 — 이 층에서 멈춘다
            .pointerInput(Unit) {},
    ) {
        Header(onBack)
        Column(
            Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState()),
        ) {
            Spacer(Modifier.height(HEADER_BODY_GAP))
            ActionRow()
            Spacer(Modifier.height(SECTION_GAP))
            MateRow(mates)
            Spacer(Modifier.height(SECTION_GAP))
            if (rooms.isEmpty()) {
                Text(
                    ChatBox.EMPTY,
                    style = HifisType.body,
                    color = colors.inkSecondary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = EMPTY_PAD),
                )
            } else {
                rooms.forEach { room -> RoomRow(room, now) }
            }
            Spacer(Modifier.height(SECTION_GAP))
        }
        MeBar(me)
    }
}

/** 잎 헤더 — 왼쪽 뒤로가기, 그 옆에 화면 이름. 알림함과 같은 줄이다 */
@Composable
private fun Header(onBack: () -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .height(Dimens.headerHeight)
            .padding(horizontal = Dimens.screenEdge - Dimens.headerIconInset),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        HeaderIconButton(R.drawable.ic_chevron_left, "뒤로", onBack)
        Spacer(Modifier.width(HEADER_TITLE_GAP))
        Text(ChatBox.TITLE, style = HifisType.header, color = HifisTheme.colors.ink)
    }
}

/**
 * 단추 줄 — 검색 · 직원 찾기 · 새 대화
 *
 * 참고한 화면은 가운데가 `친구 추가하기` 였다. 회사에는 친구를 맺는 절차가 없어서
 * (다 같은 조직이다) **직원 찾기**로 바꿨다 — 조직도에서 사람을 골라 1:1 을 여는 자리다.
 * 오른쪽 `+` 는 여럿을 부르는 새 대화라 둘이 하는 일이 다르다.
 *
 * **아직 셋 다 갈 곳이 없다.**
 */
@Composable
private fun ActionRow() {
    val colors = HifisTheme.colors
    Row(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = Dimens.screenEdge),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Box(
            Modifier
                .size(ACTION_HEIGHT)
                .clip(CircleShape)
                .background(colors.surface)
                .tap(label = ChatBox.SEARCH) {},
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                painterResource(R.drawable.ic_search),
                contentDescription = ChatBox.SEARCH,
                tint = colors.inkSecondary,
                modifier = Modifier.size(Dimens.headerIcon),
            )
        }

        val pill = RoundedCornerShape(ACTION_RADIUS)
        Row(
            Modifier
                .weight(1f)
                .height(ACTION_HEIGHT)
                .clip(pill)
                .background(colors.surface, pill)
                .tap(label = ChatBox.FIND_STAFF) {},
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                painterResource(R.drawable.ic_people),
                contentDescription = null, // 바로 옆에 글자가 있다
                tint = colors.ink,
                modifier = Modifier.size(20.dp),
            )
            Spacer(Modifier.width(8.dp))
            Text(
                ChatBox.FIND_STAFF,
                style = HifisType.label.copy(fontSize = 15.sp, fontWeight = FontWeight.SemiBold),
                color = colors.ink,
            )
        }

        // **새 대화만 브랜드색이다.** 이 줄에서 새로 만드는 것은 이것뿐이라
        // 화면당 강조 한 곳 규칙을 여기에 쓴다
        Box(
            Modifier
                .size(ACTION_HEIGHT)
                .clip(pill)
                .background(colors.brand, pill)
                .tap(label = ChatBox.NEW_ROOM) {},
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                painterResource(R.drawable.ic_plus),
                contentDescription = ChatBox.NEW_ROOM,
                tint = Color.White,
                modifier = Modifier.size(Dimens.headerIcon),
            )
        }
    }
}

/**
 * 접속 중인 동료 — 가로로 한 장씩
 *
 * 참고한 화면은 사진만 있고 이름이 없었다. 우리는 **사진이 없어서** 글자 아바타라
 * 이름을 같이 적는다 — 글자만 두면 누구인지 못 읽는다.
 */
@Composable
private fun MateRow(mates: List<ChatMate>) {
    if (mates.isEmpty()) return
    val colors = HifisTheme.colors
    Row(
        Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = Dimens.screenEdge),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        mates.forEach { mate ->
            val shape = RoundedCornerShape(MATE_RADIUS)
            Column(
                Modifier
                    .size(MATE_CARD)
                    .clip(shape)
                    .background(colors.surface, shape)
                    .tap(label = mate.name) {}
                    .padding(horizontal = 6.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Avatar(mate.name, MATE_AVATAR, mate.presence)
                Spacer(Modifier.height(8.dp))
                Text(
                    mate.name,
                    style = HifisType.caption,
                    color = colors.inkSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

/**
 * 대화방 한 줄 — 아바타 · 이름 · 마지막 말 · 시각
 *
 * **안 읽은 방만 진하다.** 읽은 방은 이름도 미리보기도 가라앉아서 훑을 때 새것만 튄다.
 * 알림 끈 방은 줄 전체가 흐려지고 시각 앞에 꺼진 종이 붙는다.
 */
@Composable
private fun RoomRow(room: ChatRoom, now: kotlinx.datetime.LocalDateTime) {
    val colors = HifisTheme.colors
    val unread = room.unreadCount > 0
    Row(
        Modifier
            .fillMaxWidth()
            .tap(label = room.title) {
                // 아직 갈 곳이 없다 — 방 화면이 생기면 잇는다
            }
            .padding(horizontal = Dimens.screenEdge, vertical = ROW_PAD_V)
            // 알림을 끈 방은 통째로 가라앉는다 — 안 읽어도 재촉하지 않는 방이다
            .alpha(if (room.muted) MUTED_DIM else 1f),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (room.isGroup) {
            GroupAvatar(room.title, ROW_AVATAR)
        } else {
            Avatar(room.title, ROW_AVATAR, room.presence)
        }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(
                room.title,
                style = HifisType.body.copy(
                    fontWeight = if (unread) FontWeight.SemiBold else FontWeight.Medium,
                ),
                color = colors.ink,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(Modifier.height(2.dp))
            Text(
                ChatBox.preview(room),
                style = HifisType.caption.copy(fontSize = 14.sp),
                color = if (unread) colors.inkSecondary else colors.inkTertiary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        Spacer(Modifier.width(10.dp))
        Column(horizontalAlignment = Alignment.End) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (room.muted) {
                    Icon(
                        painterResource(R.drawable.ic_bell_off),
                        contentDescription = "알림 꺼짐",
                        tint = colors.inkTertiary,
                        modifier = Modifier.size(MUTE_ICON),
                    )
                    Spacer(Modifier.width(4.dp))
                }
                Text(
                    ChatBox.timeLabel(room.at, now),
                    style = HifisType.caption,
                    color = colors.inkTertiary,
                )
            }
            if (unread) {
                Spacer(Modifier.height(6.dp))
                // **여기는 숫자를 쓴다.** 헤더 종·말풍선의 점과 다르다 — 저기는 "볼 게 있다"
                // 하나만 말하면 되지만, 방이 여럿이면 어느 방이 얼마나 밀렸는지가 곧 순서다
                Box(
                    Modifier
                        .height(BADGE_HEIGHT)
                        .clip(CircleShape)
                        .background(colors.brand)
                        .padding(horizontal = 7.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        room.unreadCount.toString(),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                    )
                }
            }
        }
    }
}

/** 내 칸 — 화면 맨 아래에 붙는다. 내 아바타·이름·상태와 알림 끄기 */
@Composable
private fun MeBar(me: ChatMate) {
    val colors = HifisTheme.colors
    val shape = RoundedCornerShape(ME_RADIUS)
    Row(
        Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = Dimens.screenEdge, vertical = 8.dp)
            .height(ME_HEIGHT)
            .clip(shape)
            .background(colors.surface, shape)
            .padding(horizontal = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Avatar(me.name, ME_AVATAR, me.presence)
        Spacer(Modifier.width(10.dp))
        Column(Modifier.weight(1f)) {
            Text(
                me.name,
                style = HifisType.label.copy(fontSize = 15.sp, fontWeight = FontWeight.SemiBold),
                color = colors.ink,
            )
            Text(
                ChatBox.presenceLabel(me.presence),
                style = HifisType.caption,
                color = colors.inkTertiary,
            )
        }
        Box(
            Modifier
                .size(Dimens.headerIconButton)
                .clip(CircleShape)
                .tap(label = "알림 끄기") {},
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                painterResource(R.drawable.ic_bell),
                contentDescription = "알림 끄기",
                tint = colors.inkSecondary,
                modifier = Modifier.size(Dimens.headerIcon),
            )
        }
    }
}

/**
 * 사람 아바타 — **사진이 없다.** 이름 글자를 색 원에 넣는다
 *
 * 글자와 색은 `shared` 가 정한다 ([ChatBox.initial] · [ChatBox.colorIndex]) —
 * 두 플랫폼이 같은 사람에게 같은 글자·같은 색을 준다.
 */
@Composable
private fun Avatar(name: String, size: androidx.compose.ui.unit.Dp, presence: ChatPresence?) {
    val tint = eventColor(ChatBox.colorIndex(name))
    Box(Modifier.size(size), contentAlignment = Alignment.Center) {
        Box(
            Modifier
                .fillMaxSize()
                .clip(CircleShape)
                .background(tint),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                ChatBox.initial(name),
                // 원 지름에 견줘 잡는다 — 자리마다 크기가 달라서 값을 박으면 큰 원이 허전하다
                fontSize = (size.value * 0.34f).sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
            )
        }
        if (presence != null) PresenceDot(presence, Modifier.align(Alignment.BottomEnd))
    }
}

/**
 * 그룹방 아바타 — 사람 하나로 못 말한다. 색 원에 사람 둘
 *
 * **색은 방 이름에서 나온다.** 처음에 브랜드색으로 두었더니 그룹방 셋이 다 같은
 * 파랑이라 목록에서 안 갈렸다. 브랜드색은 이 화면에서 `새 대화` 하나만 쓴다.
 */
@Composable
private fun GroupAvatar(name: String, size: androidx.compose.ui.unit.Dp) {
    Box(
        Modifier
            .size(size)
            .clip(CircleShape)
            .background(eventColor(ChatBox.colorIndex(name))),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            painterResource(R.drawable.ic_people),
            contentDescription = null, // 바로 옆에 방 이름이 있다
            tint = Color.White,
            modifier = Modifier.size(size * 0.5f),
        )
    }
}

/**
 * 접속 상태 점 — 아바타 오른쪽 아래
 *
 * **바탕색 테를 먼저 깔고 그 안에 색 원을 넣는다.** 테두리(border)로 두르면
 * 바깥 안티에일리어싱 틈으로 아래 색이 비쳐 흐린 테가 생긴다 (헤더 배지에서 겪었다).
 */
@Composable
private fun PresenceDot(presence: ChatPresence, modifier: Modifier = Modifier) {
    val colors = HifisTheme.colors
    Box(
        modifier
            .offset(x = 1.dp, y = 1.dp)
            .size(PRESENCE_DOT + PRESENCE_RING * 2)
            .background(colors.background, CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            Modifier
                .size(PRESENCE_DOT)
                .background(presenceColor(presence, colors), CircleShape),
        )
    }
}

/**
 * 접속 상태 색 — **[app.hifis.shared.home.Tone] 을 안 쓴다**
 *
 * 뜻이 다르다. 방해 금지는 *문제*(`BAD`)가 아니라 본인이 켠 상태고, 접속 중도
 * *잘 됐다*(`GOOD`)가 아니다. 색만 겹칠 뿐이라 그 표에 끌어다 붙이면 다음에
 * 뜻 색을 손볼 때 여기가 같이 움직인다.
 */
private fun presenceColor(presence: ChatPresence, colors: HifisColors): Color = when (presence) {
    ChatPresence.ONLINE -> colors.success
    ChatPresence.BUSY -> colors.danger
    ChatPresence.OFFLINE -> colors.inkTertiary
}

/** 뒤로가기 터치 자리와 화면 이름 사이 — 알림함과 같은 값 */
private val HEADER_TITLE_GAP = 2.dp

/** 헤더와 단추 줄 사이 */
private val HEADER_BODY_GAP = 12.dp

/** 단추 줄 · 동료 줄 · 방 목록을 가르는 여백 */
private val SECTION_GAP = 16.dp

/** 단추 줄 높이와 모서리 — 셋이 같은 높이로 서야 줄이 맞는다 */
private val ACTION_HEIGHT = 46.dp
private val ACTION_RADIUS = 14.dp

/** 접속 중 동료 카드 — 한 장 크기·모서리·안의 아바타 */
private val MATE_CARD = 104.dp
private val MATE_RADIUS = 16.dp
private val MATE_AVATAR = 52.dp

/** 방 한 줄 — 위아래 여백과 아바타 */
private val ROW_PAD_V = 8.dp
private val ROW_AVATAR = 54.dp

/** 알림 끈 방을 얼마나 가라앉히나 */
private const val MUTED_DIM = 0.55f

/** 시각 앞에 붙는 꺼진 종 */
private val MUTE_ICON = 14.dp

/** 안읽음 수 배지 높이 — 두 자리 수가 들어가게 폭은 글자가 정한다 */
private val BADGE_HEIGHT = 20.dp

/** 접속 점과 그것을 두르는 바탕색 테 */
private val PRESENCE_DOT = 12.dp
private val PRESENCE_RING = 2.5.dp

/** 내 칸 — 높이·모서리·아바타 */
private val ME_HEIGHT = 60.dp
private val ME_RADIUS = 20.dp
private val ME_AVATAR = 40.dp

/** 대화가 하나도 없을 때 그 자리의 위아래 여백 */
private val EMPTY_PAD = 52.dp
