import SwiftUI
import SharedKit

/// 오늘 근무 카드 — 홈 첫 장. 실시간 시계 + 서버가 판정한 오늘 근태
///
/// V2 의 `_HeroStatusCard` 를 그대로 옮겼다 (`home_status.dart`).
/// 짜임과 간격은 V2 그대로고, **색과 글꼴만 V3 토큰**이다.
/// 안드로이드 `TodayWorkCard.kt` 와 같은 카드다 — 한쪽만 고치면 갈린다.
///
/// 진행률 계산은 `shared` 의 `TodayWork.rateAt` 이 한다.
struct TodayWorkCard: View {
    let work: TodayWork

    var body: some View {
        // 매초 다시 그린다. **다른 탭으로 가면 이 화면이 사라져 자동으로 멈춘다**
        TimelineView(.periodic(from: .now, by: 1)) { context in
            card(now: context.date)
        }
    }

    private func card(now: Date) -> some View {
        let parts = Calendar.current.dateComponents([.hour, .minute, .second], from: now)
        let hour = parts.hour ?? 0
        let minute = parts.minute ?? 0
        let second = parts.second ?? 0
        let rate = work.rateAt(nowMinutes: Int32(hour * 60 + minute))

        return VStack(spacing: 0) {
            HStack {
                Text("오늘 근무")
                    .font(HifisFont.label)
                    .foregroundStyle(HifisColor.inkSecondary)
                Spacer()
                StatusBadge(label: work.status.label, color: HifisColor.tone(work.status.tone))
            }

            Spacer().frame(height: 16)

            Text(String(format: "%02d:%02d:%02d", hour, minute, second))
                .font(HifisFont.display)
                .monospacedDigit()
                .foregroundStyle(HifisColor.ink)
                .frame(maxWidth: .infinity)

            Spacer().frame(height: 20)

            WorkGauge(rate: CGFloat(rate))

            Spacer().frame(height: 10)

            // 시작 — 진행률 — 종료
            HStack {
                Text(work.shiftStartText)
                    .font(HifisFont.caption)
                    .foregroundStyle(HifisColor.inkTertiary)
                Spacer()
                Text("\(Int(rate * 100))%")
                    .font(.system(size: 14, weight: .bold))
                    .foregroundStyle(HifisColor.brand)
                Spacer()
                Text(work.shiftEndText)
                    .font(HifisFont.caption)
                    .foregroundStyle(HifisColor.inkTertiary)
            }

            Spacer().frame(height: 18)

            // 실제 출퇴근 스캔 기록
            HStack {
                ScanRecord(label: "출근", time: work.checkInText)
                Spacer()
                ScanRecord(label: "퇴근", time: work.checkOutText)
            }
        }
        .padding(HifisSize.cardPadding)
        .background(HifisColor.surface)
        .clipShape(RoundedRectangle(cornerRadius: HifisSize.cardRadius, style: .continuous))
        .overlay(
            RoundedRectangle(cornerRadius: HifisSize.cardRadius, style: .continuous)
                .strokeBorder(HifisColor.line, lineWidth: 1)
        )
        .shadow(color: .black.opacity(0.04), radius: 12, y: 6)
    }

}

private struct StatusBadge: View {
    let label: String
    let color: Color

    var body: some View {
        Text(label)
            .font(.system(size: 13, weight: .semibold))
            .foregroundStyle(color)
            .padding(.horizontal, 12)
            .padding(.vertical, 6)
            .background(color.opacity(0.12), in: Capsule())
    }
}

/// 스캔 기록 한 짝 — 안 찍힌 시각은 흐리게 둔다
private struct ScanRecord: View {
    let label: String
    let time: String

    var body: some View {
        HStack(spacing: 8) {
            Text(label)
                .font(HifisFont.caption)
                .foregroundStyle(HifisColor.inkTertiary)
            Text(time)
                .font(HifisFont.body)
                .monospacedDigit()
                .foregroundStyle(
                    time == TodayWork.companion.NO_TIME
                        ? HifisColor.inkTertiary
                        : HifisColor.ink
                )
        }
    }
}

/// 근무 진행 게이지 — 트랙 + 채움 + 지금 자리를 짚는 손잡이
private struct WorkGauge: View {
    let rate: CGFloat

    var body: some View {
        GeometryReader { proxy in
            let full = proxy.size.width
            // 손잡이가 양 끝에서 트랙 밖으로 안 나가게 지름만큼 뺀 거리 위를 움직인다
            let thumbX = (full - HifisSize.gaugeThumb) * rate

            ZStack(alignment: .leading) {
                Capsule()
                    .fill(HifisColor.line)
                    .frame(height: HifisSize.gaugeTrack)
                Capsule()
                    .fill(
                        LinearGradient(
                            colors: [HifisColor.brandGradientStart, HifisColor.brandGradientEnd],
                            startPoint: .leading,
                            endPoint: .trailing
                        )
                    )
                    .frame(width: full * rate, height: HifisSize.gaugeTrack)
                Circle()
                    .fill(HifisColor.surface)
                    .frame(width: HifisSize.gaugeThumb, height: HifisSize.gaugeThumb)
                    .overlay(Circle().strokeBorder(HifisColor.brand, lineWidth: 3))
                    .shadow(color: .black.opacity(0.2), radius: 3, y: 2)
                    .offset(x: thumbX)
            }
            .frame(height: HifisSize.gaugeRow)
        }
        .frame(height: HifisSize.gaugeRow)
    }
}
