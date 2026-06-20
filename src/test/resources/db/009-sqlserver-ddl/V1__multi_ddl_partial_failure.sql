-- 009 sec02/sec03: SQL Server のトランザクショナル DDL 特性の実機検証。
-- 1 マイグレーション内に複数 DDL を置き、2 文目を確実に失敗させる。
-- 1 文目（step_one 作成）は成功するが、2 文目（同名テーブルの再作成）が失敗する。
-- SQL Server では Flyway がマイグレーションをトランザクションで包むため、失敗時に
-- 1 文目を含むマイグレーション全体がロールバックされ、step_one は残らない。
CREATE TABLE step_one (
    id BIGINT NOT NULL PRIMARY KEY
);

-- 2 文目: 同名テーブルの重複作成 → 「There is already an object named 'step_one'」で確実に失敗
CREATE TABLE step_one (
    id BIGINT NOT NULL PRIMARY KEY
);
