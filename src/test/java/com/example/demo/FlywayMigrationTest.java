package com.example.demo;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

// 005 sec05: 本番同等の PostgreSQL コンテナ上で Flyway マイグレーションが適用されることを実機検証する。
@SpringBootTest
@ActiveProfiles("test")
@Testcontainers
class FlywayMigrationTest {

    // @ServiceConnection（SB 3.1+ / SB4 でも推奨）でコンテナの接続情報を Spring Boot へ自動連携する。
    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:18-alpine");

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void migrationShouldCreateUsersTable() {
        Integer count = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM information_schema.tables WHERE table_name = 'users'",
            Integer.class);
        assertThat(count).isEqualTo(1);
    }

    @Test
    void migrationShouldApplyAllVersions() {
        Integer versionCount = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM flyway_schema_history WHERE success = true",
            Integer.class);
        assertThat(versionCount).isGreaterThanOrEqualTo(3);
    }
}
