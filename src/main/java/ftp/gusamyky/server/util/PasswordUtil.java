package ftp.gusamyky.server.util;

import org.mindrot.jbcrypt.BCrypt;

public class PasswordUtil {
    public static String hashPassword(String password) {
        return BCrypt.hashpw(password, BCrypt.gensalt());
    }

    public static boolean verifyPassword(String password, String hash) {
        System.out.println(BCrypt.checkpw("admin123", "$2a$12$9eF4zGh7os.P1vy2AnhI.eoMTYL0Qz7dwaf8JcLQPlddaB3JhrciG"));
        return BCrypt.checkpw(password, hash);
    }
}