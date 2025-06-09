from ftplib import FTP
import sys
import socket

def test_ftp_connection(host, port=2121):
    try:
        # Create FTP connection
        ftp = FTP()
        print(f"Connecting to {host}:{port}...")
        
        # Set a longer timeout
        ftp.connect(host=host, port=port, timeout=30)
        
        # Try to login (you'll need to provide valid credentials)
        print("Attempting login...")
        ftp.login(user="test", passwd="test")
        
        # List directory contents
        print("\nDirectory listing:")
        ftp.retrlines('LIST')
        
        # Close connection
        ftp.quit()
        print("\nConnection test successful!")
        return True
        
    except socket.timeout:
        print("\nError: Connection timed out. The server might not be accessible or the port might be blocked.")
        return False
    except ConnectionRefusedError:
        print("\nError: Connection refused. The server might not be running or the port might be closed.")
        return False
    except Exception as e:
        print(f"\nError: {str(e)}")
        return False

if __name__ == "__main__":
    # Try both with and without the port in the hostname
    hosts = [
        "ftpfs-app.greenpebble-56bf6262.westeurope.azurecontainerapps.io",
        "ftpfs-app.greenpebble-56bf6262.westeurope.azurecontainerapps.io:2121"
    ]
    
    for host in hosts:
        print(f"\nTrying connection to {host}...")
        if test_ftp_connection(host):
            print(f"Successfully connected to {host}")
            break 