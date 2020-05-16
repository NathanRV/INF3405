package TestEncoderDecoder;

import ca.polymtl.inf3405.server.requestEncoding.*;
import ca.polymtl.inf3405.server.request.Request;
import ca.polymtl.inf3405.server.request.Requests;
import ca.polymtl.inf3405.server.response.Response;
import ca.polymtl.inf3405.server.response.Responses;
import ca.polymtl.inf3405.server.responseEncoding.ResponseDecoder;
import ca.polymtl.inf3405.server.responseEncoding.ResponseEncoder;
import org.junit.jupiter.api.Test;

import java.security.SecureRandom;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class EncodeDecodeTest {
    @Test
    public void encodeDecodeRequestTest() {
        SecureRandom s = new SecureRandom();
        byte[] token = new byte[Request.AUTH_LEN];
        s.nextBytes(token);

        Requests re = Requests.LOG_OUT;
        String stringToken = Base64.getEncoder().encodeToString(token);
        String message = "This is @ test";

        Request r = new Request(re, stringToken, message);
        RequestEncoder enc = new RequestEncoder();
        byte[] encodedRequest = enc.encodeRequest(r);

        RequestDecoder dec = new RequestDecoder();
        Request r2 = dec.decodeRequest(encodedRequest);

        assertEquals(r.getRequest(), r2.getRequest());
        assertEquals(r.getToken(), r2.getToken());
        assertEquals(r.getPayload(), r2.getPayload());
    }

    @Test
    public void encodeDecodeResponseTest() {
        Response r = new Response(Responses.AUTHENTICATED, "soeifjsoeijfoisejofeisj");

        ResponseEncoder enc = new ResponseEncoder();
        byte[] encodedResponse = enc.encodeResponse(r);

        ResponseDecoder dec = new ResponseDecoder();
        Response r2 = dec.decodeResponse(encodedResponse);

        assertEquals(r.getResponse(), r2.getResponse());
        assertEquals(r.getPayload(), r2.getPayload());
    }
}
