package ca.polymtl.inf3405.exceptions;

/**
 * Exception lancée lorsque l'utilisateur recherché ne se retrouve pas dans la base de données
 */
public class NoUserException extends Exception {
    /**
     * Constructeur de l'exception
     * @param message le message de l'exception
     */
    public NoUserException(String message) {
        super(message);
    }
}
