# MySQL Setup Guide for FTP_FS Application

## Installation Instructions

### Windows

1. **Download MySQL Installer**:
   - Go to the [MySQL Downloads page](https://dev.mysql.com/downloads/installer/)
   - Download the MySQL Installer for Windows

2. **Run the Installer**:
   - Choose "Full" installation option
   - Complete the installation wizard, setting root password when prompted

3. **Start MySQL Service**:
   - Go to Services (press Win+R, type `services.msc`)
   - Find "MySQL80" or similar service
   - Ensure it's set to "Automatic" startup
   - Right-click and select "Start" if not running

### macOS

1. **Using Homebrew** (recommended):
   ```bash
   brew install mysql
   brew services start mysql
   ```

2. **Or download the DMG package**:
   - Go to [MySQL Downloads](https://dev.mysql.com/downloads/mysql/)
   - Download and install the DMG package
   - Start MySQL from System Preferences

### Linux (Ubuntu/Debian)

```bash
sudo apt update
sudo apt install mysql-server
sudo systemctl start mysql
sudo systemctl enable mysql
```

## Configuration for FTP_FS

1. **Secure your MySQL installation** (recommended):
   ```bash
   sudo mysql_secure_installation
   ```

2. **Set up the database and user**:
   ```sql
   CREATE DATABASE ftp_fs;
   CREATE USER 'ftp_user'@'localhost' IDENTIFIED BY 'your_password';
   GRANT ALL PRIVILEGES ON ftp_fs.* TO 'ftp_user'@'localhost';
   FLUSH PRIVILEGES;
   ```

3. **Update the application's config.properties**:
   Edit `src/main/resources/config.properties` to match your setup:
   ```properties
   db.url=jdbc:mysql://localhost:3306/ftp_fs?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC
   db.user=ftp_user
   db.password=your_password
   ```

## Troubleshooting

### Common Issues

1. **MySQL service not running**
   - Windows: Check Services console
   - macOS: `brew services list` or System Preferences
   - Linux: `sudo systemctl status mysql`

2. **Connection refused errors**
   - Ensure MySQL is running on port 3306: `sudo netstat -tuln | grep 3306`
   - Check firewall settings: MySQL port needs to be open
   - Verify MySQL is bound to the correct network interface

3. **Authentication issues**
   - Verify username and password in config.properties
   - For MySQL 8+, you might need to use `mysql_native_password` authentication:
     ```sql
     ALTER USER 'ftp_user'@'localhost' IDENTIFIED WITH mysql_native_password BY 'your_password';
     ```

4. **Database access permissions**
   - Ensure the user has appropriate permissions:
     ```sql
     SHOW GRANTS FOR 'ftp_user'@'localhost';
     ```

### Testing Your Connection

Test your connection from command line:

```bash
mysql -u ftp_user -p -h localhost ftp_fs
```

If this works but the application still fails, check your JDBC connection string and credentials in the application.
