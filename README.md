# FTP_FS
# FTP File System Project

A file system management application with FTP capabilities and MySQL database integration.

## Prerequisites

- Java 17 or higher
- MySQL 8.0 or higher
- Maven

## Setup Instructions

### 1. Database Setup

Ensure MySQL is installed and running. By default, the application will:

- Connect to MySQL at `localhost:3306`
- Create a database named `ftp_fs` if it doesn't exist
- Use the credentials specified in `config.properties`

### 2. Configuration

The application configuration is in `src/main/resources/config.properties`. You can customize:

```properties
db.url=jdbc:mysql://localhost:3306/ftp_fs?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC
db.user=root
db.password=your_password
```

### 3. Build and Run

```bash
mvn clean package
java -cp target/ftp-client-1.0-SNAPSHOT.jar server.MainServer
```

## Troubleshooting

### Database Connection Issues

If you see "Connection refused" errors:

1. **Verify MySQL is Running**
   ```bash
   # macOS/Linux
   sudo service mysql status
   # or
   ps aux | grep mysqld

   # Windows
   # Check Services application
   ```

2. **Check MySQL Port**
   ```bash
   # macOS/Linux
   sudo lsof -i :3306

   # Windows
   netstat -ano | findstr :3306
   ```

3. **Test MySQL Connection**
   ```bash
   mysql -u root -p -h 127.0.0.1
   ```

4. **Verify Credentials**
   Ensure the username and password in `config.properties` are correct.

5. **Check Firewall Settings**
   Ensure your firewall allows connections to port 3306.

## Project Structure

- `server.MainServer`: Main entry point for the server application
- `common.db.DatabaseManager`: Handles database connections and operations
- `common.db.DatabaseConfig`: Loads and manages database configuration

## Contributing

When contributing to this project, please follow the existing code style and add appropriate tests.
Projekt FTP-klient/serwer z bazą danych MySQL i GUI JavaFX

## Uruchomienie

- Klient: `MainClient.java`
- Serwer: `MainServer.java`

## Konfiguracja bazy danych

Ustaw dane dostępowe w pliku `src/main/resources/config.properties`.

## Struktura katalogów

- `client/` – kod klienta
- `server/` – kod serwera
- `common/` – klasy wspólne (modele, narzędzia, obsługa bazy) 

## Uruchomienie przez Docker

Możesz uruchomić serwer w kontenerze Docker. Najpierw zbuduj plik JAR:

```bash
mvn clean package
```

Następnie zbuduj obraz Docker:

```bash
docker build -t ftpfs:latest .
```

Uruchom kontener, przekazując konfigurację przez zmienne środowiskowe (ENV):

```bash
docker run -e DB_URL="jdbc:mysql://adres.mysql:3306/ftp_fs" \
           -e DB_USER="user" \
           -e DB_PASSWORD="haslo" \
           -e SERVER_PORT=2121 \
           -e SERVER_FILES_DIR=/data \
           -p 2121:2121 ftpfs:latest
```

Każda wartość z pliku `config.properties` może być nadpisana przez zmienną środowiskową (ENV), np. `DB_URL`, `DB_USER`, `SERVER_PORT` itd. Szczegóły znajdziesz w komentarzach w pliku `config.properties`. 