# 아빠모드

![아빠모드 랜딩 이미지](docs/assets/landing-hero.png)

퇴근길 3분 동안 회사의 긴장을 내려놓고, 집 문 앞에서 따뜻한 아빠로 전환하도록 돕는 Android 앱입니다. 기분 점검, 짧은 호흡, 사랑 한마디, 오늘의 작은 가족 행동을 하나의 루틴으로 이어줍니다.

## 핵심 기능

- 퇴근 전 스트레스와 몸/마음 상태를 빠르게 기록
- 3분 호흡 루틴으로 집에 들어가기 전 마음 정리
- 아내와 아이에게 건넬 따뜻한 첫마디 선택
- 오늘 실천할 작은 아빠 행동 선택 및 완료 기록
- 퇴근 시간 알림과 로컬 기록 히스토리 제공

## 앱 정보

- 패키지명: `com.jeiel85.daddymode`
- 현재 버전: `1.0.1` (`versionCode 2`)
- 플랫폼: Android 7.0 이상
- 데이터: 기기 로컬 Room DB와 SharedPreferences 사용
- 백업 정책: 가족 기록 보호를 위해 앱 데이터 자동 백업 제외

## 빌드

Android Studio에서 프로젝트를 열거나, 로컬 JDK/Android SDK가 준비된 환경에서 다음 명령을 실행합니다.

```powershell
.\gradlew.bat :app:assembleDebug
```

Play 배포용 AAB는 로컬 `.keystore/release-signing.env` 값을 불러온 뒤 생성합니다.

```powershell
$envFile = ".\.keystore\release-signing.env"
Get-Content $envFile | Where-Object { $_ -and -not $_.StartsWith("#") } | ForEach-Object {
  $name, $value = $_ -split "=", 2
  [Environment]::SetEnvironmentVariable($name, $value, "Process")
}
.\gradlew.bat :app:exportReleaseToDesktop --no-daemon
```

결과물은 Windows Desktop의 `Build` 폴더에 `DadMode-v1.0.1-vc2.aab`와 `DadMode-v1.0.1-vc2-release-notes.txt`로 복사됩니다.

## 공개 자료

- GitHub Pages landing page: `docs/index.html`
- Play Console 그래픽: `store-graphics/play-console-current/`
- 앱 아이콘 원본: `assets/dad-mode-icon-source.png`
- 랜딩 이미지 원본: `assets/dad-mode-landing-source.png`

## 릴리즈 체크

```powershell
.\gradlew.bat test assembleDebug
.\gradlew.bat :app:bundleRelease --no-daemon
```
