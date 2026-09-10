package app.hifis.shared.ai

/**
 * AI 채팅 첫 화면에 세워 두는 **말 걸기 보기**
 *
 * 빈 입력칸만 두면 무엇을 물어봐도 되는지 몰라서 아무도 안 쓴다.
 * 그래서 대표적인 넷을 미리 꺼내 둔다.
 *
 * **문구를 `shared` 에 두는 이유**는 두 플랫폼이 같은 말을 해야 해서다 —
 * 화면마다 새로 적으면 한쪽만 고쳐지고 갈린다 (`Notice.DATE_PATTERN` 과 같은 자리).
 *
 * 값은 아직 **자리 표시자다.** AI 를 실제로 붙일 때 무엇을 시킬 수 있는지에 맞춰 간다.
 *
 * @property icon 아이콘 이름 — 양 플랫폼이 **같은 이름**을 쓴다
 */
enum class AiPrompt(val label: String, val icon: String) {
    TODAY_TASKS("오늘 할 일 알려줘", "ic_work"),
    BOOK_SCHEDULE("이번 주 일정 잡아줘", "ic_schedule"),
    FIND_MEETING("회의록에서 찾아줘", "ic_meeting"),
    DRAFT_APPROVAL("휴가 신청서 써줘", "ic_approval"),
    ;

    companion object {
        /** 세우는 **차례**. 순서를 바꾸면 양 플랫폼이 같이 바뀐다 */
        val all: List<AiPrompt> = entries.toList()

        /** 마크 옆에 붙는 이름 */
        const val BRAND = "HiFIS AI"

        /** 큰 물음 한 줄 */
        const val TITLE = "무엇을 도와드릴까요?"

        /** 입력칸이 비었을 때 보이는 글자 */
        const val PLACEHOLDER = "메시지를 입력해주세요"
    }
}
