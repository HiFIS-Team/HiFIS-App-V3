import SwiftUI
import SharedKit

/// AI 채팅 — **아래에서 올라오는 페이지**
///
/// 탭바의 AI 동그라미를 누르면 탭이 바뀌는 대신 이 화면이 통째로 올라온다
/// (`MainScreen.swift` 의 `shouldSelectTab`). 탭을 옮기면 지금 보던 화면을 잃는데,
/// AI 는 **하던 일을 두고 잠깐 묻는 자리**라 덮고 올라왔다가 닫히는 편이 맞다.
///
/// ## 이 화면만 **밝다**
///
/// 앱은 다크로 못 박아 두었지만(`DESIGN.md`) 여기는 예외다. 대화 화면은 글자가
/// 길게 이어져서 밝은 바탕이 읽기 편하고, **덮고 올라오는 딴 자리**라 앱 색이
/// 끊겨도 어색하지 않다. 밝기는 `overrideUserInterfaceStyle = .light` 로
/// 올라오는 컨트롤러에서 못 박는다 — 그러면 `HifisColor` 가 알아서 라이트 값을 낸다.
///
/// 값은 아직 자리 표시자다 — **AI 도 서버도 안 붙였다.** 보내기는 아무 일도 안 한다.
struct AiChatView: View {
    let onClose: () -> Void

    @State private var message = ""
    /// 글자가 **다 올라온 뒤에** 하나씩 든다 — 아래 `Entrance` 참고
    @State private var shown = false

    var body: some View {
        ZStack(alignment: .bottom) {
            HifisColor.surface.ignoresSafeArea()
            glow
            content
        }
        .onAppear {
            // 페이지가 올라오는 동안은 가만히 둔다. 같이 움직이면 둘이 겹쳐 어지럽다
            DispatchQueue.main.asyncAfter(deadline: .now() + Self.settle) { shown = true }
        }
    }

    /// 바닥에서 옅게 번지는 빛 — **브랜드색 한 가지로만** 낸다
    ///
    /// 참고한 화면은 여러 색을 섞었는데, 우리는 강조를 브랜드 파랑 하나로 쓰기로 했다.
    /// 여기서 색을 더 풀면 그 규칙이 이 화면부터 무너진다.
    private var glow: some View {
        LinearGradient(
            colors: [HifisColor.brand.opacity(0), HifisColor.brand.opacity(0.14)],
            startPoint: .top,
            endPoint: .bottom
        )
        .frame(height: 420)
        .frame(maxHeight: .infinity, alignment: .bottom)
        .ignoresSafeArea()
        .allowsHitTesting(false)
    }

    private var content: some View {
        VStack(alignment: .leading, spacing: 0) {
            header
            // **글은 헤더 밑에 붙는다.** 빈 자리는 보기와 입력칸 사이로 내린다 —
            // 물음이 화면 한가운데에 있으면 눈이 먼저 닿는 자리가 아니다
            Spacer().frame(height: 20)
            brandRow.modifier(Entrance(step: 0, shown: shown))
            Spacer().frame(height: 14)
            Text(AiPrompt.companion.TITLE)
                .font(.system(size: 26, weight: .bold))
                .foregroundStyle(HifisColor.ink)
                .modifier(Entrance(step: 1, shown: shown))
            Spacer().frame(height: 28)
            prompts
            Spacer(minLength: 24)
            inputBar.modifier(Entrance(step: 2 + AiPrompt.companion.all.count, shown: shown))
        }
        .padding(.horizontal, HifisSize.screenEdge)
        .padding(.bottom, 12)
        .frame(maxWidth: .infinity, alignment: .leading)
    }

    /// 이 화면의 아이콘 줄 — **왼쪽 닫기, 오른쪽 설정**
    ///
    /// 탭 화면의 `AppHeader` 를 안 쓴다. 여기는 덮고 올라온 딴 자리라
    /// 지점·검색·알림으로 갈 일이 없다 — 나갈 문과 이 화면의 설정뿐이다.
    private var header: some View {
        HStack(spacing: 0) {
            iconButton("ic_close", label: "닫기", action: onClose)
                .padding(.leading, -HifisSize.headerIconInset)
            Spacer(minLength: 0)
            iconButton("ic_settings", label: "AI 설정") {
                // 아직 갈 곳이 없다 — AI 설정 화면이 생기면 잇는다
            }
            .padding(.trailing, -HifisSize.headerIconInset)
        }
        .padding(.top, 4)
    }

    /// 헤더 아이콘 하나 — 그림이 화면 끝 `screenEdge` 에 서야 한다
    ///
    /// 터치 자리(44)가 그림(22)보다 넓어서 **가운데 정렬**을 하고, 부르는 쪽이
    /// 그 차이만큼 바깥으로 당긴다. 탭 화면 헤더가 하는 계산과 같다 —
    /// `.leading` 으로 두면 그림이 터치 자리 끝에 붙어 화면 끝에서 11 만큼 더 나간다.
    private func iconButton(
        _ icon: String,
        label: String,
        action: @escaping () -> Void
    ) -> some View {
        Button(action: action) {
            Image(icon)
                .renderingMode(.template)
                .resizable()
                .frame(width: HifisSize.headerIcon, height: HifisSize.headerIcon)
                .foregroundStyle(HifisColor.ink)
                .frame(width: HifisSize.headerIconButton, height: HifisSize.headerIconButton)
                .contentShape(Rectangle())
        }
        .buttonStyle(TapStyle())
        .accessibilityLabel(label)
    }

    /// 마크 + 이름 — 마크는 **제 그라데이션 그대로** 선다 (탭바 동그라미와 같은 그림)
    private var brandRow: some View {
        HStack(spacing: 8) {
            Image("brand_mark")
                .resizable()
                .scaledToFit()
                .frame(width: 26)
            Text(AiPrompt.companion.BRAND)
                .font(.system(size: 15, weight: .semibold))
                .foregroundStyle(HifisColor.inkSecondary)
        }
    }

    /// 말 걸기 보기 — 빈 칸만 두면 무엇을 물어도 되는지 몰라서 아무도 안 쓴다
    private var prompts: some View {
        VStack(alignment: .leading, spacing: 0) {
            ForEach(Array(AiPrompt.companion.all.enumerated()), id: \.element.icon) { index, prompt in
                // **빈 자리는 안 눌린다.** 남는 폭을 `Spacer` 가 밖에서 채워서
                // 단추는 아이콘과 글자만큼만 넓다 — 줄 전체를 누르는 자리로 두면
                // 글 옆 빈 곳을 스쳐도 입력칸이 채워진다
                HStack(spacing: 0) {
                    Button { message = prompt.label } label: {
                        HStack(spacing: 14) {
                            Image(prompt.icon)
                                .renderingMode(.template)
                                .resizable()
                                .frame(width: HifisSize.moreIcon, height: HifisSize.moreIcon)
                                .foregroundStyle(HifisColor.ink)
                            Text(prompt.label)
                                .font(.system(size: 17, weight: .medium))
                                .foregroundStyle(HifisColor.ink)
                        }
                        .frame(height: HifisSize.moreRow)
                        .contentShape(Rectangle())
                    }
                    .buttonStyle(TapStyle())
                    Spacer(minLength: 0)
                }
                .modifier(Entrance(step: 2 + index, shown: shown))
            }
        }
    }

    private var inputBar: some View {
        HStack(spacing: 10) {
            TextField(AiPrompt.companion.PLACEHOLDER, text: $message)
                .font(.system(size: 16))
                .foregroundStyle(HifisColor.ink)
                .textFieldStyle(.plain)
                .submitLabel(.send)

            sendButton
        }
        .padding(.leading, 20)
        .padding(.trailing, 6)
        .padding(.vertical, 6)
        .modifier(GlassCapsule())
    }

    /// 보내기 — **글자가 들어오면 파란 동그라미가 튀어 들어온다**
    ///
    /// V2 사내톡 입력칸이 하던 움직임을 그대로 옮겼다 (거기는 Flutter 라
    /// `AnimatedSwitcher` + `ScaleTransition` 이었다). 두 벌이 **갈아 끼워지면서**
    /// 들어오는 쪽은 커지며 나타나고 나가는 쪽은 작아지며 사라진다.
    ///
    /// 들어올 때 **1을 살짝 넘겼다 돌아온다** — V2 의 `easeOutBack` 이 그 느낌이었다.
    /// 스프링의 감쇠를 낮춰 같은 것을 낸다. 그냥 색만 바꾸면 눌러도 되는 때가 왔다는 것이
    /// 눈에 안 띈다.
    ///
    /// 비었을 때는 **동그라미를 안 그린다** (V2 도 그랬다). 회색 동그라미를 두면
    /// 죽은 단추가 하나 놓인 것처럼 보인다.
    private var sendButton: some View {
        ZStack {
            if message.isEmpty {
                Image("ic_send")
                    .renderingMode(.template)
                    .resizable()
                    .frame(width: Self.sendIcon, height: Self.sendIcon)
                    .foregroundStyle(HifisColor.inkTertiary)
                    .frame(width: Self.sendButton, height: Self.sendButton)
                    .transition(.scale.combined(with: .opacity))
            } else {
                Button {
                    // 아직 보낼 곳이 없다 — AI 도 서버도 안 붙였다
                } label: {
                    Image("ic_send_fill")
                        .renderingMode(.template)
                        .resizable()
                        .frame(width: Self.sendIcon, height: Self.sendIcon)
                        .foregroundStyle(.white)
                        .frame(width: Self.sendButton, height: Self.sendButton)
                        .background(HifisColor.brand, in: Circle())
                        .contentShape(Circle())
                }
                .buttonStyle(TapStyle())
                .accessibilityLabel("보내기")
                .transition(.scale.combined(with: .opacity))
            }
        }
        .frame(width: Self.sendButton, height: Self.sendButton)
        .animation(.spring(response: 0.28, dampingFraction: 0.58), value: message.isEmpty)
    }

    /// 보내기 동그라미와 그 안의 비행기 — V2 와 같은 값이다 (38 / 20)
    private static let sendButton: CGFloat = 38
    private static let sendIcon: CGFloat = 20

    /// 글자가 다 올라온 뒤 **드는 데 걸리는 시간** — 페이지 올라오는 것과 안 겹치게
    private static let settle = 0.22
}

/// 하나씩 **떠오르듯 든다** — 한 번만, 순서대로
///
/// 다 같이 나타나면 화면이 한 번에 꽉 차서 어디를 봐야 할지 모른다.
/// 마크 → 물음 → 보기 넷 → 입력칸 순으로 조금씩 늦춰 눈이 따라가게 한다.
private struct Entrance: ViewModifier {
    let step: Int
    let shown: Bool

    /// 한 칸 늦추는 간격. 너무 길면 굼떠 보이고 짧으면 순서가 안 읽힌다
    private static let stagger = 0.06

    func body(content: Content) -> some View {
        content
            .opacity(shown ? 1 : 0)
            // 아래에서 올라오며 초점이 잡힌다 — 흐림이 있어야 '떠오르는' 느낌이 난다
            .offset(y: shown ? 0 : 16)
            .blur(radius: shown ? 0 : 5)
            .animation(
                .spring(response: 0.5, dampingFraction: 0.82)
                    .delay(Double(step) * Self.stagger),
                value: shown
            )
    }
}

/// 유리 알약 — **입력칸을 담는다**
///
/// 유리는 iOS 에서만 진짜라 화면 부품에는 안 쓰기로 했지만, 이 화면은 **iOS 에만 있다.**
/// 안드로이드가 흉내낼 자리가 아예 없어서 그 걱정이 붙지 않는다.
///
/// 바닥의 빛 위에 떠 있어서 유리가 그 색을 받아 낸다 — 색 면으로 깔면 빛이 끊긴다.
/// 배포 하한이 16.0 이라 옛 기기에는 유리가 없다. `.ultraThinMaterial` 로 내려간다.
private struct GlassCapsule: ViewModifier {
    func body(content: Content) -> some View {
        if #available(iOS 26.0, *) {
            content.glassEffect(.regular, in: .capsule)
        } else {
            content
                .background(.ultraThinMaterial, in: Capsule())
                .overlay(Capsule().strokeBorder(HifisColor.line, lineWidth: 1))
        }
    }
}
