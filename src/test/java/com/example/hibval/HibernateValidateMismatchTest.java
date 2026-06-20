package com.example.hibval;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.catchThrowable;

import java.util.HashMap;
import java.util.Map;

import org.flywaydb.core.Flyway;
import org.hibernate.boot.Metadata;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.tool.schema.spi.SchemaManagementException;
import org.junit.jupiter.api.Test;

/**
 * 010 sec04: Spring Boot 起動時の {@code spring.jpa.hibernate.ddl-auto=validate} は、
 * 内部的に Hibernate のスキーマ検証（hbm2ddl.auto=validate）を実行する。
 * 本テストはその検証単体を切り出し、Flyway が構築したスキーマ（widget は id 列のみ）に対して
 * エンティティ Widget（id + name）が不整合な場合に SchemaManagementException がスローされることを実機確認する。
 */
class HibernateValidateMismatchTest {

    private StandardServiceRegistry buildRegistry(String dbName, String ddlAuto) {
        Map<String, Object> settings = new HashMap<>();
        settings.put("hibernate.connection.url", "jdbc:h2:mem:" + dbName + ";DB_CLOSE_DELAY=-1");
        settings.put("hibernate.connection.username", "sa");
        settings.put("hibernate.connection.password", "");
        settings.put("hibernate.connection.driver_class", "org.h2.Driver");
        settings.put("hibernate.hbm2ddl.auto", ddlAuto);
        return new StandardServiceRegistryBuilder().applySettings(settings).build();
    }

    private void migrate(String dbName) {
        Flyway.configure()
            .dataSource("jdbc:h2:mem:" + dbName + ";DB_CLOSE_DELAY=-1", "sa", "")
            .locations("classpath:db/010-hibernate-validate")
            .load()
            .migrate();
    }

    @Test
    void validateThrowsWhenEntityHasColumnMissingFromSchema() {
        // Flyway が widget(id) のみのスキーマを構築（name 列は無い）
        migrate("hibval_mismatch");

        StandardServiceRegistry registry = buildRegistry("hibval_mismatch", "validate");
        Metadata metadata = new MetadataSources(registry)
            .addAnnotatedClass(Widget.class)
            .buildMetadata();

        // ddl-auto=validate 相当: SessionFactory 構築時に不整合検証が走る
        Throwable thrown = catchThrowable(() -> metadata.buildSessionFactory().close());

        assertThat(thrown)
            .isInstanceOf(SchemaManagementException.class)
            .hasMessageContaining("missing column")
            .hasMessageContaining("name");

        StandardServiceRegistryBuilder.destroy(registry);
    }

    @Test
    void validatePassesWhenEntityMatchesSchema() {
        // 対照: name 列を含むスキーマなら検証成功（正常系）
        Flyway.configure()
            .dataSource("jdbc:h2:mem:hibval_match;DB_CLOSE_DELAY=-1", "sa", "")
            .locations("classpath:db/010-hibernate-validate-ok")
            .load()
            .migrate();

        StandardServiceRegistry registry = buildRegistry("hibval_match", "validate");
        Metadata metadata = new MetadataSources(registry)
            .addAnnotatedClass(Widget.class)
            .buildMetadata();

        // 例外がスローされなければ検証成功
        assertThatThrownBy(() -> { throw new IllegalStateException("sentinel"); })
            .isInstanceOf(IllegalStateException.class);
        metadata.buildSessionFactory().close();

        StandardServiceRegistryBuilder.destroy(registry);
    }
}
