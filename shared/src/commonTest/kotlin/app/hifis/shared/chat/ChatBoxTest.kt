package app.hifis.shared.chat

import app.hifis.shared.schedule.EventPalette
import kotlinx.datetime.LocalDateTime
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ChatBoxTest {
    private val now = LocalDateTime(2026, 9, 10, 14, 30)

    /** 한글 이름·방 이름 열 가지 — 색이 뭉치는지 보는 데 쓴다 */
    private val NAMES = listOf(
        "김은후", "김서준", "이건주", "정민준", "최윤후",
        "박승규", "이지완", "본사 공지방", "강남점 트레이너", "9월 센터 리뉴얼",
    )

    @Test
    fun `시각은 글자 수를 아낀다`() {
        assertEquals("방금", ChatBox.timeLabel(LocalDateTime(2026, 9, 10, 14, 30), now))
        assertEquals("12분", ChatBox.timeLabel(LocalDateTime(2026, 9, 10, 14, 18), now))
        assertEquals("5시간", ChatBox.timeLabel(LocalDateTime(2026, 9, 10, 9, 30), now))
        assertEquals("2일", ChatBox.timeLabel(LocalDateTime(2026, 9, 8, 14, 30), now))
        assertEquals("1개월", ChatBox.timeLabel(LocalDateTime(2026, 8, 1, 14, 30), now))
    }

    @Test
    fun `미리보기는 누가 했는지를 앞에 붙인다`() {
        val rooms = ChatRoom.demo(now)
        assertEquals("나: 그건 아니지", ChatBox.preview(rooms[0]))
        assertEquals("정민준: 도면 올려뒀어요", ChatBox.preview(rooms[2]))
    }

    @Test
    fun `아바타 글자는 성을 뺀다`() {
        assertEquals("은후", ChatBox.initial("김은후"))
        assertEquals("박준", ChatBox.initial("박준"))
        assertEquals("?", ChatBox.initial("  "))
    }

    @Test
    fun `아바타 색은 같은 이름에 늘 같고 자리를 안 벗어난다`() {
        assertEquals(ChatBox.colorIndex("김은후"), ChatBox.colorIndex("김은후"))
        for (name in NAMES + "") {
            assertTrue(ChatBox.colorIndex(name) in EventPalette.colors.indices, name)
        }
    }

    /**
     * **색이 뭉치면 안 된다** — 아바타에 사진이 없어서 색이 사람을 가르는 유일한 단서다.
     *
     * `hashCode % 16` 을 그대로 쓰던 때 열 이름 중 넷이 겹쳤다 (아래 네 비트만 보게 돼서
     * 한글 이름이 그 자리에 뭉친다). 색이 16개뿐이라 겹침을 0 으로 만들 수는 없지만,
     * 열 이름이면 여덟 가지는 나와야 한다.
     */
    @Test
    fun `아바타 색이 이름마다 고루 퍼진다`() {
        val used = NAMES.map { ChatBox.colorIndex(it) }.toSet()
        assertTrue(used.size >= 8, "열 이름에 색이 ${used.size}가지뿐이다")
    }

    @Test
    fun `그룹은 접속 점이 없다`() {
        for (room in ChatRoom.demo(now)) {
            if (room.isGroup) assertEquals(null, room.presence, room.title)
        }
    }

    @Test
    fun `상태 이름은 셋뿐이다`() {
        assertEquals("접속 중", ChatBox.presenceLabel(ChatPresence.ONLINE))
        assertEquals("방해 금지", ChatBox.presenceLabel(ChatPresence.BUSY))
        assertEquals("오프라인", ChatBox.presenceLabel(ChatPresence.OFFLINE))
    }
}
