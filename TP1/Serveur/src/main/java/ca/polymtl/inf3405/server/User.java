package ca.polymtl.inf3405.server;

import de.rtner.security.auth.spi.SimplePBKDF2;

/**
 *
 */
public class User {
    private String userName, passwordHash;

    /**
     *
     * @param userName
     */
    public User(String userName) {
        this.userName = userName;
        this.passwordHash = "\0";
    }

    /**
     *
     * @param userName
     * @param passwordHash
     */
    public User(String userName, String passwordHash) {
        this.userName = userName;
        this.passwordHash = passwordHash;
    }

    /**
     *
     * @param user
     */
    public User(User user) {
        this.userName = user.userName;
        this.passwordHash = user.passwordHash;
    }

    /**
     *
     * @return
     */
    public String getUserName() {
        return userName;
    }

    /**
     *
     * @return
     */
    public String getPasswordHash() {
        return passwordHash;
    }

    /**
     *
     * @param userName
     */
    public void setUserName(String userName) {
        this.userName = userName;
    }

    /**
     *
     * @param password
     */
    public void setPassword(String password) {
        this.passwordHash = new SimplePBKDF2().deriveKeyFormatted(password);
    }

    /**
     *
     * @param password
     * @return
     */
    public boolean isGoodPassword(String password) {
        return new SimplePBKDF2().verifyKeyFormatted(passwordHash, password);
    }
}
