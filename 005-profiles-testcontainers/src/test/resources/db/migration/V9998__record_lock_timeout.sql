CREATE TABLE lock_timeout_seen (v TEXT);
INSERT INTO lock_timeout_seen SELECT current_setting('lock_timeout');
