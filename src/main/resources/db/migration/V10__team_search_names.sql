-- 팀 검색용 한글 이름·별명 (예: 맨체스터 유나이티드 | 맨유). 화면 표시는 그대로, 검색에만 씀
ALTER TABLE team ADD COLUMN search_names VARCHAR(500);
