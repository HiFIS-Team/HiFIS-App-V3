package app.hifis.shared.schedule

import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.plus

/**
 * 일정 한 건 — 달력 점 하나와 목록 한 줄
 *
 * V2 의 `Event` 에서 **홈·달력이 쓰는 것만** 가져왔다 (`schedule_data.dart`).
 * 장소·메모·참석자·수정 권한은 상세를 만들 때 붙인다 — 쓸 자리가 없는 값을 미리 끌어오지 않는다.
 */
data class ScheduleEvent(
    val id: String,
    val title: String,
    val kind: EventKind,
    val date: LocalDate,
    /** 시작 시각 `"10:00"` — 종일 일정이면 null */
    val start: String?,
) {
    /** 목록에 찍는 시각 — 종일이면 글자로 대신한다 */
    val timeText: String get() = start ?: ALL_DAY

    companion object {
        const val ALL_DAY = "종일"

        /**
         * 서버를 붙이기 전에 화면을 보기 위한 값 — **진짜가 아니다**
         *
         * `기준일`(오늘)로부터 며칠 뒤인지로 만든다. 날짜를 박아 두면
         * 하루만 지나도 달력이 비어 보인다.
         */
        fun demo(today: LocalDate): List<ScheduleEvent> = listOf(
            ScheduleEvent("1", "주간 센터 회의", EventKind.MEETING, today, "10:00"),
            ScheduleEvent("2", "PT 수업 — 김수현 회원", EventKind.LESSON, today, "14:00"),
            ScheduleEvent("3", "신규 회원 이벤트 준비", EventKind.EVENT, today.plus(1, DateTimeUnit.DAY), "11:00"),
            ScheduleEvent("4", "그룹 수업 — 필라테스", EventKind.LESSON, today.plus(2, DateTimeUnit.DAY), "19:00"),
            ScheduleEvent("5", "월차", EventKind.OFF, today.plus(3, DateTimeUnit.DAY), null),
            ScheduleEvent("6", "본사 월간 보고", EventKind.MEETING, today.plus(5, DateTimeUnit.DAY), "15:00"),
            ScheduleEvent("7", "기구 점검", EventKind.ETC, today.plus(8, DateTimeUnit.DAY), "09:00"),
        )
    }
}

/**
 * 일정 종류 — **색으로 종류를 가른다** (V2 와 같다)
 *
 * V2 서버는 종류를 enum 이 아니라 **자유 문자열**로 받는다. [label] 을 그대로
 * 주고받으므로 **라벨을 고치면 이미 쌓인 일정이 `기타` 로 떨어진다.**
 */
enum class EventKind(val label: String) {
    MEETING("회의"),
    LESSON("수업"),
    EVENT("이벤트"),
    OFF("휴무"),
    ETC("기타"),
    ;

    companion object {
        val all: List<EventKind> = entries.toList()

        /** 서버가 준 문자열을 종류로 — 모르는 값은 [ETC] 로 떨어진다 */
        fun parse(value: String?): EventKind =
            entries.firstOrNull { it.label == value } ?: ETC
    }
}
