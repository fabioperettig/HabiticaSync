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
public class HabiticaContentEggs {

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
    public static Map<String, String> getEggImageUrls(JSONObject content) {
        Map<String, String> eggImages = new HashMap<>();

        // Image Links
        String baseUrl = "https://habitica-assets.s3.amazonaws.com/mobileApp/images/Pet_Egg_";

        JSONObject eggData = new JSONObject();

        System.out.println("🔍 Estrutura encontrada para eggs:");
        System.out.println(eggData.toString(2));

        if (content.has("dropEggs")) {
            eggData = content.getJSONObject("dropEggs");
        } else if (content.has("eggs")) {
            eggData = content.getJSONObject("eggs");
        } else {
            System.out.println("⚠️ Nenhum campo de eggs encontrado no conteúdo.");
        }


        Iterator<String> keys = eggData.keys();

        while (keys.hasNext()) {
            String key = keys.next(); // ex: "Wolf-Egg"
            String imageUrl = baseUrl + key + ".png";
            eggImages.put(key, imageUrl);
        }

        System.out.println("✅ Mapeadas " + eggImages.size() + " imagens de eggs via padrão de URL.");
        return eggImages;
    }

    private static JSONObject mergeJsonObjects(JSONObject main, JSONObject toMerge) {
        JSONObject merged = new JSONObject(main.toString());
        for (String key : toMerge.keySet()) {
            merged.put(key, toMerge.get(key));
        }
        return merged;
    }
}
