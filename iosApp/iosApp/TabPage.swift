import SwiftUI

/// 탭 화면의 공용 껍데기 — **헤더 + 본문**
///
/// 홈·업무·일정·근태가 다 이걸 쓴다. 화면마다 헤더를 따로 그리면 언젠가 한 화면만
/// 빠지고, 그 탭에서는 사내톡·알림으로 갈 방법이 없어진다.
///
/// **본문 스크롤은 화면이 정한다.** 헤더는 붙어 있고 본문만 굴리는 화면도 있고
/// (홈), 통째로 굴리는 화면도 있다 (일정).
///
/// **헤더는 아이콘 줄뿐이다.** 화면 이름은 `ScreenTitle` 로 본문 **안에** 넣는다 —
/// 굴릴 때 같이 올라가야 한다. 여기서 그리면 붙어 있는 줄이 둘이 된다.
///
/// **제품 고르개(`ProductSwitch`)는 여기 없다 — 홈에만 선다** (대표 결정, 2026-09-11).
/// 탭마다 세우면 업무 화면에서는 제품 알약과 공통/개인 알약이 위아래로 겹쳐 서서,
/// 모양이 같은데 무게가 다른 것이 둘 나란히 보인다.
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

/// 화면 이름 — **본문 맨 위에 넣는다.** 헤더가 아니다
///
/// 굴리면 같이 올라간다. 붙어 있는 것은 아이콘 줄(헤더)뿐이다.
///
/// **홈에는 안 쓴다** — 첫 화면이라 어디인지 물을 일이 없고,
/// 그 자리는 알림 배너가 먼저 차지한다.
struct ScreenTitle: View {
    let text: String

    init(_ text: String) { self.text = text }

    var body: some View {
        Text(text)
            .font(HifisFont.title)
            .foregroundStyle(HifisColor.ink)
            .frame(maxWidth: .infinity, alignment: .leading)
            // 헤더와 붙으면 헤더의 일부처럼 보인다 — 위아래로 띄운다
            .padding(.horizontal, HifisSize.screenEdge)
            .padding(.top, 18)
            .padding(.bottom, 10)
    }
}
