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
    path VARCHAR(1024),
    timestamp DATETIME DEFAULT CURRENT_TIMESTAMP,
    status VARCHAR(50),
    FOREIGN KEY (client_id) REFERENCES Klienci(id)
);

-- File metadata table
CREATE TABLE IF NOT EXISTS Pliki (
    id INT PRIMARY KEY AUTO_INCREMENT,
    filename VARCHAR(255) NOT NULL,
    directory VARCHAR(1024) NOT NULL,
    size BIGINT,
    owner_id INT,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    last_modified DATETIME DEFAULT CURRENT_TIMESTAMP,
    is_directory BOOLEAN DEFAULT FALSE,
    FOREIGN KEY (owner_id) REFERENCES Klienci(id)
);

-- Create default admin user if not exists
INSERT IGNORE INTO Klienci (username, password)
VALUES ('admin', '$2a$10$Mju4RpaR8TXhwp7xgRFynu7kkxKpQrw9Q6AjcJlpf4JBh0QV.Zv1q'); -- Password: admin123
