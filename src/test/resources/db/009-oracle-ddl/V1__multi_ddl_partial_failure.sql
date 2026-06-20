-- 009 sec01/sec03: Oracle の DDL 自動コミット特性の実機検証。
-- 1 マイグレーション内に複数 DDL を置き、2 文目を確実に失敗させる。
-- 1 文目（step_one 作成）は成功し、2 文目（同名テーブルの再作成）が ORA-00955 で失敗する。
-- Oracle では DDL が暗黙的に COMMIT されるため、失敗後も step_one は残る（ロールバックされない）。
CREATE TABLE step_one (
    id NUMBER(19) NOT NULL PRIMARY KEY
);

-- 2 文目: 同名テーブルの重複作成 → ORA-00955（name is already used by an existing object）で確実に失敗
CREATE TABLE step_one (
    id NUMBER(19) NOT NULL PRIMARY KEY
);
