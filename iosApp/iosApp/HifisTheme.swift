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

    /// 잘 돌아간다 — 출근 중
    static let success = dynamic(light: 0x00_C4_71, dark: 0x2F_D9_8D)
    /// 짚어 볼 것 — 지각·조기 퇴근
    static let warning = dynamic(light: 0xFF_9F_0A, dark: 0xFF_B3_40)

    /// 게이지를 채우는 그라데이션 두 끝 — 단색으로 채우면 막대가 납작해 보인다
    static let brandGradientStart = dynamic(light: 0x35_90_E7, dark: 0x5F_A9_FF)
    static let brandGradientEnd = dynamic(light: 0x1A_6C_DD, dark: 0x2F_86_F5)

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

    /// 카드 모서리 — **카드는 전부 이 값이다**
    static let cardRadius: CGFloat = 24
    /// 카드 안쪽 여백
    static let cardPadding: CGFloat = 24

    /// 게이지가 차지하는 줄 높이 — 손잡이가 트랙 밖으로 나오기 때문에 트랙보다 크다
    static let gaugeRow: CGFloat = 18
    /// 게이지 트랙 두께
    static let gaugeTrack: CGFloat = 8
    /// 게이지 손잡이 지름
    static let gaugeThumb: CGFloat = 14

    /// 바로가기 아이콘을 담는 네모 — 누를 수 있어 보이라고 면을 깐다
    static let shortcutChip: CGFloat = 48
    /// 그 네모의 모서리
    static let shortcutChipRadius: CGFloat = 14
    /// 바로가기 아이콘 그림 크기
    static let shortcutIcon: CGFloat = 24
}

/// 글자 크기 한 벌 — **안드로이드 `HifisType` 과 같은 값**이다
///
/// 색은 안 들어 있다. 쓰는 자리에서 토큰으로 준다.
///
/// 글꼴은 **OS 기본**이다 (SF Pro · Apple SD Gothic Neo). Pretendard 를 안 넣는다 —
/// 하단바를 각자 OS 표준으로 둔 것과 같은 결이다.
enum HifisFont {
    /// 시계처럼 크게 세우는 숫자
    static let display = Font.system(size: 40, weight: .bold)
    /// 값 — 스캔 시각처럼 읽어야 하는 것
    static let body = Font.system(size: 16, weight: .semibold)
    /// 머리말·강조 한 줄
    static let label = Font.system(size: 14, weight: .medium)
    /// 곁들이는 글자
    static let caption = Font.system(size: 13, weight: .regular)
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
