package com.example.demo;

import static org.assertj.core.api.Assertions.assertThat;

import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

@SpringBootTest
@Import(TestcontainersConfiguration.class)
class UserMigrationTest {

    @Autowired
    Flyway flyway;

    @Test
    void migrationsAreApplied() {
        assertThat(flyway.info().current()).isNotNull();
    }
}
