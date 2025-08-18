-- Create database if not exists (PostgreSQL creates it automatically)
-- Set default character set and collation
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;

-- Create extensions if needed
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE EXTENSION IF NOT EXISTS "pg_trgm";

-- Set timezone
SET timezone = 'UTC';

