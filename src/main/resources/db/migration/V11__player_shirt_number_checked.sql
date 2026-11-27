-- 등번호 채우기: football-data.org 무료 데이터에는 등번호가 거의 없어서 Wikidata 에서 찾아 넣음. 언제 찾아봤는지 기록
ALTER TABLE player ADD COLUMN shirt_number_checked_at DATETIME(6);
