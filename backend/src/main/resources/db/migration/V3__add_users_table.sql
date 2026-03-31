CREATE TABLE users (
    id UUID PRIMARY KEY,
    username VARCHAR(100) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(50) NOT NULL
);

-- Note: We no longer perform INSERTs here to avoid hash mismatch issues.
-- Initial users (employee1, manager1, secretary1) are now created by DataInitializer.java
-- using the application's internal BCryptPasswordEncoder.
