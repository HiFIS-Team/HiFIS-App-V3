import SwiftUI
import SharedKit

/// 일정 추가 — 아래에서 올라오는 판
///
/// 담는 것: 제목 · 시작 · 종료 · 종류 · 공유 범위 · 색 · 메모.
/// 안드로이드 `EventFormSheet.kt` 와 같은 판이다 — 한쪽만 고치면 갈린다.
///
/// **아직 아무것도 저장되지 않는다.** 서버가 없어서 `저장` 을 누르면 그냥 닫힌다.
/// 날짜·시각도 지금은 글자로 받는다 — 고르개는 다음이다.
struct EventFormSheet: View {
    let onDismiss: () -> Void

    @State private var title = ""
    @State private var startDate = ""
    @State private var startTime = ""
    @State private var endDate = ""
    @State private var endTime = ""
    @State private var kind = EventKind.meeting
    @State private var scope = EventScope.center
    @State private var colorIndex: Int32 = Int32(EventPalette.shared.DEFAULT)
    @State private var memo = ""

    var body: some View {
        ScrollView {
            VStack(alignment: .leading, spacing: 0) {
                header

                section("제목")
                field($title, "무엇을 계획하고 있나요?")

                section("시작")
                dateTimeRow($startDate, $startTime)

                section("종료")
                dateTimeRow($endDate, $endTime)

                section("카테고리")
                kindChips

                section("공유 범위")
                scopeCards

                section("색상")
                colorSwatches

                section("메모", optional: true)
                field($memo, "참석자·장소·준비물 등 상세 내용을 적어주세요", minHeight: 110)

                Spacer().frame(height: 28)
                saveButton
            }
            .padding(.horizontal, HifisSize.screenEdge)
            .padding(.bottom, 32)
        }
        .background(HifisColor.surface)
        .presentationDragIndicator(.visible)
    }

    private var header: some View {
        HStack(spacing: 14) {
            Image("ic_schedule")
                .renderingMode(.template)
                .resizable()
                .frame(width: 24, height: 24)
                .foregroundStyle(HifisColor.brand)
                .frame(width: 48, height: 48)
                .background(
                    RoundedRectangle(cornerRadius: 14, style: .continuous)
                        .fill(HifisColor.brand.opacity(HifisColor.toneFillOpacity))
                )
            VStack(alignment: .leading, spacing: 1) {
                Text("일정 추가")
                    .font(.system(size: 20, weight: .bold))
                    .foregroundStyle(HifisColor.ink)
                Text("센터와 공유할 일정을 만들어요")
                    .font(HifisFont.caption)
                    .foregroundStyle(HifisColor.inkTertiary)
            }
            Spacer(minLength: 8)
            Button(action: onDismiss) {
                Image("ic_close")
                    .renderingMode(.template)
                    .resizable()
                    .frame(width: 20, height: 20)
                    .foregroundStyle(HifisColor.inkTertiary)
                    .frame(width: 36, height: 36)
                    .contentShape(Rectangle())
            }
            .buttonStyle(TapStyle())
            .accessibilityLabel("닫기")
        }
        .padding(.top, 8)
    }

    private func section(_ label: String, optional: Bool = false) -> some View {
        HStack(alignment: .bottom, spacing: 5) {
            Text(label)
                .font(.system(size: 14, weight: .bold))
                .foregroundStyle(HifisColor.ink)
            if optional {
                Text("(선택)")
                    .font(HifisFont.caption)
                    .foregroundStyle(HifisColor.inkTertiary)
            }
        }
        .padding(.top, 22)
        .padding(.bottom, 10)
    }

    /// 글자를 받는 칸 — 면은 `fieldFill` 이다
    private func field(
        _ text: Binding<String>,
        _ placeholder: String,
        trailing: String? = nil,
        minHeight: CGFloat = 52
    ) -> some View {
        HStack(alignment: minHeight > 60 ? .top : .center, spacing: 10) {
            TextField("", text: text, axis: minHeight > 60 ? .vertical : .horizontal)
                .font(HifisFont.label)
                .foregroundStyle(HifisColor.ink)
                .tint(HifisColor.brand)
                .lineLimit(minHeight > 60 ? 4 : 1, reservesSpace: minHeight > 60)
                .overlay(alignment: .leading) {
                    if text.wrappedValue.isEmpty {
                        Text(placeholder)
                            .font(HifisFont.label)
                            .foregroundStyle(HifisColor.inkTertiary)
                            .allowsHitTesting(false)
                    }
                }
            if let trailing {
                Image(trailing)
                    .renderingMode(.template)
                    .resizable()
                    .frame(width: 18, height: 18)
                    .foregroundStyle(HifisColor.inkTertiary)
            }
        }
        .padding(.horizontal, 16)
        .padding(.vertical, 15)
        .frame(minHeight: minHeight, alignment: .topLeading)
        .background(
            HifisColor.fieldFill,
            in: RoundedRectangle(cornerRadius: 14, style: .continuous)
        )
    }

    /// 날짜 + 시각 — 날짜가 더 길어서 자리를 더 준다
    private func dateTimeRow(_ date: Binding<String>, _ time: Binding<String>) -> some View {
        HStack(spacing: 10) {
            field(date, "YYYY-MM-DD", trailing: "ic_schedule")
                .frame(maxWidth: .infinity)
            field(time, "--:--", trailing: "ic_attendance")
                .frame(width: 130)
        }
    }

    private var kindChips: some View {
        FlowLayout(spacing: 8, lineSpacing: 8) {
            ForEach(EventKind.companion.all, id: \.label) { item in
                let on = item == kind
                Button { kind = item } label: {
                    HStack(spacing: 6) {
                        Image(item.icon)
                            .renderingMode(.template)
                            .resizable()
                            .frame(width: 16, height: 16)
                            .foregroundStyle(on ? HifisColor.brand : HifisColor.inkSecondary)
                        Text(item.label)
                            .font(.system(size: 13, weight: .semibold))
                            .foregroundStyle(on ? HifisColor.brand : HifisColor.ink)
                    }
                    .padding(.horizontal, 14)
                    .padding(.vertical, 10)
                    .background(
                        Capsule().fill(on ? HifisColor.brand.opacity(0.18) : .clear)
                    )
                    .overlay(
                        Capsule().strokeBorder(on ? HifisColor.brand : HifisColor.line, lineWidth: 1)
                    )
                    .contentShape(Capsule())
                }
                .buttonStyle(TapStyle())
            }
        }
    }

    private var scopeCards: some View {
        LazyVGrid(
            columns: [GridItem(.flexible(), spacing: 10), GridItem(.flexible(), spacing: 10)],
            spacing: 10
        ) {
            ForEach(EventScope.companion.all, id: \.label) { item in
                let on = item == scope
                Button { scope = item } label: {
                    VStack(alignment: .leading, spacing: 3) {
                        HStack(spacing: 7) {
                            Image(item.icon)
                                .renderingMode(.template)
                                .resizable()
                                .frame(width: 17, height: 17)
                                .foregroundStyle(on ? HifisColor.brand : HifisColor.inkSecondary)
                            Text(item.label)
                                .font(.system(size: 14, weight: .bold))
                                .foregroundStyle(on ? HifisColor.brand : HifisColor.ink)
                        }
                        Text(item.detail)
                            .font(HifisFont.caption)
                            .foregroundStyle(HifisColor.inkTertiary)
                            .multilineTextAlignment(.leading)
                    }
                    .frame(maxWidth: .infinity, alignment: .leading)
                    .padding(.horizontal, 14)
                    .padding(.vertical, 13)
                    .background(
                        RoundedRectangle(cornerRadius: 14, style: .continuous)
                            .fill(on ? HifisColor.brand.opacity(0.16) : .clear)
                    )
                    .overlay(
                        RoundedRectangle(cornerRadius: 14, style: .continuous)
                            .strokeBorder(on ? HifisColor.brand : HifisColor.line, lineWidth: 1)
                    )
                    .contentShape(Rectangle())
                }
                .buttonStyle(TapStyle())
            }
        }
    }

    private var colorSwatches: some View {
        FlowLayout(spacing: 10, lineSpacing: 10) {
            ForEach(0..<Int(EventPalette.shared.colors.count), id: \.self) { index in
                let on = Int32(index) == colorIndex
                Button { colorIndex = Int32(index) } label: {
                    Circle()
                        .fill(HifisEventColor.at(Int32(index)))
                        .frame(width: 34, height: 34)
                        // 고른 것에만 테를 둘러 띄운다 — 색끼리 붙어 있어 테가 없으면 못 찾는다
                        .overlay(
                            Circle().strokeBorder(on ? HifisColor.ink : .clear, lineWidth: 2.5)
                        )
                        .overlay {
                            if on {
                                Image("ic_check")
                                    .renderingMode(.template)
                                    .resizable()
                                    .frame(width: 16, height: 16)
                                    .foregroundStyle(.white)
                            }
                        }
                }
                .buttonStyle(TapStyle())
                .accessibilityLabel("색 \(index + 1)")
            }
        }
    }

    private var saveButton: some View {
        let enabled = !title.trimmingCharacters(in: .whitespaces).isEmpty
        return Button { if enabled { onDismiss() } } label: {
            Text("저장")
                .font(.system(size: 16, weight: .bold))
                .foregroundStyle(enabled ? .white : HifisColor.inkTertiary)
                .frame(maxWidth: .infinity)
                .frame(height: 54)
                .background(
                    RoundedRectangle(cornerRadius: 14, style: .continuous)
                        .fill(enabled ? HifisColor.brand : HifisColor.fieldFill)
                )
                .contentShape(Rectangle())
        }
        .buttonStyle(TapStyle())
        .disabled(!enabled)
    }
}

/// 칩처럼 폭이 제각각인 것을 줄바꿈하며 세운다
///
/// SwiftUI 에는 이런 배치가 없다 (`LazyVGrid` 는 칸 폭이 같아야 한다).
/// 안드로이드의 `FlowRow` 자리다.
struct FlowLayout: Layout {
    var spacing: CGFloat = 8
    var lineSpacing: CGFloat = 8

    func sizeThatFits(proposal: ProposedViewSize, subviews: Subviews, cache: inout ()) -> CGSize {
        let maxWidth = proposal.width ?? .infinity
        var x: CGFloat = 0, y: CGFloat = 0, lineHeight: CGFloat = 0
        for view in subviews {
            let size = view.sizeThatFits(.unspecified)
            if x > 0, x + size.width > maxWidth {
                x = 0
                y += lineHeight + lineSpacing
                lineHeight = 0
            }
            x += size.width + spacing
            lineHeight = max(lineHeight, size.height)
        }
        return CGSize(width: maxWidth == .infinity ? x : maxWidth, height: y + lineHeight)
    }

    func placeSubviews(
        in bounds: CGRect,
        proposal: ProposedViewSize,
        subviews: Subviews,
        cache: inout ()
    ) {
        var x: CGFloat = 0, y: CGFloat = 0, lineHeight: CGFloat = 0
        for view in subviews {
            let size = view.sizeThatFits(.unspecified)
            if x > 0, x + size.width > bounds.width {
                x = 0
                y += lineHeight + lineSpacing
                lineHeight = 0
            }
            view.place(
                at: CGPoint(x: bounds.minX + x, y: bounds.minY + y),
                proposal: ProposedViewSize(size)
            )
            x += size.width + spacing
            lineHeight = max(lineHeight, size.height)
        }
    }
}
