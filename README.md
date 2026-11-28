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
선수 커리어 · 한글 이름은 [Wikidata](https://www.wikidata.org), 해외축구 뉴스는 네이버 뉴스 검색, AI 챗봇은 Google Gemini 를 씁니다.

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
| **이적시장** | 확정 이적 목록 (발표일 · 이적 유형 · 이적료 · 옵션 · 계약 기간), 여름 / 겨울 이적시장 · 리그 · 한국 선수 필터, 이적료순 정렬, 선수 · 팀 검색 |
| **해외파** | 해외 리그에서 뛰는 한국 선수를 세계 지도에 소속 도시별로 표시, 나라 · 도시별 선수 목록 |
| **전술판** | 두 팀 포메이션(12종)을 한 피치에 배치, 선수 · 공 드래그, 패스 · 침투 화살표 · 선 · 펜, 벤치 · 스쿼드 교체, 실행 취소 / 다시 실행, 이미지(PNG) 저장. 왼쪽 패널에서 팀 설정 · 선수 교체, 팀은 영문 · 한글 · 별명으로 검색해서 선택 |
| **해외축구 뉴스** | 네이버 뉴스 검색으로 주제별(해외축구 · 5대 리그 · 챔피언스리그 · 해외파 · 이적시장) 최신 기사, 검색 · 더 보기, 10분 캐시 |
| **회원** | 이메일 가입 · 로그인, 구글 · 카카오 · 애플 소셜 로그인, 로그인 상태 유지, 닉네임 · 비밀번호 변경, 탈퇴 |
| **보안** | JWT + 리프레시 토큰 회전(Redis), 로그인 시도 제한, 비밀번호 규칙(조합 · 연속 문자 · 흔한 단어 · 개인정보 금지) |
| **팀 · 선수 페이지** | 팀: 구단 정보(감독 · 홈구장 · 창단), 시즌 순위 · 최근 5경기, 최근 / 다음 경기, 선수단(포지션 번호 · 나이 · 경기 · 골 · 도움), 팀 내 득점 · 도움 순위 / 선수: 프로필(포지션 번호 · 키 · 포지션 · 국적), 대회별 기록, 커리어(소속팀 이력 · 팀별 경기 · 골 · 국가대표), 최근 이적 |
| **AI 축구 챗봇** | 오른쪽 아래 "AI에게 묻기" 버튼. "오늘 경기", "손흥민 이번 시즌 기록"처럼 물으면 AI가 사이트 DB(일정 · 순위 · 팀 · 선수 · 이적 · 해외파 · 커리어)를 직접 조회해서 답하고, 관련 페이지 바로가기를 붙여 줌 (Google Gemini, 붐비면 자동 재시도 · 예비 모델 전환) |
| **검색** | 헤더 검색창에서 팀 · 선수 검색 (입력 중 미리보기), 결과 페이지 `/search`. 영문 팀도 한글 · 별명(맨유 · 토트넘 · 레알 · 바르사 …)으로, 외국 선수도 한글 이름으로 검색 |
| **디자인** | 붉은끼 도는 블랙 다크 테마 (색은 `styles.css` 맨 위 `:root` 에 모여 있음), 어두운 리그 로고 뒤를 밝게 비춤, 브라우저 탭 아이콘, 페이지별 지연 로딩으로 첫 화면을 가볍게 |

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
| External API | football-data.org v4, Wikidata(SPARQL), 네이버 API 허브(뉴스 검색), Google Gemini API |
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
| **Spring Data JPA** | 엔티티 기반 DB 접근 (회원 · 팀 · 경기 · 게시글 · 해외파 · 이적) |
| **Flyway** | DB 테이블 생성 · 변경 이력 관리 (`db/migration/V1~V11`) |
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
├── config-example/               # 내 컴퓨터 설정 예시 (복사해서 config/application.yaml 로, config/ 는 깃에 안 올라감)
├── .githooks/pre-commit          # 커밋 전 키 · 비밀 값 · 빌드 결과물 검사
├── .github/workflows/            # push 마다 gitleaks 로 키 유출 검사 (.gitleaks.toml 규칙)
├── docs/
│   └── SOCIAL_LOGIN.md           # 소셜 로그인 키 발급 · 설정 방법
├── src/main
│   ├── java/Eojinn/FC1878
│   │   ├── global                # 설정, 보안(JWT · Security), 공통 에러 처리
│   │   ├── member                # 회원가입 · 로그인 · 계정 관리 · 비밀번호 규칙
│   │   ├── oauth                 # 소셜 로그인 (구글 · 카카오 · 애플)
│   │   ├── football              # 리그 · 팀 · 경기 · 순위 조회, 팀 한글 이름 사전(검색용)
│   │   ├── external              # football-data.org 연동 · 자동 동기화
│   │   ├── prediction            # 승부예측 · 정산
│   │   ├── ranking               # 포인트 랭킹 (Redis)
│   │   ├── community             # 게시글 · 댓글
│   │   ├── overseas              # 해외파 선수 목록
│   │   ├── transfer              # 이적시장 (이적 목록 · 기간 · 필터)
│   │   ├── tactic                # 전술판용 팀 스쿼드
│   │   ├── player                # 선수 목록 (스쿼드 하루 1회 동기화), 한글 이름 채우기 (Wikidata)
│   │   ├── search                # 헤더 팀·선수 검색
│   │   ├── team                  # 팀 · 선수 페이지 (프로필 모으기, 팀 상세 12시간 캐시)
│   │   ├── career                # 선수 커리어 (Wikidata 조회, 24시간 캐시)
│   │   ├── news                  # 해외축구 뉴스 (네이버 뉴스 검색, 10분 캐시)
│   │   ├── chat                  # AI 축구 챗봇 (Gemini 함수 호출로 DB 조회, 하루 질문 수 제한)
│   │   └── dev                   # API 키가 없을 때 쓰는 샘플 데이터
│   └── resources
│       ├── application.yaml      # 공통 설정 (키는 넣지 않음)
│       └── db/migration          # Flyway SQL (V1__init.sql …)
└── frontend                      # React + Vite
    ├── public                    # 브라우저 탭 아이콘 (favicon)
    └── src
        ├── api                   # API 호출 · 토큰 자동 갱신
        ├── auth                  # 로그인 상태 관리
        ├── components            # 공통 컴포넌트 (헤더, 엠블럼, 세계 지도, 전술판 피치, AI 챗봇 창, 팀 선택 …)
        ├── hooks                 # 데이터 불러오기, 실행 취소 기록
        ├── pages                 # 화면 (홈, 경기일정, 순위, 이적시장, 해외파, 전술판 …)
        └── utils                 # 포메이션 좌표, 비밀번호 규칙, 표시 형식
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

> 윈도우에서 서버 로그의 한글이 깨지면 실행 전에 `chcp 65001` 을 한 번 입력하세요.

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
  `src/main/resources/application.yaml`에는 `${FOOTBALL_API_KEY:}` 같은 **빈 자리만** 둡니다. (`:` 뒤에 실제 값을 쓰지 않기)
- `config-example/application.yaml` 을 `config/application.yaml` 로 복사한 뒤, **쓰려는 줄만 맨 앞의 `#` 을 지우고** 값을 넣습니다.
  값이 없는 줄의 `#` 은 지우지 마세요. 이 파일의 값은 비어 있어도 기본 설정을 덮어써서, 예를 들어 `jwt.secret` 이 빈 값이 되면 서버가 켜지지 않습니다.
- **커밋 전 검사**: 저장소를 받은 뒤 한 번만 아래 명령을 실행하면, 키·토큰·개인 키가 섞인 커밋을 자동으로 막습니다.

  ```bash
  git config core.hooksPath .githooks
  ```

  막는 것: `config/` · `.env` · `*.p8` 파일, `node_modules` · `build` · `.gradle` · `.idea` · `frontend/dist` 폴더,
  football-data 토큰(16진수 32자리), Google 키(`AIza…`, 새 형식 `AQ.…`), 환경변수 기본값에 적은 비밀 값, 개인 키(PRIVATE KEY).
- **깃허브 검사**: push 할 때마다 GitHub Actions(`.github/workflows/secret-scan.yml`, gitleaks)가 커밋에 키가 섞였는지 한 번 더 검사합니다. 내 컴퓨터의 커밋 전 검사를 건너뛰었어도 깃허브 Actions 탭에 빨간 ✖ 로 알려 줍니다.
- 키가 이미 올라갔다면: ① 키를 먼저 새로 발급(기존 키 폐기) ② `git filter-repo` 로 기록에서 지우고 강제 push. 기록에서 지워도 이미 복사된 곳이 있을 수 있어서 ①이 꼭 필요합니다.
- 애플 로그인 키 파일(`.p8`)도 커밋하지 않습니다.
- `compose.yaml`의 계정 정보는 **로컬 개발 전용** 값입니다.

| 환경변수 | 용도 |
|---|---|
| `FOOTBALL_API_KEY` | football-data.org 토큰 (없으면 샘플 데이터 사용) |
| `GOOGLE_CLIENT_ID` · `GOOGLE_CLIENT_SECRET` | 구글 로그인 |
| `KAKAO_CLIENT_ID` · `KAKAO_CLIENT_SECRET` | 카카오 로그인 |
| `NAVER_CLIENT_ID` · `NAVER_CLIENT_SECRET` | 해외축구 뉴스 (네이버 API 허브) |
| `GEMINI_API_KEY` | AI 축구 챗봇 (Google AI Studio 무료 키, 요즘 키는 `AQ.` 로 시작) · 모델은 `GEMINI_MODEL` 로 변경 가능 |
| `JWT_SECRET` | JWT 서명 키 (운영 서버에서는 꼭 랜덤 값) |
| 애플 로그인 설정 | `docs/SOCIAL_LOGIN.md` 참고 |

```gitignore
### Secrets ###
application-local.yml
application-local.yaml
.env
/config/
*.p8

### Frontend ###
node_modules/
frontend/dist/
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
| 이적시장 (기간·리그·한국 선수·검색·정렬) | GET | `/api/transfers?window=2026-summer&league=프리미어리그&korean=true&q=&sort=fee` | - |
| 해외파 선수 목록 (지도 좌표 포함) | GET | `/api/overseas` | - |
| 팀 스쿼드 (전술판 벤치) | GET | `/api/teams/{id}/squad` | - |
| 팀·선수 검색 (헤더 검색창) | GET | `/api/search?q=아스널&limit=5` | - |
| 팀 페이지 (정보·순위·경기·선수단) | GET | `/api/teams/{id}/profile` | - |
| 선수 페이지 (프로필·대회별 기록) | GET | `/api/players/{id}` | - |
| 선수 커리어 (Wikidata) | GET | `/api/players/{id}/career`, `/api/career?name=손흥민&lang=ko&korea=true` | - |
| AI 챗봇 질문 / 남은 질문 수 | POST / GET | `/api/chat` `{"messages":[{"role":"user","text":"오늘 경기"}]}`, `/api/chat/status` | - (로그인하면 한도 ↑) |
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

### 이적시장

football-data.org 무료 플랜에는 이적 데이터가 없어서, 확정된 이적은 DB(`transfer` 테이블)에서 직접 관리합니다.

- 처음 데이터는 `V6__transfer.sql` 이 넣습니다. (2026-11-23 기준, 한국 선수 9건 + 2026 여름 · 겨울 주요 이적)
- 새 이적은 `transfer` 테이블에 한 줄을 추가하면 됩니다. 발표일(`transfer_date`)을 보고 여름(6~9월) / 겨울(12~2월) 이적시장으로 자동 분류합니다.
- `fee_million_eur` 는 공개 · 보도된 기본 이적료(백만 유로)이고, 옵션 · 완전 이적 조항은 `fee_note` 에 적습니다.
- `type` 값: `PERMANENT`(완전 이적) · `LOAN`(임대) · `FREE`(자유 이적) · `LOAN_RETURN`(임대 복귀)

### 선수 · 검색

- 토큰이 있으면 서버 시작 3분 뒤, 이후 하루마다 대회별 팀 스쿼드를 받아 `player` 테이블에 저장합니다. (요청 사이 8초 간격)
- 헤더 검색은 팀(영문 · 한글 이름)과 선수를 찾습니다. 선수는 스쿼드 · 해외파 · 이적시장에서 함께 찾습니다.
- **한글 · 영문 모두 검색**: 대소문자 · 하이픈 · 띄어쓰기 · 단어 순서를 무시합니다. (예: `minjae kim`, `kimminjae` → 김민재 / `사카` → Bukayo Saka)
  - 해외파는 영문 이름과 다른 표기(예: Baek Seung-ho)를 `overseas_player.name_en`, `name_aliases` 에 둡니다. (`V9`)
  - 외국 선수 한글 이름은 서버 시작 10분 뒤, 이후 6시간마다 Wikidata 에서 300명씩 찾아 `player.korean_name` 에 채웁니다. 같은 이름이 여럿이면 생년월일로 구분하고, 구분이 안 되면 넣지 않습니다. 선수 페이지에서 커리어를 열 때 알게 된 한글 이름도 저장합니다.
- **팀 한글 검색**: football-data.org 는 팀 이름을 영문으로만 줘서, `football/service/TeamKoreanNames.java` 에 5대 리그 · 챔피언스리그 단골 팀(약 140팀)의 한글 이름과 별명을 넣어 두었습니다.
  서버가 켜질 때와 경기 동기화 때 `team.search_names` 에 채워서(`V10`) 검색에만 쓰고, 화면에 보이는 팀 이름은 바꾸지 않습니다. 검색되지 않는 팀은 이 파일에 한 줄 추가하면 됩니다.
- **등번호**: 실제 선수 등번호 대신 **포지션 대표 번호**로 보여 줍니다 (`frontend/src/utils/positionNumber.js`).
  골키퍼 1, 오른쪽 풀백 2, 왼쪽 풀백 3, 센터백 4 · 5, 수비형 미드필더 6, 오른쪽 윙 7, 중앙 미드필더 8, 스트라이커 9, 공격형 미드필더 10, 왼쪽 윙 11.
  전술판은 포메이션 자리마다 번호가 정해지고(예: 4-3-3 → 1 · 3 5 4 2 · 10 6 8 · 11 9 7), 교체로 들어온 선수는 나간 선수 자리 번호를 받습니다.
- 게시글 검색은 커뮤니티 페이지의 검색창으로 옮겼습니다.
- **선수 기록**: 3시간마다 대회별 득점 순위(상위 100명)를 받아 `player_stat` 에 저장합니다. 무료 플랜은 이 순위에 든 선수의 골 · 도움 · 출전 · 페널티 골만 제공해서, 순위 밖 선수는 기록이 비어 있습니다.
- **요청 간격**: 모든 football-data.org 요청은 `FootballDataClient` 에서 최소 6.5초씩 띄워 보냅니다. (분당 10회 제한, 여러 동기화가 동시에 돌아도 안전)
- 팀 페이지의 감독 · 홈구장 같은 구단 정보는 팀마다 12시간 동안 저장해 두고 씁니다.

### 선수 커리어

- 소속팀 이력 · 팀별 경기 · 골 · 국가대표 경력은 [Wikidata](https://www.wikidata.org)(누구나 편집하는 공개 데이터, CC0)에서 가져옵니다. 별도 키는 필요 없습니다.
- 이름으로 후보를 찾은 뒤 **축구 선수 + 생년월일**(DB 선수) 또는 **한국 국적**(해외파 · 이적시장 선수)으로 한 명을 고릅니다. 생년월일이 맞는 사람이 없으면 다른 선수를 보여 주지 않도록 "찾지 못함"으로 표시합니다.
- 같은 선수는 24시간(못 찾은 경우 1시간) 동안 서버 메모리에 저장해 두고 씁니다.
- Wikidata 는 사람이 편집하는 데이터라 경기 · 골 기록이 없는 구간이 있고, 최신 이적이 늦게 반영될 수 있습니다.

### AI 축구 챗봇

- [Google Gemini API](https://aistudio.google.com/) 를 씁니다. AI Studio 에서 무료 키를 받아 `GEMINI_API_KEY`(또는 `config/application.yaml` 의 `app.ai.api-key`)에 넣으면 켜집니다. 키가 없으면 챗봇 창에 "연결 전" 안내만 나옵니다.
- **동작:** 질문 → AI 가 필요한 조회 도구를 고름(예: 선수 검색 → 선수 기록) → 서버가 DB 를 조회해 결과를 돌려줌 → AI 가 그 결과로만 한국어 답변을 씀. 기록을 지어내지 않도록 사실은 반드시 도구 결과로 답하게 지시했습니다.
- 쓸 수 있는 도구(읽기 전용): 팀 · 선수 검색, 팀 정보, 선수 기록, 날짜별 경기(최대 7일), 리그 목록, 순위표, 해외파, 이적, 커리어(Wikidata).
- **무료 한도 관리:** 손님은 IP 당 하루 10번, 회원은 하루 50번 (`app.ai.guest-daily-limit`, `member-daily-limit`). AI 쪽 오류로 답을 못 하면 횟수를 돌려줍니다. Gemini 무료 등급 자체의 분당 · 하루 한도는 AI Studio 에서 확인하세요.
- 키는 AI Studio 에서 받으면 `AQ.` 로 시작하는 새 형식(인증 키)이 나옵니다. 예전 `AIza…` 키도 그대로 쓸 수 있습니다.
- 모델 이름은 `app.ai.model`(기본 `gemini-flash-latest`, 추천 `gemini-3.5-flash-lite`). 서버 로그에 "모델을 찾지 못했어요"가 나오면 AI Studio 에 있는 모델 이름으로 바꾸면 됩니다.
- **붐빌 때**: 무료 등급은 사람이 몰리면 503(모델 붐빔) · 429(한도 초과)가 옵니다. 같은 모델로 1.5초 뒤 한 번 더 보내고, 그래도 안 되면 예비 모델
  `app.ai.fallback-models`(기본 `gemini-3.5-flash-lite, gemini-3.6-flash`)로 넘어갑니다. 한 질문 안에서는 처음 답한 모델을 끝까지 씁니다.
- 응답 형식(Content-Type)이 예상과 달라도 글자로 받은 뒤 JSON 으로 읽고, 못 읽으면 받은 내용 앞부분을 로그에 남깁니다.
- ⚠ Gemini 무료 등급은 보낸 내용이 Google 의 서비스 개선에 쓰일 수 있습니다. 개인정보는 보내지 않습니다(질문과 사이트 공개 데이터만 전송).
- 대화는 브라우저 탭에만 저장되고(sessionStorage), 서버는 대화를 저장하지 않습니다.

### 전술판

- 경기를 고르면 두 팀이 올라오고, 각 팀 스쿼드를 football-data.org 에서 받아와 **포지션별로 11명을 자동 배치**합니다.
  무료 플랜에서는 실제 선발 라인업을 받을 수 없어서, 실제 선발과 다를 수 있습니다. (벤치에서 교체 가능)
- 스쿼드는 분당 요청 제한 때문에 팀마다 12시간 동안 서버 메모리에 저장해 두고 씁니다.
- API 키가 없으면(샘플 데이터) 번호만 있는 선수로 표시되고, 이름은 직접 입력할 수 있습니다.
- 화면 왼쪽 패널에서 팀 설정 · 선수 교체를 하고, 팀은 이름(영문 · 한글 · 별명)으로 검색해서 고릅니다. (↑↓ · Enter 로도 선택)
- 판은 브라우저(localStorage)에 자동 저장되고, `이미지로 저장` 을 누르면 팀 이름 · 포메이션이 들어간 PNG 로 받을 수 있습니다.
- 단축키: `Ctrl+Z` 실행 취소, `Ctrl+Shift+Z` / `Ctrl+Y` 다시 실행, `Esc` 선택 해제

### 소셜 로그인

- 키 발급 · 설정 방법은 `docs/SOCIAL_LOGIN.md` 에 있습니다. 로컬에서 등록할 돌아오는 주소는 `http://localhost:5173/login/oauth2/code/{google|kakao}` 입니다.
- 프론트(5173)를 거쳐 로그인하기 때문에 `frontend/vite.config.js` 에서 로그인 경로(`/oauth2`, `/login/oauth2`)는 `changeOrigin: false` 로 둡니다.
  이걸 빼면 백엔드가 주소를 8080 으로 알아서 카카오 KOE006 · 구글 redirect_uri_mismatch 가 납니다.

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
- [x] 이적시장 (확정 이적 · 필터 · 검색)
- [x] 전술판 (포메이션 · 드래그 · 그리기 · 교체 · 이미지 저장)
- [x] 다크 테마
- [x] 해외축구 뉴스 (네이버 뉴스 검색)
- [x] 팀 · 선수 페이지, 선수 커리어 (Wikidata)
- [x] 한글 · 영문 검색 (선수 한글 이름, 팀 한글 이름 · 별명)
- [x] AI 축구 챗봇 (Gemini)
- [x] 키 유출 방지 (커밋 전 검사 · GitHub Actions gitleaks)
- [ ] 비밀번호 재설정 (이메일) · 이메일 인증
- [ ] 관리자 기능 (해외파 · 이적 · 게시글 관리)
- [ ] 전술판 공유 링크 · 저장 목록 (로그인 사용자)
- [ ] 팀빌더
- [ ] 경기 상세 라인업 · 평점 (무료 데이터 확보 시)
- [ ] Swagger(springdoc-openapi) API 문서화
- [ ] Docker 이미지 빌드 및 AWS 배포
- [ ] CI/CD 파이프라인 및 보안 점검 (OWASP ZAP)

<br/>

## 👤 Author

**Eojin Kim** · [@Eojinn](https://github.com/Eojinn)
