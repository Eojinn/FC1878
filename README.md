# ⚽ FC1878

> 축구 팬을 위한 올인원 축구 커뮤니티 플랫폼
> *Since 1878 — Glory Glory.*

![Java](https://img.shields.io/badge/Java-21-b30000?style=flat-square&logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.x-6DB33F?style=flat-square&logo=springboot&logoColor=white)
![Gradle](https://img.shields.io/badge/Gradle-Groovy-02303A?style=flat-square&logo=gradle&logoColor=white)
![MySQL](https://img.shields.io/badge/MySQL-4479A1?style=flat-square&logo=mysql&logoColor=white)
![Redis](https://img.shields.io/badge/Redis-DC382D?style=flat-square&logo=redis&logoColor=white)
![Docker](https://img.shields.io/badge/Docker-2496ED?style=flat-square&logo=docker&logoColor=white)
![Status](https://img.shields.io/badge/status-skeleton-lightgrey?style=flat-square)

<br/>

## 📌 프로젝트 소개

**FC1878**은 축구 팬들이 모여 경기 정보, 팀 관리, 커뮤니티 기능을 한곳에서 이용할 수 있는 웹 서비스입니다.
현재는 **Spring Boot 기반 백엔드 뼈대(skeleton)** 를 구성한 초기 단계입니다.

- **Backend** : Spring Boot REST API
- **Frontend** : React + Vite (`frontend/` 폴더, 실행 방법은 `frontend/README.md`)
- **Infra** : Docker · AWS (예정)

<br/>

## 🛠️ 기술 스택

| 구분 | 기술 |
|---|---|
| Language | Java 21 |
| Framework | Spring Boot, Spring Web, Spring Security, Spring Data JPA |
| Database | MySQL |
| Cache | Redis |
| Build | Gradle (Groovy DSL) |
| Config | YAML |
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
| **Spring Web** | REST API 개발 (React 프론트엔드와 통신) |
| **Spring Data JPA** | 엔티티 기반 DB 접근 (회원 · 팀 · 경기 · 게시글) |
| **MySQL Driver** | MySQL 데이터베이스 연결 |
| **Spring Data Redis (Access+Driver)** | 캐시 · 랭킹 · 토큰 저장 |
| **Spring Security** | 인증 · 인가 (로그인, 권한 관리) |
| **Validation** | 요청 데이터 유효성 검사 |
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
├── settings.gradle
├── compose.yaml                  # 로컬 개발용 MySQL · Redis 컨테이너
├── gradle/wrapper/
└── src
    ├── main
    │   ├── java/.../FC1878
    │   │   └── Fc1878Application.java        # 애플리케이션 진입점
    │   └── resources
    │       └── application.yaml              # 공통 설정
    └── test
        └── java/.../FC1878
            ├── Fc1878ApplicationTests.java   # 컨텍스트 로드 테스트
            ├── TestFc1878Application.java    # Testcontainers 실행용
            └── TestcontainersConfiguration.java
```

<br/>

## 🚀 실행 방법

### 사전 준비

- JDK 21
- Docker Desktop (실행 중이어야 함)

### 실행

```bash
# 저장소 클론
git clone https://github.com/Eojinn/FC1878.git
cd FC1878

# 애플리케이션 실행 (compose.yaml의 MySQL · Redis 컨테이너가 자동으로 함께 실행됨)
./gradlew bootRun
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

- DB 비밀번호, JWT 시크릿, AWS 키 등 **민감 정보는 저장소에 커밋하지 않습니다.**
- 민감 정보는 `application-local.yaml` 또는 `.env`로 분리하고 `.gitignore`에 등록합니다.
- `compose.yaml`의 계정 정보는 **로컬 개발 전용** 값입니다.

```gitignore
### Secrets ###
application-local.yaml
.env
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
| 실시간 스코어 (STOMP) | WS | `/ws` → 구독 `/topic/live`, `/topic/fixtures/{id}` | - |

경기 데이터는 [football-data.org](https://www.football-data.org/) 무료 플랜에서 가져옵니다.
(프리미어리그 · 라리가 · 세리에A · 분데스리가 · 리그1 · 챔피언스리그, 현재 시즌, 스코어는 지연 제공)

- 환경변수 `FOOTBALL_API_KEY` 에 football-data.org 토큰을 넣으면 2분마다 경기, 3시간마다 순위가 동기화되고
  종료된 경기는 승부예측이 자동 정산(적중 시 +10P)됩니다.
- 토큰이 없으면 화면 확인용 **샘플 데이터**가 들어갑니다. 샘플에서 실제 데이터로 바꿀 때는 `docker compose down -v` 로 DB를 비우고 다시 실행하세요.

<br/>

## 🗺️ 로드맵

- [x] Spring Boot 프로젝트 뼈대 생성
- [x] GitHub 저장소 연결
- [x] 패키지 구조 설계 (domain / controller / service / repository)
- [x] 회원가입 · 로그인 (Spring Security + JWT)
- [x] 팀 · 경기 · 게시판 도메인 설계
- [ ] 뉴스 · 이적시장 · 해외파 선수 · 팀빌더 · 전술판
- [x] React 프론트엔드 연동
- [ ] Swagger(springdoc-openapi) API 문서화
- [ ] Docker 이미지 빌드 및 AWS 배포
- [ ] CI/CD 파이프라인 및 보안 점검 (OWASP ZAP)

<br/>

## 👤 Author

**Eojin Kim** · [@Eojinn](https://github.com/Eojinn)
