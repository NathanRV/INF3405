package ca.polymtl.inf3405.protocol.request;

public class Request {
    private final Requests request;
    private final String token;
    private final String payload;
    public static final int AUTH_LEN = 256 * Byte.BYTES;

    public Request(Requests request, String token, String payload) {
        this.request = request;
        this.token = token;
        this.payload = payload;
    }

    public Requests getRequest() {
        return request;
    }

    public String getToken() {
        return token;
    }

    public String getPayload() {
        return payload;
    }
}

