-- V2: Password Reset Token tablosu
-- token_hash: SHA-256 hash (hex string, 64 karakter) -- plaintext token saklanmaz
-- user_id: users tablosuna FK
-- expires_at: token'in gecerlilik suresi
-- used_at: NULL ise kullanilmamis, dolu ise kullanilmis (single-use)
-- created_at: olusturulma zamani

CREATE TABLE password_reset_tokens (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id     BIGINT NOT NULL,
    token_hash  VARCHAR(64) NOT NULL,
    expires_at  DATETIME(6) NOT NULL,
    used_at     DATETIME(6),
    created_at  DATETIME(6) NOT NULL,
    CONSTRAINT fk_prt_user FOREIGN KEY (user_id) REFERENCES users(id)
);

-- token_hash uzerinde UNIQUE index: her hash yalnizca bir kez var olabilir
CREATE UNIQUE INDEX UQ_PRT_TOKEN_HASH ON password_reset_tokens (token_hash);

-- user_id + used_at uzerinde index: aktif tokenlari hizli bulmak icin
CREATE INDEX IDX_PRT_USER_USED ON password_reset_tokens (user_id, used_at);



