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

    /// 지금 칠할 브랜드색 — 제품을 옮기면 **서서히 이쪽으로 온다**
    @Published private(set) var brand: Color = HifisColor.brand
    @Published private(set) var brandStart: Color = HifisColor.brandGradientStart
    @Published private(set) var brandEnd: Color = HifisColor.brandGradientEnd

    /// 고르개가 쓰는 차례 — `Product` 를 index 로 바꿔 들고 있는다
    var index: Int {
        get { Product.companion.all.firstIndex(of: product) ?? 0 }
        set { move(to: Product.companion.all[min(max(newValue, 0), Product.companion.all.count - 1)]) }
    }

    // ── 브랜드색 물들이기 ──

    private var link: CADisplayLink?
    private var from: (RGB, RGB, RGB) = (.zero, .zero, .zero)
    private var to: (RGB, RGB, RGB) = (.zero, .zero, .zero)
    private var began: CFTimeInterval = 0

    /// 제품을 옮긴다 — 색은 [fade] 초에 걸쳐 따라온다
    ///
    /// **셸이 통째로 새로 서는데도 색은 이어진다.** 이 객체가 셸 바깥에 살아남아
    /// 매 프레임 보간한 값을 내주기 때문이다. 새로 선 화면은 그 중간값을 읽는다 —
    /// 안 그러면 색만 툭 갈려서 앱이 튄 것처럼 보인다.
    /// (안드로이드는 `HifisTheme` 이 셸 바깥에서 `animateColorAsState` 로 한다.)
    func move(to next: Product) {
        guard next != product else { return }
        from = (rgb(brand), rgb(brandStart), rgb(brandEnd))
        let palette = HifisBrand.palette(next)
        to = (rgb(palette.brand), rgb(palette.start), rgb(palette.end))
        product = next

        link?.invalidate()
        began = CACurrentMediaTime()
        let link = CADisplayLink(target: self, selector: #selector(step))
        // 굴리는 중에도 돌아야 한다 — 기본 모드만 주면 스크롤할 때 멈춘다
        link.add(to: .main, forMode: .common)
        self.link = link
    }

    @objc private func step() {
        let t = min((CACurrentMediaTime() - began) / Self.fade, 1)
        // easeOut — 앞이 빠르고 끝이 느긋하다 (알약·잎과 같은 결)
        let e = 1 - pow(1 - t, 3)
        brand = mix(from.0, to.0, e)
        brandStart = mix(from.1, to.1, e)
        brandEnd = mix(from.2, to.2, e)
        if t >= 1 {
            link?.invalidate()
            link = nil
        }
    }

    /// 브랜드색이 옮겨 가는 데 걸리는 시간 — 안드로이드 `BRAND_FADE` 와 같은 값
    private static let fade: CFTimeInterval = 0.42
}

/// 색을 섞으려면 숫자가 있어야 한다 — `Color` 는 성분을 안 내준다
private struct RGB {
    var r: CGFloat = 0, g: CGFloat = 0, b: CGFloat = 0
    static let zero = RGB()
}

/// **다크로 풀어서 잰다.** 앱이 늘 어둡게 가므로 그 값이 화면에 실제로 뜨는 색이다
private func rgb(_ color: Color) -> RGB {
    let resolved = UIColor(color).resolvedColor(with: UITraitCollection(userInterfaceStyle: .dark))
    var out = RGB()
    var a: CGFloat = 0
    resolved.getRed(&out.r, green: &out.g, blue: &out.b, alpha: &a)
    return out
}

private func mix(_ a: RGB, _ b: RGB, _ t: Double) -> Color {
    Color(
        red: a.r + (b.r - a.r) * t,
        green: a.g + (b.g - a.g) * t,
        blue: a.b + (b.b - a.b) * t
    )
}

/// 브랜드색을 아래로 내려보내는 껍데기 — **셸 상태를 지켜보다 바뀔 때마다 다시 심는다**
///
/// 탭 화면은 `UIHostingController` 안에 따로 서 있어서, 만들 때 심은 값은 안 따라온다.
/// 이 껍데기가 셸을 지켜보다가 색이 바뀔 때마다 환경값을 새로 내려 준다.
struct BrandScope<Content: View>: View {
    @EnvironmentObject private var shell: ShellState
    @ViewBuilder var content: () -> Content

    var body: some View {
        content()
            .environment(\.brand, shell.brand)
            .environment(\.brandGradientStart, shell.brandStart)
            .environment(\.brandGradientEnd, shell.brandEnd)
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

    var body: some View {
        ModeSwitch(
            segments: Product.companion.labels,
            selected: Binding(get: { shell.index }, set: { shell.index = $0 }),
            slides: false
        )
    }
}
