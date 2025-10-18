package app.security.hashing;

import org.mindrot.jbcrypt.BCrypt;

public class PasswordHasher {

    // Hash a password for the first time
    public String hashPassFirstTime(String pw) {
        if (pw == null) {
            throw new IllegalArgumentException("Password cannot be null");
        }
        return BCrypt.hashpw(pw, BCrypt.gensalt());
    }

    // Check that an unencrypted password matches one that has previously been hashed
    public boolean checkPw(String plainTextPw, String hashedPw) {
        return BCrypt.checkpw(plainTextPw, hashedPw);
    }

}
