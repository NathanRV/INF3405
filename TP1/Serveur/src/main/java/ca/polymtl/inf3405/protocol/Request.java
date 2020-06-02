package ca.polymtl.inf3405.protocol;

import com.google.gson.Gson;

import java.util.HashMap;
import java.util.Map;

/**
 * Classe qui représente une requête du client au serveur
 */
public class Request {
    private final String request;
    private final String token;
    private final Map<String, String> payload;

    /**
     * Constructeur par défaut
     * @param request  la requête
     * @param token    le token d'authentification
     * @param payload  les données complémentaires à la requête
     */
    public Request(String request, String token, Map<String, String> payload) {
        this.request = request;
        this.token = token;
        this.payload = payload;
    }

    public String getRequest() {
        return request;
    }

    public String getToken() {
        return token;
    }

    public Map<String, String> getPayload() {
        return payload;
    }

    /**
     * Méthode qui encode là requête en format JSON
     * @return  la requête encodée
     */
    public String encodeRequest() {
        Gson gson = new Gson();
        return gson.toJson(this);
    }

    /**
     * Méthode qui décode une requête en format JSON
     * @param json  la requête encodée
     * @return      la requête décodée
     */
    public static Request decodeRequest(String json) {
        Gson gson = new Gson();
        return gson.fromJson(json, Request.class);
    }
}





