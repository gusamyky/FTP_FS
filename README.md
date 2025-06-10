# FTP_FS - Secure File Transfer System

## Project Overview

FTP_FS is a secure file transfer system that provides a robust and reliable way to transfer files between clients and servers. The system implements a custom FTP-like protocol with enhanced security features and user management capabilities.

### Key Features

- Secure user authentication with password hashing (using jBCrypt)
- File upload and download capabilities
- User registration and management
- Operation history tracking
- Concurrent client handling with thread pooling
- Configurable server settings
- MySQL database integration for persistent storage
- Comprehensive logging system
- Graceful server shutdown handling

## Prerequisites

- Java 17 or higher
- MySQL Server 8.0 or higher
- Maven 3.6 or higher
- Git (for cloning the repository)

## Installation and Setup

1. **Clone the repository**
   ```bash
   git clone https://github.com/gusamyky/FTP_FS
   cd FTP_FS
   ```

2. **Set up MySQL Database**
   - Follow the detailed instructions in `MYSQL_SETUP.md` to set up the MySQL database
   - The database schema will be automatically initialized on first run
   - Ensure MySQL service is running before starting the application

3. **Configure the Application**
   Create a `config.properties` file in `src/main/resources/` with the following settings:
   ```properties
   # Server Configuration
   server.port=2121
   server.host=0.0.0.0
   server.filesDir=server_files

   # Database Configuration
   db.url=jdbc:mysql://localhost:3306/ftp_fs?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC
   db.user=your_username
   db.password=your_password
   db.urlNoDb=jdbc:mysql://localhost:3306/
   ```

4. **Build the Project**
   ```bash
   mvn clean install
   ```

## Running the Application

### Starting the Server
```bash
java -jar target/ftp-server-1.0-SNAPSHOT.jar
```

The server will start listening on the configured port (default: 2121).

### Client Usage
The system supports multiple clients connecting simultaneously. Each client can:
- Register new accounts
- Login with existing credentials
- Upload and download files
- View file listings
- Check operation history
- Generate usage reports

## Project Structure

```
FTP_FS/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   ├── common/           # Common utilities and models
│   │   │   │   ├── models/      # Data models
│   │   │   │   └── utils/       # Utility classes
│   │   │   └── ftp/
│   │   │       ├── client/       # Client-side implementation
│   │   │       └── server/       # Server-side implementation
│   │   └── resources/
│   │       ├── config.properties # Configuration file
│   │       └── schema.sql       # Database schema
│   └── test/                    # Test files
├── server_files/               # Default directory for file storage
├── pom.xml                    # Maven configuration
├── README.md                  # This file
└── MYSQL_SETUP.md            # MySQL setup instructions
```

## Development Guidelines

### Code Style
- Follow Java coding conventions
- Use meaningful variable and method names
- Add comments for complex logic
- Keep methods focused and concise
- Use proper exception handling
- Implement comprehensive logging

### Building and Testing
```bash
# Build the project
mvn clean install

# Run tests
mvn test
```

### Version Control
- Use feature branches for new development
- Create pull requests for code review
- Keep commits atomic and well-documented
- Follow conventional commit messages

## Error Handling and Logging

The application uses a comprehensive logging system with the following features:
- Console logging for development
- Detailed error messages for troubleshooting
- Exception handling with proper cleanup
- Graceful shutdown handling

### Common Error Scenarios

1. **Database Connection Issues**
   - Connection refused errors
   - Authentication failures
   - Schema initialization problems

2. **File Operation Issues**
   - Permission denied errors
   - Disk space limitations
   - File locking conflicts

3. **Network Issues**
   - Connection timeouts
   - Port conflicts
   - Firewall restrictions

4. **Authentication Issues**
   - Invalid credentials
   - Session expiration
   - Account lockouts

## Configuration

### Server Configuration
- `server.port`: Port number for the server (default: 2121)
- `server.host`: Host address to bind to (default: 0.0.0.0)
- `server.filesDir`: Directory for storing uploaded files

### Database Configuration
- `db.url`: MySQL database URL with connection parameters
- `db.user`: Database username
- `db.password`: Database password
- `db.urlNoDb`: Base database URL without database name

## Protocol Documentation

The system implements a custom FTP-like protocol with the following commands:

### Authentication Commands
- `LOGIN <username> <password>`: User authentication
- `REGISTER <username> <password>`: New user registration
- `LOGOUT`: End user session

### File Operation Commands
- `UPLOAD <filename> <size>`: File upload
- `DOWNLOAD <filename>`: File download
- `LIST [path]`: List files in directory

### System Commands
- `HISTORY`: View operation history
- `REPORT`: Generate usage report
- `ECHO`: Test connection

### Command Format
Each command follows a simple text-based protocol:
```
COMMAND_NAME [arguments]
```

## Troubleshooting

### Common Issues and Solutions

1. **Database Connection Issues**
   - Verify MySQL service is running
   - Check database credentials in config.properties
   - Ensure database schema is properly initialized
   - Check MySQL error logs for detailed information

2. **File Operation Issues**
   - Check file permissions in server_files directory
   - Verify sufficient disk space
   - Check file size limits
   - Ensure proper file locking mechanisms
   - Monitor system resources

3. **Network Issues**
   - Verify port availability
   - Check firewall settings
   - Ensure correct host configuration
   - Test network connectivity

4. **Authentication Issues**
   - Verify password hashing is working correctly
   - Check user credentials
   - Ensure proper session management
   - Monitor authentication logs

## Security Considerations

1. **Password Security**
   - Passwords are hashed using jBCrypt
   - Salt is automatically generated
   - Secure password storage in database

2. **File Security**
   - Secure file transfer
   - Access control based on user permissions
   - File integrity verification

3. **Network Security**
   - Configurable host binding
   - Port security
   - Connection encryption


## Credits

- MySQL Connector/J for database connectivity
- jBCrypt for password hashing
- Contributors and maintainers of the project

## Support

For issues, feature requests, or contributions:
1. Check existing issues
2. Create a new issue with detailed information
3. Follow the contribution guidelines
