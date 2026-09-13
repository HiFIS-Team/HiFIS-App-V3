import SwiftUI
import SharedKit

/// TeamFIS 회원 — **보유 회원 전체가 기본**이고 필터가 그 위에서 갈래를 좁힌다
///
/// 짜임은 **TeamFIS 것과 같다** (2026-09-14 대표가 그 레포를 지목). 값은 HiFIS 토큰으로
/// 옮겨 심었다 — 그쪽은 모서리가 4 로 각진 벌인데, 그대로 가져오면 이 화면만 앱에서 튄다.
///
/// **필터 줄은 헤더와 함께 붙어 있다.** 같이 흘러가면 목록 아래에서 갈래를 바꾸려고
/// 맨 위까지 되돌아가야 한다.
///
/// 값은 아직 `MemberBoard.demo` 다 — **서버를 안 붙였다.**
///
/// 안드로이드 `TeamMemberScreen` 과 같은 화면이다.
struct TeamMemberView: View {
    var onSearch: () -> Void = {}
    var onScan: () -> Void = {}
    var onChat: () -> Void = {}
    var onNotification: () -> Void = {}

    private let members = MemberBoard.shared.demo
    @State private var filter: MemberStatus?

    private var shown: [Member] {
        MemberBoard.shared.shown(members: members, filter: filter)
    }

    var body: some View {
        TabPage(onSearch: onSearch, onScan: onScan, onChat: onChat, onNotification: onNotification) {
            filterBar

            if shown.isEmpty {
                Text(MemberBoard.shared.EMPTY)
                    .font(HifisFont.body)
                    .foregroundStyle(HifisColor.inkSecondary)
                    .frame(maxWidth: .infinity)
                    .padding(.vertical, Self.emptyPad)
                Spacer(minLength: 0)
            } else {
                ScrollView {
                    // 수십 줄로 길어지는 자리라 **보이는 줄만 만든다**
                    LazyVStack(spacing: 0) {
                        ForEach(Array(shown.enumerated()), id: \.element.id) { index, member in
                            if index > 0 { RowDivider() }
                            MemberRow(member: member)
                        }
                    }
                }
            }
        }
    }

    /// 필터 줄 — 활성 · 홀딩 · 만료
    ///
    /// **`전체` 칸을 안 둔다.** 아무것도 안 고른 상태가 곧 보유 회원 전체이고 그게 기본이다.
    /// 칩마다 숫자를 달아 **고르지 않고도 갈래별 규모**가 보이게 한다.
    ///
    /// 오른쪽 끝이 **회원 추가**다. TeamFIS 는 안드로이드에서 FAB 을 쓰지만 우리는
    /// 양 플랫폼을 같은 자리에 둔다 — 이 앱에 FAB 관습이 없다.
    private var filterBar: some View {
        HStack(spacing: Self.chipGap) {
            ForEach(MemberBoard.shared.statuses, id: \.self) { status in
                CountChip(
                    label: MemberBoard.shared.statusLabel(status: status),
                    count: Int(MemberBoard.shared.count(members: members, status: status)),
                    picked: filter == status
                ) {
                    // 고른 것을 다시 누르면 풀려서 전체로 돌아온다
                    filter = (filter == status) ? nil : status
                }
            }
            Spacer(minLength: 0)
            AddButton()
        }
        .padding(.horizontal, HifisSize.screenEdge)
        .padding(.top, Self.barTop)
        .padding(.bottom, Self.barBottom)
    }

    /// 필터 줄 위아래 여백
    private static let barTop: CGFloat = 8
    private static let barBottom: CGFloat = 12
    /// 필터 칩 사이
    fileprivate static let chipGap: CGFloat = 8
    /// 고른 갈래에 아무도 없을 때 그 자리의 위아래 여백
    private static let emptyPad: CGFloat = 52
}

/// 필터 칩 하나 — 이름 + 숫자. 고르면 제품색으로 찬다
private struct CountChip: View {
    @Environment(\.brand) private var brand
    let label: String
    let count: Int
    let picked: Bool
    let action: () -> Void

    var body: some View {
        Button(action: action) {
            HStack(spacing: 4) {
                Text(label)
                    .font(HifisFont.label)
                    .foregroundStyle(picked ? .white : HifisColor.inkSecondary)
                Text("\(count)")
                    .font(HifisFont.label.monospacedDigit())
                    // 고른 칩은 제품색 위라 흐린 회색이 안 보인다 — 흰색을 반투명하게 깐다
                    .foregroundStyle(picked ? Color.white.opacity(0.7) : HifisColor.inkTertiary)
            }
            .padding(.horizontal, Self.pad)
            .frame(height: Self.height)
            .background(
                RoundedRectangle(cornerRadius: Self.radius, style: .continuous)
                    .fill(picked ? brand : HifisColor.surface)
            )
            .contentShape(Rectangle())
        }
        .buttonStyle(.plain)
        .animation(.easeOut(duration: 0.14), value: picked)
    }

    fileprivate static let height: CGFloat = 36
    fileprivate static let radius: CGFloat = 14
    private static let pad: CGFloat = 14
}

/// 회원 추가 — 칩과 같은 높이의 네모에 **플러스만 제품색**이다
///
/// 바탕까지 채우지 않는다. 고른 필터 칩이 이미 제품색 **면**이라, 같은 줄에 찬 면이
/// 둘이면 어느 것이 고른 것인지 흐려진다.
///
/// > **아직 갈 화면이 없다.** 붙으면 여기서 연다.
private struct AddButton: View {
    @Environment(\.brand) private var brand

    var body: some View {
        Button {} label: {
            Image("ic_plus")
                .renderingMode(.template)
                .resizable()
                .frame(width: Self.icon, height: Self.icon)
                .foregroundStyle(brand)
                .frame(width: CountChip.height, height: CountChip.height)
                .background(
                    RoundedRectangle(cornerRadius: CountChip.radius, style: .continuous)
                        .fill(HifisColor.surface)
                )
                .contentShape(Rectangle())
        }
        .buttonStyle(.plain)
        .accessibilityLabel("회원 추가")
    }

    private static let icon: CGFloat = 20
}

/// 회원 한 줄
///
/// ```
/// 김수현 회원님  [홀딩]              12/30회차
/// 마지막 9/5
/// ```
private struct MemberRow: View {
    let member: Member

    var body: some View {
        Button {} label: {
            HStack(spacing: 0) {
                VStack(alignment: .leading, spacing: 4) {
                    HStack(spacing: Self.badgeGap) {
                        Text(member.label)
                            .font(HifisFont.body)
                            .foregroundStyle(HifisColor.ink)
                        StatusBadge(status: member.status)
                    }
                    Text(member.detail)
                        .font(HifisFont.caption)
                        .foregroundStyle(HifisColor.inkTertiary)
                }
                Spacer(minLength: Self.badgeGap)
                Text(member.progress)
                    // 회차는 자릿수가 바뀌어도 오른쪽 끝이 안 흔들려야 한다
                    .font(HifisFont.label.monospacedDigit())
                    .foregroundStyle(HifisColor.inkSecondary)
            }
            .padding(.horizontal, HifisSize.screenEdge)
            .padding(.vertical, Self.pad)
            .contentShape(Rectangle())
        }
        .buttonStyle(.plain)
    }

    private static let pad: CGFloat = 16
    fileprivate static let badgeGap: CGFloat = 8
}

/// 상태 배지 — **활성에는 안 붙는다**
///
/// 대부분이 활성이라 다 붙이면 목록이 배지로 뒤덮인다. 눈에 걸려야 할 예외에만 붙인다.
/// **만료는 제품색**이다 — 재등록을 붙여야 할 자리다.
private struct StatusBadge: View {
    @Environment(\.brand) private var brand
    let status: MemberStatus

    var body: some View {
        if status != MemberStatus.active {
            Text(MemberBoard.shared.statusLabel(status: status))
                .font(HifisFont.caption)
                .foregroundStyle(status == MemberStatus.expired ? brand : HifisColor.inkTertiary)
                .padding(.horizontal, Self.h)
                .padding(.vertical, Self.v)
                .background(
                    RoundedRectangle(cornerRadius: Self.radius, style: .continuous)
                        .fill(HifisColor.fieldFill)
                )
        }
    }

    private static let radius: CGFloat = 8
    private static let h: CGFloat = 8
    private static let v: CGFloat = 2
}

/// 줄 사이 얇은 선 — **바깥까지 안 간다** (화면 가장자리에 선이 붙으면 답답하다)
private struct RowDivider: View {
    var body: some View {
        Rectangle()
            .fill(HifisColor.line)
            .frame(height: 1)
            .padding(.horizontal, HifisSize.screenEdge)
    }
}
