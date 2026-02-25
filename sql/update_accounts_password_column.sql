-- Update the password column to support BCrypt hashes (which are 60 characters long)
-- The previous size was VARCHAR(45), which is insufficient for BCrypt.
ALTER TABLE accounts MODIFY password VARCHAR(255);
