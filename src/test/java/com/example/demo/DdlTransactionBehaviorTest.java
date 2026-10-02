package com.example.demo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import javax.sql.DataSource;
import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.FlywayException;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.testcontainers.mysql.MySQLContainer;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

// 008 sec01/sec02/sec03: MySQL（InnoDB）の DDL 暗黙的コミットと PostgreSQL のトランザクショナル DDL を
// 同一マイグレーション（2 文目で確実に失敗）で対比して実機検証する。
// 失敗後に 1 文目で作った step_one が残るか否かが両 DB の本質的な差である。
@Testcontainers
class DdlTransactionBehaviorTest {

    @Container
    static MySQLContainer mysql = new MySQLContainer("mysql:8.4");

    @Container
    static PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:18-alpine");

    private static final String LOCATION = "classpath:db/008-ddl-demo";

    @Test
    void mysqlImplicitCommitLeavesPartialDdl() {
        DataSource ds = dataSource(
            mysql.getJdbcUrl(), mysql.getUsername(), mysql.getPassword(),
            "com.mysql.cj.jdbc.Driver");
        Flyway flyway = Flyway.configure()
            .dataSource(ds)
            .locations(LOCATION)
            .load();

        // V2 の 2 文目（同名テーブル重複）で必ず失敗する。
        assertThatThrownBy(flyway::migrate)
            .isInstanceOf(FlywayException.class)
            .satisfies(e -> System.out.println("[MySQL DDL] " + rootMessage(e)));

        // MySQL では 1 文目の CREATE TABLE が暗黙的コミット済みのため step_one は残る。
        JdbcTemplate jdbc = new JdbcTemplate(ds);
        Integer stepOne = jdbc.queryForObject(
            "SELECT COUNT(*) FROM information_schema.tables "
            + "WHERE table_schema = DATABASE() AND table_name = 'step_one'",
            Integer.class);
        assertThat(stepOne).as("MySQL: 暗黙的コミットで step_one は残る").isEqualTo(1);
    }

    @Test
    void postgresTransactionalDdlRollsBackAll() {
        DataSource ds = dataSource(
            postgres.getJdbcUrl(), postgres.getUsername(), postgres.getPassword(),
            "org.postgresql.Driver");
        // 各 PostgreSQL テストを専用スキーマで隔離し、共有コンテナでの相互干渉を防ぐ。
        Flyway flyway = Flyway.configure()
            .dataSource(ds)
            .locations(LOCATION)
            .schemas("demo_ddl")
            .defaultSchema("demo_ddl")
            .load();

        assertThatThrownBy(flyway::migrate)
            .isInstanceOf(FlywayException.class)
            .satisfies(e -> System.out.println("[PostgreSQL DDL] " + rootMessage(e)));

        // PostgreSQL ではマイグレーション全体がロールバックされ step_one は残らない。
        JdbcTemplate jdbc = new JdbcTemplate(ds);
        Integer stepOne = jdbc.queryForObject(
            "SELECT COUNT(*) FROM information_schema.tables "
            + "WHERE table_schema = 'demo_ddl' AND table_name = 'step_one'",
            Integer.class);
        assertThat(stepOne).as("PostgreSQL: トランザクショナル DDL で step_one はロールバック").isEqualTo(0);
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
