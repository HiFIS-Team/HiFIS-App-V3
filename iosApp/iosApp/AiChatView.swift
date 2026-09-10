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

    var body: some View {
        ZStack(alignment: .bottom) {
            HifisColor.surface.ignoresSafeArea()
            glow
            content
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
            closeButton
            Spacer(minLength: 0)
            brandRow
            Spacer().frame(height: 14)
            Text(AiPrompt.companion.TITLE)
                .font(.system(size: 26, weight: .bold))
                .foregroundStyle(HifisColor.ink)
            Spacer().frame(height: 28)
            prompts
            Spacer().frame(height: 24)
            inputBar
        }
        .padding(.horizontal, HifisSize.screenEdge)
        .padding(.bottom, 12)
        .frame(maxWidth: .infinity, alignment: .leading)
    }

    private var closeButton: some View {
        Button(action: onClose) {
            Image("ic_close")
                .renderingMode(.template)
                .resizable()
                .frame(width: HifisSize.headerIcon, height: HifisSize.headerIcon)
                .foregroundStyle(HifisColor.ink)
                .frame(
                    width: HifisSize.headerIconButton,
                    height: HifisSize.headerIconButton,
                    alignment: .leading
                )
                .contentShape(Rectangle())
        }
        .buttonStyle(TapStyle())
        .accessibilityLabel("닫기")
        // 터치 자리가 그림보다 넓어서 생기는 여백만큼 왼쪽으로 당긴다 — 헤더와 같은 셈이다
        .padding(.leading, -HifisSize.headerIconInset)
        .padding(.top, 8)
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
            ForEach(AiPrompt.companion.all, id: \.icon) { prompt in
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
                        Spacer(minLength: 0)
                    }
                    .frame(height: HifisSize.moreRow)
                    .frame(maxWidth: .infinity, alignment: .leading)
                    .contentShape(Rectangle())
                }
                .buttonStyle(TapStyle())
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

            Button {
                // 아직 보낼 곳이 없다 — AI 도 서버도 안 붙였다
            } label: {
                Image("ic_send")
                    .renderingMode(.template)
                    .resizable()
                    .frame(width: 20, height: 20)
                    // 글자가 없으면 눌러도 할 일이 없다 — 죽여 둔다
                    .foregroundStyle(message.isEmpty ? HifisColor.inkTertiary : .white)
                    .frame(width: 38, height: 38)
                    .background(message.isEmpty ? HifisColor.line : HifisColor.brand, in: Circle())
            }
            .buttonStyle(TapStyle())
            .disabled(message.isEmpty)
            .accessibilityLabel("보내기")
        }
        .padding(.leading, 20)
        .padding(.trailing, 6)
        .padding(.vertical, 6)
        .background(HifisColor.surface, in: Capsule())
        .overlay(Capsule().strokeBorder(HifisColor.line, lineWidth: 1))
    }
}
