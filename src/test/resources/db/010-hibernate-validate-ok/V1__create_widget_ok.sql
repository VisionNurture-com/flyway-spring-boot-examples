-- 010 sec04 正常系: name 列を含むため Hibernate validate が成功する
CREATE TABLE widget (
    id   BIGINT       NOT NULL,
    name VARCHAR(255) NOT NULL,
    PRIMARY KEY (id)
);
