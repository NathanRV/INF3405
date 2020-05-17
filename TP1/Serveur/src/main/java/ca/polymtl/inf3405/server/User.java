package ca.polymtl.inf3405.server;

import de.rtner.security.auth.spi.SimplePBKDF2;

public class User {
    private String userName, passwordHash;

    public User(String userName) {
        this.userName = userName;
        this.passwordHash = "\0";
    }

    public User(String userName, String passwordHash) {
        this.userName = userName;
        this.passwordHash = passwordHash;
    }

    public User(User user) {
        this.userName = user.userName;
        this.passwordHash = user.passwordHash;
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

    public void setPassword(String password) {
        this.passwordHash = new SimplePBKDF2().deriveKeyFormatted(password);
    }

    public boolean isGoodPassword(String password) {
        return new SimplePBKDF2().verifyKeyFormatted(passwordHash, password);
    }
}
