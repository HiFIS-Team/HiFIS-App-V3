package app.hifis.shared.notification

import app.hifis.shared.home.Tone
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Duration
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes

/**
 * 알림 종류 (서버 `NotificationOut.type`) — **아이콘과 뜻(색)을 여기서 정한다**
 *
 * 서버가 문자열로 주고 종류가 늘 수 있어서, 모르는 값은 [OTHER] 로 떨어진다.
 * 색은 [Tone] 으로만 정하고 각 플랫폼이 한 번만 잇는다 (홈 알림 배너와 같은 규칙).
 *
 * **빨강은 개인 업무 누락 하나뿐이다** (V2 2026-08-21). 경고를 여기저기 쓰면
 * 정작 봐야 할 줄이 안 튄다 — 목록에서 빨간 줄은 이것만이어야 한다.
 *
 * 아이콘은 **이미 있는 것을 다시 쓴다.** 알림마다 새로 그리면 같은 뜻인데
 * 자리마다 다른 그림이 된다.
 */
enum class NotificationKind(val wire: String, val icon: String, val tone: Tone) {
    ATTENDANCE("ATTENDANCE", "ic_attendance", Tone.INFO),
    LEAVE("LEAVE", "ic_sun", Tone.CAUTION),
    NOTICE("NOTICE", "ic_notice", Tone.INFO),
    CHAT("CHAT", "ic_chat", Tone.INFO),
    APPROVAL("APPROVAL", "ic_approval", Tone.GOOD),
    PROJECT("PROJECT", "ic_project", Tone.CAUTION),
    PAYROLL("PAYROLL", "ic_salary", Tone.GOOD),
    SCHEDULE("SCHEDULE", "ic_schedule", Tone.INFO),
    RANKING("RANKING", "ic_ranking", Tone.CAUTION),

    // 아래 둘은 **대표·관리자만** 받는다 (회의록 작성 · 직원 가입·퇴사)
    MEETING("MEETING", "ic_meeting", Tone.INFO),
    STAFF("STAFF", "ic_staff", Tone.INFO),

    /**
     * 개인 업무를 남기고 퇴근했다 — **이것만 빨갛게 뜬다**
     *
     * 서버가 `MY_TASK` 에서 갈라 보낸다. 같은 `MY_TASK` 에 승인 알림이 섞여 있어서
     * 종류째 빨갛게 하면 승인도 경고로 보인다.
     */
    MY_TASK_MISSING("MY_TASK_MISSING", "ic_work", Tone.BAD),

    /** PT 만족도 폼에 답이 왔다 — 결과를 볼 수 있는 사람만 받는다 */
    PT_SURVEY("PT_SURVEY", "ic_dumbbell", Tone.GOOD),

    /** 모르는 종류 — 종 그림에 무채색 */
    OTHER("", "ic_bell", Tone.NEUTRAL),
    ;

    companion object {
        /** 서버 문자열 → 종류. 모르는 값은 [OTHER] */
        fun parse(wire: String?): NotificationKind =
            entries.firstOrNull { it != OTHER && it.wire == wire } ?: OTHER
    }
}

/**
 * 알림 한 건 (서버 `NotificationOut`)
 *
 * `Notification` 이라고 두면 두 플랫폼 다 시스템 알림 클래스와 이름이 겹쳐서 `App` 을 붙였다
 * (V2 도 같은 이유로 그랬다).
 *
 * @property body 한 줄 더 붙는 설명 (`삭제테스트 · 테스트매니저` 처럼). 없으면 안 그린다
 * @property link 눌렀을 때 갈 곳 — `/notices/{id}` 처럼 앱 안 주소. **아직 아무 데도 안 잇는다**
 * @property read 읽음 — 화면에서 바꾼다
 */
data class AppNotification(
    val id: String,
    val kind: NotificationKind,
    val title: String,
    val body: String?,
    val link: String?,
    val read: Boolean,
    val createdAt: LocalDateTime,
) {
    /** 읽음으로 — 화면이 먼저 바꾸고 서버에는 나중에 보낸다 */
    fun markRead(): AppNotification = copy(read = true)

    companion object {
        /**
         * 서버를 붙이기 전에 화면을 보기 위한 값 — **진짜가 아니다**
         *
         * 시각은 [now] 를 기준으로 상대적으로 잡는다 — 오늘·이전 묶음과
         * `12분 전` 같은 꼬리표가 다 보이게. 서버가 붙으면 지운다.
         * 이 값을 보고 서버에 칸을 만들지 않는다.
         */
        fun demo(now: LocalDateTime): List<AppNotification> = listOf(
            AppNotification(
                "demo-1", NotificationKind.PROJECT,
                "마감이 이틀 남았어요", "9월 센터 리뉴얼 · 아직 진행 중", "/projects/demo",
                read = false, createdAt = now - 12.minutes,
            ),
            AppNotification(
                "demo-2", NotificationKind.APPROVAL,
                "월차가 승인됐어요", "9월 12일 (금)", "/approvals/demo",
                read = false, createdAt = now - 3.hours,
            ),
            AppNotification(
                "demo-3", NotificationKind.NOTICE,
                "9월 안전교육 필수 이수 안내", null, "/notices/demo",
                read = true, createdAt = now - 1.days,
            ),
            AppNotification(
                "demo-4", NotificationKind.PAYROLL,
                "8월 급여명세서가 올라왔어요", null, "/payroll",
                read = true, createdAt = now - 3.days,
            ),
        )

        /** 시각 셈 — 오늘·이전을 가르고 꼬리표를 만드는 데만 쓴다. 지역은 뜻이 없어 UTC 로 고정 */
        private operator fun LocalDateTime.minus(d: Duration): LocalDateTime =
            (toInstant(TimeZone.UTC) - d).toLocalDateTime(TimeZone.UTC)
    }
}
