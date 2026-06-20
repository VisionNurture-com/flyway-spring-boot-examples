-- 009 sec01.3: Repeatable プロシージャ（R__）が参照するテーブルを V__ で先に用意する。
-- R__ は V__ の後に実行されるため、テーブル定義は V__・参照する PL/SQL は R__ という分担になる。
CREATE TABLE plsql_demo (
    id   NUMBER(19) PRIMARY KEY,
    note VARCHAR2(100)
);
