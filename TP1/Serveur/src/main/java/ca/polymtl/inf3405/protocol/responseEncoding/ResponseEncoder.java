package ca.polymtl.inf3405.protocol.responseEncoding;

import ca.polymtl.inf3405.protocol.response.*;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public class ResponseEncoder {
    public ResponseEncoder() {}

    public byte[] encodeResponse(Response r) {
        byte response = encodeResponse(r.getResponse());
        byte[] payload = encodeString(r.getPayload());
        int size = Byte.BYTES + payload.length + Integer.BYTES;

        ByteBuffer buffer = ByteBuffer.allocate(size);
        buffer.putInt(size);
        buffer.put(response);
        buffer.put(payload);

        return buffer.array();
    }

    private byte encodeResponse (Responses r) {
        switch (r) {
            case OK:
                return 0x1;
            case AUTHENTICATED:
                return 0x2;
            case USER_CREATED:
                return 0x3;
            case WRONG_PASSWORD:
                return 0x4;
            case WRONG_REQUEST:
                return 0x5;
            case EXPIRED_TOKEN:
                return 0x6;
            default:
                throw new IllegalStateException("Unexpected value: " + r);
        }
    }

    private byte[] encodeString(String s) {
        return s.getBytes(StandardCharsets.UTF_8);
    }
}
