package serveur;

import java.net.Socket;
import java.time.Instant;

final public class Message {
    private final String senderName;
    private final String senderIp;
    private final Integer senderPort;
    private final Instant time;
    private final String message;

    private static final int MAXIMUM_SIZE = 200;

    public Message(User user, Socket clientSocket, String message) throws MessageSizeException {
        if (message.length() > MAXIMUM_SIZE) {
            throw new MessageSizeException("La taille maximale du message est de 200 caractères.");
        }

        this.senderName = user.getUserName();
        this.senderIp = clientSocket.getRemoteSocketAddress().toString();
        this.senderPort = clientSocket.getPort();
        this.time = Instant.now();
        this.message = message;
    }

    public Message(String senderName, String senderIp, Integer senderPort, Instant time, String message)
            throws MessageSizeException {
        if (message.length() > MAXIMUM_SIZE) {
            throw new MessageSizeException("La taille maximale du message est de 200 caractères.");
        }

        this.senderName = senderName;
        this.senderIp = senderIp;
        this.senderPort = senderPort;
        this.time = time;
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

    public Instant getTime() {
        return time;
    }

    public String getMessage() {
        return message;
    }
}
