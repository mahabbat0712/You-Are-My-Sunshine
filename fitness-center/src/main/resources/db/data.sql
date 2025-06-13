-- Sample data for Fitness Center application

-- Administrator user (password: admin123)
INSERT INTO users (username, password, email, first_name, last_name, phone, active)
VALUES ('admin', '$2a$10$uY7FbC4pDuYrMToi0P03..ru7m3MiqTQ/zurpv3QNN5.9E5quamqO', 'admin@fitnesscenter.com', 'Admin', 'User', '+12345678900', true);

-- Assign admin role
INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id FROM users u, roles r WHERE u.username = 'admin' AND r.name = 'ADMIN';

-- Trainers (password: trainer123)
INSERT INTO users (username, password, email, first_name, last_name, phone, active)
VALUES 
  ('trainer1', '$2a$10$NFBYACo1.1jNID9E76SLD.TKkRrPxdVnJv7JU5qJsJCX/9aEb6Aj6', 'john.smith@fitnesscenter.com', 'John', 'Smith', '+12345678901', true),
  ('trainer2', '$2a$10$NFBYACo1.1jNID9E76SLD.TKkRrPxdVnJv7JU5qJsJCX/9aEb6Aj6', 'sarah.johnson@fitnesscenter.com', 'Sarah', 'Johnson', '+12345678902', true),
  ('trainer3', '$2a$10$NFBYACo1.1jNID9E76SLD.TKkRrPxdVnJv7JU5qJsJCX/9aEb6Aj6', 'michael.davis@fitnesscenter.com', 'Michael', 'Davis', '+12345678903', true);

-- Assign trainer roles
INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id FROM users u, roles r WHERE u.username IN ('trainer1', 'trainer2', 'trainer3') AND r.name = 'TRAINER';

-- Trainer profiles
INSERT INTO trainer_profiles (user_id, specialization, experience_years, hourly_rate, bio)
SELECT u.id, 'Strength Training', 7, 50.00, 'Specializes in strength training and bodybuilding. Worked with professional athletes for over 5 years.'
FROM users u WHERE u.username = 'trainer1';

INSERT INTO trainer_profiles (user_id, specialization, experience_years, hourly_rate, bio)
SELECT u.id, 'Cardio & Weight Loss', 5, 45.00, 'Expert in cardiovascular fitness and weight loss programs. Certified nutritionist with experience in custom diet plans.'
FROM users u WHERE u.username = 'trainer2';

INSERT INTO trainer_profiles (user_id, specialization, experience_years, hourly_rate, bio)
SELECT u.id, 'Yoga & Flexibility', 8, 55.00, 'Yoga master with expertise in improving flexibility and core strength. Specializes in rehabilitation programs.'
FROM users u WHERE u.username = 'trainer3';

-- Clients (password: client123)
INSERT INTO users (username, password, email, first_name, last_name, phone, active)
VALUES 
  ('client1', '$2a$10$GypbQkfEbPgS7KUH6J7nFeMMzUb3T6wVT05HJzZ5OYNXo2ynhcfUe', 'alice.walker@email.com', 'Alice', 'Walker', '+12345678904', true),
  ('client2', '$2a$10$GypbQkfEbPgS7KUH6J7nFeMMzUb3T6wVT05HJzZ5OYNXo2ynhcfUe', 'robert.brown@email.com', 'Robert', 'Brown', '+12345678905', true),
  ('client3', '$2a$10$GypbQkfEbPgS7KUH6J7nFeMMzUb3T6wVT05HJzZ5OYNXo2ynhcfUe', 'jennifer.white@company.com', 'Jennifer', 'White', '+12345678906', true),
  ('client4', '$2a$10$GypbQkfEbPgS7KUH6J7nFeMMzUb3T6wVT05HJzZ5OYNXo2ynhcfUe', 'david.miller@company.com', 'David', 'Miller', '+12345678907', true);

-- Assign client roles
INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id FROM users u, roles r WHERE u.username IN ('client1', 'client2', 'client3', 'client4') AND r.name = 'CLIENT';

-- Client profiles
INSERT INTO client_profiles (user_id, account_type_id, completed_cycles, date_of_birth, health_notes)
SELECT u.id, 1, 2, '1990-03-15', 'No significant health issues. Focused on building muscle mass.'
FROM users u WHERE u.username = 'client1';

INSERT INTO client_profiles (user_id, account_type_id, completed_cycles, date_of_birth, health_notes)
SELECT u.id, 1, 0, '1985-07-22', 'Recovering from knee injury. Needs low-impact exercises.'
FROM users u WHERE u.username = 'client2';

INSERT INTO client_profiles (user_id, account_type_id, completed_cycles, date_of_birth, health_notes)
SELECT u.id, 2, 3, '1988-11-10', 'Corporate client with focus on weight loss and stress management.'
FROM users u WHERE u.username = 'client3';

INSERT INTO client_profiles (user_id, account_type_id, completed_cycles, date_of_birth, health_notes)
SELECT u.id, 2, 1, '1992-04-28', 'Corporate client focusing on general fitness and endurance.'
FROM users u WHERE u.username = 'client4';

-- Training Cycles
INSERT INTO training_cycles (name, description, duration_days, price, active, created_at)
VALUES
  ('Weight Loss Starter', 'A beginner-friendly program designed for weight loss. Includes cardio exercises, light strength training, and nutritional guidance.', 30, 199.99, true, CURRENT_TIMESTAMP - INTERVAL '30 days'),
  
  ('Muscle Building', 'Intensive program focused on muscle gain and strength. Includes progressive strength training, dietary recommendations for muscle growth, and recovery techniques.', 60, 299.99, true, CURRENT_TIMESTAMP - INTERVAL '45 days'),
  
  ('Flexibility & Core', 'Program designed to improve flexibility, core strength, and posture. Includes yoga sessions, Pilates, and targeted core exercises.', 45, 249.99, true, CURRENT_TIMESTAMP - INTERVAL '20 days'),
  
  ('Athletic Performance', 'Advanced program for improving athletic performance. Includes explosive strength training, agility drills, and sports-specific exercises.', 90, 399.99, true, CURRENT_TIMESTAMP - INTERVAL '15 days'),
  
  ('Senior Fitness', 'Specialized program for seniors focusing on maintaining mobility, balance, and strength. Includes low-impact exercises and joint-friendly workouts.', 30, 179.99, true, CURRENT_TIMESTAMP - INTERVAL '10 days'),
  
  ('Corporate Wellness', 'Designed for busy professionals. Focuses on stress reduction, quick effective workouts, and exercises that can be done at a desk.', 30, 229.99, true, CURRENT_TIMESTAMP - INTERVAL '5 days'),
  
  ('Post-Rehabilitation', 'Program for clients recovering from injuries. Focuses on gradual strengthening, improved range of motion, and pain reduction.', 45, 279.99, false, CURRENT_TIMESTAMP - INTERVAL '60 days');

-- Equipment
INSERT INTO equipment (name, description)
VALUES
  ('Treadmill', 'Professional-grade treadmill with adjustable incline and speed settings'),
  ('Dumbbells Set', 'Set of dumbbells ranging from 5 to 50 pounds'),
  ('Exercise Ball', 'Stability ball for core workouts and balance training'),
  ('Resistance Bands', 'Set of resistance bands with different strengths'),
  ('Yoga Mat', 'Non-slip yoga mat for floor exercises'),
  ('Kettlebells', 'Various weights of kettlebells for functional training'),
  ('Pull-up Bar', 'Adjustable pull-up bar for upper body strength'),
  ('Rowing Machine', 'Professional rowing machine for full body workouts'),
  ('Jump Rope', 'Adjustable jump rope for cardio training'),
  ('Foam Roller', 'Foam roller for myofascial release and recovery');

-- Sample orders 
INSERT INTO orders (client_id, training_cycle_id, trainer_id, original_price, discount_percentage, final_price, status, order_date, payment_date, completion_date)
SELECT 
  c.id, -- client_id
  tc.id, -- training_cycle_id
  t.id, -- trainer_id
  tc.price, -- original_price
  CASE WHEN cp.account_type_id = 2 THEN 10.0 ELSE 0.0 END, -- discount_percentage (10% for corporate clients)
  tc.price * (1 - CASE WHEN cp.account_type_id = 2 THEN 0.1 ELSE 0 END), -- final_price
  'COMPLETED', -- status
  CURRENT_TIMESTAMP - INTERVAL '60 days', -- order_date
  CURRENT_TIMESTAMP - INTERVAL '59 days', -- payment_date
  CURRENT_TIMESTAMP - INTERVAL '30 days' -- completion_date
FROM
  users c, training_cycles tc, users t, client_profiles cp
WHERE
  c.username = 'client1' AND
  tc.name = 'Weight Loss Starter' AND
  t.username = 'trainer2' AND
  cp.user_id = c.id;

INSERT INTO orders (client_id, training_cycle_id, trainer_id, original_price, discount_percentage, final_price, status, order_date, payment_date)
SELECT 
  c.id, -- client_id
  tc.id, -- training_cycle_id
  t.id, -- trainer_id
  tc.price, -- original_price
  0.0, -- discount_percentage
  tc.price, -- final_price
  'PAID', -- status
  CURRENT_TIMESTAMP - INTERVAL '15 days', -- order_date
  CURRENT_TIMESTAMP - INTERVAL '14 days' -- payment_date
FROM
  users c, training_cycles tc, users t
WHERE
  c.username = 'client2' AND
  tc.name = 'Flexibility & Core' AND
  t.username = 'trainer3';

INSERT INTO orders (client_id, training_cycle_id, trainer_id, original_price, discount_percentage, final_price, status, order_date, payment_date)
SELECT 
  c.id, -- client_id
  tc.id, -- training_cycle_id
  t.id, -- trainer_id
  tc.price, -- original_price
  10.0, -- discount_percentage (corporate discount)
  tc.price * 0.9, -- final_price (10% off)
  'PAID', -- status
  CURRENT_TIMESTAMP - INTERVAL '10 days', -- order_date
  CURRENT_TIMESTAMP - INTERVAL '9 days' -- payment_date
FROM
  users c, training_cycles tc, users t
WHERE
  c.username = 'client3' AND
  tc.name = 'Corporate Wellness' AND
  t.username = 'trainer1';

INSERT INTO orders (client_id, training_cycle_id, original_price, discount_percentage, final_price, status, order_date)
SELECT 
  c.id, -- client_id
  tc.id, -- training_cycle_id
  tc.price, -- original_price
  0.0, -- discount_percentage
  tc.price, -- final_price
  'PENDING', -- status
  CURRENT_TIMESTAMP - INTERVAL '2 days' -- order_date
FROM
  users c, training_cycles tc
WHERE
  c.username = 'client4' AND
  tc.name = 'Muscle Building';

-- Sample assignments
INSERT INTO assignments (order_id, category_id, trainer_id, title, description, schedule, status, created_at)
SELECT 
  o.id, -- order_id
  ac.id, -- category_id
  o.trainer_id, -- trainer_id
  'Daily Cardio Routine', -- title
  'Start with 10 minutes of brisk walking, followed by 15 minutes of jogging at moderate intensity, and finish with 5 minutes of cool-down walking.', -- description
  'Monday, Wednesday, Friday - Morning', -- schedule
  'COMPLETED', -- status
  o.order_date + INTERVAL '1 day' -- created_at
FROM
  orders o, assignment_categories ac
WHERE
  o.status = 'COMPLETED' AND
  ac.name = 'EXERCISE';

INSERT INTO assignments (order_id, category_id, trainer_id, title, description, schedule, status, created_at)
SELECT 
  o.id, -- order_id
  ac.id, -- category_id
  o.trainer_id, -- trainer_id
  'Strength Training Circuit', -- title
  'Complete 3 rounds of: 12 squats, 10 push-ups, 15 lunges per leg, and 30-second plank.', -- description
  'Tuesday, Thursday, Saturday - Afternoon', -- schedule
  'COMPLETED', -- status
  o.order_date + INTERVAL '2 days' -- created_at
FROM
  orders o, assignment_categories ac
WHERE
  o.status = 'COMPLETED' AND
  ac.name = 'EXERCISE';

INSERT INTO assignments (order_id, category_id, trainer_id, title, description, schedule, status, created_at)
SELECT 
  o.id, -- order_id
  ac.id, -- category_id
  o.trainer_id, -- trainer_id
  'Nutrition Plan', -- title
  'Follow this meal plan: Breakfast: Protein smoothie with fruits. Lunch: Grilled chicken salad. Dinner: Baked fish with steamed vegetables. Snacks: Greek yogurt, almonds, or fruit.', -- description
  'Daily', -- schedule
  'COMPLETED', -- status
  o.order_date + INTERVAL '1 day' -- created_at
FROM
  orders o, assignment_categories ac
WHERE
  o.status = 'COMPLETED' AND
  ac.name = 'DIET';

-- Assignment equipment
INSERT INTO assignment_equipment (assignment_id, equipment_id)
SELECT 
  a.id, -- assignment_id
  e.id -- equipment_id
FROM
  assignments a, equipment e
WHERE
  a.title = 'Daily Cardio Routine' AND
  e.name = 'Treadmill';

INSERT INTO assignment_equipment (assignment_id, equipment_id)
SELECT 
  a.id, -- assignment_id
  e.id -- equipment_id
FROM
  assignments a, equipment e
WHERE
  a.title = 'Strength Training Circuit' AND
  e.name = 'Dumbbells Set';

INSERT INTO assignment_equipment (assignment_id, equipment_id)
SELECT 
  a.id, -- assignment_id
  e.id -- equipment_id
FROM
  assignments a, equipment e
WHERE
  a.title = 'Strength Training Circuit' AND
  e.name = 'Exercise Ball';

-- Sample reviews
INSERT INTO reviews (order_id, rating, comment, created_at)
SELECT
  o.id, -- order_id
  5, -- rating
  'Great program! I lost 10 pounds and feel much more energetic. The trainer was very supportive and motivating.', -- comment
  o.completion_date + INTERVAL '2 days' -- created_at
FROM
  orders o
WHERE
  o.status = 'COMPLETED'; 