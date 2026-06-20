-- V1: 環境別プレースホルダーで監査ログテーブルを作成する
-- ${audit_table} / ${retention_days} は flyway.toml の environments.*.flyway.placeholders から注入される。
CREATE TABLE ${audit_table} (
    id             BIGINT       NOT NULL PRIMARY KEY,
    action         VARCHAR(100) NOT NULL,
    retention_days INT          NOT NULL DEFAULT ${retention_days}
);
