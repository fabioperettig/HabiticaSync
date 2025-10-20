package com.fabio.habiticasync.content;

import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.classic.HttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.json.JSONObject;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Handles fetching and parsing of Habitica's public content data.
 * Provides access to metadata such as pet/mount names and image URLs.
 */
public class HabiticaContentPets {

    private static final String CONTENT_URL = "https://habitica.com/api/v3/content";

    /**
     * Retrieves all public content data from Habitica API.
     */
    public static JSONObject getContent() throws Exception {
        HttpClient client = HttpClients.createDefault();
        HttpGet request = new HttpGet(CONTENT_URL);
        request.addHeader("x-client", "fabioperettiguimaraes-HabiticaSyncApp");
        ClassicHttpResponse response = (ClassicHttpResponse) client.execute(request);
        String responseBody = EntityUtils.toString(response.getEntity());

        JSONObject root = new JSONObject(responseBody);
        return root.has("data") ? root.getJSONObject("data") : root;
    }

    /**
     * Extracts pet image URLs into a map of key → URL.
     */
    public static Map<String, String> getPetImageUrls(JSONObject content) {
        Map<String, String> petImages = new HashMap<>();

        // Image Links
        String baseUrl = "https://habitica-assets.s3.amazonaws.com/mobileApp/images/stable_Pet-";

        JSONObject petData = null;

        // Tenta achar qualquer estrutura relacionada a pets
        if (content.has("petInfo")) {
            petData = content.getJSONObject("petInfo");
        } else if (content.has("petData")) {
            petData = content.getJSONObject("petData");
        } else if (content.has("items") && content.getJSONObject("items").has("pets")) {
            petData = content.getJSONObject("items").getJSONObject("pets");
        } else {
            System.out.println("⚠️ Nenhum campo de pets encontrado no conteúdo.");
            return petImages;
        }


        Iterator<String> keys = petData.keys();

        while (keys.hasNext()) {
            String key = keys.next(); // ex: "Wolf-Base"
            String imageUrl = baseUrl + key + ".png";
            petImages.put(key, imageUrl);
        }

        System.out.println("✅ Mapeadas " + petImages.size() + " imagens de pets via padrão de URL.");
        return petImages;
    }
}
