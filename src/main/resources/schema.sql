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

-- Files table
CREATE TABLE IF NOT EXISTS Pliki (
    id INT PRIMARY KEY AUTO_INCREMENT,
    filename VARCHAR(255) NOT NULL,
    size BIGINT NOT NULL,
    owner_id INT NOT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (owner_id) REFERENCES Klienci(id),
    UNIQUE KEY unique_filename_owner (filename, owner_id)
);
