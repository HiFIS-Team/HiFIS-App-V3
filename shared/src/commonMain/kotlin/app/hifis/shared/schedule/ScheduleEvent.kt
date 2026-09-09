package app.hifis.shared.schedule

import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.plus

/**
 * 일정 한 건
 *
 * **V2 보다 넓다.** V2 의 `Event` 는 종류에서 색을 뽑았는데, V3 는 **색을 직접 고른다**
 * ([colorIndex]). 종류가 열여섯이라 색으로 다 가를 수가 없고,
 * 색은 색대로 "내 눈에 띄게" 쓰는 값이라 종류와 묶어 둘 이유가 없다.
 *
 * 공유 범위([scope])도 V3 에서 새로 생겼다 — V2 는 참석자 목록만 있었다.
 */
data class ScheduleEvent(
    val id: String,
    val title: String,
    val kind: EventKind,
    /** 시작 날짜 */
    val date: LocalDate,
    /** 시작 시각 `"10:00"` — 종일이면 null */
    val start: String? = null,
    /** 종료 날짜 — 하루짜리면 null */
    val endDate: LocalDate? = null,
    /** 종료 시각 */
    val end: String? = null,
    val scope: EventScope = EventScope.CENTER,
    /** [EventPalette.colors] 의 자리 */
    val colorIndex: Int = EventPalette.DEFAULT,
    val memo: String? = null,
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
            ScheduleEvent("1", "주간 센터 회의", EventKind.MEETING, today, "10:00", colorIndex = 0),
            ScheduleEvent("2", "PT 수업 — 김수현 회원", EventKind.LESSON, today, "14:00", colorIndex = 4),
            ScheduleEvent(
                "3", "신규 회원 이벤트 준비", EventKind.COMPANY_EVENT,
                today.plus(1, DateTimeUnit.DAY), "11:00", colorIndex = 8,
            ),
            ScheduleEvent(
                "4", "그룹 수업 — 필라테스", EventKind.LESSON,
                today.plus(2, DateTimeUnit.DAY), "19:00", colorIndex = 5,
            ),
            ScheduleEvent(
                "5", "월차", EventKind.LEAVE,
                today.plus(3, DateTimeUnit.DAY), null, colorIndex = 14,
            ),
            ScheduleEvent(
                "6", "본사 월간 보고", EventKind.MEETING,
                today.plus(5, DateTimeUnit.DAY), "15:00", colorIndex = 12,
            ),
            ScheduleEvent(
                "7", "기구 점검", EventKind.WORK,
                today.plus(8, DateTimeUnit.DAY), "09:00", colorIndex = 9,
            ),
        )
    }
}

/**
 * 일정 종류 — **글자와 아이콘만 갖는다. 색은 안 갖는다**
 *
 * V2 는 다섯 가지였고 종류마다 색이 정해져 있었다. V3 는 열여섯이라
 * 색으로 다 가를 수가 없어서 [EventPalette] 로 갈랐다.
 *
 * 서버는 종류를 **자유 문자열**로 받는다 (V2 와 같다). [label] 을 그대로 주고받으므로
 * **라벨을 고치면 이미 쌓인 일정이 `일반` 으로 떨어진다.**
 *
 * @property icon 양 플랫폼이 **같은 이름**을 쓴다 (`tools/icons/sync_ios_icons.py`)
 */
enum class EventKind(val label: String, val icon: String) {
    MEETING("회의", "ic_people"),

    /** 센터에서 제일 많은 일정이라 앞에 둔다 — 참고한 화면에는 없던 것이다 */
    LESSON("수업", "ic_dumbbell"),

    DEADLINE("마감", "ic_attendance"),
    FIELD("외근·출장", "ic_pin"),
    LEAVE("휴가", "ic_sun"),
    DAY_OFF("휴무", "ic_moon"),
    COMPANY_EVENT("사내행사", "ic_notice"),
    ANNIVERSARY("기념일", "ic_gift"),
    WORK("업무", "ic_work"),
    INTERVIEW("면접", "ic_person_check"),
    TRAINING("교육·워크샵", "ic_cap"),

    /** 참고한 화면의 `고객·미팅` — 우리는 회원이다 */
    MEMBER_MEETING("회원 미팅", "ic_chat"),

    DINNER("회식·모임", "ic_glass"),
    HEALTH("건강·병원", "ic_heart"),
    PERSONAL("개인일정", "ic_person"),
    GENERAL("일반", "ic_dots"),
    ;

    companion object {
        val all: List<EventKind> = entries.toList()

        /** 서버가 준 문자열을 종류로 — 모르는 값은 [GENERAL] 로 떨어진다 */
        fun parse(value: String?): EventKind = entries.firstOrNull { it.label == value } ?: GENERAL
    }
}

/**
 * 공유 범위 — 이 일정을 누가 보는가
 *
 * **V3 에서 새로 생겼다.** V2 는 참석자 목록만 있어서 "센터 전체에 알린다" 같은 것을
 * 하려면 사람을 하나씩 골라야 했다.
 *
 * `전사` 는 **조직 하나 안의 전체**다 (`.claude/멀티테넌트.md`) — 다른 회사에는 안 간다.
 */
enum class EventScope(val label: String, val detail: String, val icon: String) {
    COMPANY("전사", "모든 구성원에게 공유", "ic_more"),
    CENTER("센터", "같은 센터 구성원에게 공유", "ic_branch"),
    PROJECT("프로젝트", "선택한 프로젝트 멤버에게만 공유", "ic_project"),
    PRIVATE("개인", "나만 볼 수 있어요", "ic_person"),
    PICKED("대상 지정", "선택한 구성원에게만 공유", "ic_person_check"),
    ;

    companion object {
        val all: List<EventScope> = entries.toList()
    }
}

/**
 * 일정 색표 — **고르는 색이다. 뜻이 없다**
 *
 * `0xRRGGBB` 로 담는다. 두 플랫폼이 **같은 숫자**를 읽어야 같은 색이 나온다.
 *
 * 어두운 바닥 위에 서는 값들이라 너무 어두운 색은 뺐다.
 * 상태색(`success`·`warning`·`danger`)과 비슷한 것이 섞여 있지만 자리가 다르다 —
 * 상태색은 배지에, 이건 일정 점에 나온다.
 */
object EventPalette {
    val colors: List<Int> = listOf(
        0x4A9BFF, // 0 파랑 (기본)
        0x3B6FE0, // 1 남색
        0x38BDF8, // 2 하늘
        0x2E9AA8, // 3 청록
        0x34D399, // 4 민트
        0x22C55E, // 5 초록
        0x84CC16, // 6 연두
        0xD4A017, // 7 금
        0xF59E0B, // 8 호박
        0xF97316, // 9 주황
        0xEF4444, // 10 빨강
        0xEC4899, // 11 분홍
        0xD946EF, // 12 자홍
        0xA855F7, // 13 보라
        0x7C5CFF, // 14 남보라
        0x64748B, // 15 회청
    )

    /** 처음 고르는 자리 */
    const val DEFAULT = 0

    /** 자리가 벗어나도 색은 나와야 한다 — 서버가 모르는 값을 줄 수 있다 */
    fun at(index: Int): Int = colors.getOrElse(index) { colors[DEFAULT] }
}
