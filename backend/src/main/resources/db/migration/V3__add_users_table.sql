CREATE TABLE users (
    id UUID PRIMARY KEY,
    username VARCHAR(100) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(50) NOT NULL
);

-- Passwords: "password"
-- Use standard BCrypt for "password" (10 rounds): $2a$10$8.UnVuG9UMJ1nS6GYbGcH.Uph3xzWp29W7KoSeXFpWGTsEeugZ1He

-- Employee
INSERT INTO users (id, username, password, role) VALUES ('a1b2c3d4-e5f6-7890-1234-567890abcdef', 'employee1', '$2a$10$8.UnVuG9UMJ1nS6GYbGcH.Uph3xzWp29W7KoSeXFpWGTsEeugZ1He', 'EMPLOYEE');
-- Manager
INSERT INTO users (id, username, password, role) VALUES ('b2c3d4e5-f6a7-8901-2345-678901abcdef', 'manager1', '$2a$10$8.UnVuG9UMJ1nS6GYbGcH.Uph3xzWp29W7KoSeXFpWGTsEeugZ1He', 'MANAGER');
-- Secretary
INSERT INTO users (id, username, password, role) VALUES ('c3d4e5f6-a7b8-9012-3456-789012abcdef', 'secretary1', '$2a$10$8.UnVuG9UMJ1nS6GYbGcH.Uph3xzWp29W7KoSeXFpWGTsEeugZ1He', 'SECRETARY');
