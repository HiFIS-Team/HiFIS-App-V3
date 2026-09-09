import SwiftUI

/// 홈 헤더 — 왼쪽 지점, 오른쪽 검색·사내톡·알림·마이
///
/// 안드로이드 `HomeHeader` 와 **같은 줄**이다. 한쪽만 고치면 갈린다.
///
/// **오른쪽 넷은 순서가 고정이다.** 자리를 외운 사람에게 순서가 바뀌면 못 찾는다.
/// 새 버튼이 생겨도 이 넷 사이에 끼우지 말고 지점 옆(왼쪽)에 붙인다.
///
/// 헤더는 `surface`, 본문은 `background` 라 **선을 안 그어도 층이 갈린다**.
struct AppHeader: View {
    /// 한 지점을 보고 있으면 true — 지점 아이콘이 브랜드색으로 바뀐다
    var branchPicked: Bool = false
    /// 출퇴근 스캔 버튼을 세울지 — **`doesFieldWork` (점장·직원) 에게만 true**
    ///
    /// 대표·관리자는 출퇴근을 안 찍어서 눌러도 할 일이 없다.
    /// V2 는 데스크톱만 그렇게 하고 **폰은 전원에게 띄워 놨었다.**
    /// 로그인이 붙기 전이라 지금은 늘 true 지만, 자리는 여기다.
    var canScan: Bool = true
    /// 안 읽은 방이 있으면 true
    var chatUnread: Bool = false
    /// 안 읽은 알림이 있으면 true
    var notificationUnread: Bool = false

    let onBranch: () -> Void
    let onSearch: () -> Void
    let onScan: () -> Void
    let onChat: () -> Void
    let onNotification: () -> Void
    let onProfile: () -> Void

    var body: some View {
        HStack(spacing: 0) {
            HeaderIconButton(
                icon: "ic_branch",
                label: "지점",
                active: branchPicked,
                action: onBranch
            )

            Spacer(minLength: 0)

            HeaderIconButton(icon: "ic_search", label: "검색", action: onSearch)
            if canScan {
                HeaderIconButton(icon: "ic_scan", label: "출퇴근 스캔", action: onScan)
            }
            HeaderIconButton(
                icon: "ic_chat",
                label: "사내톡",
                badge: chatUnread,
                action: onChat
            )
            HeaderIconButton(
                icon: "ic_bell",
                label: "알림",
                badge: notificationUnread,
                action: onNotification
            )
            HeaderIconButton(icon: "ic_person", label: "마이", action: onProfile)
        }
        // 터치 자리가 그림보다 넓어서 그만큼 빼야 **그림**이 화면 끝 20 에 선다
        .padding(.horizontal, HifisSize.screenEdge - HifisSize.headerIconInset)
        .frame(height: HifisSize.headerHeight)
        // 상태바 뒤까지 헤더 색이 올라간다 — 헤더는 안전영역 안에 그대로 있다
        .background(HifisColor.surface.ignoresSafeArea(edges: .top))
    }
}
