package com.example.demo;

import static org.assertj.core.api.Assertions.assertThatNoException;

import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

// 記事 sec03: Spring を介さないプレーン Flyway API による standalone マイグレーションテスト。
// CI でマイグレーション SQL そのものを検証する最小構成（PostgreSQL 18 / Testcontainers）。
@Testcontainers
class CiMigrationTest {

    @Container
    private final PostgreSQLContainer<?> postgres =
        new PostgreSQLContainer<>("postgres:18-alpine");

    @Test
    void testMigrationsCanRunSuccessfully() {
        Flyway flyway = Flyway.configure()
            .dataSource(
                postgres.getJdbcUrl(),
                postgres.getUsername(),
                postgres.getPassword()
            )
            .locations("classpath:db/pg-migration")
            .load();

        // SQL 構文エラーがあれば例外がスローされ、テストが失敗する
        assertThatNoException().isThrownBy(flyway::migrate);
    }
}
