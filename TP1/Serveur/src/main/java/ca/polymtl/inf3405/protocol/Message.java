package ca.polymtl.inf3405.protocol;

import ca.polymtl.inf3405.exceptions.MessageSizeException;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import java.time.Instant;

/**
 *
 */
final public class Message {
    private final String senderName;
    private final String senderIp;
    private final Integer senderPort;
    private final String time;
    private final String message;

    private static final int MAXIMUM_SIZE = 200;

    /**
     *
     * @param senderName
     * @param senderIp
     * @param senderPort
     * @param time
     * @param message
     * @throws MessageSizeException
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

    /**
     *
     * @return
     */
    public String getSenderName() {
        return senderName;
    }

    /**
     *
     * @return
     */
    public String getSenderIp() {
        return senderIp;
    }

    /**
     *
     * @return
     */
    public Integer getSenderPort() {
        return senderPort;
    }

    /**
     *
     * @return
     */
    public String getTime() {
        return time;
    }

    /**
     *
     * @return
     */
    public String getMessage() {
        return message;
    }

    /**
     *
     * @return
     */
    public String encodeMessage() {
        Gson gson = new Gson();
        return gson.toJson(this);
    }

    /**
     *
     * @param string
     * @return
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
