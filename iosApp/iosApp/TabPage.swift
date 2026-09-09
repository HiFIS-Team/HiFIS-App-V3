import SwiftUI

/// 탭 화면의 공용 껍데기 — **헤더 + 본문**
///
/// 홈·업무·일정·근태가 다 이걸 쓴다. 화면마다 헤더를 따로 그리면 언젠가 한 화면만
/// 빠지고, 그 탭에서는 사내톡·알림으로 갈 방법이 없어진다.
///
/// **본문 스크롤은 화면이 정한다.** 헤더는 붙어 있고 본문만 굴리는 화면도 있고
/// (홈), 통째로 굴리는 화면도 있다 (일정).
struct TabPage<Content: View>: View {
    var onBranch: () -> Void = {}
    var onSearch: () -> Void = {}
    var onScan: () -> Void = {}
    var onChat: () -> Void = {}
    var onNotification: () -> Void = {}
    var onProfile: () -> Void = {}
    @ViewBuilder var content: () -> Content

    var body: some View {
        VStack(spacing: 0) {
            AppHeader(
                onBranch: onBranch,
                onSearch: onSearch,
                onScan: onScan,
                onChat: onChat,
                onNotification: onNotification,
                onProfile: onProfile
            )
            content()
        }
        .frame(maxWidth: .infinity, maxHeight: .infinity)
        .background(HifisColor.background.ignoresSafeArea())
    }
}
