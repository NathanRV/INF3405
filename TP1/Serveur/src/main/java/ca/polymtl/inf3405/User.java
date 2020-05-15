package ca.polymtl.inf3405;

import de.rtner.security.auth.spi.SimplePBKDF2;

public class User {
    private String userName, passwordHash;

    public User(String userName, String passwordHash) {
        this.userName = userName;
        this.passwordHash = passwordHash;
    }

    public String getUserName() {
        return userName;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public void setPassword(String password) {
        this.passwordHash = new SimplePBKDF2().deriveKeyFormatted(password);
    }

    public boolean isGoodPassword(String password) {
        return new SimplePBKDF2().verifyKeyFormatted(passwordHash, password);
    }
}
