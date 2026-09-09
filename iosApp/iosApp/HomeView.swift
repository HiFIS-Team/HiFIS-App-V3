import SwiftUI

/// 홈 — 모든 직원이 처음 보는 화면
///
/// **지금은 헤더뿐이다.** 본문에 뭘 얹을지는 아직 안 정했다
/// (`.claude/SPEC.md` 에 정해지면 여기 붙인다).
///
/// 누르는 자리는 아직 아무 데도 안 간다 — 갈 화면이 없다.
struct HomeView: View {
    var body: some View {
        VStack(spacing: 0) {
            HomeHeader(
                onBranch: {},
                onSearch: {},
                onChat: {},
                onNotification: {},
                onProfile: {}
            )
            Spacer(minLength: 0)
        }
        .frame(maxWidth: .infinity, maxHeight: .infinity)
        .background(HifisColor.background.ignoresSafeArea())
    }
}
