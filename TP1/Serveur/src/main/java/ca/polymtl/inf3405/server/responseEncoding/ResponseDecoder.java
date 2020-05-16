package ca.polymtl.inf3405.server.responseEncoding;

import ca.polymtl.inf3405.server.response.*;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public class ResponseDecoder {
    private static final int SIZE_POS = 0;
    private static final int RES_POS = SIZE_POS + Integer.BYTES;
    private static final int PAYLOAD_POS = RES_POS + Byte.BYTES;

    public ResponseDecoder() {}

    public Response decodeResponse(byte[] r) {
        ByteBuffer buffer = ByteBuffer.wrap(r);

        int size = buffer.getInt();
        Responses response = decodeResponse(buffer.get());

        byte[] encodedPayload = new byte[size-PAYLOAD_POS];
        buffer.get(encodedPayload, 0, size-PAYLOAD_POS);
        String payload = decodePayload(encodedPayload);

        return new Response(response, payload);
    }

    private Responses decodeResponse(byte r) {
        switch (r) {
            case 0x1:
                return Responses.OK;
            case 0x2:
                return Responses.AUTHENTICATED;
            case 0x3:
                return Responses.USER_CREATED;
            case 0x4:
                return Responses.WRONG_PASSWORD;
            case 0x5:
                return Responses.WRONG_REQUEST;
            case 0x6:
                return Responses.EXPIRED_TOKEN;
            default:
                throw new IllegalStateException("Unexpected value: " + r);
        }
    }

    private String decodePayload(byte[] p) {
        return new String(p, StandardCharsets.UTF_8);
    }
}
