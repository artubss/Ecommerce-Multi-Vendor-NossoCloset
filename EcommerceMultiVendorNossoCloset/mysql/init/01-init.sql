-- Create database if not exists
CREATE DATABASE IF NOT EXISTS ecommerce_multi_vendor CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- Use the database
USE ecommerce_multi_vendor;

-- Create user if not exists and grant privileges
CREATE USER IF NOT EXISTS 'ecommerce_user'@'%' IDENTIFIED BY 'ecommerce_pass';
GRANT ALL PRIVILEGES ON ecommerce_multi_vendor.* TO 'ecommerce_user'@'%';
FLUSH PRIVILEGES;

-- Set default character set
SET NAMES utf8mb4;
SET CHARACTER SET utf8mb4;
SET character_set_connection=utf8mb4;

