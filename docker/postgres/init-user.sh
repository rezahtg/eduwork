#!/bin/bash
set -e

psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname "$POSTGRES_DB" <<-EOSQL
    -- Ensure the user has the correct password
    ALTER USER eduwork_user WITH PASSWORD 'eduwork_pass';
    
    -- Grant all privileges
    GRANT ALL PRIVILEGES ON DATABASE eduwork_dev TO eduwork_user;
    
    -- Confirm user exists
    SELECT 'User created successfully: ' || usename FROM pg_user WHERE usename = 'eduwork_user';
EOSQL
