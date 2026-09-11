import SwiftUI
import SharedKit

/// 알림함 — **옆에서 밀려 들어오는 잎** (헤더의 종이 연다)
///
/// 전체 / 안읽음을 전환하며 **오늘·이전**으로 묶어 보여준다. V2 알림 화면을 그대로
/// 옮겼다 — 제목 줄 오른쪽에 새로고침·설정, 그 아래 전환 스위치, 비었으면 빈 카드.
/// 안드로이드 `NotificationScreen` 과 같은 화면이다.
///
/// 눌러서 읽음 처리한다. **갈 곳으로 넘어가는 것은 아직 없다** — 갈 화면이 없다.
/// 새로고침·설정도 아직 아무 일도 안 한다 (서버도 설정 화면도 없다).
///
/// 값은 `AppNotification.demo` 다 — **서버를 안 붙였다.**
struct NotificationView: View {
    let onBack: () -> Void

    @State private var items: [AppNotification]
    @State private var unreadOnly = false
    /// 시각은 한 번만 잡는다 — 매 프레임 흐르면 `12분 전` 이 보는 중에 바뀐다
    private let now: Kotlinx_datetimeLocalDateTime

    init(onBack: @escaping () -> Void) {
        self.onBack = onBack
        let parts = Foundation.Calendar.current.dateComponents(
            [.year, .month, .day, .hour, .minute], from: Date()
        )
        // 시각은 `shared` 가 짓는다 — 생성자를 직접 부르면 같은 이름이 둘이라 어느 것이 나올지 모른다
        let now = NotificationBox.shared.at(
            year: Int32(parts.year ?? 2026),
            month: Int32(parts.month ?? 1),
            day: Int32(parts.day ?? 1),
            hour: Int32(parts.hour ?? 0),
            minute: Int32(parts.minute ?? 0)
        )
        self.now = now
        _items = State(initialValue: AppNotification.companion.demo(now: now))
    }

    private var unreadCount: Int { items.filter { !$0.read }.count }

    var body: some View {
        let box = NotificationBox.shared
        let sections = box.sections(items: items, unreadOnly: unreadOnly, today: now.date)

        VStack(spacing: 0) {
            header
            ScrollView {
                VStack(alignment: .leading, spacing: 0) {
                    Spacer().frame(height: Self.headerBodyGap)
                    ModeSwitch(
                        left: box.ALL,
                        right: box.unreadLabel(unreadCount: Int32(unreadCount)),
                        rightSelected: $unreadOnly
                    )
                    .padding(.horizontal, HifisSize.screenEdge)
                    Spacer().frame(height: Self.switchBodyGap)

                    VStack(alignment: .leading, spacing: 0) {
                        if sections.isEmpty {
                            EmptyCard(text: box.emptyLabel(unreadOnly: unreadOnly))
                        } else {
                            if !sections.today.isEmpty {
                                SectionLabel(box.TODAY)
                                NotificationCard(items: sections.today, now: now, onOpen: open)
                            }
                            if !sections.today.isEmpty && !sections.earlier.isEmpty {
                                Spacer().frame(height: Self.sectionGap)
                            }
                            if !sections.earlier.isEmpty {
                                SectionLabel(box.EARLIER)
                                NotificationCard(items: sections.earlier, now: now, onOpen: open)
                            }
                        }
                    }
                    .padding(.horizontal, HifisSize.screenEdge)
                }
                .padding(.bottom, 24)
            }
        }
        .frame(maxWidth: .infinity, maxHeight: .infinity)
        .background(HifisColor.background.ignoresSafeArea())
    }

    /// 읽음 — 화면을 먼저 바꾼다. 서버가 붙으면 그 뒤에 보낸다
    private func open(_ picked: AppNotification) {
        items = items.map { $0.id == picked.id ? $0.markRead() : $0 }
    }

    /// 잎 헤더 — 왼쪽 뒤로가기, 그 옆에 화면 이름, 오른쪽 새로고침·설정
    ///
    /// 탭 화면 헤더와 같은 줄 높이·같은 끝 자리다. **화면 이름이 헤더에 든다** —
    /// 잎은 본문 위에 제목을 또 세우지 않는다 (대표 요청, 2026-09-10).
    private var header: some View {
        HStack(spacing: 0) {
            HeaderIconButton(icon: "ic_chevron_left", label: "뒤로", action: onBack)
            Spacer().frame(width: Self.headerTitleGap)
            Text(NotificationBox.shared.TITLE)
                .font(HifisFont.header)
                .foregroundStyle(HifisColor.ink)
                .frame(maxWidth: .infinity, alignment: .leading)
            HeaderIconButton(icon: "ic_refresh", label: "새로고침") {
                // 아직 받을 곳이 없다 — 서버가 붙으면 다시 받는다
            }
            HeaderIconButton(icon: "ic_settings", label: "알림 설정") {
                // 아직 갈 곳이 없다 — 알림 설정 화면이 생기면 잇는다
            }
        }
        .padding(.horizontal, HifisSize.screenEdge - HifisSize.headerIconInset)
        .frame(height: HifisSize.headerHeight)
    }

    /// 뒤로가기 터치 자리와 화면 이름 사이 — 그림에서 재면 13 이다 (터치 여백 11 + 2)
    private static let headerTitleGap: CGFloat = 2
    /// 헤더와 스위치 사이
    private static let headerBodyGap: CGFloat = 16
    /// 스위치와 본문(카드) 사이
    private static let switchBodyGap: CGFloat = 20
    /// 묶음 사이
    private static let sectionGap: CGFloat = 24
}

/// 목록이 비었을 때 자리를 채우는 카드 — 둥근 네모 아이콘과 한 줄 (V2 `EmptyCard`)
private struct EmptyCard: View {
    let text: String

    var body: some View {
        VStack(spacing: 0) {
            Image("ic_bell")
                .renderingMode(.template)
                .resizable()
                .frame(width: HifisSize.alertIcon, height: HifisSize.alertIcon)
                .foregroundStyle(HifisColor.inkTertiary)
                .frame(width: HifisSize.alertChip, height: HifisSize.alertChip)
                .background(
                    HifisColor.fieldFill,
                    in: RoundedRectangle(cornerRadius: HifisSize.alertChipRadius, style: .continuous)
                )
            Spacer().frame(height: 14)
            Text(text)
                .font(HifisFont.body)
                .foregroundStyle(HifisColor.inkSecondary)
        }
        .frame(maxWidth: .infinity)
        .padding(.vertical, Self.pad)
        .modifier(CardChrome())
    }

    /// 빈 카드 위아래 여백
    private static let pad: CGFloat = 52
}

/// 오늘 · 이전 — 묶음 머리말은 전체 화면과 같은 글자다
private struct SectionLabel: View {
    let text: String
    init(_ text: String) { self.text = text }

    var body: some View {
        Text(text)
            .font(.system(size: 13, weight: .medium))
            .foregroundStyle(HifisColor.inkTertiary)
            .padding(.leading, 4)
            .padding(.bottom, 10)
    }
}

/// 한 묶음의 카드 — 줄 사이는 1px 선으로 가른다
private struct NotificationCard: View {
    let items: [AppNotification]
    let now: Kotlinx_datetimeLocalDateTime
    let onOpen: (AppNotification) -> Void

    var body: some View {
        VStack(spacing: 0) {
            ForEach(Array(items.enumerated()), id: \.offset) { index, item in
                if index > 0 {
                    Rectangle().fill(HifisColor.line).frame(height: 1)
                }
                NotificationRow(item: item, now: now) { onOpen(item) }
            }
        }
        // 줄이 제 위아래 여백을 가져서 세로는 얇다
        .padding(.horizontal, 20)
        .padding(.vertical, 4)
        .modifier(CardChrome())
    }
}

/// 알림 한 줄 — 종류별 색 원 + 제목 + 곁글 + 시각, 안 읽었으면 오른쪽에 점
///
/// **안 읽은 줄만 진하다.** 읽은 줄은 글자도 원도 가라앉아서 훑을 때 새것만 튄다.
private struct NotificationRow: View {
    let item: AppNotification
    let now: Kotlinx_datetimeLocalDateTime
    let onOpen: () -> Void

    var body: some View {
        let unread = !item.read
        let tint = HifisColor.tone(item.kind.tone)
        let fill = HifisColor.toneFillOpacity

        Button(action: onOpen) {
            HStack(alignment: .top, spacing: 0) {
                Image(item.kind.icon)
                    .renderingMode(.template)
                    .resizable()
                    .frame(width: Self.icon, height: Self.icon)
                    .foregroundStyle(unread ? tint : HifisColor.inkTertiary)
                    .frame(width: Self.circle, height: Self.circle)
                    .background(tint.opacity(unread ? fill : fill / 2), in: Circle())
                Spacer().frame(width: 14)
                VStack(alignment: .leading, spacing: 0) {
                    Text(item.title)
                        .font(.system(size: 16, weight: unread ? .semibold : .regular))
                        .foregroundStyle(unread ? HifisColor.ink : HifisColor.inkSecondary)
                    // 곁글은 제목만으로 모자란 것을 채운다 (`· 사유: …` 같은 것)
                    if let body = item.body, !body.isEmpty {
                        Spacer().frame(height: 3)
                        Text(body)
                            .font(HifisFont.caption)
                            .foregroundStyle(HifisColor.inkTertiary)
                            .lineLimit(2)
                    }
                    Spacer().frame(height: 3)
                    Text(NotificationBox.shared.timeLabel(at: item.createdAt, now: now))
                        .font(HifisFont.caption)
                        .foregroundStyle(HifisColor.inkTertiary)
                }
                .frame(maxWidth: .infinity, alignment: .leading)
                if unread {
                    Spacer().frame(width: 8)
                    Circle()
                        .fill(HifisColor.brand)
                        .frame(width: HifisSize.badgeDot, height: HifisSize.badgeDot)
                        .padding(.top, 6)
                }
            }
            .padding(.vertical, 12)
            .contentShape(Rectangle())
        }
        .buttonStyle(TapStyle())
    }

    /// 종류 색 원 · 그 안의 그림
    private static let circle: CGFloat = 40
    private static let icon: CGFloat = 20
}

/// 카드 껍데기 — 홈 카드와 같은 면·테두리·그림자
private struct CardChrome: ViewModifier {
    func body(content: Content) -> some View {
        content
            .background(HifisColor.surface)
            .clipShape(RoundedRectangle(cornerRadius: HifisSize.cardRadius, style: .continuous))
            .overlay(
                RoundedRectangle(cornerRadius: HifisSize.cardRadius, style: .continuous)
                    .strokeBorder(HifisColor.line, lineWidth: 1)
            )
            .shadow(color: .black.opacity(0.04), radius: 12, y: 6)
    }
}
