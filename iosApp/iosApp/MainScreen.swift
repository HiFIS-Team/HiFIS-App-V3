import SwiftUI
import UIKit
import SharedKit

/// 앱 셸 — 하단 탭바와 탭 화면을 들고 있다
///
/// 탭 목록은 `shared` 의 `MainTab` 하나만 읽는다. 여기서 새로 세우지 않는다
/// (V2 는 이 목록이 플랫폼마다 따로라 화면이 목록에서 새어 나갔다).
///
/// 바는 **`UITabBarController` 그대로** 다 — iOS 26 이 이 탭바를 리퀴드 글래스로
/// 그려 준다. 유리를 직접 흉내 내지 않는다.
///
/// 안드로이드는 반대로 머티리얼 3 `NavigationBar` 를 쓴다 —
/// **네비게이션은 각자 자기 OS 표준으로 간다.**
struct MainScreen: View {
    var body: some View {
        // **`ignoresSafeArea()` 를 붙인다.** 안 붙이면 SwiftUI 가 컨테이너를 홈
        // 인디케이터만큼 밀어 올리고 그 안에서 UIKit 이 또 제 여백을 잡아,
        // 바가 25pt 쯤 높이 뜬다 (파일·건강 앱과 대 보고 확인했다).
        //
        // 붙여도 **UIKit 이 보는 안전영역은 그대로 34pt 다** (찍어서 확인했다).
        // 그러니 `additionalSafeAreaInsets` 를 손대면 두 번 빼는 셈이 된다 — 건드리지 않는다.
        MainTabBar().ignoresSafeArea()
    }
}

/// 탭바만 UIKit 이다 — **SwiftUI `TabView` 에는 고른 칸 그림을 줄 자리가 없다**
///
/// `.tabItem` 도 iOS 18 의 `Tab` 도 그림을 하나만 받는다. 그래서 고른 값을 보고
/// 그림을 직접 갈아 끼웠더니, **바뀌는 시점이 어긋났다** — 그림은 누르는 즉시
/// 바뀌는데 색은 유리가 옮겨간 뒤에 따라와서, 그 사이에 "파란 선 아이콘"이라는
/// 어중간한 상태가 보였다.
///
/// UIKit 에는 그 자리가 있다 — `UITabBarItem(title:image:selectedImage:)`.
/// 넘겨 두면 **UIKit 이 제 애니메이션에 맞춰 알아서 바꾼다.** 시점이 어긋날 일이 없다.
///
/// 화면은 그대로 SwiftUI 다 (`UIHostingController`). 바꾼 것은 껍데기뿐이다.
private struct MainTabBar: UIViewControllerRepresentable {
    func makeUIViewController(context: Context) -> UITabBarController {
        let controller = UITabBarController()
        // 전체 목록에서 하단바에 자리가 있는 화면을 누르면 **그 탭으로 옮긴다**.
        // 컨트롤러를 약하게 잡는다 — 화면이 컨트롤러를 되잡으면 둘 다 안 풀린다
        let go: (MainTab) -> Void = { [weak controller] tab in
            guard let controller,
                  let index = MainTab.companion.all.firstIndex(where: { $0 == tab })
            else { return }
            controller.selectedIndex = index
        }
        controller.viewControllers = MainTab.companion.all.map { tab in
            let host = UIHostingController(rootView: screen(for: tab, go: go))
            host.tabBarItem = UITabBarItem(
                title: tab.label,
                image: UIImage(named: tab.icon),
                selectedImage: UIImage(named: tab.iconFilled)
            )
            return host
        }
        controller.tabBar.tintColor = UIColor(HifisColor.brand)
        // **지금은 늘 어둡게 간다.** 이 한 줄이 자식 화면과 탭바까지 다 어둡게 만든다 —
        // 동적 `UIColor` 도 `UITraitCollection.current` 도 여기서 정해진다.
        // 라이트 한 벌은 그대로 두었다. 설정에서 고르게 할 때 `.unspecified` 로 되돌린다
        controller.overrideUserInterfaceStyle = .dark
        addAiChatButton(to: controller)
        return controller
    }

    /// 떠 있는 AI 채팅 단추를 **탭바 컨트롤러에 붙인다**
    ///
    /// 화면(`UIHostingController`) 안에 두지 않는 이유는 둘이다 —
    /// 다섯 탭에 다 떠 있어야 하고, **탭바가 얼마나 높은지는 UIKit 만 안다.**
    /// iOS 26 탭바는 떠 있는 유리 캡슐이라 높이가 상수가 아니다.
    /// `tabBar.topAnchor` 에 걸어 두면 OS 가 캡슐을 어떻게 그리든 그 위에 선다.
    ///
    /// 단추 자체는 그대로 SwiftUI 다 — 안드로이드와 같은 토큰을 쓴다.
    private func addAiChatButton(to controller: UITabBarController) {
        let host = UIHostingController(rootView: AiChatButton {
            // 아직 갈 곳이 없다 — 채팅 화면이 생기면 잇는다
        })
        // 안 비우면 단추 뒤에 네모 바탕이 깔린다
        host.view.backgroundColor = .clear
        controller.addChild(host)
        controller.view.addSubview(host.view)
        host.didMove(toParent: controller)

        host.view.translatesAutoresizingMaskIntoConstraints = false
        NSLayoutConstraint.activate([
            host.view.trailingAnchor.constraint(
                equalTo: controller.view.trailingAnchor,
                constant: -HifisSize.aiChatMargin
            ),
            host.view.bottomAnchor.constraint(
                equalTo: controller.tabBar.topAnchor,
                constant: -HifisSize.aiChatMargin
            ),
            host.view.widthAnchor.constraint(equalToConstant: HifisSize.aiChatButton),
            host.view.heightAnchor.constraint(equalToConstant: HifisSize.aiChatButton),
        ])
    }

    func updateUIViewController(_ controller: UITabBarController, context: Context) {}

    private func screen(for tab: MainTab, go: @escaping (MainTab) -> Void) -> AnyView {
        if tab == MainTab.home { return AnyView(HomeView()) }
        if tab == MainTab.schedule { return AnyView(ScheduleView()) }
        if tab == MainTab.more { return AnyView(MoreView(onTab: go)) }
        return AnyView(ComingSoonView(label: tab.label))
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
