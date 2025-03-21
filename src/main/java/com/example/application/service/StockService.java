package com.example.application.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.ResponseEntity;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.ArrayList;
import java.util.List;

@Service
public class StockService {
    
    private final RestTemplate restTemplate;
    private final String apiKey;
    private static final String BASE_URL = "https://www.alphavantage.co/query";

    public StockService(@Value("${alphavantage.api.key:demo}") String apiKey) {
        this.apiKey = apiKey;
        this.restTemplate = new RestTemplate();
    }

    public List<StockQuote> getStockQuotes(List<String> symbols) {
        List<StockQuote> quotes = new ArrayList<>();
        for (String symbol : symbols) {
            try {
                String url = String.format("%s?function=GLOBAL_QUOTE&symbol=%s&apikey=%s",
                        BASE_URL, symbol.toUpperCase(), apiKey);
                
                ResponseEntity<AlphaVantageResponse> response = restTemplate.getForEntity(url, AlphaVantageResponse.class);
                AlphaVantageResponse data = response.getBody();
                
                if (data != null) {
                    if (data.information != null) {
                        System.err.println("API Error for " + symbol + ": " + data.information);
                        quotes.add(createDefaultQuote(symbol));
                    } else if (data.globalQuote != null) {
                        quotes.add(data.globalQuote);
                    } else {
                        quotes.add(createDefaultQuote(symbol));
                    }
                } else {
                    quotes.add(createDefaultQuote(symbol));
                }
                
                // Add a small delay to avoid hitting API rate limits
                Thread.sleep(1000);
                
            } catch (Exception e) {
                System.err.println("Error fetching stock quote for " + symbol + ": " + e.getMessage());
                quotes.add(createDefaultQuote(symbol));
            }
        }
        return quotes;
    }

    private StockQuote createDefaultQuote(String symbol) {
        StockQuote defaultQuote = new StockQuote();
        defaultQuote.setSymbol(symbol.toUpperCase());
        defaultQuote.setPrice("0.00");
        defaultQuote.setChange("0.00");
        defaultQuote.setChangePercent("0.00%");
        return defaultQuote;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class AlphaVantageResponse {
        @JsonProperty("Global Quote")
        private StockQuote globalQuote;

        @JsonProperty("Information")
        private String information;

        @JsonProperty("Note")
        private String note;

        public StockQuote getGlobalQuote() {
            return globalQuote;
        }

        public void setGlobalQuote(StockQuote globalQuote) {
            this.globalQuote = globalQuote;
        }

        public String getInformation() {
            return information;
        }

        public void setInformation(String information) {
            this.information = information;
        }

        public String getNote() {
            return note;
        }

        public void setNote(String note) {
            this.note = note;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class StockQuote {
        @JsonProperty("01. symbol")
        private String symbol;
        
        @JsonProperty("05. price")
        private String price;
        
        @JsonProperty("09. change")
        private String change;
        
        @JsonProperty("10. change percent")
        private String changePercent;

        public String getSymbol() {
            return symbol;
        }

        public void setSymbol(String symbol) {
            this.symbol = symbol;
        }

        public String getPrice() {
            return price;
        }

        public void setPrice(String price) {
            this.price = price;
        }

        public String getChange() {
            return change;
        }

        public void setChange(String change) {
            this.change = change;
        }

        public String getChangePercent() {
            return changePercent;
        }

        public void setChangePercent(String changePercent) {
            this.changePercent = changePercent;
        }
    }
}