package app.hifis.shared.notification

import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.number
import kotlinx.datetime.toInstant

/**
 * 알림함 — 문구와 **묶는 법·꼬리표 셈**을 두 플랫폼이 같이 쓴다
 *
 * 전체 / 안읽음을 전환하며 **오늘·이전**으로 묶어 보여준다 (V2 와 같다).
 * 눌러서 읽음 처리하고, 갈 곳이 있는 알림은 그 화면으로 넘어간다 — 갈 화면이
 * 아직 없어서 지금은 읽음 처리까지다.
 *
 * **아직 서버가 없다.** 목록은 [AppNotification.demo] 다.
 */
object NotificationBox {
    const val TITLE = "알림"
    const val ALL = "전체"
    const val UNREAD = "안읽음"
    const val EMPTY = "알림이 없어요."
    const val EMPTY_UNREAD = "안 읽은 알림이 없어요."
    const val TODAY = "오늘"
    const val EARLIER = "이전"

    /**
     * 시각을 `shared` 가 짓는다 — iOS 에서 `LocalDateTime` 생성자를 직접 부르면
     * 같은 이름의 생성자가 둘이라 어느 것이 나올지 모른다 (`Calendar.dateOf` 와 같은 이유)
     */
    fun at(year: Int, month: Int, day: Int, hour: Int, minute: Int): LocalDateTime =
        LocalDateTime(year, month, day, hour, minute)

    /** 안읽음 칸의 글자 — 안 읽은 것이 있으면 수를 붙인다 (`안읽음 3`) */
    fun unreadLabel(unreadCount: Int): String =
        if (unreadCount > 0) "$UNREAD $unreadCount" else UNREAD

    /** 빈 상태 한 줄 — 안읽음만 보는 중이면 그렇다고 말한다 */
    fun emptyLabel(unreadOnly: Boolean): String = if (unreadOnly) EMPTY_UNREAD else EMPTY

    /** 오늘·이전으로 가른 목록 — 둘 다 비면 빈 상태다 */
    data class Sections(val today: List<AppNotification>, val earlier: List<AppNotification>) {
        val isEmpty: Boolean get() = today.isEmpty() && earlier.isEmpty()
    }

    /** 걸러서(안읽음만) 오늘·이전으로 가른다. 차례는 들어온 그대로 — 서버가 최신순으로 준다 */
    fun sections(
        items: List<AppNotification>,
        unreadOnly: Boolean,
        today: LocalDate,
    ): Sections {
        val shown = if (unreadOnly) items.filter { !it.read } else items
        return Sections(
            today = shown.filter { it.createdAt.date == today },
            earlier = shown.filter { it.createdAt.date != today },
        )
    }

    /**
     * 시각 꼬리표 — `방금 · 12분 전 · 오후 2:30 · 어제 · 7.28`
     *
     * 오늘 안이면 몇 분 전인지가 중요하고, 하루가 지나면 날짜만 있으면 된다.
     */
    fun timeLabel(at: LocalDateTime, now: LocalDateTime): String {
        val minutes = (now.toInstant(TimeZone.UTC) - at.toInstant(TimeZone.UTC)).inWholeMinutes
        if (minutes < 1) return "방금"
        if (minutes < 60) return "${minutes}분 전"
        if (at.date == now.date) {
            val hour12 = if (at.hour % 12 == 0) 12 else at.hour % 12
            val minute = at.minute.toString().padStart(2, '0')
            return "${if (at.hour < 12) "오전" else "오후"} $hour12:$minute"
        }
        if (at.date == now.date.minus(1, DateTimeUnit.DAY)) return "어제"
        return "${at.month.number}.${at.day}"
    }
}
