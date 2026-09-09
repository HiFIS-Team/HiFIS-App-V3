package app.hifis.shared.home

/**
 * 오늘 근무 — 홈 첫 카드가 그리는 값
 *
 * V2 의 `_HeroStatusCard` 가 보던 것과 같다 (`home_status.dart`).
 * **화면이 아니라 계산이 여기 있다** — 진행률을 두 플랫폼이 따로 셈하면
 * 같은 시각에 다른 퍼센트가 뜬다.
 *
 * 시:분은 서버가 문자열(`"09:00"`)로 준다. 날짜 타입으로 바꾸지 않는다 —
 * 근무 시간표는 날짜가 없는 값이라 타입을 붙이면 오늘 날짜를 억지로 끼워야 한다.
 */
data class TodayWork(
    val status: WorkStatus,
    /** 근무 시작 시간표 — 안 정해진 사람은 null */
    val shiftStart: String?,
    /** 근무 종료 시간표 */
    val shiftEnd: String?,
    /** 출근 스캔 시각 — 안 찍었으면 null */
    val checkIn: String?,
    /** 퇴근 스캔 시각 — 아직이면 null */
    val checkOut: String?,
) {
    /**
     * 근무 진행률 0.0 ~ 1.0
     *
     * - 출근을 안 찍었으면 **0** (시간이 지나도 안 올라간다)
     * - 퇴근을 찍었으면 **그 시각까지**, 아니면 지금까지로 잰다
     * - 근무 시간표가 없는 사람은 기준이 없어 **0 에 머문다**
     *
     * @param nowMinutes 자정부터 지금까지의 분 (`시*60 + 분`)
     */
    fun rateAt(nowMinutes: Int): Float {
        val start = minutesOf(shiftStart) ?: return 0f
        val end = minutesOf(shiftEnd) ?: return 0f
        if (end <= start) return 0f
        if (checkIn == null) return 0f

        val at = minutesOf(checkOut) ?: nowMinutes
        val elapsed = (at - start).toFloat()
        return (elapsed / (end - start)).coerceIn(0f, 1f)
    }

    /** 화면에 찍는 출근 시각 — 안 찍었으면 [NO_TIME] */
    val checkInText: String get() = checkIn ?: NO_TIME

    /** 화면에 찍는 퇴근 시각 */
    val checkOutText: String get() = checkOut ?: NO_TIME

    /** 시간표가 없으면 자리만 잡는다 */
    val shiftStartText: String get() = shiftStart ?: NO_TIME
    val shiftEndText: String get() = shiftEnd ?: NO_TIME

    companion object {
        /** 아직 안 찍힌 자리 */
        const val NO_TIME = "--:--"

        /**
         * 서버를 붙이기 전에 화면을 보기 위한 값 — **진짜가 아니다**
         *
         * 서버가 붙으면 지운다. 이 값을 보고 서버에 칸을 만들지 않는다.
         */
        val demo = TodayWork(
            status = WorkStatus.IN_PROGRESS,
            shiftStart = "09:00",
            shiftEnd = "18:00",
            checkIn = "08:57",
            checkOut = null,
        )
    }
}

/**
 * 배지 색을 고르는 기준 — **색 이름이 아니라 뜻**이다
 *
 * 두 플랫폼이 각자 `when` 으로 상태를 색에 잇게 두면 언젠가 갈린다.
 * 뜻을 여기서 정하고 색은 각 플랫폼의 토큰에서 한 번만 잇는다.
 */
enum class WorkTone {
    /** 아직 아무 일도 없다 — 미출근·휴무·판정 불가 */
    NEUTRAL,

    /** 잘 돌아간다 — 출근 중 */
    GOOD,

    /** 짚어 볼 것 — 지각·조기 퇴근 */
    CAUTION,

    /** 문제 — 결근·퇴근 누락 */
    BAD,

    /** 알림 — 휴가 */
    INFO,
}

/**
 * 오늘 근태 판정 — **서버가 정한다.** 앱은 문구로 옮기기만 한다
 *
 * V2 서버는 열 가지를 주는데 홈 배지는 **지금 어떤 상태인지**만 보면 되므로
 * 문구를 짧게 쓴다. 어느 날 무슨 일이 있었는지는 근태 화면에서 본다.
 *
 * `NORMAL` 과 `OVERTIME` 을 **둘 다 '퇴근'** 으로 둔 것은 V2 와 같다 —
 * 이 배지는 지금 상태를 알리는 자리라 문구를 늘리지 않는다.
 */
enum class WorkStatus(val label: String, val tone: WorkTone) {
    /** 아직 안 찍었다. 근무 시간이 다 지나도록 안 찍히면 서버가 [ABSENT] 로 바꾼다 */
    NOT_IN("미출근", WorkTone.NEUTRAL),
    IN_PROGRESS("출근", WorkTone.GOOD),
    NORMAL("퇴근", WorkTone.NEUTRAL),
    OVERTIME("퇴근", WorkTone.NEUTRAL),
    LATE("지각", WorkTone.CAUTION),
    EARLY_LEAVE("조기 퇴근", WorkTone.CAUTION),
    LATE_AND_EARLY("지각·조기 퇴근", WorkTone.CAUTION),
    NO_CHECKOUT("퇴근 누락", WorkTone.BAD),
    ABSENT("결근", WorkTone.BAD),
    ON_LEAVE("휴가", WorkTone.INFO),
    DAY_OFF("휴무", WorkTone.NEUTRAL),
    UNKNOWN("판정 불가", WorkTone.NEUTRAL),
}

/** `"09:00"` → 540. 형식이 다르거나 비어 있으면 null */
internal fun minutesOf(hhmm: String?): Int? {
    val parts = hhmm?.split(":") ?: return null
    if (parts.size != 2) return null
    val hour = parts[0].toIntOrNull() ?: return null
    val minute = parts[1].toIntOrNull() ?: return null
    return hour * 60 + minute
}
