-- Add finance_user_key column to users table and create a unique index
ALTER TABLE users
    ADD COLUMN IF NOT EXISTS finance_user_key VARCHAR(100);

-- Create a unique index for finance_user_key to avoid duplicates (nullable allowed)
CREATE UNIQUE INDEX IF NOT EXISTS ux_users_finance_user_key ON users(finance_user_key);

