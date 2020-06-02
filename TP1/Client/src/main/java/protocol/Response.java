package protocol;

import com.google.gson.Gson;

import java.util.Map;

/**
 *
 */
public class Response {
    private final String response;
    private final Map<String, String> payload;

    /**
     *
     * @param response
     * @param payload
     */
    public Response(String response, Map<String, String> payload) {
        this.response = response;
        this.payload = payload;
    }

    /**
     *
     * @return
     */
    public String getResponse() {
        return response;
    }

    /**
     *
     * @return
     */
    public Map<String, String> getPayload() {
        return payload;
    }

    /**
     *
     * @return
     */
    public String encodeResponse() {
        Gson gson = new Gson();
        return gson.toJson(this);
    }

    /**
     *
     * @param json
     * @return
     */
    public static Response decodeResponse(String json) {
        Gson gson = new Gson();
        return gson.fromJson(json, Response.class);
    }
}
