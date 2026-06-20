-- 008 sec01/sec02/sec03: DDL トランザクション特性の実機検証（V1 = 正常適用される基準テーブル）。
-- MySQL/PostgreSQL 双方で動く移植可能な DDL のみを使う。
CREATE TABLE base_marker (
    id BIGINT NOT NULL PRIMARY KEY
);
