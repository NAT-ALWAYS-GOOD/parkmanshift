ALTER TABLE users ADD COLUMN check_in_code VARCHAR(4);
UPDATE users SET check_in_code = LPAD(CAST(FLOOR(RANDOM() * 10000) AS TEXT), 4, '0');
ALTER TABLE users ALTER COLUMN check_in_code SET NOT NULL;
ALTER TABLE users ADD CONSTRAINT users_check_in_code_unique UNIQUE (check_in_code);
