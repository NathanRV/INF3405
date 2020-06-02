package ca.polymtl.inf3405.protocol;

import com.google.gson.Gson;

import java.util.Map;

/**
 * Classe qui représente une réponse du serveur à la requête du client
 */
public class Response {
    private final String response;
    private final Map<String, String> payload;

    /**
     * Constructeur par défaut
     * @param response  le code de la réponse
     * @param payload   les données complémentaires à la réponse
     */
    public Response(String response, Map<String, String> payload) {
        this.response = response;
        this.payload = payload;
    }

    public String getResponse() {
        return response;
    }

    public Map<String, String> getPayload() {
        return payload;
    }

    /**
     * Méthode qui encode la réponse dans un format JSON
     * @return la réponse encodée
     */
    public String encodeResponse() {
        Gson gson = new Gson();
        return gson.toJson(this);
    }

    /**
     * Méthode qui décode la réponse en format JSON
     * @param json la requête encodée
     * @return     la requête décodée
     */
    public static Response decodeResponse(String json) {
        Gson gson = new Gson();
        return gson.fromJson(json, Response.class);
    }
}
