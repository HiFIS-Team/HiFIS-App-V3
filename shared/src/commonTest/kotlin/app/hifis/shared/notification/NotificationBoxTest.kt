package app.hifis.shared.notification

import kotlinx.datetime.LocalDateTime
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class NotificationBoxTest {
    private val now = LocalDateTime(2026, 9, 10, 14, 30)

    @Test
    fun `꼬리표 - 방금 · 분 전 · 오늘은 시각 · 어제 · 날짜`() {
        assertEquals("방금", NotificationBox.timeLabel(LocalDateTime(2026, 9, 10, 14, 30), now))
        assertEquals("12분 전", NotificationBox.timeLabel(LocalDateTime(2026, 9, 10, 14, 18), now))
        assertEquals("오전 9:05", NotificationBox.timeLabel(LocalDateTime(2026, 9, 10, 9, 5), now))
        assertEquals("오후 12:40", NotificationBox.timeLabel(LocalDateTime(2026, 9, 10, 12, 40), now))
        assertEquals("어제", NotificationBox.timeLabel(LocalDateTime(2026, 9, 9, 23, 59), now))
        assertEquals("7.28", NotificationBox.timeLabel(LocalDateTime(2026, 7, 28, 10, 0), now))
    }

    @Test
    fun `오늘과 이전으로 가른다`() {
        val sections = NotificationBox.sections(AppNotification.demo(now), unreadOnly = false, today = now.date)
        assertEquals(listOf("demo-1", "demo-2"), sections.today.map { it.id })
        assertEquals(listOf("demo-3", "demo-4"), sections.earlier.map { it.id })
    }

    @Test
    fun `안읽음만 보면 읽은 것은 빠진다`() {
        val sections = NotificationBox.sections(AppNotification.demo(now), unreadOnly = true, today = now.date)
        assertTrue(sections.today.all { !it.read } && sections.earlier.all { !it.read })
        assertEquals(2, sections.today.size + sections.earlier.size)
    }

    @Test
    fun `빈 목록은 빈 상태다`() {
        assertTrue(NotificationBox.sections(emptyList(), unreadOnly = false, today = now.date).isEmpty)
        assertEquals("알림이 없어요.", NotificationBox.emptyLabel(unreadOnly = false))
        assertEquals("안 읽은 알림이 없어요.", NotificationBox.emptyLabel(unreadOnly = true))
    }

    @Test
    fun `안읽음 칸은 안 읽은 수를 붙인다`() {
        assertEquals("안읽음", NotificationBox.unreadLabel(0))
        assertEquals("안읽음 3", NotificationBox.unreadLabel(3))
    }

    @Test
    fun `모르는 종류는 OTHER 로 떨어진다`() {
        assertEquals(NotificationKind.APPROVAL, NotificationKind.parse("APPROVAL"))
        assertEquals(NotificationKind.OTHER, NotificationKind.parse("SOMETHING_NEW"))
        assertEquals(NotificationKind.OTHER, NotificationKind.parse(null))
        assertEquals(NotificationKind.OTHER, NotificationKind.parse(""))
    }
}
