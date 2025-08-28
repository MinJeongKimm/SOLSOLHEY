-- Drop obsolete username column from users table (idempotent)
ALTER TABLE users DROP COLUMN IF EXISTS username;
