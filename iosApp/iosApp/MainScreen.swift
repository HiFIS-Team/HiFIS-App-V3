import SwiftUI
import SharedKit

/// 앱 셸 — 하단 탭바와 탭 화면을 들고 있다
///
/// 탭 목록은 `shared` 의 `MainTab` 하나만 읽는다. 여기서 새로 세우지 않는다
/// (V2 는 이 목록이 플랫폼마다 따로라 화면이 목록에서 새어 나갔다).
///
/// 바는 **`TabView` 그대로** 다. iOS 26 부터 시스템이 이 탭바를 리퀴드 글래스로
/// 그려 준다 — 직접 유리를 흉내 내면 OS 가 바뀔 때마다 우리 것만 뒤처진다.
/// 그 아래 버전에서는 예전 탭바로 떨어진다 (최소 지원이 iOS 16 이다).
///
/// 안드로이드는 반대로 머티리얼 3 표준 `NavigationBar` 를 쓴다 —
/// **네비게이션은 각자 자기 OS 표준으로 간다.**
struct MainScreen: View {
    /// 고른 탭 — `MainTab.icon` 은 탭마다 다른 값이라 그대로 표식으로 쓴다
    @State private var selected: String = MainTab.home.icon

    var body: some View {
        TabView(selection: $selected) {
            ForEach(MainTab.companion.all, id: \.icon) { tab in
                screen(for: tab)
                    // 고른 칸은 **속을 채운** 아이콘으로 바꾼다.
                    // `tabItem` 에는 선택 상태용 그림을 따로 주는 자리가 없어서
                    // 고른 값을 보고 그림 자체를 갈아 끼운다
                    .tabItem {
                        Label(
                            tab.label,
                            image: selected == tab.icon ? tab.iconFilled : tab.icon
                        )
                    }
                    .tag(tab.icon)
            }
        }
        .tint(HifisColor.brand)
    }

    @ViewBuilder
    private func screen(for tab: MainTab) -> some View {
        if tab == MainTab.home {
            HomeView()
        } else {
            ComingSoonView(label: tab.label)
        }
    }
}

/// 아직 안 만든 탭 — **임시다**
///
/// 빈 화면으로 두면 탭을 눌렀을 때 앱이 멈춘 것처럼 보인다. 화면이 생기면 지운다.
/// 글꼴·타입 스케일이 정해지기 전이라 크기를 직접 적었다.
private struct ComingSoonView: View {
    let label: String

    var body: some View {
        Text("\(label) — 준비 중")
            .font(.system(size: 15))
            .foregroundStyle(HifisColor.inkTertiary)
            .frame(maxWidth: .infinity, maxHeight: .infinity)
            .background(HifisColor.background.ignoresSafeArea())
    }
}
