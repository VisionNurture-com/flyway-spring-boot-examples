package com.example.hibval;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.testcontainers.postgresql.PostgreSQLContainer;

/**
 * 010 sec04 FAQ: Testcontainers の withReuse(true) API が Testcontainers 2.0.5 で有効に機能し、
 * 再利用フラグ付きコンテナが起動・接続できることを実機確認する。
 * reuse のコンテナは自分で start() し、stop() / close() は呼ばない（呼ぶと次の実行で使い回せない）。
 * try-with-resources で囲むと、ブロックの終わりで close() が呼ばれてコンテナが止まる。
 * （実際のコンテナ再利用には ~/.testcontainers.properties の testcontainers.reuse.enable=true が必要。
 *  reuse は試験的な機能で、CI での利用には向かない。手元の開発だけで使う。）
 */
class TestcontainersReuseTest {

    static PostgreSQLContainer pg = new PostgreSQLContainer("postgres:18-alpine").withReuse(true);

    @BeforeAll
    static void startContainer() {
        pg.start();
    }

    @Test
    void reusableContainerStartsAndConnects() {
        assertThat(pg.isRunning()).isTrue();
        assertThat(pg.getJdbcUrl()).startsWith("jdbc:postgresql://");
        assertThat(pg.isShouldBeReused()).isTrue();
    }
}
