package com.fabio.habiticasync.tests;

import com.fabio.habiticasync.integrations.GoogleSheetsSync;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.model.Spreadsheet;

public class SheetsConnectionTest {
    public static void main(String[] args) {
        try {
            // Cria o serviço autenticado
            Sheets service = GoogleSheetsSync.getSheetsService();

            // ID da sua planilha (o trecho entre /d/ e /edit na URL)
            String spreadsheetId = "1nLGR5gg9e4wL-2u_yo6uUq_DBB7CjQgSrsUucUoMeV4";

            // Faz uma chamada simples pra ler o título da planilha
            Spreadsheet spreadsheet = service.spreadsheets().get(spreadsheetId).execute();

            System.out.println("✅ Conexão bem-sucedida!");
            System.out.println("Título da planilha: " + spreadsheet.getProperties().getTitle());
        } catch (Exception e) {
            System.err.println("❌ Erro ao conectar ao Google Sheets:");
            e.printStackTrace();
        }
    }
}