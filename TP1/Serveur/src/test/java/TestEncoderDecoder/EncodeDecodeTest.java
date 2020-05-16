package TestEncoderDecoder;

import ca.polymtl.inf3405.requestEncoding.*;
import org.junit.jupiter.api.Test;

import java.security.SecureRandom;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class EncodeDecodeTest {
    @Test
    public void encodeDecodeTest() {
        SecureRandom s = new SecureRandom();
        byte[] token = new byte[Request.AUTH_LEN];
        s.nextBytes(token);

        Requests re = Requests.LOG_OUT;
        String stringToken = Base64.getEncoder().encodeToString(token);
        String message = "This is @ test";

        Request r = new Request(re, stringToken, message);
        RequestEncoder enc = new RequestEncoder();
        byte[] encodedRequest = enc.encodePacket(r);

        RequestDecoder dec = new RequestDecoder();
        Request r2 = dec.decodePacket(encodedRequest);

        assertEquals(r.getRequest(), r2.getRequest());
        assertEquals(r.getToken(), r2.getToken());
        assertEquals(r.getPayload(), r2.getPayload());
    }
}
