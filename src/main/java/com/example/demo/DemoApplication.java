package com.example.demo;

import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.core.JdbcTemplate;

@SpringBootApplication
public class DemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);
    }

    // 起動時に Flyway が適用したマイグレーション結果をログへ出力（記事の実行結果に対応）
    @Bean
    CommandLineRunner verifyMigration(DataSource dataSource) {
        return args -> {
            JdbcTemplate jdbc = new JdbcTemplate(dataSource);
            // Flyway は履歴表を小文字クオートで作成するため H2 では要クオート
            List<Map<String, Object>> rows = jdbc.queryForList(
                    "SELECT \"installed_rank\", \"version\", \"description\", \"script\", \"success\" "
                  + "FROM \"flyway_schema_history\" ORDER BY \"installed_rank\"");
            System.out.println("[Flyway] flyway_schema_history ----------------------------------");
            for (Map<String, Object> r : rows) {
                System.out.printf("  rank=%s version=%s desc=%s script=%s success=%s%n",
                        r.get("installed_rank"), r.get("version"), r.get("description"),
                        r.get("script"), r.get("success"));
            }
            // V3 の age カラムが追加されたことを情報スキーマで裏取り
            Integer ageCol = jdbc.queryForObject(
                    "SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS "
                  + "WHERE TABLE_NAME = 'USERS' AND COLUMN_NAME = 'AGE'", Integer.class);
            System.out.println("[Flyway] users.age column present = " + (ageCol != null && ageCol > 0));
        };
    }
}
