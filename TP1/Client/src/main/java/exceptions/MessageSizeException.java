package exceptions;

/**
 * Exception lancée lorsque la taille du message ne respecte pas la taille maximale.
 */
public class MessageSizeException extends Exception {
    /**
     * Constructeur de l'exception
     * @param message le message de l'exception
     */
    public MessageSizeException(String message) {
        super(message);
    }
}
