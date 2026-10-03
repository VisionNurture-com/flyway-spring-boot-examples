// Flyway Gradle プラグインの classpath に JDBC ドライバを供給
buildscript {
    dependencies {
        classpath("com.h2database:h2:2.4.240")
        // 008 の MySQL / 009 の Oracle に Gradle プラグインからつなぐときの DB 対応モジュールと JDBC ドライバ。
        // buildscript には Spring Boot の BOM が効かないため、BOM と同じ版を書く
        classpath("org.flywaydb:flyway-mysql:12.11.0")
        classpath("com.mysql:mysql-connector-j:9.7.0")
        classpath("org.flywaydb:flyway-database-oracle:12.11.0")
        classpath("com.oracle.database.jdbc:ojdbc11:23.26.3.0.0")
    }
}

plugins {
    java
    id("org.springframework.boot") version "4.1.1"
    id("io.spring.dependency-management") version "1.1.7"
    // 003/004: Flyway CLI コマンド（flywayInfo/Validate/Repair/Migrate）を Gradle から実行
    id("org.flywaydb.flyway") version "12.11.0"
}

group = "com.example"
version = "0.0.1-SNAPSHOT"

// Flyway Gradle プラグインの設定（標準 CLI コマンド用）。
// コマンドごとに接続が閉じても適用状態が残るよう、file ベース H2 を参照する
// （bootRun 経路は application.properties の in-mem H2 のまま・記事002 と整合）。
// パスは ${projectDir} から書く。相対パスは Gradle デーモンの作業フォルダ（~/.gradle/daemon/<版>/）を基準に解決されるため。
flyway {
    url = "jdbc:h2:file:${projectDir}/data/testdb"
    user = "sa"
    password = ""
    locations = arrayOf("filesystem:src/main/resources/db/migration")
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(25)
    }
}

repositories {
    mavenCentral()
}

// Spring Boot 4.1.1 の依存管理が決める Flyway は 12.4.0。12.x の最新 12.11.0 を使うため版を上書きする
extra["flyway.version"] = "12.11.0"

dependencies {
    // SB4 破壊的変更 #1: flyway-core 単体では自動構成されない → starter が必須
    implementation("org.springframework.boot:spring-boot-starter-flyway")
    implementation("org.springframework.boot:spring-boot-starter-jdbc")
    // sec05 JPA 連携検証: ddl-auto=validate が Flyway 構築スキーマと共存することを実機確認（SB4/Hibernate 7）
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    // 005 sec05: Flyway 10.x で分離された PostgreSQL 用モジュール（Testcontainers 実機検証）
    implementation("org.flywaydb:flyway-database-postgresql")
    runtimeOnly("com.h2database:h2")
    runtimeOnly("org.postgresql:postgresql")

    // 005 sec05: Testcontainers で本番同等（PostgreSQL）の Flyway マイグレーションを実機検証
    // Testcontainers 2.x 座標（testcontainers- プレフィックス）。版は SB4.1 BOM が管理（2.0.5）
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.boot:spring-boot-testcontainers")
    testImplementation("org.testcontainers:testcontainers-junit-jupiter")
    testImplementation("org.testcontainers:testcontainers-postgresql")
    // 008 sec01/sec03: MySQL（InnoDB）の DDL 暗黙的コミット特性を PostgreSQL と対比して実機検証
    testImplementation("org.flywaydb:flyway-mysql")
    testImplementation("org.testcontainers:testcontainers-mysql")
    testRuntimeOnly("com.mysql:mysql-connector-j")
    // 009 sec01/sec02/sec03: Oracle（DDL 自動コミット）と SQL Server（トランザクショナル DDL）の
    // DDL 特性を対比して実機検証。Flyway 10.x で分離された DB 固有モジュール + 各 JDBC ドライバ。
    testImplementation("org.flywaydb:flyway-database-oracle")
    testImplementation("org.flywaydb:flyway-sqlserver")
    testImplementation("org.testcontainers:testcontainers-oracle-free")
    testImplementation("org.testcontainers:testcontainers-mssqlserver")
    testRuntimeOnly("com.oracle.database.jdbc:ojdbc11")
    testRuntimeOnly("com.microsoft.sqlserver:mssql-jdbc")
}

tasks.withType<Test> {
    useJUnitPlatform()
}
