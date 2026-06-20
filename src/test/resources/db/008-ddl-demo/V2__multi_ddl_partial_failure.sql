-- 008 sec01/sec02/sec03: 1 マイグレーション内に複数 DDL を置き、2 文目を確実に失敗させる。
-- 1 文目（step_one 作成）は成功し、2 文目（同名テーブルの再作成）が必ず失敗する。
-- 失敗後に step_one が残るか否かで DDL のトランザクション特性を客観判定する:
--   MySQL（InnoDB）  : 1 文目が暗黙的コミット済 → step_one は残る
--   PostgreSQL       : マイグレーション全体がロールバック → step_one は消える
CREATE TABLE step_one (
    id BIGINT NOT NULL PRIMARY KEY
);

-- 2 文目: 同名テーブルの重複作成 → 両 DB で確実にエラー
CREATE TABLE step_one (
    id BIGINT NOT NULL PRIMARY KEY
);
