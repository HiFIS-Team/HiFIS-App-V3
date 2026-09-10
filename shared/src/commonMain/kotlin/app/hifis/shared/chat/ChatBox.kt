package app.hifis.shared.chat

import app.hifis.shared.schedule.EventPalette
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant

/**
 * 사내톡 목록 — 문구와 **미리보기·시각·아바타 셈**을 두 플랫폼이 같이 쓴다
 *
 * 화면은 디스코드 대화 목록을 옮긴 것이다 (대표 지목, 2026-09-10) —
 * 위에 접속 중인 동료가 가로로 서고, 아래에 방이 세로로 선다.
 *
 * **아직 서버가 없다.** 목록은 [ChatRoom.demo] 이고 방을 열 수도 없다.
 */
object ChatBox {
    const val TITLE = "사내톡"

    /** 내가 보낸 말 앞에 붙는 이름 */
    const val ME = "나"

    /** 위쪽 단추 셋 — 찾기 · 직원 · 새 대화 */
    const val SEARCH = "대화 검색"
    const val FIND_STAFF = "직원 찾기"
    const val NEW_ROOM = "새 대화"

    const val EMPTY = "아직 대화가 없어요."

    /** 내 칸의 상태 한 줄 — 아래 [presenceLabel] 과 같은 말을 쓴다 */
    fun presenceLabel(presence: ChatPresence): String = when (presence) {
        ChatPresence.ONLINE -> "접속 중"
        ChatPresence.BUSY -> "방해 금지"
        ChatPresence.OFFLINE -> "오프라인"
    }

    /**
     * 목록에 뜨는 마지막 말 한 줄 — **누가 했는지를 앞에 붙인다**
     *
     * 그룹은 누가 했는지가 있어야 읽히고, DM 도 내가 한 말인지 상대가 한 말인지가
     * 한눈에 갈려야 한다 (`나: 그건 아니지`).
     */
    fun preview(room: ChatRoom): String = "${room.lastSender}: ${room.lastBody}"

    /**
     * 목록 오른쪽 시각 — `방금 · 12분 · 3시간 · 2일 · 1개월`
     *
     * 알림함(`NotificationBox.timeLabel`)과 **다르다.** 저기는 한 건을 짚어 보는
     * 자리라 `오후 2:30` 까지 말하고, 여기는 긴 목록을 훑는 자리라 **글자 수를 아낀다.**
     */
    fun timeLabel(at: LocalDateTime, now: LocalDateTime): String {
        val minutes = (now.toInstant(TimeZone.UTC) - at.toInstant(TimeZone.UTC)).inWholeMinutes
        if (minutes < 1) return "방금"
        if (minutes < 60) return "${minutes}분"
        val hours = minutes / 60
        if (hours < 24) return "${hours}시간"
        val days = hours / 24
        if (days < 30) return "${days}일"
        return "${days / 30}개월"
    }

    /**
     * 아바타에 넣는 글자 — **성을 뺀 이름**이다 (김은후 → 은후)
     *
     * 성 한 자만 두면 김씨끼리 다 같아 보인다. 두 자짜리 이름은 통째로 쓴다.
     */
    fun initial(name: String): String {
        val trimmed = name.trim()
        if (trimmed.isEmpty()) return "?"
        return if (trimmed.length >= 3) trimmed.takeLast(2) else trimmed
    }

    /**
     * 아바타 색 자리 — 이름에서 **늘 같은 값**이 나온다
     *
     * 색은 [EventPalette] 에서 집는다. 일정 색과 같은 성격이라서다 —
     * **뜻이 없는 고른 색**이고, 글자를 안 읽고도 사람을 가르라고 있는 것뿐이다.
     * 여기서 색을 새로 정하면 앱에 뜻 없는 팔레트가 둘이 된다.
     *
     * 셈을 `shared` 가 하므로 **두 플랫폼이 같은 사람에게 같은 색**을 준다.
     *
     * `hashCode % 16` 을 그대로 쓰면 안 된다. 그러면 **아래 네 비트만** 보게 되는데,
     * 한글 이름은 그 자리가 뭉친다 — 재 보니 열 이름 중 넷이 같은 색으로 겹쳤다
     * (김은후·김서준이 같은 색). 위 비트를 아래로 접어 섞고 나서 나눈다.
     *
     * 부호는 `abs` 가 아니라 마스크로 지운다 — `abs(Int.MIN_VALUE)` 는 음수 그대로다.
     */
    fun colorIndex(name: String): Int {
        if (name.isEmpty()) return EventPalette.DEFAULT
        var h = name.hashCode() and 0x7FFF_FFFF
        h = h xor (h shr 16)
        h = h xor (h shr 8)
        return h % EventPalette.colors.size
    }
}
