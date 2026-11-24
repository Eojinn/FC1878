-- 선수 생년월일 + 대회별 기록 (football-data.org 득점 순위에서 3시간마다 동기화)
ALTER TABLE player ADD COLUMN date_of_birth DATE;

CREATE TABLE player_stat (
    id             BIGINT AUTO_INCREMENT PRIMARY KEY,
    player_id      BIGINT      NOT NULL,
    league_id      BIGINT      NOT NULL,
    season         INT         NOT NULL,
    team_id        BIGINT,
    played_matches INT,
    goals          INT         NOT NULL DEFAULT 0,
    assists        INT,
    penalties      INT,
    created_at     DATETIME(6) NOT NULL,
    updated_at     DATETIME(6) NOT NULL,
    CONSTRAINT uk_player_stat UNIQUE (player_id, league_id, season),
    CONSTRAINT fk_player_stat_player FOREIGN KEY (player_id) REFERENCES player (id) ON DELETE CASCADE,
    CONSTRAINT fk_player_stat_league FOREIGN KEY (league_id) REFERENCES league (id),
    CONSTRAINT fk_player_stat_team FOREIGN KEY (team_id) REFERENCES team (id) ON DELETE SET NULL
);
