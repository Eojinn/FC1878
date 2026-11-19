CREATE TABLE league (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    external_id INT          NOT NULL UNIQUE,
    name        VARCHAR(100) NOT NULL,
    country     VARCHAR(60),
    logo_url    VARCHAR(500),
    created_at  DATETIME(6)  NOT NULL,
    updated_at  DATETIME(6)  NOT NULL
);

CREATE TABLE team (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    external_id INT          NOT NULL UNIQUE,
    name        VARCHAR(100) NOT NULL,
    logo_url    VARCHAR(500),
    created_at  DATETIME(6)  NOT NULL,
    updated_at  DATETIME(6)  NOT NULL
);

CREATE TABLE member (
    id               BIGINT AUTO_INCREMENT PRIMARY KEY,
    email            VARCHAR(100) NOT NULL UNIQUE,
    password         VARCHAR(255) NOT NULL,
    nickname         VARCHAR(30)  NOT NULL UNIQUE,
    role             VARCHAR(20)  NOT NULL,
    point            INT          NOT NULL DEFAULT 0,
    favorite_team_id BIGINT,
    created_at       DATETIME(6)  NOT NULL,
    updated_at       DATETIME(6)  NOT NULL,
    CONSTRAINT fk_member_team FOREIGN KEY (favorite_team_id) REFERENCES team (id)
);

CREATE TABLE fixture (
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    external_id   INT         NOT NULL UNIQUE,
    league_id     BIGINT      NOT NULL,
    home_team_id  BIGINT      NOT NULL,
    away_team_id  BIGINT      NOT NULL,
    season        INT         NOT NULL,
    round         VARCHAR(100),
    kickoff_at    DATETIME(6) NOT NULL,
    status        VARCHAR(20) NOT NULL,
    elapsed       INT,
    home_score    INT,
    away_score    INT,
    home_penalty  INT,
    away_penalty  INT,
    settled       BIT(1)      NOT NULL DEFAULT 0,
    created_at    DATETIME(6) NOT NULL,
    updated_at    DATETIME(6) NOT NULL,
    CONSTRAINT fk_fixture_league FOREIGN KEY (league_id) REFERENCES league (id),
    CONSTRAINT fk_fixture_home FOREIGN KEY (home_team_id) REFERENCES team (id),
    CONSTRAINT fk_fixture_away FOREIGN KEY (away_team_id) REFERENCES team (id),
    INDEX idx_fixture_kickoff (kickoff_at),
    INDEX idx_fixture_league_kickoff (league_id, kickoff_at)
);

CREATE TABLE standing (
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    league_id     BIGINT      NOT NULL,
    team_id       BIGINT      NOT NULL,
    season        INT         NOT NULL,
    team_rank     INT         NOT NULL,
    played        INT         NOT NULL,
    win           INT         NOT NULL,
    draw          INT         NOT NULL,
    lose          INT         NOT NULL,
    goals_for     INT         NOT NULL,
    goals_against INT         NOT NULL,
    points        INT         NOT NULL,
    form          VARCHAR(10),
    created_at    DATETIME(6) NOT NULL,
    updated_at    DATETIME(6) NOT NULL,
    CONSTRAINT uk_standing UNIQUE (league_id, season, team_id),
    CONSTRAINT fk_standing_league FOREIGN KEY (league_id) REFERENCES league (id),
    CONSTRAINT fk_standing_team FOREIGN KEY (team_id) REFERENCES team (id)
);

CREATE TABLE prediction (
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    member_id  BIGINT      NOT NULL,
    fixture_id BIGINT      NOT NULL,
    pick       VARCHAR(10) NOT NULL,
    result     VARCHAR(10) NOT NULL,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    CONSTRAINT uk_prediction UNIQUE (member_id, fixture_id),
    CONSTRAINT fk_prediction_member FOREIGN KEY (member_id) REFERENCES member (id),
    CONSTRAINT fk_prediction_fixture FOREIGN KEY (fixture_id) REFERENCES fixture (id)
);

CREATE TABLE post (
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    member_id  BIGINT       NOT NULL,
    category   VARCHAR(20)  NOT NULL,
    title      VARCHAR(200) NOT NULL,
    content    TEXT         NOT NULL,
    view_count INT          NOT NULL DEFAULT 0,
    deleted    BIT(1)       NOT NULL DEFAULT 0,
    created_at DATETIME(6)  NOT NULL,
    updated_at DATETIME(6)  NOT NULL,
    CONSTRAINT fk_post_member FOREIGN KEY (member_id) REFERENCES member (id),
    INDEX idx_post_category_created (category, created_at)
);

CREATE TABLE comment (
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    post_id    BIGINT        NOT NULL,
    member_id  BIGINT        NOT NULL,
    content    VARCHAR(1000) NOT NULL,
    created_at DATETIME(6)   NOT NULL,
    updated_at DATETIME(6)   NOT NULL,
    CONSTRAINT fk_comment_post FOREIGN KEY (post_id) REFERENCES post (id),
    CONSTRAINT fk_comment_member FOREIGN KEY (member_id) REFERENCES member (id)
);
