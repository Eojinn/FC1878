# ⚽ FC1878

> 축구 팬을 위한 올인원 축구 커뮤니티 플랫폼
> *Since 1878 — Glory Glory.*

![Java](https://img.shields.io/badge/Java-21-b30000?style=flat-square&logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.1-6DB33F?style=flat-square&logo=springboot&logoColor=white)
![Gradle](https://img.shields.io/badge/Gradle-Groovy-02303A?style=flat-square&logo=gradle&logoColor=white)
![React](https://img.shields.io/badge/React-19-61DAFB?style=flat-square&logo=react&logoColor=black)
![Vite](https://img.shields.io/badge/Vite-7-646CFF?style=flat-square&logo=vite&logoColor=white)
![MySQL](https://img.shields.io/badge/MySQL-4479A1?style=flat-square&logo=mysql&logoColor=white)
![Redis](https://img.shields.io/badge/Redis-DC382D?style=flat-square&logo=redis&logoColor=white)
![Docker](https://img.shields.io/badge/Docker-2496ED?style=flat-square&logo=docker&logoColor=white)
![Status](https://img.shields.io/badge/status-in%20development-C8102E?style=flat-square)

<br/>

## 📌 프로젝트 소개

**FC1878**은 축구 팬들이 모여 경기 정보, 승부예측, 커뮤니티 기능을 한곳에서 이용할 수 있는 웹 서비스입니다.
경기일정과 순위는 실제 데이터([football-data.org](https://www.football-data.org/))로 보여주고, 경기 결과에 따라 승부예측 포인트가 자동으로 쌓입니다.

- **Backend** : Spring Boot REST API + WebSocket(STOMP)
- **Frontend** : React + Vite (`frontend/` 폴더, 실행 방법은 `frontend/README.md`)
- **Infra** : Docker Compose(로컬 MySQL · Redis) · AWS (예정)

<br/>

## ✨ 주요 기능

| 기능 | 내용 |
|---|---|
| **경기일정 · 실시간 스코어** | 날짜 · 리그 · 팀별 일정, 진행 중인 경기는 WebSocket으로 스코어 갱신 |
| **순위** | 프리미어리그 · 라리가 · 세리에A · 분데스리가 · 리그1 순위표와 최근 5경기 |
| **승부예측 · 랭킹** | 홈 / 무 / 원정 예측, 경기 종료 후 자동 정산(적중 시 +10P), 포인트 랭킹 |
| **커뮤니티** | 카테고리별 게시글 · 댓글 (자유 · 경기 · 이적 · 해외파 · 전술) |
| **해외파** | 해외 리그에서 뛰는 한국 선수를 세계 지도에 소속 도시별로 표시, 나라 · 도시별 선수 목록 |
| **회원** | 이메일 가입 · 로그인, 구글 · 카카오 · 애플 소셜 로그인, 로그인 상태 유지, 닉네임 · 비밀번호 변경, 탈퇴 |
| **보안** | JWT + 리프레시 토큰 회전(Redis), 로그인 시도 제한, 비밀번호 규칙(조합 · 연속 문자 · 흔한 단어 · 개인정보 금지) |

<br/>

## 🛠️ 기술 스택

| 구분 | 기술 |
|---|---|
| Language | Java 21, JavaScript |
| Backend | Spring Boot 4, Spring Web MVC, Spring Security, OAuth2 Client, Spring Data JPA, WebSocket(STOMP) |
| Frontend | React 19, React Router, Vite, d3-geo · topojson(세계 지도) |
| Database | MySQL, Flyway(DB 마이그레이션) |
| Cache · Token | Redis |
| Auth | JWT (jjwt), HttpOnly 쿠키 리프레시 토큰 |
| External API | football-data.org v4 |
| Build | Gradle (Groovy DSL), npm |
| Test | JUnit 5, Testcontainers |
| DevOps | Docker Compose, Spring Boot Actuator |
| Tools | IntelliJ IDEA, Git, GitHub, Postman |

<br/>

## ⚙️ 프로젝트 초기 설정

[Spring Initializr](https://start.spring.io)로 생성했습니다.

| 항목 | 값 |
|---|---|
| Project | Gradle - Groovy |
| Language | Java |
| Configuration | YAML |
| Java | 21 |
| Packaging | Jar |
| Name | FC1878 |

<br/>

## 📦 의존성 (Dependencies)

| 의존성 | 용도 |
|---|---|
| **Spring Web MVC** | REST API 개발 (React 프론트엔드와 통신) |
| **Spring Data JPA** | 엔티티 기반 DB 접근 (회원 · 팀 · 경기 · 게시글 · 해외파) |
| **Flyway** | DB 테이블 생성 · 변경 이력 관리 (`db/migration/V1~V5`) |
| **MySQL Driver** | MySQL 데이터베이스 연결 |
| **Spring Data Redis** | 리프레시 토큰 · 로그인 시도 횟수 · 소셜 로그인 요청 저장 |
| **Spring Security** | 인증 · 인가 (JWT 필터, 권한 관리) |
| **OAuth2 Client** | 구글 · 카카오 · 애플 소셜 로그인 |
| **WebSocket** | 실시간 스코어 전송 (STOMP) |
| **Validation** | 요청 데이터 유효성 검사 (비밀번호 규칙 포함) |
| **jjwt** | JWT 발급 · 검증 |
| **Lombok** | 보일러플레이트 코드 감소 |
| **Spring Boot DevTools** | 개발 중 자동 재시작 |
| **Spring Boot Actuator** | 헬스 체크 · 모니터링 |
| **Docker Compose Support** | 로컬 개발 시 MySQL · Redis 컨테이너 자동 실행 |
| **Testcontainers** | 실제 DB 컨테이너 기반 통합 테스트 |

<br/>

## 📁 프로젝트 구조

```text
FC1878
├── build.gradle                  # 빌드 설정 및 의존성
├── compose.yaml                  # 로컬 개발용 MySQL · Redis 컨테이너
├── docs/
│   └── SOCIAL_LOGIN.md           # 소셜 로그인 키 발급 · 설정 방법
├── src/main
│   ├── java/Eojinn/FC1878
│   │   ├── global                # 설정, 보안(JWT · Security), 공통 에러 처리
│   │   ├── member                # 회원가입 · 로그인 · 계정 관리 · 비밀번호 규칙
│   │   ├── oauth                 # 소셜 로그인 (구글 · 카카오 · 애플)
│   │   ├── football              # 리그 · 팀 · 경기 · 순위 조회
│   │   ├── external              # football-data.org 연동 · 자동 동기화
│   │   ├── prediction            # 승부예측 · 정산
│   │   ├── ranking               # 포인트 랭킹 (Redis)
│   │   ├── community             # 게시글 · 댓글
│   │   ├── overseas              # 해외파 선수 목록
│   │   └── dev                   # API 키가 없을 때 쓰는 샘플 데이터
│   └── resources
│       ├── application.yaml      # 공통 설정 (키는 넣지 않음)
│       └── db/migration          # Flyway SQL (V1__init.sql …)
└── frontend                      # React + Vite
    └── src
        ├── api                   # API 호출 · 토큰 자동 갱신
        ├── auth                  # 로그인 상태 관리
        ├── components            # 공통 컴포넌트 (헤더, 엠블럼, 세계 지도 …)
        ├── pages                 # 화면 (홈, 경기일정, 순위, 해외파 …)
        └── utils
```

<br/>

## 🚀 실행 방법

### 사전 준비

- JDK 21
- Node.js 20 이상
- Docker Desktop (실행 중이어야 함)

### 실행

```bash
# 저장소 클론
git clone https://github.com/Eojinn/FC1878.git
cd FC1878

# 1) 백엔드 (compose.yaml의 MySQL · Redis 컨테이너가 자동으로 함께 실행됨)
./gradlew bootRun

# 2) 프론트엔드 (새 터미널)
cd frontend
npm install        # 처음 받았을 때, 또는 package.json이 바뀌었을 때
npm run dev        # http://localhost:5173
```

### 테스트

```bash
./gradlew test
```

### 헬스 체크

```bash
curl http://localhost:8080/actuator/health
```

<br/>

## 🔐 보안 및 설정 관리

- DB 비밀번호, JWT 시크릿, API 키, 소셜 로그인 키 등 **민감 정보는 저장소에 커밋하지 않습니다.**
- 키는 **환경변수** 또는 **`config/application.yaml`**(프로젝트 루트, gitignore 처리)에 넣습니다.
  `src/main/resources/application.yaml`에는 `${FOOTBALL_API_KEY:}` 같은 자리만 있습니다.
- 애플 로그인 키 파일(`.p8`)도 커밋하지 않습니다.
- `compose.yaml`의 계정 정보는 **로컬 개발 전용** 값입니다.

| 환경변수 | 용도 |
|---|---|
| `FOOTBALL_API_KEY` | football-data.org 토큰 (없으면 샘플 데이터 사용) |
| `GOOGLE_CLIENT_ID` · `GOOGLE_CLIENT_SECRET` | 구글 로그인 |
| `KAKAO_CLIENT_ID` · `KAKAO_CLIENT_SECRET` | 카카오 로그인 |
| 애플 로그인 설정 | `docs/SOCIAL_LOGIN.md` 참고 |

```gitignore
### Secrets ###
application-local.yml
application-local.yaml
.env
/config/
*.p8
```

<br/>

## 📝 커밋 컨벤션

| 타입 | 설명 |
|---|---|
| `feat` | 새로운 기능 추가 |
| `fix` | 버그 수정 |
| `refactor` | 기능 변화 없는 구조 개선 |
| `docs` | 문서 수정 |
| `test` | 테스트 코드 추가 · 수정 |
| `chore` | 빌드 · 설정 등 기타 변경 |

```text
예) feat: 회원가입 API 추가
```

<br/>

## 🔌 API 목록 (buildup-football.com 기능 대응)

| 기능 | Method | URL | 인증 |
|---|---|---|---|
| 회원가입 / 로그인(JWT) | POST | `/api/auth/signup`, `/api/auth/login` | - |
| 로그인 자동 연장 / 로그아웃 | POST | `/api/auth/refresh`, `/api/auth/logout` (리프레시 토큰은 HttpOnly 쿠키) | - |
| 소셜 로그인 (구글·카카오·애플) | GET | `/oauth2/authorization/{google\|kakao\|apple}` → 끝나면 프론트 `/oauth/callback` | - |
| 사용 가능한 소셜 로그인 목록 | GET | `/api/auth/providers` | - |
| 내 정보 / 좋아하는 팀 팔로우 | GET / PUT | `/api/members/me`, `/api/members/me/favorite-team` | ✅ |
| 닉네임 · 비밀번호 변경 | PATCH | `/api/members/me/nickname`, `/api/members/me/password` | ✅ |
| 회원 탈퇴 (비밀번호 확인) | POST | `/api/members/me/withdraw` | ✅ |
| 리그 목록 | GET | `/api/leagues` | - |
| 경기일정 (날짜·리그·팀 필터) | GET | `/api/fixtures?date=2026-11-22&leagueId=1&teamId=3` | - |
| 경기 상세 | GET | `/api/fixtures/{id}` | - |
| 순위표 | GET | `/api/leagues/{id}/standings?season=2026` | - |
| 승부예측 현황 (홈/무/원정 %) | GET | `/api/fixtures/{id}/predictions` | - |
| 승부예측 참여 | POST | `/api/fixtures/{id}/predictions` `{"pick":"HOME"}` | ✅ |
| 포인트 랭킹 / 내 순위 | GET | `/api/rankings`, `/api/members/me/rank` | - / ✅ |
| 커뮤니티 글 | GET/POST/PUT/DELETE | `/api/posts`, `/api/posts/{id}` | 쓰기 ✅ |
| 댓글 | GET/POST/DELETE | `/api/posts/{id}/comments`, `/api/comments/{id}` | 쓰기 ✅ |
| 해외파 선수 목록 (지도 좌표 포함) | GET | `/api/overseas` | - |
| 실시간 스코어 (STOMP) | WS | `/ws` → 구독 `/topic/live`, `/topic/fixtures/{id}` | - |

<br/>

## 📊 데이터

### 경기 · 순위

경기 데이터는 [football-data.org](https://www.football-data.org/) 무료 플랜에서 가져옵니다.
(프리미어리그 · 라리가 · 세리에A · 분데스리가 · 리그1 · 챔피언스리그, 현재 시즌, 스코어는 지연 제공)

- 환경변수 `FOOTBALL_API_KEY` 에 football-data.org 토큰을 넣으면 2분마다 경기, 3시간마다 순위가 동기화되고
  종료된 경기는 승부예측이 자동 정산(적중 시 +10P)됩니다.
- 토큰이 없으면 화면 확인용 **샘플 데이터**가 들어갑니다. 샘플에서 실제 데이터로 바꿀 때는 `docker compose down -v` 로 DB를 비우고 다시 실행하세요.

### 해외파

football-data.org 무료 플랜에는 MLS · 스코틀랜드 · 일본 리그 등이 없어서, 해외파 목록은 DB(`overseas_player` 테이블)에서 직접 관리합니다.

- 처음 데이터는 `V5__overseas_player.sql` 이 넣습니다. (2026-11-23 기준, 27명)
- 이적이 생기면 해당 선수의 `club` · `league` · `city` · `latitude` · `longitude` 를 수정하면 지도와 목록에 바로 반영됩니다.
- 화면의 "기준일"은 목록에서 가장 최근에 수정된 날짜입니다.

<br/>

## 🗺️ 로드맵

- [x] Spring Boot 프로젝트 뼈대 생성
- [x] GitHub 저장소 연결
- [x] 패키지 구조 설계 (domain / controller / service / repository)
- [x] 회원가입 · 로그인 (Spring Security + JWT)
- [x] 팀 · 경기 · 게시판 도메인 설계
- [x] React 프론트엔드 연동
- [x] 실제 경기 · 순위 데이터 연동 (football-data.org)
- [x] 로그인 상태 유지 · 소셜 로그인 · 로그인 시도 제한 · 비밀번호 규칙
- [x] 해외파 선수 세계 지도
- [ ] 비밀번호 재설정 (이메일) · 이메일 인증
- [ ] 관리자 기능 (해외파 · 게시글 관리)
- [ ] 뉴스 · 이적시장 · 팀빌더 · 전술판
- [ ] Swagger(springdoc-openapi) API 문서화
- [ ] Docker 이미지 빌드 및 AWS 배포
- [ ] CI/CD 파이프라인 및 보안 점검 (OWASP ZAP)

<br/>

## 👤 Author

**Eojin Kim** · [@Eojinn](https://github.com/Eojinn)
