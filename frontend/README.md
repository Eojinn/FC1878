# FC1878 Web (React)

## 실행
```bash
# 1) 백엔드 먼저 실행 (프로젝트 루트에서, Docker Desktop 켜둔 상태)
./gradlew bootRun

# 2) 프론트엔드
cd frontend
npm install
npm run dev        # http://localhost:5173
```
`/api`, `/ws` 요청은 vite 프록시가 `localhost:8080` 으로 넘겨줍니다.

## 테스트 계정 (dev 샘플 데이터)
- `sample1@fc1878.dev` / `password1234!`

## 엠블럼
리그·팀 엠블럼은 백엔드의 `logoUrl`(football-data.org 제공 이미지)을 `<Emblem>` 컴포넌트가 표시합니다.
이미지가 없거나 불러오지 못하면 이름 약자가 들어간 원으로 대체됩니다.
