package ca.polymtl.inf3405.server;

import java.net.InetAddress;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.time.Instant;

/**
 * Classe qui représente un utilisateur connecté au serveur
 */
public class ConnectedUser extends User {
    private final String token;
    private final InetAddress userAddress;
    private final int userPort;

    /**
     * Constructeur par défaut
     * @param user l'utilisateur
     * @param addr l'adresse de l'utilisateur
     * @param port le port de l'utilisateur
     */
    public ConnectedUser(User user, InetAddress addr, int port) {
        super(user);
        token = generateToken();
        userAddress = addr;
        userPort = port;
    }

    public String getToken() {
        return token;
    }

    public InetAddress getUserAddress() {
        return userAddress;
    }

    public int getUserPort() {
        return userPort;
    }

    /**
     * Génère un token aléatoire pour des fins d'authentification
     * @return
     */
    private String generateToken() {
        SecureRandom s = new SecureRandom();
        byte[] token = new byte[256];
        s.nextBytes(token);
        return new String(token, StandardCharsets.UTF_8);
    }
}
