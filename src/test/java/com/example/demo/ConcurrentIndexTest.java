package com.example.demo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

// 008 sec02.3: CREATE INDEX CONCURRENTLY のトランザクション制約を PostgreSQL の挙動として直接検証する。
//
// Flyway 経由（executeInTransaction の true/false いずれも）では、Flyway が schema-history テーブルに
// 取得するロックを保持したまま CONCURRENTLY を実行するため、CONCURRENTLY がそのロックの解放を待ち
// 自己デッドロックする（本検証で実測・JUnit @Timeout はネイティブ socket read を中断できず効かない）。
// そこで制約の本体である「PostgreSQL は明示トランザクション内で CONCURRENTLY を拒否する」ことと
// 「トランザクション外なら成功する」ことを raw JDBC で決定的に検証する。これが
// 「.sql.conf の executeInTransaction=false が必要」という記事の根拠そのものである。
@Testcontainers
class ConcurrentIndexTest {

    @Container
    private final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:18-alpine");

    // negative: 明示トランザクション内（autoCommit=false）では CONCURRENTLY が即座に拒否される。
    @Test
    @Timeout(value = 60, unit = TimeUnit.SECONDS)
    void concurrentlyInsideExplicitTransactionIsRejected() throws SQLException {
        try (Connection conn = connection()) {
            try (Statement st = conn.createStatement()) {
                st.execute("CREATE TABLE products (id BIGINT PRIMARY KEY, name VARCHAR(100))");
            }
            conn.setAutoCommit(false); // 明示トランザクション開始
            try (Statement st = conn.createStatement()) {
                assertThatThrownBy(() ->
                    st.execute("CREATE INDEX CONCURRENTLY idx_products_name ON products(name)"))
                    .isInstanceOf(SQLException.class)
                    .satisfies(e -> System.out.println("[CONCURRENTLY in-tx] " + e.getMessage()));
            }
            conn.rollback();
        }
    }

    // positive: トランザクション外（autoCommit=true）なら同じ CONCURRENTLY が成功する。
    @Test
    @Timeout(value = 60, unit = TimeUnit.SECONDS)
    void concurrentlyOutsideTransactionSucceeds() throws SQLException {
        try (Connection conn = connection()) {
            conn.setAutoCommit(true); // トランザクション外（executeInTransaction=false 相当）
            try (Statement st = conn.createStatement()) {
                st.execute("CREATE TABLE products (id BIGINT PRIMARY KEY, name VARCHAR(100))");
                st.execute("CREATE INDEX CONCURRENTLY idx_products_name ON products(name)");
            }
            try (Statement st = conn.createStatement();
                 var rs = st.executeQuery(
                     "SELECT COUNT(*) FROM pg_indexes "
                     + "WHERE schemaname = 'public' AND indexname = 'idx_products_name'")) {
                rs.next();
                assertThat(rs.getInt(1))
                    .as("トランザクション外なら CONCURRENTLY 索引が作成される").isEqualTo(1);
            }
        }
    }

    private Connection connection() throws SQLException {
        return java.sql.DriverManager.getConnection(
            postgres.getJdbcUrl(), postgres.getUsername(), postgres.getPassword());
    }
}
