package ca.polymtl.inf3405.protocol.requestEncoding;

import ca.polymtl.inf3405.exceptions.MessageSizeException;
import ca.polymtl.inf3405.protocol.request.Request;
import ca.polymtl.inf3405.protocol.request.Requests;
import ca.polymtl.inf3405.server.Message;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;

public class RequestDecoder {
    private static final int SIZE_POS = 0;
    private static final int REQ_POS = SIZE_POS + Integer.BYTES;
    private static final int AUTH_POS = REQ_POS + Byte.BYTES;
    private static final int PAYLOAD_POS = AUTH_POS + Request.AUTH_LEN;

    public RequestDecoder() {}

    public Request decodeRequest(byte[] r) {
        ByteBuffer buffer = ByteBuffer.wrap(r);

        int size = buffer.getInt();
        Requests request = decodeRequest(buffer.get());

        byte[] encodedToken = new byte[Request.AUTH_LEN];
        buffer.get(encodedToken, 0, Request.AUTH_LEN);
        String token = decodeToken(encodedToken);

        byte[] encodedPayload = new byte[size-PAYLOAD_POS];
        buffer.get(encodedPayload, 0, size-PAYLOAD_POS);
        String payload = decodePayload(encodedPayload);

        return new Request(request, token, payload);
    }

    private Requests decodeRequest(byte r) {
        switch (r) {
            case 0x1:
                return Requests.LOG_IN;
            case 0x2:
                return Requests.LOG_OUT;
            case 0x3:
                return Requests.SEND_MESSAGE;
            case 0x4:
                return Requests.REQUEST_NEW_MESSAGES;
            default:
                throw new IllegalStateException("Unexpected value: " + r);
        }
    }

    private String decodePayload(byte[] p) {
        return new String(p, StandardCharsets.UTF_8);
    }

    private String decodeToken(byte[] t) {
        return Base64.getEncoder().encodeToString(t);
    }

    public Message decodeMessage(String s) throws MessageSizeException {
        int SENDER_NAME = 0;
        int SENDER_IP = 1;
        int SENDER_PORT = 2;
        int TIME = 3;
        int MESSAGE = 4;
        int N_OF_FIELDS = 5;

        String[] m = s.split("\n", N_OF_FIELDS);
        return new Message(m[SENDER_NAME], m[SENDER_IP], Integer.parseInt(m[SENDER_PORT]), Instant.parse(m[TIME]),
                m[MESSAGE]);
    }
}
