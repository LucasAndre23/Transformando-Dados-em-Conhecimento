package com.example;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class FiltroPaises {

    private static final String CSV_FILE_PATH = "./src/main/resources/dados/Vendas Globais.csv";

    public static void main(String[] args) {
        System.out.println("--- Iniciando extração de países únicos do arquivo: Vendas Globais.csv ---");

        try (Reader reader = new FileReader(CSV_FILE_PATH);
             CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT.withHeader())) {

            List<CSVRecord> registros = csvParser.getRecords();
            
            // extrair a coluna "ClientePaís", remover espaços e coletar apenas valores únicos (Set)
            Set<String> paisesUnicos = registros.stream()
                    .map(r -> r.get("ClientePaís").trim())
                    .filter(pais -> !pais.isEmpty())
                    .collect(Collectors.toSet());

            System.out.println("\n*******************************************************");
            System.out.println("****** LISTA DE PAÍSES ÚNICOS (Em Ordem Alfabética) ******");
            System.out.println("*******************************************************");
            
           
            paisesUnicos.stream()
                        .sorted()
                        .forEach(System.out::println);
            
            System.out.println("*******************************************************\n");
            System.out.printf("Total de %d países únicos encontrados.\n", paisesUnicos.size());

        } catch (IOException e) {
            System.err.println("Erro ao carregar o arquivo. Verifique o caminho: " + CSV_FILE_PATH);
            System.err.println("Detalhes do erro: " + e.getMessage());
        }
    }
}
