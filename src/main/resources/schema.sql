-- Database schema for FTP_FS application

-- Create database if not exists
CREATE DATABASE IF NOT EXISTS ftp_fs;

-- Use the database
USE ftp_fs;

-- Client table
CREATE TABLE IF NOT EXISTS Klienci (
    id INT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    last_login DATETIME
);

-- Operation history table
CREATE TABLE IF NOT EXISTS HistoriaOperacji (
    id INT PRIMARY KEY AUTO_INCREMENT,
    client_id INT,
    operation VARCHAR(255) NOT NULL,
    timestamp DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (client_id) REFERENCES Klienci(id)
);
