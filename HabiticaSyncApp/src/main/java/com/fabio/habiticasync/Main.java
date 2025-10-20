package com.fabio.habiticasync;

import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.classic.HttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.json.JSONObject;

import com.fabio.habiticasync.integrations.GoogleSheetsSync;
import com.fabio.habiticasync.utils.JsonExplorer;
import com.fabio.habiticasync.content.HabiticaContentPets;
import com.fabio.habiticasync.content.HabiticaContentPotions;

import javax.naming.spi.ObjectFactoryBuilder;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

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
            JSONObject pets = items.getJSONObject("pets");
            JSONObject potions = items.getJSONObject("hatchingPotions");


            // Exibe dados no Terminal
            System.out.println("\n Pets:");
            printNameCountMap(pets);

            System.out.println("\n🥚 Ovos:");
            printNameCountMap(eggs);

            System.out.println("\n🧪 Poções de Eclosão:");
            printNameCountMap(potions);


            //Envio para Sheets
            String spreadsheetId = "1nLGR5gg9e4wL-2u_yo6uUq_DBB7CjQgSrsUucUoMeV4";
            updateSheetsEgg(eggs,spreadsheetId);
            updateSheetsPets(pets,spreadsheetId);
            updateSheetsPotions(potions,spreadsheetId);




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

        /*
        try {
            JSONObject content = HabiticaContentPets.getContent();
            //JsonExplorer.print(content, "");

            // 📝 Salva o JSON completo em um arquivo local
            java.nio.file.Files.writeString(
                    java.nio.file.Path.of("habitica_content.json"),
                    content.toString(2) // o '2' deixa formatado com indentação bonita
            );

            System.out.println("✅ JSON completo salvo em habitica_content.json");
        } catch (Exception e) {
            System.out.println("❌ Erro ao buscar o conteúdo público do Habitica:");
            e.printStackTrace();
        }*/

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

        dataToSend.add(List.of("Ovo", "Quantidade", "Imagem"));

        try {
            JSONObject content = com.fabio.habiticasync.content.HabiticaContentEggs.getContent();
            Map<String, String> eggImageUrls = com.fabio.habiticasync.content
                    .HabiticaContentEggs.getEggImageUrls(content);

            Iterator<String> eggKeys = eggs.keys();
            while (eggKeys.hasNext()) {
                String eggName = eggKeys.next();
                int quantity = eggs.optInt(eggName, 0);

                String imageFormula = "";
                if (eggImageUrls.containsKey(eggName)) {
                    String url = eggImageUrls.get(eggName);
                    imageFormula = "=IMAGE(\"" + url + "\"; 4; 50; 50)";
                }

                dataToSend.add(List.of(eggName, quantity, imageFormula));
            }

            GoogleSheetsSync.writeValues(spreadsheetId, "Página1!A1:C" + dataToSend.size(), dataToSend);
            System.out.println("✅ Eggs enviados com imagens para o Google Sheets!");

        } catch (Exception e) {
            System.out.println("❌ Falha ao enviar eggs com imagens:");
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

        dataToSend.add(List.of("Pets", "Nível", "Imagem"));

        try {
            JSONObject content = com.fabio.habiticasync.content.HabiticaContentPets.getContent();
            Map<String, String> petImageUrls = com.fabio.habiticasync.content
                    .HabiticaContentPets.getPetImageUrls(content);

            Iterator<String> petKeys = pets.keys();
            while (petKeys.hasNext()) {
                String petName = petKeys.next();
                int quantity = pets.optInt(petName, 0);

                String imageFormula = "";
                if (petImageUrls.containsKey(petName)) {
                    String url = petImageUrls.get(petName);
                    imageFormula = "=IMAGE(\"" + url + "\"; 4; 50; 50)";
                }

                dataToSend.add(List.of(petName, quantity, imageFormula));
            }

            GoogleSheetsSync.writeValues(spreadsheetId, "Página1!E1:G" + dataToSend.size(), dataToSend);
            System.out.println("✅ Pets enviados com imagens para o Google Sheets!");

        } catch (Exception e) {
            System.out.println("❌ Falha ao enviar pets com imagens:");
            e.printStackTrace();
        }
    }

    private static void updateSheetsPotions(JSONObject potions, String spreadsheetId) {
        System.out.println("\nEnviando ovos para o Google Sheets...");
        List<List<Object>> dataToSend = new java.util.ArrayList<>();

        dataToSend.add(List.of("Poção", "Quantidade", "Imagem"));

        try {
            JSONObject content = com.fabio.habiticasync.content.HabiticaContentPotions.getContent();
            Map<String, String> potionImageUrls = com.fabio.habiticasync.content
                    .HabiticaContentPotions.getPotionImageUrls(content);

            Iterator<String> potionKeys = potions.keys();
            while (potionKeys.hasNext()) {
                String potionName = potionKeys.next();
                int quantity = potions.optInt(potionName, 0);

                String imageFormula = "";
                if (potionImageUrls.containsKey(potionName)) {
                    String url = potionImageUrls.get(potionName);
                    imageFormula = "=IMAGE(\"" + url + "\"; 4; 50; 50)";
                }

                dataToSend.add(List.of(potionName, quantity, imageFormula));
            }

            GoogleSheetsSync.writeValues(spreadsheetId, "Página1!I1:K" + dataToSend.size(), dataToSend);
            System.out.println("✅ Eggs enviados com imagens para o Google Sheets!");

        } catch (Exception e) {
            System.out.println("❌ Falha ao enviar potions com imagens:");
            e.printStackTrace();
        }


    }

}