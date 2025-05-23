-- Database schema for Fitness Center application

-- Users table for all users in the system (clients, trainers, administrators)
CREATE TABLE IF NOT EXISTS users (
                                     id SERIAL PRIMARY KEY,
                                     username VARCHAR(50) NOT NULL UNIQUE,
                                     password VARCHAR(100) NOT NULL, -- For storing BCrypt hashed passwords
                                     email VARCHAR(100) NOT NULL UNIQUE,
                                     first_name VARCHAR(50) NOT NULL,
                                     last_name VARCHAR(50) NOT NULL,
                                     phone VARCHAR(20),
                                     created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                     active BOOLEAN NOT NULL DEFAULT TRUE
);

-- Roles table (client, trainer, administrator)
CREATE TABLE IF NOT EXISTS roles (
                                     id SERIAL PRIMARY KEY,
                                     name VARCHAR(20) NOT NULL UNIQUE
);

-- User roles mapping (many-to-many relationship)
CREATE TABLE IF NOT EXISTS user_roles (
                                          user_id INTEGER NOT NULL REFERENCES users(id) ON DELETE CASCADE,
                                          role_id INTEGER NOT NULL REFERENCES roles(id),
                                          PRIMARY KEY (user_id, role_id)
);

-- Training cycles (programs offered by the center)
CREATE TABLE IF NOT EXISTS training_cycles (
                                               id SERIAL PRIMARY KEY,
                                               name VARCHAR(100) NOT NULL,
                                               description TEXT NOT NULL,
                                               duration_days INTEGER NOT NULL CHECK (duration_days > 0),
                                               price NUMERIC(10, 2) NOT NULL CHECK (price >= 0),
                                               active BOOLEAN NOT NULL DEFAULT TRUE,
                                               created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Client account types (regular, corporate)
CREATE TABLE IF NOT EXISTS account_types (
                                             id SERIAL PRIMARY KEY,
                                             name VARCHAR(50) NOT NULL UNIQUE,
                                             description TEXT
);

-- Client profiles with additional client-specific information
CREATE TABLE IF NOT EXISTS client_profiles (
                                               user_id INTEGER PRIMARY KEY REFERENCES users(id) ON DELETE CASCADE,
                                               account_type_id INTEGER NOT NULL REFERENCES account_types(id),
                                               completed_cycles INTEGER NOT NULL DEFAULT 0,
                                               date_of_birth DATE,
                                               health_notes TEXT
);

-- Discount rules for different account types and/or completed cycles
CREATE TABLE IF NOT EXISTS discount_rules (
                                              id SERIAL PRIMARY KEY,
                                              name VARCHAR(50) NOT NULL,
                                              account_type_id INTEGER REFERENCES account_types(id),
                                              min_completed_cycles INTEGER,
                                              discount_percentage NUMERIC(5, 2) NOT NULL CHECK (discount_percentage BETWEEN 0 AND 100),
                                              active BOOLEAN NOT NULL DEFAULT TRUE,
                                              CONSTRAINT unique_discount_rule UNIQUE (account_type_id, min_completed_cycles)
);

-- Orders (clients purchasing training cycles)
CREATE TABLE IF NOT EXISTS orders (
                                      id SERIAL PRIMARY KEY,
                                      client_id INTEGER NOT NULL REFERENCES users(id),
                                      training_cycle_id INTEGER NOT NULL REFERENCES training_cycles(id),
                                      trainer_id INTEGER REFERENCES users(id),
                                      original_price NUMERIC(10, 2) NOT NULL CHECK (original_price >= 0),
                                      discount_percentage NUMERIC(5, 2) NOT NULL DEFAULT 0 CHECK (discount_percentage BETWEEN 0 AND 100),
                                      final_price NUMERIC(10, 2) NOT NULL CHECK (final_price >= 0),
                                      status VARCHAR(20) NOT NULL DEFAULT 'PENDING', -- PENDING, PAID, COMPLETED, CANCELLED
                                      order_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                      payment_date TIMESTAMP,
                                      completion_date TIMESTAMP
);

-- Equipment types available in the fitness center
CREATE TABLE IF NOT EXISTS equipment (
                                         id SERIAL PRIMARY KEY,
                                         name VARCHAR(100) NOT NULL UNIQUE,
                                         description TEXT
);

-- Assignment categories (exercise, equipment, diet)
CREATE TABLE IF NOT EXISTS assignment_categories (
                                                     id SERIAL PRIMARY KEY,
                                                     name VARCHAR(50) NOT NULL UNIQUE
);

-- Assignments from trainers to clients
CREATE TABLE IF NOT EXISTS assignments (
                                           id SERIAL PRIMARY KEY,
                                           order_id INTEGER NOT NULL REFERENCES orders(id) ON DELETE CASCADE,
                                           category_id INTEGER NOT NULL REFERENCES assignment_categories(id),
                                           trainer_id INTEGER NOT NULL REFERENCES users(id),
                                           title VARCHAR(100) NOT NULL,
                                           description TEXT NOT NULL,
                                           schedule TEXT,
                                           status VARCHAR(20) NOT NULL DEFAULT 'ASSIGNED', -- ASSIGNED, ACCEPTED, REJECTED, COMPLETED
                                           created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                           updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Equipment needed for assignments
CREATE TABLE IF NOT EXISTS assignment_equipment (
                                                    assignment_id INTEGER NOT NULL REFERENCES assignments(id) ON DELETE CASCADE,
                                                    equipment_id INTEGER NOT NULL REFERENCES equipment(id),
                                                    PRIMARY KEY (assignment_id, equipment_id)
);

-- Client reviews for completed training cycles
CREATE TABLE IF NOT EXISTS reviews (
                                       id SERIAL PRIMARY KEY,
                                       order_id INTEGER NOT NULL UNIQUE REFERENCES orders(id),
                                       rating INTEGER NOT NULL CHECK (rating BETWEEN 1 AND 5),
                                       comment TEXT,
                                       created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Initial data for roles
INSERT INTO roles (name) VALUES ('CLIENT'), ('TRAINER'), ('ADMIN');

-- Initial data for assignment categories
INSERT INTO assignment_categories (name) VALUES ('EXERCISE'), ('EQUIPMENT'), ('DIET');

-- Initial data for account types
INSERT INTO account_types (name, description) VALUES
                                                  ('REGULAR', 'Standard individual account'),
                                                  ('CORPORATE', 'Corporate client with special privileges');

-- Initial discount rules
INSERT INTO discount_rules (name, account_type_id, min_completed_cycles, discount_percentage, active)
VALUES
    ('Regular client loyalty', 1, 3, 5.00, true),
    ('Regular client VIP', 1, 10, 15.00, true),
    ('Corporate base discount', 2, 0, 10.00, true),
    ('Corporate loyalty', 2, 5, 20.00, true);