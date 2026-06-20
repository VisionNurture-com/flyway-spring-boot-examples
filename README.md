# flyway-spring-boot-examples

[Flyway](https://documentation.red-gate.com/flyway) を Spring Boot 4.1 / Java 25 で実践するためのハンズオン用サンプルプロジェクトです。
ブログ連載「**Flyway 入門シリーズ**」（全10回）の解説に登場するコード・マイグレーション・テスト・CI/CD ワークフローを、1つの動くプロジェクトに集約しています。掲載コードはすべて実機で動作確認したものです。

## 動作環境

| 項目 | バージョン |
|------|-----------|
| Java | 25 (LTS) |
| Spring Boot | 4.1.0 |
| Flyway | 12.8.1 |
| Gradle | 9.5.1（wrapper 同梱） |
| Maven | 3.9 系（wrapper 同梱） |
| 基本 DB | H2（インメモリ / file） |
| DB 別テスト | PostgreSQL / MySQL / Oracle / SQL Server（[Testcontainers](https://testcontainers.com/) 経由・Docker 必須） |

> Spring Boot 4 では Flyway の自動構成に `spring-boot-starter-flyway` が必須です（`flyway-core` 単体では自動構成されません）。

## クイックスタート

```bash
# アプリ起動（H2 インメモリ・Flyway が V1〜V3 を適用）
./gradlew bootRun        # Gradle
./mvnw spring-boot:run   # Maven

# Flyway CLI コマンド（file ベース H2 を参照）
./gradlew flywayInfo flywayMigrate flywayValidate

# テスト（PostgreSQL/MySQL/Oracle/SQL Server は Docker 必須）
./gradlew test
```

## モジュール ↔ 記事 対応表

各ファイルがシリーズのどの記事に対応するかの一覧です（本表がインライン参照の正本です）。

| 記事 | テーマ | 主な対応コード |
|------|--------|---------------|
| [001 Flyway とは](https://www.visionnurture.com/flyway_tool_guide_for_beginner_001/) | 基本概念・他ツール比較 | （概念編・コードなし） |
| [002 Spring Boot 入門](https://www.visionnurture.com/flyway_tool_guide_for_beginner_002/) | Gradle/Maven 設定・初回マイグレーション | `src/main/`（`DemoApplication` / `User` / `db/migration/V1〜V3`）・`build.gradle.kts` / `pom.xml` |
| [003 スクリプト管理](https://www.visionnurture.com/flyway_tool_guide_for_beginner_003/) | 命名規則・checksum/Out-of-Order | `src/main/resources/db/migration/` + `flywayValidate` / `flywayRepair` |
| [004 コマンド完全ガイド](https://www.visionnurture.com/flyway_tool_guide_for_beginner_004/) | migrate/info/validate/repair/clean/baseline | Flyway Gradle/Maven プラグインの各コマンド |
| [005 設定入門](https://www.visionnurture.com/flyway_tool_guide_for_beginner_005/) | flyway.toml・環境別設定・Testcontainers | `flyway.toml` / `db/toml-migration/` / `FlywayMigrationTest` / `db/pg-migration/` |
| [006 ツール比較](https://www.visionnurture.com/flyway_tool_guide_for_beginner_006/) | 主要ツール徹底比較 | （比較編・コードなし） |
| [007 CI/CD 入門](https://www.visionnurture.com/flyway_tool_guide_for_beginner_007/) | パイプライン・Expand/Contract・K8s | `db/expand-contract/` / `db/ci-migration/` / `CiMigrationTest` / `.github/workflows/ci-migrate.yml` / `k8s/` |
| [008 MySQL/PostgreSQL](https://www.visionnurture.com/flyway_tool_guide_for_beginner_008/) | DDL トランザクション特性・CONCURRENTLY | `DdlTransactionBehaviorTest` / `ConcurrentIndexTest` / `db/008-ddl-demo/` |
| [009 Oracle/SQL Server](https://www.visionnurture.com/flyway_tool_guide_for_beginner_009/) | DDL 特性・PL/SQL・Repeatable | `OracleSqlServerDdlTest` / `db/009-*/` |
| [010 GitHub Actions](https://www.visionnurture.com/flyway_tool_guide_for_beginner_010/) | setup-flyway@v3・Hibernate 検証・Testcontainers | `.github/workflows/flyway-setup-v3.yml` / `flyway-matrix.yml` / `src/test/java/com/example/hibval/` / `db/010-*/` |

## ディレクトリ構成

```
src/main/java/com/example/demo/   アプリ本体（DemoApplication / User エンティティ）
src/main/resources/
  application.properties          H2 インメモリ + Flyway + JPA validate 設定
  db/migration/                   共通マイグレーション V1〜V3
src/test/java/com/example/
  demo/                           DB 別 DDL 特性・CI・移行テスト
  hibval/                         Hibernate 検証 + Testcontainers reuse（記事010）
db/                               CLI / CI / Expand-Contract 用マイグレーション群
.github/workflows/                GitHub Actions ワークフロー（記事007/010）
k8s/                              Kubernetes Init Container デモ（記事007）
flyway.toml                       Flyway CLI 用の環境別設定（記事005/010）
```

## テストと外部 DB について

`./gradlew test` は PostgreSQL / MySQL / Oracle / SQL Server のコンテナを Testcontainers で起動します。**Docker が必要**です。Oracle / SQL Server のイメージはサイズが大きく起動に時間がかかります。

> サンプルの認証情報（`k8s/10-secret.yaml` 等）はすべてローカルデモ用のダミーです。実運用では外部シークレットストアを使用してください。

## ライセンス

[LICENSE](LICENSE) を参照してください。
