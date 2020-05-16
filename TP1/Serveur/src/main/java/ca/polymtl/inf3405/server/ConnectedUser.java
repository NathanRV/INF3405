package ca.polymtl.inf3405.server;

import ca.polymtl.inf3405.protocol.request.Request;

import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.time.Instant;

public class ConnectedUser extends User {
    public final String token;
    public volatile Instant lastConnection;

    public ConnectedUser(User user) {
        super(user);
        token = generateToken();
        lastConnection = Instant.now();
    }

    public String getToken() {
        return token;
    }

    public Instant getLastConnection() {
        return lastConnection;
    }

    public synchronized void updateLastConnection() {
        lastConnection = Instant.now();
    }

    private String generateToken() {
        SecureRandom s = new SecureRandom();
        byte[] token = new byte[Request.AUTH_LEN];
        s.nextBytes(token);
        return new String(token, StandardCharsets.UTF_8);
    }
}
