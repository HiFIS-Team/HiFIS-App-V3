import SwiftUI
import UIKit
import SharedKit

/// 지금 어느 제품에 들어와 있나 — **셸이 들고 화면은 읽기만 한다**
///
/// 화면마다 `product` 를 손으로 넘기지 않는다. 제품은 홈·업무·일정이 신경 쓸 것이
/// 아니라 **그 화면들을 담고 있는 껍데기의 상태**다.
///
/// 그리고 iOS 는 넘길 수도 없다 — 탭 화면은 `UIHostingController` 안에 한 번
/// 만들어져 앉아 있어서, 생성 때 박아 넣은 값은 나중에 바뀌어도 안 따라온다.
/// 관찰되는 객체를 하나 심어 두면 값이 바뀔 때 화면이 알아서 다시 그린다.
/// (안드로이드는 `LocalProduct` — 같은 생각을 각자 OS 방식으로 한다.)
final class ShellState: ObservableObject {
    @Published private(set) var product: Product = Product.companion.default_

    /// 지금 보고 있는 지점 — **nil 이면 전 지점** (`Branch.ALL`)
    ///
    /// **제품보다 오래 산다.** 제품을 옮겨도 보던 지점은 그대로다 — 지점은 제품이
    /// 아니라 *어느 센터를 보고 있나*라서다. 화면들은 이 값을 읽기만 한다
    @Published private(set) var branch: String?

    func pick(branch id: String?) { branch = id }

    /// 고르개가 쓰는 차례 — `Product` 를 index 로 바꿔 들고 있는다
    var index: Int {
        get { Product.companion.all.firstIndex(of: product) ?? 0 }
        set { move(to: Product.companion.all[min(max(newValue, 0), Product.companion.all.count - 1)]) }
    }

    /// 전환 직전 화면을 얼려 둔 그림 — **이게 흐려지면서 새 셸이 드러난다**
    ///
    /// 셸을 통째로 `opacity` 로 녹이면 **리퀴드 글래스가 사라진다.** 유리는 뒤에 있는 것을
    /// 퍼 와서 흐리는 효과라, 반투명한 겹 안에서는 그릴 수가 없다 — 그 동안 아이콘과
    /// 글자만 남았다가 다 녹으면 유리가 툭 생겼다 (대표가 봤다, 2026-09-11).
    ///
    /// 그래서 **새 셸은 처음부터 온전히 서고**(유리도 제대로 선다), 얼려 둔 **그림**만
    /// 그 위에서 흐려진다. 그림은 이미 그려진 픽셀이라 반투명해도 문제가 없다.
    @Published private(set) var frozen: UIImage?

    func move(to next: Product) {
        guard next != product else { return }
        frozen = Self.snapshot()
        product = next
        // 새 셸이 한 번 그려진 **뒤에** 얼린 그림을 걷는다 — 같은 틱에 걷으면 겹칠 새가 없다
        DispatchQueue.main.async { [weak self] in
            withAnimation(.easeInOut(duration: Self.fade)) { self?.frozen = nil }
        }
    }

    /// 지금 화면을 그대로 한 장 뜬다 — 유리까지 그려진 상태로 굳는다
    private static func snapshot() -> UIImage? {
        let window = UIApplication.shared.connectedScenes
            .compactMap { $0 as? UIWindowScene }
            .flatMap(\.windows)
            .first { $0.isKeyWindow }
        guard let window else { return nil }
        return UIGraphicsImageRenderer(bounds: window.bounds).image { _ in
            // `afterScreenUpdates: false` — **지금 떠 있는 그대로** 뜬다.
            // true 로 두면 이미 바뀐 뒤를 떠서 얼릴 것이 없다
            window.drawHierarchy(in: window.bounds, afterScreenUpdates: false)
        }
    }

    /// 얼린 그림이 흐려지는 데 걸리는 시간 — 안드로이드 `SHELL_FADE` 와 같은 값 (320ms)
    ///
    /// **색을 따로 물들이지 않는다.** 두 겹이 겹쳐 보이는 동안 색도 저절로 옮겨 간다.
    static let fade: CFTimeInterval = 0.32
}

/// 이 겹의 제품과 브랜드색을 아래로 내려보내는 껍데기
///
/// **제품 하나가 색 한 벌이다.** 셸이 통째로 녹아드니 색을 따로 움직일 일이 없다 —
/// 나가는 겹은 제 색 그대로 흐려지고 들어오는 겹이 제 색으로 짙어진다.
struct ShellScope<Content: View>: View {
    /// **이 겹이 그리는 제품** — 셸에서 읽지 않는다. 녹아드는 동안 겹마다 다르다
    let product: Product
    @ViewBuilder var content: () -> Content

    var body: some View {
        let palette = HifisBrand.palette(product)
        content()
            .environment(\.product, product)
            .environment(\.brand, palette.brand)
            .environment(\.brandGradientStart, palette.start)
            .environment(\.brandGradientEnd, palette.end)
    }
}

/// 제품 고르개 — **헤더 바로 아래**, 탭 화면 전부에 선다
///
/// 칸 고르개(공통/개인 · 전체/안읽음)와 **같은 부품**이다 (대표 요청, 2026-09-11).
/// 트레이너는 출근을 HiFIS 에서 찍고 수업은 TeamFIS 에서 해서 하루에 여러 번 오간다 —
/// 한 탭에만 두면 그 탭을 거쳐 가야 한다.
///
/// 제품이 바뀌면 **탭바까지 통째로 바뀐다.** 그래서 탭바 안이 아니라 그 위에 선다.
///
/// ## 알약이 안 미끄러진다 (`slides: false`)
///
/// 제품을 옮기면 셸이 통째로 새로 서고, **이 고르개도 같이 새로 선다.**
/// 새로 선 알약에는 미끄러져 올 자리가 없어서 그냥 제자리에 나타난다.
///
/// 그런데 TeamFIS ↔ WeFIS 는 둘 다 자리 화면이라 화면이 안 바뀌어서 **거기서만
/// 미끄러졌다.** 셋 중 하나만 다르게 움직이니 그게 더 이상하게 보였다
/// (대표가 봤다 — "하이피스랑 팀피스는 딱딱 바로, 팀피스랑 위피스는 자연스럽고").
///
/// 그래서 **셋 다 즉시 옮긴다.** 앱이 통째로 갈리는 자리라 알약만 천천히 따라가는 것도
/// 어차피 맞지 않는다. 미끄러지는 것은 화면이 그대로 있는 칸 고르개(전체/안읽음 ·
/// 공통/개인)의 몫이다.
struct ProductSwitch: View {
    @EnvironmentObject private var shell: ShellState
    /// **보여주는 것은 이 겹의 제품이다** — 나가는 겹은 나가는 제품을 고른 채로 흐려진다
    @Environment(\.product) private var product

    var body: some View {
        ModeSwitch(
            segments: Product.companion.labels,
            // **여기서 애니메이션을 걸지 않는다.** 셸은 즉시 갈리고,
            // 얼려 둔 그림이 그 위에서 흐려진다 (`ShellState.frozen`)
            selected: Binding(
                get: { Product.companion.all.firstIndex(of: product) ?? 0 },
                set: { shell.index = $0 }
            ),
            slides: false
        )
    }
}
