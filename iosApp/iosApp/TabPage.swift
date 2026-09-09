import SwiftUI

/// 탭 화면의 공용 껍데기 — **헤더 + 본문**
///
/// 홈·업무·일정·근태가 다 이걸 쓴다. 화면마다 헤더를 따로 그리면 언젠가 한 화면만
/// 빠지고, 그 탭에서는 사내톡·알림으로 갈 방법이 없어진다.
///
/// **본문 스크롤은 화면이 정한다.** 헤더는 붙어 있고 본문만 굴리는 화면도 있고
/// (홈), 통째로 굴리는 화면도 있다 (일정).
///
/// `title` 을 주면 헤더 바로 아래에 화면 이름이 한 줄 선다. **홈은 안 준다** —
/// 첫 화면이라 어디인지 물을 일이 없고, 그 자리는 알림 배너가 먼저 차지한다.
struct TabPage<Content: View>: View {
    var title: String? = nil
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
            if let title {
                Text(title)
                    .font(HifisFont.title)
                    .foregroundStyle(HifisColor.ink)
                    .frame(maxWidth: .infinity, alignment: .leading)
                    // 헤더와 붙으면 헤더의 일부처럼 보인다 — 위아래로 띄운다
                    .padding(.horizontal, HifisSize.screenEdge)
                    .padding(.top, 18)
                    .padding(.bottom, 10)
            }
            content()
        }
        .frame(maxWidth: .infinity, maxHeight: .infinity)
        .background(HifisColor.background.ignoresSafeArea())
    }
}
