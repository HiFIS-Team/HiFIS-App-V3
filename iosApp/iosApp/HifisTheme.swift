import SwiftUI

/// 화면이 쓰는 색 한 벌 — **안드로이드 `HifisColors` 와 같은 값**이다
///
/// 한쪽만 고치면 두 플랫폼이 갈린다. 값을 바꿀 때는
/// `androidApp/.../ui/theme/Colors.kt` 와 `.claude/DESIGN.md` 를 같이 고친다.
///
/// 다크 대응은 `UIColor` 의 트레잇 클로저로 한다 — 에셋 카탈로그에 색을 넣으면
/// 값이 코드 밖으로 나가서 안드로이드 쪽과 나란히 놓고 비교할 수가 없다.
enum HifisColor {
    /// 화면 바닥
    static let background = dynamic(light: 0xF5_F6_F8, dark: 0x0F_11_16)
    /// 헤더·카드처럼 바닥 위에 올라오는 면
    static let surface = dynamic(light: 0xFF_FF_FF, dark: 0x18_1B_21)
    /// 본문 글자와 아이콘
    static let ink = dynamic(light: 0x15_18_1E, dark: 0xF2_F4_F7)
    /// 보조 설명
    static let inkSecondary = dynamic(light: 0x5A_62_72, dark: 0xA3_AA_B8)
    /// 흐린 값·플레이스홀더
    static let inkTertiary = dynamic(light: 0x94_9B_A9, dark: 0x6B_72_80)
    /// 구분선·테두리
    static let line = dynamic(light: 0xE7_E9_EE, dark: 0x26_2A_33)

    /// 강조 — 화면당 한 곳에만 쓴다.
    ///
    /// 라이트 `#217FE1` 은 `assets/brand/logo.png` **마크 픽셀의 평균색**이다.
    /// 다크 값은 그걸 밝힌 게 아니라, 어두운 면에서 탁해지지 않게 따로 잡았다.
    static let brand = dynamic(light: 0x21_7F_E1, dark: 0x4A_9B_FF)

    /// 안 읽음 점·삭제
    static let danger = dynamic(light: 0xF0_44_52, dark: 0xFF_6B_76)

    private static func dynamic(light: UInt32, dark: UInt32) -> Color {
        Color(UIColor { trait in
            UIColor(rgb: trait.userInterfaceStyle == .dark ? dark : light)
        })
    }
}

/// 화면이 쓰는 크기·간격 — **안드로이드 `Dimens` 와 같은 값**이다
enum HifisSize {
    /// 화면 좌우 여백 — 모든 화면이 같다
    static let screenEdge: CGFloat = 20
    /// 헤더 높이 (상태바 아래)
    static let headerHeight: CGFloat = 56

    /// 헤더 아이콘의 **터치 자리** — 그림보다 크다
    static let headerIconButton: CGFloat = 44
    /// 헤더 아이콘 **그림** 크기
    static let headerIcon: CGFloat = 22

    /// 터치 자리가 그림보다 넓어서 생기는 한쪽 여백 — `(44 - 22) / 2`
    ///
    /// 헤더 줄 여백을 ``screenEdge`` 에서 이만큼 빼야 **그림**이 화면 끝 20 에 선다.
    /// 9 라고 직접 적지 않는 이유는 버튼 크기를 바꿔도 따라오게 하기 위해서다.
    static let headerIconInset = (headerIconButton - headerIcon) / 2

    /// 안 읽음 점 지름 — 숫자는 안 쓴다
    static let badgeDot: CGFloat = 7
    /// 그 점을 두르는 바탕색 테두리
    static let badgeRing: CGFloat = 1.5
}

private extension UIColor {
    convenience init(rgb: UInt32) {
        self.init(
            red: CGFloat((rgb >> 16) & 0xFF) / 255,
            green: CGFloat((rgb >> 8) & 0xFF) / 255,
            blue: CGFloat(rgb & 0xFF) / 255,
            alpha: 1
        )
    }
}
