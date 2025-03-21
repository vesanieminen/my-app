package com.example.application.service;

import java.util.*;
import java.util.stream.Collectors;

public class StockSymbolUtil {
    private static final Map<String, String> nameToSymbol = new HashMap<>();
    private static final Map<String, String> symbolToName = new HashMap<>();

    static {
        // Technology
        addMapping("Apple", "AAPL");
        addMapping("Microsoft", "MSFT");
        addMapping("Amazon", "AMZN");
        addMapping("Alphabet (Google)", "GOOGL");
        addMapping("NVIDIA", "NVDA");
        addMapping("Tesla", "TSLA");
        addMapping("Meta Platforms", "META");
        addMapping("Taiwan Semiconductor", "TSM");
        addMapping("Broadcom", "AVGO");
        addMapping("Oracle", "ORCL");
        addMapping("Cisco", "CSCO");
        addMapping("Adobe", "ADBE");
        addMapping("Netflix", "NFLX");
        addMapping("Intel", "INTC");
        addMapping("Salesforce", "CRM");
        addMapping("Advanced Micro Devices", "AMD");
        addMapping("Qualcomm", "QCOM");
        addMapping("IBM", "IBM");

        // Finance
        addMapping("JPMorgan Chase", "JPM");
        addMapping("Visa", "V");
        addMapping("Mastercard", "MA");
        addMapping("Bank of America", "BAC");
        addMapping("Wells Fargo", "WFC");
        addMapping("Morgan Stanley", "MS");
        addMapping("Goldman Sachs", "GS");
        addMapping("Charles Schwab", "SCHW");

        // Healthcare
        addMapping("UnitedHealth", "UNH");
        addMapping("Johnson & Johnson", "JNJ");
        addMapping("Eli Lilly", "LLY");
        addMapping("Pfizer", "PFE");
        addMapping("Merck", "MRK");
        addMapping("AbbVie", "ABBV");
        addMapping("Thermo Fisher Scientific", "TMO");
        addMapping("Abbott Laboratories", "ABT");

        // Consumer
        addMapping("Walmart", "WMT");
        addMapping("Procter & Gamble", "PG");
        addMapping("Coca-Cola", "KO");
        addMapping("PepsiCo", "PEP");
        addMapping("McDonald's", "MCD");
        addMapping("Nike", "NKE");
        addMapping("Starbucks", "SBUX");
        addMapping("Disney", "DIS");
        addMapping("Home Depot", "HD");
        addMapping("Target", "TGT");

        // Energy
        addMapping("Exxon Mobil", "XOM");
        addMapping("Chevron", "CVX");
        addMapping("Shell", "SHEL");
        addMapping("BP", "BP");
        addMapping("ConocoPhillips", "COP");

        // Telecommunications
        addMapping("Verizon", "VZ");
        addMapping("AT&T", "T");
        addMapping("T-Mobile", "TMUS");

        // Automotive
        addMapping("Ford", "F");
        addMapping("General Motors", "GM");
        addMapping("Toyota", "TM");
        addMapping("Honda", "HMC");
        addMapping("Volkswagen", "VWAGY");
        addMapping("BMW", "BMWYY");
        addMapping("Mercedes-Benz", "MBG.DE");

        // Airlines
        addMapping("Delta Air Lines", "DAL");
        addMapping("United Airlines", "UAL");
        addMapping("American Airlines", "AAL");
        addMapping("Southwest Airlines", "LUV");

        // Entertainment
        addMapping("Netflix", "NFLX");
        addMapping("Sony", "SONY");
        addMapping("Warner Bros Discovery", "WBD");
        addMapping("Paramount", "PARA");

        // Social Media
        addMapping("Pinterest", "PINS");
        addMapping("Snap", "SNAP");
        addMapping("X (Twitter)", "TWTR");

        // E-commerce
        addMapping("eBay", "EBAY");
        addMapping("Shopify", "SHOP");
        addMapping("Etsy", "ETSY");
        addMapping("Alibaba", "BABA");

        // Gaming
        addMapping("Electronic Arts", "EA");
        addMapping("Activision Blizzard", "ATVI");
        addMapping("Take-Two Interactive", "TTWO");
        addMapping("Roblox", "RBLX");

        // Chinese Tech
        addMapping("Alibaba", "BABA");
        addMapping("Baidu", "BIDU");
        addMapping("JD.com", "JD");
        addMapping("NetEase", "NTES");
        addMapping("Tencent", "TCEHY");

        // European Tech
        addMapping("SAP", "SAP");
        addMapping("ASML Holding", "ASML");
        addMapping("Adyen", "ADYEY");
        addMapping("Spotify", "SPOT");

        // Cryptocurrencies and Fintech
        addMapping("Coinbase", "COIN");
        addMapping("Block (Square)", "SQ");
        addMapping("PayPal", "PYPL");
        addMapping("Robinhood", "HOOD");
    }

    private static void addMapping(String name, String symbol) {
        nameToSymbol.put(name.toLowerCase(), symbol);
        symbolToName.put(symbol.toLowerCase(), name);
    }

    public static String getSymbol(String input) {
        String normalized = input.trim().toLowerCase();
        
        // Direct match with a company name
        String symbol = nameToSymbol.get(normalized);
        if (symbol != null) {
            return symbol;
        }

        // Direct match with a symbol (case-insensitive)
        String name = symbolToName.get(normalized);
        if (name != null) {
            return normalized.toUpperCase();
        }

        // If input looks like a symbol (all caps), return it as is
        if (input.trim().toUpperCase().equals(input.trim())) {
            return input.trim();
        }

        // Partial match with company names
        for (Map.Entry<String, String> entry : nameToSymbol.entrySet()) {
            if (entry.getKey().contains(normalized)) {
                return entry.getValue();
            }
        }

        // No match found, return the input as uppercase (assuming it's a symbol)
        return input.trim().toUpperCase();
    }

    public static List<String> searchCompanies(String query) {
        if (query == null || query.trim().isEmpty()) {
            return new ArrayList<>(nameToSymbol.values().stream()
                .map(symbol -> symbol + " - " + symbolToName.get(symbol.toLowerCase()))
                .sorted()
                .collect(Collectors.toList()));
        }

        Set<String> results = new TreeSet<>(); // Using TreeSet for automatic sorting
        String normalizedQuery = query.toLowerCase().trim();

        // Search in company names
        for (Map.Entry<String, String> entry : nameToSymbol.entrySet()) {
            if (entry.getKey().contains(normalizedQuery)) {
                results.add(entry.getValue() + " - " + capitalizeFirstLetter(entry.getKey()));
            }
        }

        // Search in symbols
        for (Map.Entry<String, String> entry : symbolToName.entrySet()) {
            if (entry.getKey().contains(normalizedQuery)) {
                results.add(entry.getKey().toUpperCase() + " - " + entry.getValue());
            }
        }

        return new ArrayList<>(results);
    }

    private static String capitalizeFirstLetter(String input) {
        if (input == null || input.isEmpty()) {
            return input;
        }
        return input.substring(0, 1).toUpperCase() + input.substring(1);
    }

    public static List<String> getAllStocks() {
        return nameToSymbol.entrySet().stream()
            .map(entry -> entry.getValue() + " - " + capitalizeFirstLetter(entry.getKey()))
            .sorted()
            .collect(Collectors.toList());
    }
}