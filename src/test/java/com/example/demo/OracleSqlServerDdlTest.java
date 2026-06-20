package com.example.demo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Duration;
import javax.sql.DataSource;
import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.FlywayException;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.mssqlserver.MSSQLServerContainer;
import org.testcontainers.oracle.OracleContainer;

// 009 sec01/sec02/sec03: Oracle（DDL 自動コミット）と SQL Server（トランザクショナル DDL）の
// DDL 特性を、同一構造のマイグレーション（2 文目で確実に失敗）で対比して実機検証する。
// 008（MySQL vs PostgreSQL）と同じ手法をエンタープライズ DB ペアに適用する。
//   Oracle      : 1 文目の CREATE TABLE が暗黙的にコミット済 → step_one は残る
//   SQL Server  : マイグレーション全体がロールバック → step_one は消える
// あわせて記事 sec01.3 / sec02.3 の Repeatable（R__）+ CREATE OR REPLACE / CREATE OR ALTER の
// 冪等な再適用も検証する。Oracle / SQL Server イメージは amd64 専用で、Apple Silicon では
// エミュレーション起動のため起動完了まで余裕を持ったタイムアウトを設定する。
@Testcontainers
class OracleSqlServerDdlTest {

    @Container
    static OracleContainer oracle =
        new OracleContainer("gvenzl/oracle-free:23-slim-faststart")
            .withStartupTimeout(Duration.ofMinutes(10));

    @Container
    static MSSQLServerContainer mssql =
        new MSSQLServerContainer("mcr.microsoft.com/mssql/server:2025-latest")
            .acceptLicense()
            .withStartupTimeout(Duration.ofMinutes(10));

    @Test
    void oracleImplicitCommitLeavesPartialDdl() {
        DataSource ds = dataSource(
            oracle.getJdbcUrl(), oracle.getUsername(), oracle.getPassword(),
            "oracle.jdbc.OracleDriver");
        Flyway flyway = Flyway.configure()
            .dataSource(ds)
            .locations("classpath:db/009-oracle-ddl")
            .cleanDisabled(false)
            .load();
        // 共有 Oracle コンテナの同一スキーマ（TEST）を使うため、他テストの残存オブジェクトを掃除して隔離する。
        flyway.clean();

        // V1 の 2 文目（同名テーブル重複 = ORA-00955）で必ず失敗する。
        assertThatThrownBy(flyway::migrate)
            .isInstanceOf(FlywayException.class)
            .satisfies(e -> System.out.println("[Oracle DDL] " + rootMessage(e)));

        // Oracle では 1 文目の CREATE TABLE が暗黙的にコミット済のため step_one は残る。
        JdbcTemplate jdbc = new JdbcTemplate(ds);
        Integer stepOne = jdbc.queryForObject(
            "SELECT COUNT(*) FROM user_tables WHERE table_name = 'STEP_ONE'",
            Integer.class);
        assertThat(stepOne).as("Oracle: DDL 自動コミットで step_one は残る").isEqualTo(1);
    }

    @Test
    void sqlServerTransactionalDdlRollsBackAll() {
        DataSource ds = dataSource(
            mssql.getJdbcUrl(), mssql.getUsername(), mssql.getPassword(),
            "com.microsoft.sqlserver.jdbc.SQLServerDriver");
        Flyway flyway = Flyway.configure()
            .dataSource(ds)
            .locations("classpath:db/009-sqlserver-ddl")
            .schemas("ddl_demo")
            .defaultSchema("ddl_demo")
            .load();

        assertThatThrownBy(flyway::migrate)
            .isInstanceOf(FlywayException.class)
            .satisfies(e -> System.out.println("[SQL Server DDL] " + rootMessage(e)));

        // SQL Server ではマイグレーション全体がロールバックされ step_one は残らない。
        JdbcTemplate jdbc = new JdbcTemplate(ds);
        Integer stepOne = jdbc.queryForObject(
            "SELECT COUNT(*) FROM sys.tables t JOIN sys.schemas s ON t.schema_id = s.schema_id "
            + "WHERE t.name = 'step_one' AND s.name = 'ddl_demo'",
            Integer.class);
        assertThat(stepOne).as("SQL Server: トランザクショナル DDL で step_one はロールバック").isEqualTo(0);
    }

    // 009 sec01.3: Oracle の PL/SQL プロシージャを Repeatable（R__）+ CREATE OR REPLACE + `/` セパレータで
    // 管理できることを検証する。Flyway が PL/SQL ブロック末尾の `/` を認識して 1 文として実行する。
    @Test
    void oracleRepeatableProcedureWithSlashSeparator() {
        DataSource ds = dataSource(
            oracle.getJdbcUrl(), oracle.getUsername(), oracle.getPassword(),
            "oracle.jdbc.OracleDriver");
        Flyway flyway = Flyway.configure()
            .dataSource(ds)
            .locations("classpath:db/009-oracle-repeatable")
            .cleanDisabled(false)
            .load();
        // 共有 Oracle コンテナの同一スキーマ（TEST）を掃除してから V1（テーブル）+ R__（プロシージャ）を適用する。
        flyway.clean();
        flyway.migrate();

        // プロシージャが作成され、呼び出すと行が挿入される（CREATE OR REPLACE + `/` セパレータが適用済）。
        JdbcTemplate jdbc = new JdbcTemplate(ds);
        jdbc.execute("BEGIN add_plsql_note(1, 'hello'); END;");
        Integer cnt = jdbc.queryForObject(
            "SELECT COUNT(*) FROM plsql_demo WHERE id = 1", Integer.class);
        assertThat(cnt).as("Oracle: R__ + CREATE OR REPLACE プロシージャが適用される").isEqualTo(1);
    }

    // 009 sec02.3: SQL Server のファンクションを Repeatable（R__）+ CREATE OR ALTER で管理できることを検証する。
    @Test
    void sqlServerRepeatableFunctionWithCreateOrAlter() {
        DataSource ds = dataSource(
            mssql.getJdbcUrl(), mssql.getUsername(), mssql.getPassword(),
            "com.microsoft.sqlserver.jdbc.SQLServerDriver");
        Flyway flyway = Flyway.configure()
            .dataSource(ds)
            .locations("classpath:db/009-sqlserver-repeatable")
            .schemas("fn_demo")
            .defaultSchema("fn_demo")
            .load();
        flyway.migrate();

        // CREATE OR ALTER で作成したスカラーファンクションが呼び出せる。
        JdbcTemplate jdbc = new JdbcTemplate(ds);
        String full = jdbc.queryForObject(
            "SELECT fn_demo.combine_name('Taro', 'Yamada')", String.class);
        assertThat(full).as("SQL Server: R__ + CREATE OR ALTER ファンクションが適用される")
            .isEqualTo("Taro Yamada");
    }

    private static String rootMessage(Throwable e) {
        Throwable t = e;
        while (t.getCause() != null && t.getCause() != t) {
            t = t.getCause();
        }
        return t.getClass().getSimpleName() + ": " + t.getMessage();
    }

    private static DataSource dataSource(String url, String user, String pass, String driver) {
        DriverManagerDataSource ds = new DriverManagerDataSource(url, user, pass);
        ds.setDriverClassName(driver);
        return ds;
    }
}
