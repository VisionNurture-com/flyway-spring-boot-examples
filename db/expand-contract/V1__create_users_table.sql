CREATE TABLE users (
    id   BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL
);
INSERT INTO users (name) VALUES ('Alice Smith'), ('Bob Tanaka');
