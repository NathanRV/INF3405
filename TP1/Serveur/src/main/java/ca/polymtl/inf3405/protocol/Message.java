package ca.polymtl.inf3405.protocol;

import ca.polymtl.inf3405.exceptions.MessageSizeException;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import java.time.Instant;

/**
 * Classe qui représente un message envoyé par un client
 */
final public class Message {
    private final String senderName;
    private final String senderIp;
    private final Integer senderPort;
    private final String time;
    private final String message;

    private static final int MAXIMUM_SIZE = 200;

    /**
     * Constructeur par défaut
     * @param senderName            l'utilisateur qui envoie le message
     * @param senderIp              l'adresse ip de l'utilisateur qui envoie le message
     * @param senderPort            le port de l'utilisateur qui envoie le message
     * @param time                  l'instant de l'envoi du message
     * @param message               le message envoyé
     * @throws MessageSizeException lorsque la taille du message excède la taille maximale
     */
    public Message(String senderName, String senderIp, Integer senderPort, Instant time, String message)
            throws MessageSizeException {
        if (message.length() > MAXIMUM_SIZE) {
            throw new MessageSizeException("La taille maximale du message est de 200 caractères.");
        }

        this.senderName = senderName;
        this.senderIp = senderIp;
        this.senderPort = senderPort;
        this.time = time.toString();
        this.message = message;
    }

    public String getSenderName() {
        return senderName;
    }

    public String getSenderIp() {
        return senderIp;
    }

    public Integer getSenderPort() {
        return senderPort;
    }

    public String getTime() {
        return time;
    }

    public String getMessage() {
        return message;
    }

    /**
     * Encode le message dans un format JSON
     * @return le message encodé
     */
    public String encodeMessage() {
        Gson gson = new Gson();
        return gson.toJson(this);
    }

    /**
     * Décode le message en format JSON
     * @param string le message encodé
     * @return       le message décodé
     */
    public static Message decodeMessage(String string) {
        Gson gson = new Gson();
        try {
            return gson.fromJson(string, Message.class);
        } catch (JsonSyntaxException e) {
            return null;
        }
    }

}
