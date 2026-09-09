# HiFIS-App-V3

[ HiFIS-V3ㅣApp ] 피트니스스타 업무를 HiFIS App 하나로

Kotlin Multiplatform 프로젝트입니다. **비즈니스 로직은 `:shared` 모듈에서 공유하고, UI는 플랫폼별 네이티브**(Android = Jetpack Compose, iOS = SwiftUI)로 작성합니다.

## 구조

```
├── shared/        # KMP 공용 모듈 (commonMain / androidMain / iosMain)
├── androidApp/    # Android 앱 (Jetpack Compose)
├── iosApp/        # iOS 앱 (SwiftUI) — SharedKit.framework 를 링크
└── gradle/
    └── libs.versions.toml   # 버전 카탈로그 (모든 의존성 버전의 단일 출처)
```

`:shared` 는 Android 에 AAR 로, iOS 에 `SharedKit.framework` 로 제공됩니다.

Application ID / Bundle ID 는 양 플랫폼 모두 `app.hifis.hifis` 입니다 (V2 와 동일).
**디버그 빌드만 `app.hifis.hifis.debug` · 이름 `HiFIS Dev`** 로 갈라져 있어, 운영 V2 가
깔린 기기에 개발 빌드를 같이 둘 수 있습니다.

앱 아이콘은 `assets/brand/logo.png` 에서 `python3 tools/icons/gen_app_icon.py` 로 굽습니다.

## 요구 환경

| 항목 | 버전 |
| --- | --- |
| JDK | 21 |
| Gradle | 9.7.1 (wrapper 포함) |
| Kotlin | 2.4.20 |
| AGP | 9.4.0 |
| Android SDK | compileSdk 37 / minSdk 26 |
| Xcode | 26.x (iOS deployment target 16.0) |

## 최초 설정

```sh
brew install openjdk@21
brew install --cask android-commandlinetools
```

셸 환경변수 (`~/.zshrc`):

```sh
export JAVA_HOME="/opt/homebrew/opt/openjdk@21/libexec/openjdk.jdk/Contents/Home"
export ANDROID_HOME="/opt/homebrew/share/android-commandlinetools"
export PATH="$JAVA_HOME/bin:$ANDROID_HOME/platform-tools:$ANDROID_HOME/cmdline-tools/latest/bin:$PATH"
```

`local.properties` 는 커밋되지 않으므로 각자 생성해야 합니다:

```sh
echo "sdk.dir=$ANDROID_HOME" > local.properties
```

## 빌드

```sh
./gradlew build                    # 전체 빌드 + 테스트
./gradlew :shared:allTests         # 공용 모듈 테스트 (Android host + iOS 시뮬레이터)
./gradlew :androidApp:assembleDebug
./gradlew :androidApp:installDebug # 연결된 기기/에뮬레이터에 설치
```

iOS 는 `iosApp/iosApp.xcodeproj` 를 Xcode 로 열어 실행합니다. 빌드 시
`Build SharedKit.framework` 스크립트 단계가 자동으로 Gradle 을 호출해
Kotlin/Native 프레임워크를 생성·링크합니다.

커맨드라인 빌드:

```sh
xcodebuild -project iosApp/iosApp.xcodeproj -scheme iosApp \
  -destination 'platform=iOS Simulator,name=iPhone 17 Pro' build
```

## 의존성 추가

버전은 항상 [`gradle/libs.versions.toml`](gradle/libs.versions.toml) 에 정의하고,
모듈에서는 `libs.` 접근자로 참조합니다. 공용 로직에 쓰이는 라이브러리는
`shared/build.gradle.kts` 의 `commonMain` 에 추가하세요.
