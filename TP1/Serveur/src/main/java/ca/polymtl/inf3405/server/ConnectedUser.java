package ca.polymtl.inf3405.server;

import java.net.InetAddress;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.time.Instant;

/**
 *
 */
public class ConnectedUser extends User {
    private final String token;
    private volatile Instant lastConnection;
    private final InetAddress userAddress;
    private final int userPort;

    /**
     *
     * @param user
     * @param addr
     * @param port
     */
    public ConnectedUser(User user, InetAddress addr, int port) {
        super(user);
        token = generateToken();
        lastConnection = Instant.now();
        userAddress = addr;
        userPort = port;
    }

    /**
     *
     * @return
     */
    public String getToken() {
        return token;
    }

    /**
     *
     * @return
     */
    public Instant getLastConnection() {
        return lastConnection;
    }

    /**
     *
     * @return
     */
    public InetAddress getUserAddress() {
        return userAddress;
    }

    /**
     *
     * @return
     */
    public int getUserPort() {
        return userPort;
    }

    /**
     *
     */
    public synchronized void updateLastConnection() {
        lastConnection = Instant.now();
    }

    /**
     *
     * @return
     */
    private String generateToken() {
        SecureRandom s = new SecureRandom();
        byte[] token = new byte[256];
        s.nextBytes(token);
        return new String(token, StandardCharsets.UTF_8);
    }
}
