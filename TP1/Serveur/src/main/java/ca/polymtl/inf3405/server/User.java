package ca.polymtl.inf3405.server;

import de.rtner.security.auth.spi.SimplePBKDF2;

/**
 * Classe représentant un utilisateur du serveur
 */
public class User {
    private String userName, passwordHash;

    /**
     * Constructeur qui attribue un mot de passe temporaire
     * @param userName le nom de l'utilisateur
     */
    public User(String userName) {
        this.userName = userName;
        this.passwordHash = "\0";
    }

    /**
     * Constructeur qui crée un utilisateur à l'aide du hash correspondant
     * @param userName     le nom de l'utilisateur
     * @param passwordHash le hash du mot de passe
     */
    public User(String userName, String passwordHash) {
        this.userName = userName;
        this.passwordHash = passwordHash;
    }

    /**
     * Constructeur par copie
     * @param user l'utililisateur à copier
     */
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

    /**
     * Méthode qui permet de convertir le mot de passe en hash sécuritaire
     * @param password le mot de passe à hasher
     */
    public void setPassword(String password) {
        this.passwordHash = new SimplePBKDF2().deriveKeyFormatted(password);
    }

    /**
     * Méthode qui vérifie si le mot de passe est valide
     * @param password le mot de passe à vérifier
     * @return         vrai si le mot de passe est valide, faux sinon
     */
    public boolean isGoodPassword(String password) {
        return new SimplePBKDF2().verifyKeyFormatted(passwordHash, password);
    }
}
