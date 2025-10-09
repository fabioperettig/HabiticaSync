package com.fabio.habiticasync;

import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.classic.HttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.json.JSONObject;

import java.util.Iterator;

/**
 * Project: HabiticaSyncApp
 * Package: com.fabio.habiticasync
 * Description:
 *     This project connects to the official Habitica API,
 *     obtaining information from the user's inventory.
 *     First step towards creating an automated integration with Google Sheets.
 *
 * @author Fabio Peretti Guimarães
 * @version 1.0
 * @since October 2025
 */
public class Main {
    public static void main(String[] args) {
        System.out.println("🔗 Iniciando conexão com a API do Habitica...");

        String userId = System.getenv("HABITICA_USER_ID");
        String apiToken = System.getenv("HABITICA_API_TOKEN");

        if (userId == null || apiToken == null) {
            System.out.println("❌ Variáveis de ambiente não encontradas.");
            System.out.println("   Defina HABITICA_USER_ID e HABITICA_API_TOKEN antes de executar.");
            return;
        }

        try {
            HttpClient client = HttpClients.createDefault();

            String url = "https://habitica.com/api/v3/user";
            HttpGet request = new HttpGet(url);

            request.addHeader("x-api-user", userId);
            request.addHeader("x-api-key", apiToken);
            request.addHeader("x-client", "fabioperettiguimaraes-HabiticaSyncApp");
            request.addHeader("accept", "application/json");

            ClassicHttpResponse response = (ClassicHttpResponse) client.execute(request);
            int status = response.getCode();
            String responseBody = EntityUtils.toString(response.getEntity());

            System.out.println("📡 Status: " + status);
            if (status != 200) {
                System.out.println("❌ Erro da API: " + responseBody);
                return;
            }

            JSONObject root = new JSONObject(responseBody);
            JSONObject data = root.getJSONObject("data");
            JSONObject items = data.getJSONObject("items");
            JSONObject eggs = items.getJSONObject("eggs");
            JSONObject potions = items.getJSONObject("hatchingPotions");

            System.out.println("\n🥚 Ovos:");
            printNameCountMap(eggs);

            System.out.println("\n🧪 Poções de Eclosão:");
            printNameCountMap(potions);

        } catch (Exception e) {
            System.out.println("❌ Erro ao conectar com a API:");
            e.printStackTrace();
        }
    }

    private static void printNameCountMap(JSONObject obj) {
        Iterator<String> keys = obj.keys();
        if (!keys.hasNext()) {
            System.out.println("  (vazio)");
            return;
        }
        while (keys.hasNext()) {
            String name = keys.next();
            int qty = obj.optInt(name, 0);
            System.out.printf("  - %s: %d%n", name, qty);
        }
    }
}