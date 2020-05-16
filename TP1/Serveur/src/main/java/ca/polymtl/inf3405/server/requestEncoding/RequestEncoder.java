package ca.polymtl.inf3405.server.requestEncoding;

import ca.polymtl.inf3405.server.request.Request;
import ca.polymtl.inf3405.server.request.Requests;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class RequestEncoder {
    public RequestEncoder() {}

    public byte[] encodeRequest(Request r) {
        byte request = encodeRequest(r.getRequest());
        byte[] token = encodeToken(r.getToken());
        byte[] payload = encodeString(r.getPayload());
        int size = Byte.BYTES + token.length + payload.length + Integer.BYTES;

        ByteBuffer buffer = ByteBuffer.allocate(size);
        buffer.putInt(size);
        buffer.put(request);
        buffer.put(token);
        buffer.put(payload);

        return buffer.array();
    }

    private byte encodeRequest(Requests r) {
        switch (r) {
            case LOG_IN:
                return 0x1;
            case LOG_OUT:
                return 0x2;
            case SEND_MESSAGE:
                return 0x3;
            case RECEIVE_MESSAGE:
                return 0x4;
            default:
                throw new IllegalStateException("Unexpected value: " + r);
        }
    }

    private byte[] encodeString(String s) {
        return s.getBytes(StandardCharsets.UTF_8);
    }

    private byte[] encodeToken(String t) {
        return Base64.getDecoder().decode(t);
    }
}
