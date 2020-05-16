package ca.polymtl.inf3405.protocol.response;

public class Response {
    private final Responses response;
    private final String payload;

    public Response(Responses response, String payload) {
        this.response = response;
        this.payload = payload;
    }

    public Responses getResponse() {
        return response;
    }

    public String getPayload() {
        return payload;
    }
}
