# 소셜 로그인 설정 가이드 (구글 · 카카오 · 애플)

키를 넣은 서비스만 로그인 화면에 버튼이 나타납니다. 키가 하나도 없으면 이메일 로그인만 보입니다.
**키는 절대 `src/main/resources/application.yaml` 에 직접 쓰지 마세요.** 아래 "키 넣는 곳"을 참고하세요.

## 공통: 돌아오는 주소(Redirect URI)

| 환경 | 주소 형식 |
|---|---|
| 로컬 개발 (Vite 5173 경유) | `http://localhost:5173/login/oauth2/code/{google\|kakao}` |
| 배포 | `https://내도메인/login/oauth2/code/{google\|kakao\|apple}` |

> 애플은 `localhost`와 `http` 주소를 허용하지 않아서 **로컬에서는 테스트할 수 없어요.** https 도메인에 배포하거나 ngrok 같은 https 터널을 써야 합니다.

## 키 넣는 곳

프로젝트 루트(`build.gradle` 옆)에 `config/application.yaml` 을 만들면 Spring Boot 가 자동으로 읽습니다.
`.gitignore` 에 `/config/` 와 `*.p8` 이 들어 있어서 GitHub 에 올라가지 않습니다.

```yaml
app:
  oauth:
    google:
      client-id: 구글_클라이언트_ID
      client-secret: 구글_클라이언트_보안_비밀번호
    kakao:
      client-id: 카카오_REST_API_키
      client-secret: 카카오_클라이언트_시크릿
      # scope: profile_nickname,account_email   # 이메일 동의 항목을 설정했을 때만
    apple:
      client-id: com.example.fc1878.web        # Services ID
      team-id: ABCDE12345
      key-id: XYZ987654
      private-key-path: C:/Users/User/keys/AuthKey_XYZ987654.p8
```

환경변수로 넣어도 됩니다: `GOOGLE_CLIENT_ID`, `GOOGLE_CLIENT_SECRET`, `KAKAO_CLIENT_ID`, `KAKAO_CLIENT_SECRET`, `KAKAO_SCOPE`,
`APPLE_CLIENT_ID`, `APPLE_TEAM_ID`, `APPLE_KEY_ID`, `APPLE_PRIVATE_KEY_PATH`.

---

## 1. 카카오

1. [Kakao Developers](https://developers.kakao.com) → 내 애플리케이션 → 애플리케이션 추가
2. **카카오 로그인** 사용 설정 켜기
3. **앱 → 플랫폼 키 → REST API 키** 에서
   - 리다이렉트 URI 등록: `http://localhost:5173/login/oauth2/code/kakao`
   - 클라이언트 시크릿 코드 확인 (활성화 상태여야 함)
4. **동의항목**에서 `닉네임` 설정
   - 이메일까지 받으려면 `카카오계정(이메일)` 동의항목이 필요합니다(비즈 앱 전환이 필요할 수 있음). 설정했다면 `scope: profile_nickname,account_email`
5. `client-id` = REST API 키, `client-secret` = 클라이언트 시크릿

메뉴 위치는 카카오 콘솔 개편에 따라 바뀔 수 있어요. `KOE006` 에러는 리다이렉트 URI 가 등록값과 다를 때 납니다.

## 2. 구글

1. [Google Cloud Console](https://console.cloud.google.com) → 프로젝트 만들기
2. **OAuth 동의 화면(Google 인증 플랫폼)** 설정: 앱 이름, 지원 이메일, 범위 `email`, `profile`, `openid`
   - 게시 전(테스트) 상태에서는 "테스트 사용자"로 등록한 계정만 로그인할 수 있어요.
3. **사용자 인증 정보 → OAuth 클라이언트 ID 만들기 → 웹 애플리케이션**
   - 승인된 리디렉션 URI: `http://localhost:5173/login/oauth2/code/google`
4. 발급된 클라이언트 ID / 클라이언트 보안 비밀번호를 넣기

## 3. 애플

> **Apple Developer Program 유료 멤버십이 필요**하고, **https 도메인**에서만 동작합니다.

1. [Apple Developer](https://developer.apple.com/account) → Certificates, Identifiers & Profiles
2. **Identifiers → App IDs**: 앱 ID 를 만들고 *Sign in with Apple* 체크
3. **Identifiers → Services IDs**: 새로 만들기 (예: `com.example.fc1878.web`) → 이 값이 `client-id`
   - *Sign in with Apple* 체크 → Configure → Primary App ID 선택
   - Domains: `내도메인`, Return URLs: `https://내도메인/login/oauth2/code/apple`
4. **Keys**: 새 키 만들기 → *Sign in with Apple* 체크 → `.p8` 파일 다운로드 (**한 번만 받을 수 있음**)
   - Key ID → `key-id`, 오른쪽 위 Team ID → `team-id`, `.p8` 경로 → `private-key-path`
5. 애플이 요구하는 client secret(JWT)은 서버가 `.p8` 로 자동 생성·갱신합니다.

---

## 동작 방식

1. 프론트의 버튼이 `/oauth2/authorization/{서비스}` 로 이동
2. 각 서비스 로그인·동의 → `/login/oauth2/code/{서비스}` 로 돌아옴
3. 서버가 회원을 찾거나 만듦
   - 이미 연결된 소셜 계정 → 그 회원
   - 서비스가 확인해 준 이메일이 기존 회원 이메일과 같음 → 그 회원에 연결 (이메일 가입자가 구글로 로그인하는 경우)
   - 그 외 → 새 회원 (비밀번호 없는 소셜 전용 계정)
4. 로그인 유지 쿠키를 주고 프론트 `/oauth/callback` 으로 보냄 → 프론트가 쿠키로 토큰을 받아 로그인 완료

소셜 전용 계정은 내 정보에서 비밀번호 변경이 숨겨지고, 탈퇴할 때 비밀번호 대신 동의 체크만 받습니다.
