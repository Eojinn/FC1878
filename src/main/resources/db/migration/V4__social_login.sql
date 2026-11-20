-- 소셜 로그인: 비밀번호 로그인 가능 여부 + 회원과 소셜 계정 연결
ALTER TABLE member ADD COLUMN password_login BIT(1) NOT NULL DEFAULT 1;

CREATE TABLE social_account (
    id               BIGINT AUTO_INCREMENT PRIMARY KEY,
    member_id        BIGINT       NOT NULL,
    provider         VARCHAR(20)  NOT NULL,
    provider_user_id VARCHAR(255) NOT NULL,
    email            VARCHAR(100),
    created_at       DATETIME(6)  NOT NULL,
    updated_at       DATETIME(6)  NOT NULL,
    CONSTRAINT uk_social_account UNIQUE (provider, provider_user_id),
    CONSTRAINT fk_social_account_member FOREIGN KEY (member_id) REFERENCES member (id)
);
