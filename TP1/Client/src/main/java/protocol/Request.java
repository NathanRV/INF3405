package protocol;

import com.google.gson.Gson;

import java.util.Map;

/**
 *
 */
public class Request {
    private final String request;
    private final String token;
    private final Map<String, String> payload;

    /**
     *
     * @param request
     * @param token
     * @param payload
     */
    public Request(String request, String token, Map<String, String> payload) {
        this.request = request;
        this.token = token;
        this.payload = payload;
    }

    /**
     *
     * @return
     */
    public String getRequest() {
        return request;
    }

    /**
     *
     * @return
     */
    public String getToken() {
        return token;
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
    public String encodeRequest() {
        Gson gson = new Gson();
        return gson.toJson(this);
    }

    /**
     *
     * @param json
     * @return
     */
    public static Request decodeRequest(String json) {
        Gson gson = new Gson();
        return gson.fromJson(json, Request.class);
    }
}





