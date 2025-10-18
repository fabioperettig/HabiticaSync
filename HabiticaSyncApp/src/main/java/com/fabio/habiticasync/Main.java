package com.fabio.habiticasync;

import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.classic.HttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.json.JSONObject;

import com.fabio.habiticasync.integrations.GoogleSheetsSync;
import com.fabio.habiticasync.utils.JsonExplorer;

import javax.naming.spi.ObjectFactoryBuilder;
import java.util.Iterator;
import java.util.List;

/**
 * Project: HabiticaSyncApp
 * Package: com.fabio.habiticasync
 * Description:
 *     This project connects to the official Habitica API,
 *     obtaining information from the user's inventory.
 *     First step towards creating an automated integration with Google Sheets.
 *
 * @author Fabio Peretti Guimarães
 * @version 1.10
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

            // pets n mounts
            JSONObject pets = items.getJSONObject("pets");

            System.out.println("\n Pets:");
            printNameCountMap(pets);

            //JsonExplorer.print(data, "");
            // Enable it to see the complete JSON.

            System.out.println("\n🥚 Ovos:");
            printNameCountMap(eggs);

            System.out.println("\n🧪 Poções de Eclosão:");
            printNameCountMap(potions);

            /* --- ENVIO TESTE ---
            System.out.println("\nEnviando dados de teste para o Google Sheets...");

            String spreadsheetId = "1nLGR5gg9e4wL-2u_yo6uUq_DBB7CjQgSrsUucUoMeV4";

            List<List<Object>> dataToSend = List.of(
                    List.of("Tipo", "Quantidade"),
                    List.of("Ovo: Lobo", 12),
                    List.of("Ovo: Tigre", 8)
            );

            GoogleSheetsSync.writeValues(spreadsheetId, "Página1!A1:B3", dataToSend);*/

            String spreadsheetId = "1nLGR5gg9e4wL-2u_yo6uUq_DBB7CjQgSrsUucUoMeV4";
            updateSheetsEgg(eggs,spreadsheetId); //ENVIO REAL
            updateSheetsPets(pets,spreadsheetId);




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

    /**
     * Sends the real egg data from Habitica to a specified Google Sheets document.
     * <p>
     * This method converts the {@link JSONObject} received from the Habitica API
     * (containing all egg types and their respective quantities) into a two-dimensional list
     * compatible with the Google Sheets API. It then writes the formatted data to the
     * target spreadsheet using the {@link GoogleSheetsSync#writeValues(String, String, List)} method.
     * </p>
     *
     * <h3>Example of data written:</h3>
     * <pre>
     *     | Ovo          | Quantidade |
     *     |--------------|------------|
     *     | Wolf         | 10         |
     *     | Dragon       | 5          |
     * </pre>
     *
     * @param eggs          the {@code JSONObject} containing all Habitica egg types and quantities.
     * @param spreadsheetId the unique ID of the target Google Sheet (found between {@code /d/} and {@code /edit} in its URL).
     *
     * @throws RuntimeException if an error occurs while sending data to Google Sheets.
     *
     * @see GoogleSheetsSync#writeValues(String, String, List)
     */
    private static void updateSheetsEgg(JSONObject eggs, String spreadsheetId) {
        System.out.println("\nEnviando ovos para o Google Sheets...");
        List<List<Object>> dataToSend = new java.util.ArrayList<>();

        dataToSend.add(List.of("Ovo", "Quantidade"));

        Iterator<String> eggKeys = eggs.keys();
        while (eggKeys.hasNext()) {
            String eggName = eggKeys.next();
            int quantity = eggs.optInt(eggName, 0);
            dataToSend.add(List.of(eggName, quantity));
        }

        try {
            GoogleSheetsSync.writeValues(spreadsheetId, "Página1!A1:B" + dataToSend.size(), dataToSend);
            System.out.println("✅ Dados de ovos enviados com sucesso!");
        } catch (Exception e) {
            System.out.println("❌ Falha ao enviar dados para o Google Sheets:");
            e.printStackTrace();
        }
    }

    /**
     * Sends the real pet data from Habitica to a specified Google Sheets document.
     *
     * @param pets          the {@code JSONObject} containing all Habitica pets types and quantities.
     * @param spreadsheetId the unique ID of the target Google Sheet (found between {@code /d/} and {@code /edit} in its URL).
     *
     * @throws RuntimeException if an error occurs while sending data to Google Sheets.
     *
     * @see GoogleSheetsSync#writeValues(String, String, List)
     */
    private static void updateSheetsPets(JSONObject pets, String spreadsheetId) {
        System.out.println("\nEnviando pets para o Google Sheets...");
        List<List<Object>> dataToSend = new java.util.ArrayList<>();

        dataToSend.add(List.of("Pets", "Quantidade"));

        Iterator<String> petsKeys = pets.keys();
        while (petsKeys.hasNext()) {
            String petName = petsKeys.next();
            int quantity = pets.optInt(petName,0);
            dataToSend.add(List.of(petName, quantity));
        }

        try {
            GoogleSheetsSync.writeValues(spreadsheetId, "Página1!C1:D" + dataToSend.size(), dataToSend);
            System.out.println("✅ Dados de pets enviados com sucesso!");
        } catch (Exception e) {
            System.out.println("❌ Falha ao enviar dados para o Google Sheets:");
            e.printStackTrace();
        }

    }
}