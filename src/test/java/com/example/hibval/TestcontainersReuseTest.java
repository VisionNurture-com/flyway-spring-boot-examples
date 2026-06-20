package com.example.hibval;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.testcontainers.containers.PostgreSQLContainer;

/**
 * 010 sec04 FAQ: Testcontainers の withReuse(true) API が Testcontainers 2.0.5 で有効に機能し、
 * 再利用フラグ付きコンテナが起動・接続できることを実機確認する。
 * （実際のコンテナ再利用には ~/.testcontainers.properties の testcontainers.reuse.enable=true が必要。
 *  本テストは API の妥当性と起動を確認する。）
 */
class TestcontainersReuseTest {

    @Test
    void reusableContainerStartsAndConnects() {
        try (PostgreSQLContainer<?> pg =
                 new PostgreSQLContainer<>("postgres:18-alpine").withReuse(true)) {
            pg.start();
            assertThat(pg.isRunning()).isTrue();
            assertThat(pg.getJdbcUrl()).startsWith("jdbc:postgresql://");
            assertThat(pg.isShouldBeReused()).isTrue();
        }
    }
}
