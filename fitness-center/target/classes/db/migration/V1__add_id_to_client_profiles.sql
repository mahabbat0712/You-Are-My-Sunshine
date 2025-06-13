-- Migration script to add ID column to client_profiles table

-- Check if client_profiles table exists
DO $$ 
BEGIN
    -- Temporarily save existing data
    IF EXISTS (SELECT FROM information_schema.tables WHERE table_name = 'client_profiles') THEN
        -- Create temporary table to store data
        CREATE TEMPORARY TABLE temp_client_profiles AS 
        SELECT * FROM client_profiles;
        
        -- Drop existing table
        DROP TABLE client_profiles;
        
        -- Create new table with id column
        CREATE TABLE client_profiles (
            id SERIAL PRIMARY KEY,
            user_id INTEGER NOT NULL UNIQUE REFERENCES users(id) ON DELETE CASCADE,
            account_type_id INTEGER NOT NULL REFERENCES account_types(id),
            completed_cycles INTEGER NOT NULL DEFAULT 0,
            date_of_birth DATE,
            health_notes TEXT
        );
        
        -- Restore data
        INSERT INTO client_profiles (user_id, account_type_id, completed_cycles, date_of_birth, health_notes)
        SELECT user_id, account_type_id, completed_cycles, date_of_birth, health_notes
        FROM temp_client_profiles;
        
        -- Drop temporary table
        DROP TABLE temp_client_profiles;
    END IF;
END $$; 