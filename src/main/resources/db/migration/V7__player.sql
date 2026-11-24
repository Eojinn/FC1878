-- 선수 (football-data.org 스쿼드를 하루 한 번 동기화). 헤더 검색·전술판 벤치에 사용
CREATE TABLE player (
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    external_id   INT          NOT NULL,
    name          VARCHAR(100) NOT NULL,
    position      VARCHAR(40),
    role          VARCHAR(2)   NOT NULL,
    shirt_number  INT,
    nationality   VARCHAR(60),
    team_id       BIGINT,
    team_name     VARCHAR(100),
    team_logo_url VARCHAR(255),
    synced_at     DATETIME(6)  NOT NULL,
    created_at    DATETIME(6)  NOT NULL,
    updated_at    DATETIME(6)  NOT NULL,
    CONSTRAINT uk_player_external UNIQUE (external_id),
    CONSTRAINT fk_player_team FOREIGN KEY (team_id) REFERENCES team (id) ON DELETE SET NULL,
    INDEX idx_player_name (name),
    INDEX idx_player_team (team_id)
);
