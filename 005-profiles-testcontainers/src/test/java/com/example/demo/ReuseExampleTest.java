package com.example.demo;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.testcontainers.postgresql.PostgreSQLContainer;

class ReuseExampleTest {

    static PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:18-alpine").withReuse(true);

    @BeforeAll
    static void startContainer() {
        postgres.start();
    }

    @Test
    void containerIsRunning() {
        assertThat(postgres.isRunning()).isTrue();
    }
}
