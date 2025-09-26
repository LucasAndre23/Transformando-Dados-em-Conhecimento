package com.example;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

public class LeitorCSV {

    private static final String CSV_FILE_PATH = "./src/main/resources/dados/Vendas Globais.csv";
    private static List<CSVRecord> registros;
    
    public static void main(String[] args) {
        carregarDados();

        if (registros == null) {
            System.err.println("Não foi possível carregar os dados.");
            return;
        }

        
        System.out.println("--- Análises do CSV ---");
        
        System.out.println("\n1. Top 10 Clientes por Vendas:");
        top10ClientesPorVendas();

        System.out.println("\n2. Top 3 Países por Vendas:");
        top3PaisesPorVendas();

        System.out.println("\n3. Categorias com Maior Faturamento no Brasil:");
        maiorFaturamentoPorCategoriaNoBrasil();

        System.out.println("\n4. Despesa de Frete por Transportadora:");
        despesaFretePorTransportadora();

        System.out.println("\n5. Principais Clientes do segmento 'Men´s Footwear' na Alemanha:");
        principaisClientesCalcadosAlemanha();
        
        System.out.println("\n6. Vendedores que Dão Mais Desconto nos EUA:");
        vendedoresComMaisDescontoNosEUA();

        System.out.println("\n7. Fornecedores com Maior Margem de Lucro em 'Womens wear':");
        fornecedoresMaiorMargemLucro();

        System.out.println("\n8. Vendas Anuais (2009-2012):");
        vendasAnuais();

        System.out.println("\n9. Principais Clientes de 'Men´s Footwear' em 2013:");
        principaisClientesCalcados2013();

        System.out.println("\n10. Vendas por País na Europa:");
        vendasPorPaisNaEuropa();
    }
    
    private static void carregarDados() {
        try (Reader reader = new FileReader(CSV_FILE_PATH);
             CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT.withHeader())) {
            registros = csvParser.getRecords();
            System.out.println("Dados carregados com sucesso. Total de registros: " + registros.size());
        } catch (IOException e) {
            System.err.println("Erro ao carregar o arquivo: " + e.getMessage());
        }
    }
    
    // 1. Quem são os meus 10 maiores clientes, em termos de vendas ($)?
    private static void top10ClientesPorVendas() {
        Map<String, Double> vendasPorCliente = registros.stream()
                .collect(Collectors.groupingBy(
                        r -> r.get("ClienteNome").trim(),
                        Collectors.summingDouble(r -> parseDoubleSafe(r.get("Vendas")))
                ));

        vendasPorCliente.entrySet().stream()
                .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
                .limit(10)
                .forEach(entry -> System.out.printf("- %s: R$ %.2f%n", entry.getKey(), entry.getValue()));
    }

    // 2. Quais os três maiores países, em termos de vendas ($)?
    private static void top3PaisesPorVendas() {
        Map<String, Double> vendasPorPais = registros.stream()
                .collect(Collectors.groupingBy(
                        r -> r.get("ClientePaís").trim(),
                        Collectors.summingDouble(r -> parseDoubleSafe(r.get("Vendas")))
                ));
        
        vendasPorPais.entrySet().stream()
                .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
                .limit(3)
                .forEach(entry -> System.out.printf("- %s: R$ %.2f%n", entry.getKey(), entry.getValue()));
    }

    // 3. Quais as categorias de produtos que geram maior faturamento (vendas $) no Brasil?
    private static void maiorFaturamentoPorCategoriaNoBrasil() {
        Map<String, Double> faturamentoNoBrasil = registros.stream()
                .filter(r -> "Brazil".equalsIgnoreCase(r.get("ClientePaís").trim()))
                .collect(Collectors.groupingBy(
                        r -> r.get("CategoriaNome").trim(),
                        Collectors.summingDouble(r -> parseDoubleSafe(r.get("Vendas")))
                ));
        
        faturamentoNoBrasil.entrySet().stream()
                .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
                .forEach(entry -> System.out.printf("- %s: R$ %.2f%n", entry.getKey(), entry.getValue()));
    }

    // 4. Qual a despesa com frete envolvendo cada transportadora?
    private static void despesaFretePorTransportadora() {
        // Define o conjunto de IDs válidos para filtro (apenas 1, 2, 3)
        Set<String> transportadorasValidas = Set.of("1", "2", "3");
        
        Map<String, Double> fretePorTransportadora = registros.stream()
                //  Mantém apenas os registros onde o ID é "1", "2" ou "3"
                .filter(r -> transportadorasValidas.contains(r.get("TransportadoraID").trim()))
                .collect(Collectors.groupingBy(
                        r -> r.get("TransportadoraID").trim(),
                        Collectors.summingDouble(r -> parseDoubleSafe(r.get("Frete")))
                ));

        // resultados para as transportadoras filtradas (1, 2, 3).
        fretePorTransportadora.forEach((transportadoraID, frete) -> {
            System.out.printf("- Transportadora %s: R$ %.2f%n", transportadoraID, frete);
        });
    }

    // 5. Quais são os principais clientes (vendas $) do segmento “Calçados Masculinos” (Men´s Footwear) na Alemanha?
    private static void principaisClientesCalcadosAlemanha() {
        Map<String, Double> vendasPorCliente = registros.stream()
                .filter(r -> "Germany".equalsIgnoreCase(r.get("ClientePaís").trim()) && "Men´s Footwear".equalsIgnoreCase(r.get("CategoriaNome").trim()))
                .collect(Collectors.groupingBy(
                        r -> r.get("ClienteNome").trim(),
                        Collectors.summingDouble(r -> parseDoubleSafe(r.get("Vendas")))
                ));
        
        vendasPorCliente.entrySet().stream()
                .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
                .forEach(entry -> System.out.printf("- %s: R$ %.2f%n", entry.getKey(), entry.getValue()));
    }

    // 6. Quais os vendedores que mais dão descontos nos Estados Unidos?
    private static void vendedoresComMaisDescontoNosEUA() {
        Map<String, Double> descontoPorVendedor = registros.stream()
                .filter(r -> "USA".equalsIgnoreCase(r.get("ClientePaís").trim()))
                .collect(Collectors.groupingBy(
                        r -> r.get("VendedorID").trim(),
                        Collectors.summingDouble(r -> parseDoubleSafe(r.get("Desconto")))
                ));

        descontoPorVendedor.entrySet().stream()
                .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
                .forEach(entry -> System.out.printf("- Vendedor %s: R$ %.2f%n", entry.getKey(), entry.getValue()));
    }

    // 7. Quais os fornecedores que dão a maior margem de lucro ($) no segmento de “Vestuário Feminino” (Womens wear)?
    private static void fornecedoresMaiorMargemLucro() {
        Map<String, Double> margemPorFornecedor = registros.stream()
                .filter(r -> "Womens wear".equalsIgnoreCase(r.get("CategoriaNome").trim()))
                .collect(Collectors.groupingBy(
                        r -> r.get("FornecedorID").trim(),
                        Collectors.summingDouble(r -> parseDoubleSafe(r.get("Margem Bruta")))
                ));

        margemPorFornecedor.entrySet().stream()
                .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
                .forEach(entry -> System.out.printf("- Fornecedor %s: R$ %.2f%n", entry.getKey(), entry.getValue()));
    }
    
    // 8. Vendas Anuais (2009-2012) e a tendência
    private static void vendasAnuais() {
        
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        Map<Integer, Double> vendasPorAno = registros.stream()
                .map(r -> new AbstractMap.SimpleEntry<>(getYearSafe(r, formatter), parseDoubleSafe(r.get("Vendas"))))
                .filter(entry -> entry.getKey() >= 2009 && entry.getKey() <= 2012)
                .collect(Collectors.groupingBy(
                        Map.Entry::getKey,
                        Collectors.summingDouble(Map.Entry::getValue)
                ));
        
        // Exibir as vendas por ano
        vendasPorAno.entrySet().stream()
            .sorted(Map.Entry.comparingByKey())
            .forEach(entry -> System.out.printf("  - Vendas em %d: R$ %.2f%n", entry.getKey(), entry.getValue()));
        
        // Analisar a tendência entre 2009 e 2012
        double vendas2009 = vendasPorAno.getOrDefault(2009, 0.0);
        double vendas2012 = vendasPorAno.getOrDefault(2012, 0.0);
        
        String tendencia = "mantendo-se estável";
        if (vendas2012 > vendas2009) {
            tendencia = "crescendo"; 
        } else if (vendas2012 < vendas2009) {
            tendencia = "decaindo";
        }
        
        System.out.println("  Analisando 2009 e 2012, o faturamento está " + tendencia + ".");
    }
    
    
    // 9. Principais clientes (vendas $) de 'Men´s Footwear' em 2013, e cidades de venda
    private static void principaisClientesCalcados2013() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        
        // Filtra os registros para o ano de 2013 e a categoria desejada
        List<CSVRecord> registros2013 = registros.stream()
                .filter(r -> "Men´s Footwear".equalsIgnoreCase(r.get("CategoriaNome").trim()) && 
                                getYearSafe(r, formatter) == 2013)
                .collect(Collectors.toList());

        // 1. Vendas por Cliente
        Map<String, Double> vendasPorCliente = registros2013.stream()
                .collect(Collectors.groupingBy(
                        r -> r.get("ClienteNome").trim(),
                        Collectors.summingDouble(r -> parseDoubleSafe(r.get("Vendas")))
                ));

        vendasPorCliente.entrySet().stream()
                .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
                .forEach(entry -> System.out.printf("  - %s: R$ %.2f%n", entry.getKey(), entry.getValue()));
        
        // 2. Vendas por Cidade
        Map<String, Double> vendasPorCidade = registros2013.stream()
                .collect(Collectors.groupingBy(
                        r -> r.get("ClienteCidade").trim(),
                        Collectors.summingDouble(r -> parseDoubleSafe(r.get("Vendas")))
                ));
        
        System.out.println("  Vendas por cidade para 'Men´s Footwear' em 2013:");
        vendasPorCidade.entrySet().stream()
                .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
                .forEach(entry -> System.out.printf("  - %s: R$ %.2f%n", entry.getKey(), entry.getValue()));
    }

    // 10. Na Europa, quanto que se vende ($) para cada país?
    private static void vendasPorPaisNaEuropa() {
        // países europeus 
        Set<String> paisesEuropa = Set.of(
            "Austria", "Belgium", "Denmark", "Finland", "France", "Germany", 
            "Ireland", "Italy", "Norway", "Poland", "Portugal", "Spain", 
            "Sweden", "Switzerland", "UK"
        ); 
        
        Map<String, Double> vendasNaEuropaPorPais = registros.stream()
                .filter(r -> paisesEuropa.contains(r.get("ClientePaís").trim())) 
                .collect(Collectors.groupingBy(
                        r -> r.get("ClientePaís").trim(),
                        Collectors.summingDouble(r -> parseDoubleSafe(r.get("Vendas")))
                ));
        
        vendasNaEuropaPorPais.forEach((pais, vendas) -> 
            System.out.printf("- %s: R$ %.2f%n", pais, vendas));
    }
    
    // Método auxiliar para obter o ano de forma segura
    private static int getYearSafe(CSVRecord r, DateTimeFormatter formatter) {
        String dataStr = r.get("Data");
        if (dataStr != null && !dataStr.trim().isEmpty()) {
            try {
                return LocalDate.parse(dataStr, formatter).getYear();
            } catch (Exception e) {
                return 0; 
            }
        }
        return 0;
    }

    // Método auxiliar para conversão segura de String para Double
    private static double parseDoubleSafe(String s) {
        try {
            return Double.parseDouble(s);
        } catch (NumberFormatException | NullPointerException e) {
            return 0.0;
        }
    }
}
