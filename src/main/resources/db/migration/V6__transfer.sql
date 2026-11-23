-- 이적시장: 확정된 이적 (구단 공식 발표 기준)
CREATE TABLE transfer (
    id               BIGINT AUTO_INCREMENT PRIMARY KEY,
    player_name      VARCHAR(40)  NOT NULL,
    player_name_en   VARCHAR(60),
    nationality      VARCHAR(30),
    korean           BIT(1)       NOT NULL DEFAULT 0,
    position         VARCHAR(2)   NOT NULL,
    from_club        VARCHAR(60)  NOT NULL,
    from_league      VARCHAR(40),
    to_club          VARCHAR(60)  NOT NULL,
    to_league        VARCHAR(40),
    type             VARCHAR(12)  NOT NULL,
    fee_million_eur  DECIMAL(7,2),
    fee_note         VARCHAR(80),
    transfer_date    DATE         NOT NULL,
    contract_until   DATE,
    created_at       DATETIME(6)  NOT NULL,
    updated_at       DATETIME(6)  NOT NULL,
    INDEX idx_transfer_date (transfer_date)
);

-- 2026-11-23 기준. 이적료는 공개·보도된 기본 이적료(백만 유로), 옵션은 fee_note 에 적음
INSERT INTO transfer (player_name, player_name_en, nationality, korean, position, from_club, from_league, to_club, to_league,
                      type, fee_million_eur, fee_note, transfer_date, contract_until, created_at, updated_at)
VALUES
-- 한국 선수
('황희찬', 'Hwang Hee-chan', '대한민국', 1, 'FW', '울버햄튼', 'EFL 챔피언십', '샬케 04', '분데스리가', 'LOAN', NULL, '완전 이적 옵션 €3M', '2026-09-02', '2027-06-30', NOW(6), NOW(6)),
('송민규', 'Song Min-kyu', '대한민국', 1, 'FW', 'FC 서울', 'K리그1', 'CD 나시오날', '프리메이라 리가', 'FREE', NULL, 'FC 서울이 이적료 없이 보내줌', '2026-08-30', NULL, NOW(6), NOW(6)),
('설영우', 'Seol Young-woo', '대한민국', 1, 'DF', '츠르베나 즈베즈다', '세르비아 수페르리가', '아우크스부르크', '분데스리가', 'PERMANENT', 4.00, '옵션 포함 최대 €5.5M', '2026-08-27', '2030-06-30', NOW(6), NOW(6)),
('김민수', 'Kim Min-su', '대한민국', 1, 'MF', '지로나', '라리가2', '레인저스', '스코티시 프리미어십', 'PERMANENT', 10.00, '바이아웃 조항 발동', '2026-08-27', NULL, NOW(6), NOW(6)),
('이한범', 'Lee Han-beom', '대한민국', 1, 'DF', '미트윌란', '수페르리가', '클뤼프 브뤼허', '주필러 프로리그', 'PERMANENT', 9.00, '옵션 포함 최대 €10.5M', '2026-08-03', NULL, NOW(6), NOW(6)),
('이강인', 'Lee Kang-in', '대한민국', 1, 'MF', '파리 생제르맹', '리그1', '아틀레티코 마드리드', '라리가', 'PERMANENT', 35.00, '옵션 포함 최대 €40M', '2026-07-25', '2031-06-30', NOW(6), NOW(6)),
('황인범', 'Hwang In-beom', '대한민국', 1, 'MF', '페예노르트', '에레디비시', 'FC 포르투', '프리메이라 리가', 'PERMANENT', 4.50, '성과 옵션 €0.5M', '2026-07-21', NULL, NOW(6), NOW(6)),
('오현규', 'Oh Hyeon-gyu', '대한민국', 1, 'FW', '헹크', '주필러 프로리그', '베식타시', '쉬페르리그', 'PERMANENT', 14.00, '옵션 포함 최대 €15M', '2026-02-05', '2029-06-30', NOW(6), NOW(6)),
('박진섭', 'Park Jin-seop', '대한민국', 1, 'MF', '전북 현대', 'K리그1', '저장 FC', '중국 슈퍼리그', 'PERMANENT', 1.00, NULL, '2026-01-03', NULL, NOW(6), NOW(6)),
-- 2026 여름 주요 이적 (이적료 순)
('엔소 페르난데스', 'Enzo Fernández', '아르헨티나', 0, 'MF', '첼시', '프리미어리그', '맨체스터 시티', '프리미어리그', 'PERMANENT', 145.00, NULL, '2026-09-01', NULL, NOW(6), NOW(6)),
('모건 로저스', 'Morgan Rogers', '잉글랜드', 0, 'MF', '애스턴 빌라', '프리미어리그', '첼시', '프리미어리그', 'PERMANENT', 138.00, NULL, '2026-07-21', NULL, NOW(6), NOW(6)),
('엘리엇 앤더슨', 'Elliot Anderson', '잉글랜드', 0, 'MF', '노팅엄 포레스트', '프리미어리그', '맨체스터 시티', '프리미어리그', 'PERMANENT', 135.00, '구단 역대 최고 이적료', '2026-07-23', '2031-06-30', NOW(6), NOW(6)),
('브래들리 바르콜라', 'Bradley Barcola', '프랑스', 0, 'FW', '파리 생제르맹', '리그1', '리버풀', '프리미어리그', 'PERMANENT', 125.00, NULL, '2026-08-31', NULL, NOW(6), NOW(6)),
('얀 디오망데', 'Yan Diomande', '코트디부아르', 0, 'FW', 'RB 라이프치히', '분데스리가', '레알 마드리드', '라리가', 'PERMANENT', 125.00, NULL, '2026-08-06', NULL, NOW(6), NOW(6)),
('산드로 토날리', 'Sandro Tonali', '이탈리아', 0, 'MF', '뉴캐슬', '프리미어리그', '토트넘', '프리미어리그', 'PERMANENT', 108.00, NULL, '2026-07-06', NULL, NOW(6), NOW(6)),
('마테우스 페르난드스', 'Mateus Fernandes', '포르투갈', 0, 'MF', '웨스트햄', '프리미어리그', '토트넘', '프리미어리그', 'PERMANENT', 99.00, NULL, '2026-07-02', NULL, NOW(6), NOW(6)),
('아유브 부아디', 'Ayyoub Bouaddi', NULL, 0, 'MF', '릴', '리그1', '맨체스터 시티', '프리미어리그', 'PERMANENT', 95.00, NULL, '2026-08-26', NULL, NOW(6), NOW(6)),
('사비뉴', 'Savinho', '브라질', 0, 'FW', '맨체스터 시티', '프리미어리그', '토트넘', '프리미어리그', 'PERMANENT', 88.00, NULL, '2026-08-25', NULL, NOW(6), NOW(6)),
('브루누 기마랑이스', 'Bruno Guimarães', '브라질', 0, 'MF', '뉴캐슬', '프리미어리그', '아스널', '프리미어리그', 'PERMANENT', 88.00, NULL, '2026-08-08', NULL, NOW(6), NOW(6)),
('앤서니 고든', 'Anthony Gordon', '잉글랜드', 0, 'FW', '뉴캐슬', '프리미어리그', '바르셀로나', '라리가', 'PERMANENT', 80.00, NULL, '2026-07-01', NULL, NOW(6), NOW(6)),
('카를로스 발레바', 'Carlos Baleba', '카메룬', 0, 'MF', '브라이튼', '프리미어리그', '맨체스터 유나이티드', '프리미어리그', 'PERMANENT', 76.00, NULL, '2026-08-25', NULL, NOW(6), NOW(6)),
('곤살루 하무스', 'Gonçalo Ramos', '포르투갈', 0, 'FW', '파리 생제르맹', '리그1', 'AC 밀란', '세리에A', 'PERMANENT', 74.00, NULL, '2026-06-30', NULL, NOW(6), NOW(6)),
('가브리에우 마르티넬리', 'Gabriel Martinelli', '브라질', 0, 'FW', '아스널', '프리미어리그', '알 힐랄', '사우디 프로리그', 'PERMANENT', 70.00, NULL, '2026-09-03', NULL, NOW(6), NOW(6)),
('제레미 자케', 'Jérémy Jacquet', '프랑스', 0, 'DF', '렌', '리그1', '리버풀', '프리미어리그', 'PERMANENT', 64.00, NULL, '2026-07-01', NULL, NOW(6), NOW(6)),
('로드리', 'Rodri', '스페인', 0, 'MF', '맨체스터 시티', '프리미어리그', '바르셀로나', '라리가', 'PERMANENT', 60.00, NULL, '2026-08-18', NULL, NOW(6), NOW(6)),
('얀 파울 판 헤커', 'Jan Paul van Hecke', '네덜란드', 0, 'DF', '브라이튼', '프리미어리그', '토트넘', '프리미어리그', 'PERMANENT', 60.00, NULL, '2026-07-01', NULL, NOW(6), NOW(6)),
-- 2026 겨울
('앙투안 세메뇨', 'Antoine Semenyo', '가나', 0, 'FW', '본머스', '프리미어리그', '맨체스터 시티', '프리미어리그', 'PERMANENT', 72.00, NULL, '2026-01-09', NULL, NOW(6), NOW(6));
