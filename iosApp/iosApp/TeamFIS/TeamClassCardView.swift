import SwiftUI
import SharedKit

/// 수업 카드 — 달력에서 고른 날의 수업 하나 (2026-09-13 대표, 참고 사진의 짜임)
///
/// ```
/// ● PT 30회                 [ 12/30회차 ]   ← 상태 점 + 상품 · 회차 알약
/// 18:00 ~ 19:00                             ← 눈이 먼저 닿는 줄
/// Ⓐ 김수현                          예정    ← 회원 · 상태
/// ```
///
/// **지나간 수업은 조용히 물러난다** (업무 목록과 같은 규칙). 끝난 것을 색으로 띄우면
/// 눈이 거기 멈추는데, 봐야 하는 건 아직 안 한 수업이다.
///
/// 안드로이드 `TeamClassCard` 와 같은 값이다.
struct TeamClassCardView: View {
    @Environment(\.brand) private var brand
    let item: TeamClass

    private var done: Bool { item.status == ClassStatus.done }
    /// 아직 안 한 것만 색을 쓴다 — 끝난 것은 물러난다
    private var mark: Color { done ? HifisColor.inkTertiary : brand }

    var body: some View {
        VStack(alignment: .leading, spacing: 0) {
            HStack(spacing: 0) {
                Circle()
                    .fill(mark)
                    .frame(width: Self.dot, height: Self.dot)
                    .padding(.trailing, Self.dotGap)
                Text(item.product)
                    .font(HifisFont.label.weight(.semibold))
                    .foregroundStyle(done ? HifisColor.inkSecondary : HifisColor.ink)
                Spacer(minLength: 0)
                // 회차는 **알약에 담는다** — 상품 이름 옆에 그냥 두면 한 줄이 둘로 안 갈린다
                Text(item.roundLabel)
                    .font(HifisFont.caption)
                    .foregroundStyle(HifisColor.inkSecondary)
                    .padding(.horizontal, Self.pillH)
                    .padding(.vertical, Self.pillV)
                    .background(Capsule().fill(HifisColor.fieldFill))
            }

            Text(item.timeLabel)
                // 시각이 카드에서 제일 큰 글자다 — 목록을 훑을 때 먼저 읽는 것이 시간이다
                .font(HifisFont.title.monospacedDigit())
                .foregroundStyle(done ? HifisColor.inkTertiary : HifisColor.ink)
                .padding(.top, Self.timeGap)

            HStack(spacing: 0) {
                // **사진이 없다.** 이름 글자를 색 원에 넣는다 (사내톡과 같은 자리에서 뽑는다)
                Text(ChatBox.shared.initial(name: item.member))
                    .font(.system(size: Self.avatarFont, weight: .bold))
                    .foregroundStyle(.white)
                    .frame(width: Self.avatar, height: Self.avatar)
                    .background(
                        Circle().fill(HifisEventColor.at(ChatBox.shared.colorIndex(name: item.member)))
                    )
                    .padding(.trailing, Self.dotGap)
                Text(item.member)
                    .font(HifisFont.label)
                    .foregroundStyle(done ? HifisColor.inkTertiary : HifisColor.inkSecondary)
                Spacer(minLength: 0)
                Text(TeamSchedule.shared.statusLabel(status: item.status))
                    .font(HifisFont.caption.weight(.bold))
                    .foregroundStyle(mark)
            }
            .padding(.top, Self.footGap)
        }
        .padding(Self.cardPadding)
        .frame(maxWidth: .infinity, alignment: .leading)
        .background(
            RoundedRectangle(cornerRadius: HifisSize.cardRadius, style: .continuous)
                .fill(HifisColor.surface)
        )
    }

    /// 카드 안쪽 여백 — 세 줄짜리라 카드 기본값(24)보다 좁다
    private static let cardPadding: CGFloat = 16
    /// 머리말 왼쪽 상태 점과 그 뒤 사이
    private static let dot: CGFloat = 8
    private static let dotGap: CGFloat = 8
    /// 회차 알약 안쪽 여백
    private static let pillH: CGFloat = 10
    private static let pillV: CGFloat = 4
    /// 머리말 → 시각 → 아래 줄 사이
    private static let timeGap: CGFloat = 8
    private static let footGap: CGFloat = 10
    /// 회원 아바타와 그 안 글자 — **두 글자가 들어간다**
    ///
    /// `ChatBox.initial` 은 이름에서 **두 글자**를 뽑는다 (사내톡과 같은 규칙이라 안 바꾼다).
    /// 22 로 뒀더니 글자가 원에 꽉 차서 답답했다.
    private static let avatar: CGFloat = 28
    private static let avatarFont: CGFloat = 10
}
