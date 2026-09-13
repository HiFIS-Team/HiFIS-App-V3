import SwiftUI
import SharedKit

/// 수업 카드 — 달력에서 고른 날의 수업 하나
///
/// **무엇을 어디에 적는지는 TeamFIS 것과 같다** (2026-09-14 대표가 그 레포를 지목).
///
/// ```
/// ● 김수현 회원님              [ 수업예정 ]   ← 상태 점 + 회원 · 상태 배지
/// 오후 2:00 ~ 3:00                           ← 눈이 먼저 닿는 줄
/// PT 30회                        12/30회차   ← 상품 · 회차
/// ```
///
/// **시간이 제일 크다.** 하루를 시간 순으로 훑는 자리라 "누가"보다 "몇 시에"가 먼저 걸려야 한다.
/// 왼쪽 점은 **상태를 색으로만** 말한다 — 배지 글자를 안 읽고도 세로로 훑을 수 있다.
///
/// 안드로이드 `TeamClassCard` 와 같은 값이다.
struct TeamClassCardView: View {
    @Environment(\.brand) private var brand
    let item: TeamClass

    var body: some View {
        VStack(alignment: .leading, spacing: 0) {
            HStack(spacing: 0) {
                Circle()
                    .fill(dotColor)
                    .frame(width: Self.dot, height: Self.dot)
                    .padding(.trailing, Self.dotGap)
                Text(item.memberLabel)
                    .font(HifisFont.label)
                    .foregroundStyle(HifisColor.inkSecondary)
                Spacer(minLength: 0)
                badge
            }

            Text(item.timeLabel)
                // 시간은 자릿수가 바뀌어도 줄이 안 흔들려야 한다
                .font(HifisFont.title.monospacedDigit())
                .foregroundStyle(HifisColor.ink)
                .padding(.top, Self.timeGap)

            HStack(spacing: 0) {
                Text(item.product)
                    .font(HifisFont.caption)
                    .foregroundStyle(HifisColor.inkTertiary)
                Spacer(minLength: 0)
                Text(item.roundLabel)
                    .font(HifisFont.caption.monospacedDigit())
                    .foregroundStyle(HifisColor.inkTertiary)
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

    /// 상태 배지 — **예정만 채운다**
    ///
    /// 지나간 것(완료·노쇼)을 채우면 눈이 거기 멈추는데, 봐야 하는 건 아직 안 한 수업이다.
    private var badge: some View {
        let filled = item.status == ClassStatus.scheduled
        return Text(TeamSchedule.shared.statusLabel(status: item.status))
            .font(HifisFont.caption)
            .foregroundStyle(
                filled ? .white
                    : (item.status == ClassStatus.noShow ? HifisColor.danger : HifisColor.inkTertiary)
            )
            .padding(.horizontal, Self.badgeH)
            .padding(.vertical, Self.badgeV)
            .background(
                RoundedRectangle(cornerRadius: Self.badgeRadius, style: .continuous)
                    .fill(filled ? brand : HifisColor.fieldFill)
            )
    }

    /// 훑을 때 쓰는 점 색 — 예정만 제품색이다
    ///
    /// 노쇼는 `danger` 다. TeamFIS 는 브랜드 레드를 노쇼에 쓰는데, 우리는 제품색이 곧
    /// 그 레드라 그대로 쓰면 **예정과 노쇼가 같은 색**이 된다 (`DESIGN.md` 상태색 규칙).
    private var dotColor: Color {
        switch item.status {
        case ClassStatus.done: HifisColor.inkTertiary
        case ClassStatus.noShow: HifisColor.danger
        default: brand
        }
    }

    /// 카드 안쪽 여백 — 세 줄짜리라 카드 기본값(24)보다 좁다
    private static let cardPadding: CGFloat = 16
    /// 머리말 왼쪽 상태 점과 그 뒤 사이
    private static let dot: CGFloat = 8
    private static let dotGap: CGFloat = 8
    /// 상태 배지 — 모서리·안쪽 여백
    private static let badgeRadius: CGFloat = 8
    private static let badgeH: CGFloat = 8
    private static let badgeV: CGFloat = 3
    /// 머리말 → 시각 → 아래 줄 사이
    private static let timeGap: CGFloat = 8
    private static let footGap: CGFloat = 12
}
