ALTER TABLE users
    ADD COLUMN name VARCHAR(255);

UPDATE users
SET name = NULLIF(trim(concat_ws(' ', first_name, last_name)), '');

UPDATE users
SET name = email
WHERE name IS NULL OR name = '';

ALTER TABLE users
    ALTER COLUMN name SET NOT NULL;

ALTER TABLE users
    DROP COLUMN first_name;

ALTER TABLE users
    DROP COLUMN last_name;
