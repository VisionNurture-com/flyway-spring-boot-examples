-- 009 sec01.3: Oracle の PL/SQL プロシージャを Repeatable（R__）+ CREATE OR REPLACE で管理する。
-- PL/SQL ブロックは末尾の `/`（スラッシュ）で 1 文として区切る。Flyway がこの `/` を認識する。
CREATE OR REPLACE PROCEDURE add_plsql_note (
    p_id   IN NUMBER,
    p_note IN VARCHAR2
)
IS
BEGIN
    INSERT INTO plsql_demo (id, note) VALUES (p_id, p_note);
    COMMIT;
END;
/
