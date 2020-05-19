package TestEncoderDecoder;

import ca.polymtl.inf3405.protocol.Request;
import ca.polymtl.inf3405.protocol.Response;

import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class EncodeDecodeTest {
    @Test
    public void encodeDecodeRequestTest() {
        Map<String, String> payload = new HashMap<>();
        payload.put("User", "123");
        payload.put("Password", "123");
        payload.put("Test", "{\"request\":\"LOG_IN\",\"token\":\"sfjoesifjseoifes\",\"payload\":{\"User\":\"123\",\"Password\":\"123\"}}");
        Request request = new Request("LOG_IN", "sfjoesifjseoifes", payload);
        String encodedRequest = request.encodeRequest();
        Request decodedRequest = Request.decodeRequest(encodedRequest);
        assertEquals(request.getRequest(), decodedRequest.getRequest());
        assertEquals(request.getToken(), decodedRequest.getToken());
        assertEquals(request.getPayload(), decodedRequest.getPayload());
    }

    @Test
    public void encodeDecodeResponseTest() {
        Map<String, String> payload = new HashMap<>();
        Response response = new Response("OK", payload);
        String encodedResponse = response.encodeResponse();
        Response decodedResponse = Response.decodeResponse(encodedResponse);
        assertEquals(response.getResponse(), decodedResponse.getResponse());
        assertEquals(response.getPayload(), decodedResponse.getPayload());
    }
}
