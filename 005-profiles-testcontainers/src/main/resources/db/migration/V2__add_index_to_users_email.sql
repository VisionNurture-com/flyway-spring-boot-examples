-- V2: users.email にユニークインデックスを追加
CREATE UNIQUE INDEX uix_users_email ON users (email);
