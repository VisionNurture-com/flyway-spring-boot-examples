package com.example.demo;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;

@SpringBootTest
@Import(TestcontainersConfiguration.class)
class LockTimeoutTest {

    @Autowired
    JdbcTemplate jdbc;

    @Test
    void lockTimeoutIsSetInsideMigration() {
        String v = jdbc.queryForObject("SELECT v FROM lock_timeout_seen", String.class);
        assertThat(v).isEqualTo("5s");
    }
}
