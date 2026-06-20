-- 009 sec02.3: SQL Server のスカラーファンクションを Repeatable（R__）+ CREATE OR ALTER で管理する。
-- CREATE OR ALTER はオブジェクトがあれば更新、なければ作成する（冪等）。
CREATE OR ALTER FUNCTION fn_demo.combine_name (@first NVARCHAR(50), @last NVARCHAR(50))
RETURNS NVARCHAR(101)
AS
BEGIN
    RETURN @first + N' ' + @last;
END;
