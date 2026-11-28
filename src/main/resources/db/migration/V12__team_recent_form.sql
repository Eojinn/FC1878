-- 팀 최근 5경기 승무패 (모든 대회 합쳐서, 왼쪽이 오래된 경기). football-data 순위표의 form 이 비어 있어서 직접 계산
ALTER TABLE team ADD COLUMN recent_form VARCHAR(10);
