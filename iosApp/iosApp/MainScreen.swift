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
        controller.viewControllers = MainTab.companion.all.map { tab in
            let host = UIHostingController(rootView: screen(for: tab))
            host.tabBarItem = UITabBarItem(
                title: tab.label,
                image: UIImage(named: tab.icon),
                selectedImage: UIImage(named: tab.iconFilled)
            )
            return host
        }
        controller.tabBar.tintColor = UIColor(HifisColor.brand)
        return controller
    }

    func updateUIViewController(_ controller: UITabBarController, context: Context) {}

    private func screen(for tab: MainTab) -> AnyView {
        if tab == MainTab.home { return AnyView(HomeView()) }
        if tab == MainTab.schedule { return AnyView(ScheduleView()) }
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
