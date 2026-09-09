import SwiftUI
import SharedKit

/// 오늘 소식 — 공지 몇 줄을 홈에 얹는다
///
/// **필독 줄은 글자만 빨간 게 아니라 줄 바탕까지 옅게 물든다.** 목록을 훑을 때
/// 글자를 읽기 전에 "여기 하나 걸려 있다"가 먼저 보이라고 그렇게 한다.
///
/// 머리말은 `오늘 근무` 카드와 **같은 크기·색**이다. 카드마다 머리말이 다르면
/// 홈이 여러 사람이 만든 것처럼 보인다.
/// 안드로이드 `TodayNewsCard.kt` 와 같은 카드다 — 한쪽만 고치면 갈린다.
struct TodayNewsCard: View {
    let notices: [Notice]
    let onOpen: (Notice) -> Void

    /// 날짜 꼴은 `shared` 가 들고 있다 — 두 플랫폼이 같은 문자열을 쓴다
    private static let formatter: DateFormatter = {
        let f = DateFormatter()
        f.locale = Locale(identifier: "ko_KR")
        f.dateFormat = Notice.companion.DATE_PATTERN
        return f
    }()

    var body: some View {
        VStack(spacing: 0) {
            HStack {
                Text("오늘 소식")
                    .font(HifisFont.label)
                    .foregroundStyle(HifisColor.inkSecondary)
                Spacer()
                Text(Self.formatter.string(from: Date()))
                    .font(HifisFont.caption)
                    .foregroundStyle(HifisColor.inkTertiary)
            }

            Spacer().frame(height: 14)

            VStack(spacing: 8) {
                ForEach(Array(notices.enumerated()), id: \.offset) { _, notice in
                    NoticeRow(notice: notice, onOpen: onOpen)
                }
            }
        }
        .padding(HifisSize.cardPadding)
        .background(HifisColor.surface)
        .clipShape(RoundedRectangle(cornerRadius: HifisSize.cardRadius, style: .continuous))
        .overlay(
            RoundedRectangle(cornerRadius: HifisSize.cardRadius, style: .continuous)
                .strokeBorder(HifisColor.line, lineWidth: 1)
        )
        .shadow(color: .black.opacity(0.04), radius: 12, y: 6)
    }
}

private struct NoticeRow: View {
    let notice: Notice
    let onOpen: (Notice) -> Void

    @Environment(\.colorScheme) private var scheme

    var body: some View {
        Button { onOpen(notice) } label: {
            HStack(spacing: 8) {
                if notice.pinned {
                    Text(Notice.companion.PINNED_LABEL)
                        .font(.system(size: 13, weight: .bold))
                        .foregroundStyle(HifisColor.danger)
                }
                Text(notice.title)
                    .font(HifisFont.body)
                    .foregroundStyle(HifisColor.ink)
                    .lineLimit(1)
                    .truncationMode(.tail)
                Spacer(minLength: 0)
            }
            .padding(.horizontal, HifisSize.rowPaddingH)
            .padding(.vertical, HifisSize.rowPaddingV)
            .frame(maxWidth: .infinity, alignment: .leading)
            .background(fill, in: RoundedRectangle(cornerRadius: HifisSize.rowRadius, style: .continuous))
            .contentShape(Rectangle())
        }
        .buttonStyle(.plain)
    }

    /// 필독이면 줄 바탕이 그 색으로 옅게 물든다. 아니면 평범한 회색 줄이다.
    /// 배지(12%)보다 옅게 둔다 — 여기는 글자를 얹는 바닥이라 그만큼 진하면 글자가 흐려진다
    private var fill: Color {
        guard notice.pinned else { return HifisColor.background }
        return HifisColor.danger.opacity(scheme == .dark ? 0.14 : 0.07)
    }
}
