-- 이미 저장된 이메일도 앞뒤 공백 제거 + 소문자로 통일 (새 가입은 코드에서 정리해서 저장)
UPDATE member
SET email = LOWER(TRIM(email))
WHERE email <> LOWER(TRIM(email)) COLLATE utf8mb4_bin;
