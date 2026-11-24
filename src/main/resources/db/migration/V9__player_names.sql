-- 이름 검색을 한글·영문 모두로: 해외파 영문 이름(+다른 표기), 선수 한글 이름
ALTER TABLE overseas_player
    ADD COLUMN name_en VARCHAR(60),
    ADD COLUMN name_aliases VARCHAR(200);  -- 쉼표로 구분한 다른 영문 표기 (예: Baek Seung-ho)

ALTER TABLE player
    ADD COLUMN korean_name VARCHAR(60),
    ADD COLUMN korean_name_checked_at DATETIME(6),  -- Wikidata 에서 한글 이름을 찾아본 시각 (못 찾아도 기록)
    ADD INDEX idx_player_korean_name (korean_name);

UPDATE overseas_player SET name_en = 'Son Heung-min',   name_aliases = 'Heung-min Son,Sonny' WHERE name = '손흥민';
UPDATE overseas_player SET name_en = 'Kim Min-jae'                                              WHERE name = '김민재';
UPDATE overseas_player SET name_en = 'Lee Kang-in',     name_aliases = 'Kang-in Lee'            WHERE name = '이강인';
UPDATE overseas_player SET name_en = 'Hwang In-beom'                                            WHERE name = '황인범';
UPDATE overseas_player SET name_en = 'Lee Jae-sung'                                             WHERE name = '이재성';
UPDATE overseas_player SET name_en = 'Hwang Hee-chan'                                           WHERE name = '황희찬';
UPDATE overseas_player SET name_en = 'Seol Young-woo',  name_aliases = 'Sul Young-woo'          WHERE name = '설영우';
UPDATE overseas_player SET name_en = 'Jeong Woo-yeong', name_aliases = 'Jung Woo-young'         WHERE name = '정우영';
UPDATE overseas_player SET name_en = 'Oh Hyeon-gyu',    name_aliases = 'Oh Hyun-kyu'            WHERE name = '오현규';
UPDATE overseas_player SET name_en = 'Lee Han-beom'                                             WHERE name = '이한범';
UPDATE overseas_player SET name_en = 'Kim Ji-soo'                                               WHERE name = '김지수';
UPDATE overseas_player SET name_en = 'Cho Gue-sung',    name_aliases = 'Cho Gyu-sung,Jo Gyu-seong' WHERE name = '조규성';
UPDATE overseas_player SET name_en = 'Yang Hyun-jun'                                            WHERE name = '양현준';
UPDATE overseas_player SET name_en = 'Kim Min-su',      name_aliases = 'Kim Min-soo'            WHERE name = '김민수';
UPDATE overseas_player SET name_en = 'Eom Ji-sung',     name_aliases = 'Um Ji-sung'             WHERE name = '엄지성';
UPDATE overseas_player SET name_en = 'Bae Jun-ho',      name_aliases = 'Bae Joon-ho'            WHERE name = '배준호';
UPDATE overseas_player SET name_en = 'Paik Seung-ho',   name_aliases = 'Baek Seung-ho'          WHERE name = '백승호';
UPDATE overseas_player SET name_en = 'Song Min-kyu',    name_aliases = 'Song Min-gyu'           WHERE name = '송민규';
UPDATE overseas_player SET name_en = 'Lee Hyun-ju',     name_aliases = 'Lee Hyeon-ju'           WHERE name = '이현주';
UPDATE overseas_player SET name_en = 'Lee Young-jun',   name_aliases = 'Lee Yeong-jun'          WHERE name = '이영준';
UPDATE overseas_player SET name_en = 'Kwon Hyeok-kyu',  name_aliases = 'Kwon Hyuk-kyu'          WHERE name = '권혁규';
UPDATE overseas_player SET name_en = 'Lee Ho-jae'                                               WHERE name = '이호재';
UPDATE overseas_player SET name_en = 'Jung Sang-bin',   name_aliases = 'Jeong Sang-bin'         WHERE name = '정상빈';
UPDATE overseas_player SET name_en = 'Park Jin-seop',   name_aliases = 'Park Jin-sub'           WHERE name = '박진섭';
UPDATE overseas_player SET name_en = 'Kim Seung-gyu',   name_aliases = 'Kim Seung-kyu'          WHERE name = '김승규';
UPDATE overseas_player SET name_en = 'Oh Se-hun',       name_aliases = 'Oh Se-hoon'             WHERE name = '오세훈';
UPDATE overseas_player SET name_en = 'Cho Yu-min',      name_aliases = 'Jo Yu-min'              WHERE name = '조유민';
