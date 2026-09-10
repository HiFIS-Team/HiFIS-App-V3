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
            // **iOS 하단바 목록을 본다** — 근태는 여기 없다 (홈 바로가기로 내려갔다).
            // 없는 탭이면 아무 일도 안 한다. 화면이 생기면 그때 잇는다
            guard let controller,
                  let index = MainTab.companion.ios.firstIndex(where: { $0 == tab })
            else { return }
            controller.selectedIndex = index
        }
        if #available(iOS 18.0, *) {
            // **`tabs` 로 세운다.** `viewControllers` 로는 아래 AI 자리를 못 만든다.
            // 화면의 `tabBarItem` 은 그대로 둔다 — 고른 칸의 채운 그림이 거기 있다
            var tabs: [UITab] = MainTab.companion.ios.map { tab in
                UITab(title: tab.label, image: UIImage(named: tab.icon), identifier: tab.name) { _ in
                    hosted(tab, go: go)
                }
            }

            // **AI 는 바가 그려 준다.** `UISearchTab` 은 바 밖에 따로 서는 자리라
            // 시스템이 탭바와 **같은 유리로** 동그라미를 그린다 —
            // 우리가 유리를 흉내내지 않는다. 아이콘·제목은 갈아 끼울 수 있다.
            // 검색 필드로 펼쳐지는 것은 화면에 `searchable` 을 붙였을 때뿐이라,
            // 안 붙이면 그냥 그 화면으로 간다.
            let ai = UISearchTab { _ in
                UIHostingController(rootView: ComingSoonView(label: "AI 채팅"))
            }
            ai.title = "AI"
            // **FS 마크를 제 색 그대로 세운다.** 탭바는 그림을 기본으로 template 처리해서
            // 한 가지 색으로 눌러 버린다 — `alwaysOriginal` 이라야 그라데이션이 산다.
            // 동그라미(유리)는 시스템이 그리니 우리가 댈 것은 이 그림뿐이다
            ai.image = UIImage(named: "brand_mark")?.withRenderingMode(.alwaysOriginal)
            tabs.append(ai)

            controller.tabs = tabs
        } else {
            // iOS 17 이하에는 그 자리가 없다 — 다섯 칸만 세운다
            controller.viewControllers = MainTab.companion.ios.map { hosted($0, go: go) }
        }

        controller.tabBar.tintColor = UIColor(HifisColor.brand)
        // **지금은 늘 어둡게 간다.** 이 한 줄이 자식 화면과 탭바까지 다 어둡게 만든다 —
        // 동적 `UIColor` 도 `UITraitCollection.current` 도 여기서 정해진다.
        // 라이트 한 벌은 그대로 두었다. 설정에서 고르게 할 때 `.unspecified` 로 되돌린다
        controller.overrideUserInterfaceStyle = .dark
        return controller
    }

    func updateUIViewController(_ controller: UITabBarController, context: Context) {}

    /// 탭 하나를 담는 화면 — 고른 칸에 **채운 그림**을 쓰도록 `tabBarItem` 을 같이 심는다
    private func hosted(_ tab: MainTab, go: @escaping (MainTab) -> Void) -> UIHostingController<AnyView> {
        let host = UIHostingController(rootView: screen(for: tab, go: go))
        host.tabBarItem = UITabBarItem(
            title: tab.label,
            image: UIImage(named: tab.icon),
            selectedImage: UIImage(named: tab.iconFilled)
        )
        return host
    }

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
        // **`TabPage` 를 쓴다.** 헤더와 AI 단추가 거기 있어서, 안 쓰면 이 두 탭에서만
        // 사내톡·알림으로 갈 방법도 AI 단추도 사라진다
        TabPage {
            Text("\(label) — 준비 중")
                .font(.system(size: 15))
                .foregroundStyle(HifisColor.inkTertiary)
                .frame(maxWidth: .infinity, maxHeight: .infinity)
        }
    }
}
