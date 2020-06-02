package ca.polymtl.inf3405.exceptions;

/**
 * Exception lancée lorsqu'il y a une erreur d'insertion dans la base de données
 */
public class DatabaseInsertionException extends Exception {
    /**
     * Constructeur de l'exception
     * @param message le message de l'exception
     */
    public DatabaseInsertionException(String message) {
        super(message);
    }
}
