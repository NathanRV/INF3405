package protocol;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import exceptions.MessageSizeException;

import javax.swing.text.StyleContext;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.Date;

/**
 *
 */
final public class Message {
    private final String senderName;
    private final String senderIp;
    private final Integer senderPort;
    private final String time;
    private final String message;
    protected static String pattern = "yyyy-MM-dd@HH:mm:ss";
    protected static SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);

    private static final int MAXIMUM_SIZE = 200;

    /**
     *
     * @param senderName
     * @param senderIp
     * @param senderPort
     * @param message
     * @throws MessageSizeException
     */
    public Message(String senderName, String senderIp, Integer senderPort, String message)
            throws MessageSizeException {
        if (message.length() > MAXIMUM_SIZE) {
            throw new MessageSizeException("La taille maximale du message est de 200 caractères.");
        }

        this.senderName = senderName;
        this.senderIp = senderIp;
        this.senderPort = senderPort;
        this.time = simpleDateFormat.format(new Date());
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

    /**
     *
     * @return
     */
    public String toConsole() {
        return "[" + senderName + "]: " + message;
    }
}
