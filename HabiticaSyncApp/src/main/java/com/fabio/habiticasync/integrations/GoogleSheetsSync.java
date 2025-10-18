package com.fabio.habiticasync.integrations;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.SheetsScopes;
import com.google.auth.oauth2.ServiceAccountCredentials;
import com.google.api.services.sheets.v4.model.ValueRange;

import java.io.FileInputStream;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.List;

/**
 * Handles authentication and connection to Google Sheets API.
 *
 * @author Fabio
 * @version 1.1
 * @since October 2025
 */
public class GoogleSheetsSync {

    private static final String APPLICATION_NAME = "HabiticaSync";
    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();

    /**
     * Builds and returns an authorized Sheets API client service.
     */
    public static Sheets getSheetsService() throws IOException, GeneralSecurityException {
        // Caminho do arquivo de credenciais (ajuste o nome conforme o seu JSON)
        String credentialsPath = "src/main/resources/habitica-sync-bot.json";

        HttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();

        try (var in = GoogleSheetsSync.class.getClassLoader().getResourceAsStream("habitica-sync-bot.json")) {
            if (in == null) {
                throw new IOException("Arquivo de credenciais não encontrado no classpath!");
            }
            ServiceAccountCredentials credentials = (ServiceAccountCredentials)
                    ServiceAccountCredentials.fromStream(in)
                            .createScoped(Collections.singleton(SheetsScopes.SPREADSHEETS));

            return new Sheets.Builder(httpTransport, JSON_FACTORY, new HttpCredentialsAdapter(credentials))
                    .setApplicationName(APPLICATION_NAME)
                    .build();
        }
    }

    /**
     * Writes data to a given range in a Google Sheet.
     *
     * @param spreadsheetId ID da planilha (entre /d/ e /edit na URL)
     * @param range intervalo de células, ex: "Página1!A1:B5"
     * @param values lista de linhas e colunas a serem escritas
     */
    public static void writeValues(String spreadsheetId, String range, List<List<Object>> values) throws Exception {
        Sheets service = getSheetsService();
        ValueRange body = new ValueRange().setRange(range).setValues(values);

        service.spreadsheets().values()
                .update(spreadsheetId, range, body)
                .setValueInputOption("RAW") //"USER_ENTERED" permite fórmulas
                .execute();

        System.out.println("✅ Dados enviados com sucesso para o Google Sheets!");
    }

}