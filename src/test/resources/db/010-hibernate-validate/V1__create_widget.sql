-- 010 sec04 検証用: あえて name 列を欠落させ、Hibernate ddl-auto=validate の
-- 不整合検出（SchemaManagementException）を実機再現する。
CREATE TABLE widget (
    id BIGINT NOT NULL,
    PRIMARY KEY (id)
);
