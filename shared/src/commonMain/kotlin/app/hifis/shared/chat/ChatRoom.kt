package app.hifis.shared.chat

import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Duration
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes

/**
 * 접속 상태 — **아바타에 붙는 점**
 *
 * **출근 여부가 아니다.** 지금 앱을 켜 두고 있는지다 — 출근한 사람이 폰을 안 보고
 * 있을 수도 있고, 쉬는 날에 켜 볼 수도 있다. 근무 상태는 근태가 따로 말한다.
 *
 * 자리를 비운 상태(`AWAY`)는 안 둔다. V2 는 접속·미접속 둘뿐이었고, 셋이 되면
 * "노랑은 뭐냐"를 매번 설명해야 한다. [BUSY] 는 **본인이 직접 켜는 것**이라 다르다.
 */
enum class ChatPresence {
    /** 켜 두고 있다 — 초록 */
    ONLINE,

    /** 방해 금지 — 본인이 켠 것이다. 빨강 */
    BUSY,

    /** 안 켜져 있다 — 회색 */
    OFFLINE,
}

/**
 * 지금 접속 중인 동료 — 목록 맨 위 가로줄에 한 장씩 선다
 *
 * **사진이 없다.** 이름 글자와 색으로 가른다 ([ChatBox.initial] · [ChatBox.colorIndex]) —
 * V2 도 사진을 안 받고 같은 방식이었다. 그래서 참고한 화면과 달리 **이름을 같이 적는다.**
 * 글자만 두면 누구인지 못 읽는다.
 */
data class ChatMate(
    val id: String,
    val name: String,
    val presence: ChatPresence,
)

/**
 * 대화방 한 칸 (서버 `ChatRoomOut` 이 올 자리)
 *
 * V2 의 방 모델에서 **목록이 쓰는 것만** 가져왔다. 참가자 목록·주인·읽은 수는
 * 방을 열어야 쓰는 값이라 여기 없다 — 목록에 쓸 자리가 없는 값을 끌어오지 않는다.
 *
 * @property title DM 은 상대 이름, 그룹은 방 이름(없으면 참가자 나열). **서버가 아니라
 *   화면에 뜰 이름 그대로다** — 누구와의 방인지 셈하는 것은 방 목록이 받아 오기 전 단계다
 * @property lastSender 마지막 말을 한 사람. 내가 했으면 [ChatBox.ME]
 * @property presence DM 상대의 접속 상태. **그룹은 null** — 여럿을 점 하나로 못 말한다
 */
data class ChatRoom(
    val id: String,
    val title: String,
    val isGroup: Boolean,
    val lastSender: String,
    val lastBody: String,
    val unreadCount: Int,
    /** 내가 이 방 알림을 껐는지 — **사람마다 다르다.** 꺼도 메시지는 오고 안읽음도 센다 */
    val muted: Boolean,
    val at: LocalDateTime,
    val presence: ChatPresence? = null,
) {
    companion object {
        /**
         * 서버를 붙이기 전에 화면을 보기 위한 값 — **진짜가 아니다**
         *
         * 서버가 붙으면 지운다. 이 값을 보고 서버에 칸을 만들지 않는다.
         */
        fun demo(now: LocalDateTime): List<ChatRoom> = listOf(
            ChatRoom(
                "r1", "박승규", isGroup = false,
                lastSender = ChatBox.ME, lastBody = "그건 아니지",
                unreadCount = 0, muted = false, at = now - 2.days,
                presence = ChatPresence.ONLINE,
            ),
            ChatRoom(
                "r2", "이건주", isGroup = false,
                lastSender = ChatBox.ME, lastBody = "사진 2장",
                unreadCount = 0, muted = false, at = now - 3.days,
                presence = ChatPresence.BUSY,
            ),
            ChatRoom(
                "r3", "9월 센터 리뉴얼", isGroup = true,
                lastSender = "정민준", lastBody = "도면 올려뒀어요",
                unreadCount = 3, muted = false, at = now - 40.minutes,
            ),
            ChatRoom(
                "r4", "강남점 트레이너", isGroup = true,
                lastSender = "김서준", lastBody = "오늘 저녁 타임 대타 가능하신 분",
                unreadCount = 1, muted = false, at = now - 5.hours,
            ),
            ChatRoom(
                "r5", "본사 공지방", isGroup = true,
                lastSender = "이지완", lastBody = "9월 안전교육 일정 공유드립니다",
                unreadCount = 0, muted = true, at = now - 17.days,
            ),
            ChatRoom(
                "r6", "최윤후", isGroup = false,
                lastSender = "최윤후", lastBody = "네 확인했습니다",
                unreadCount = 0, muted = false, at = now - 40.days,
                presence = ChatPresence.OFFLINE,
            ),
        )

        /** 위 방들과 짝이 맞는 접속 중 동료 — 데모다 */
        fun demoMates(): List<ChatMate> = listOf(
            ChatMate("m1", "박승규", ChatPresence.ONLINE),
            ChatMate("m2", "정민준", ChatPresence.ONLINE),
            ChatMate("m3", "이건주", ChatPresence.BUSY),
            ChatMate("m4", "김서준", ChatPresence.ONLINE),
        )

        /** 나 — 아래 내 칸에 뜬다. 로그인이 붙으면 그 자리를 갈아 끼운다 */
        fun demoMe(): ChatMate = ChatMate("me", "김은후", ChatPresence.ONLINE)

        private operator fun LocalDateTime.minus(d: Duration): LocalDateTime =
            (toInstant(TimeZone.UTC) - d).toLocalDateTime(TimeZone.UTC)
    }
}
