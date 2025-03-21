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
    private static final String BASE_URL = "https://finnhub.io/api/v1";

    public StockService(@Value("${finnhub.api.key}") String apiKey) {
        this.apiKey = apiKey;
        this.restTemplate = new RestTemplate();
    }

    public List<StockQuote> getStockQuotes(List<String> symbols) {
        List<StockQuote> quotes = new ArrayList<>();
        for (String symbol : symbols) {
            try {
                String url = String.format("%s/quote?symbol=%s&token=%s",
                        BASE_URL, symbol.toUpperCase(), apiKey);
                
                ResponseEntity<FinnhubQuote> response = restTemplate.getForEntity(url, FinnhubQuote.class);
                FinnhubQuote data = response.getBody();
                
                if (data != null && data.getCurrentPrice() > 0) {
                    quotes.add(convertToStockQuote(symbol, data));
                } else {
                    quotes.add(createDefaultQuote(symbol));
                }
                
                // Add a small delay to avoid hitting API rate limits
                Thread.sleep(200);
                
            } catch (Exception e) {
                System.err.println("Error fetching stock quote for " + symbol + ": " + e.getMessage());
                quotes.add(createDefaultQuote(symbol));
            }
        }
        return quotes;
    }

    public List<HistoricalDataPoint> getHistoricalData(String symbol, String resolution, long from, long to) {
        try {
            String url = String.format("%s/stock/candle?symbol=%s&resolution=%s&from=%d&to=%d&token=%s",
                    BASE_URL, symbol.toUpperCase(), resolution, from, to, apiKey);
            
            ResponseEntity<FinnhubHistoricalData> response = restTemplate.getForEntity(url, FinnhubHistoricalData.class);
            FinnhubHistoricalData data = response.getBody();
            
            List<HistoricalDataPoint> historicalData = new ArrayList<>();
            if (data != null && "ok".equals(data.getStatus())) {
                for (int i = 0; i < data.getTimestamps().length; i++) {
                    HistoricalDataPoint point = new HistoricalDataPoint();
                    point.setTimestamp(data.getTimestamps()[i]);
                    point.setClose(data.getClosePrices()[i]);
                    historicalData.add(point);
                }
            }
            return historicalData;
        } catch (Exception e) {
            System.err.println("Error fetching historical data for " + symbol + ": " + e.getMessage());
            return new ArrayList<>();
        }
    }

    private StockQuote convertToStockQuote(String symbol, FinnhubQuote finnhubQuote) {
        StockQuote quote = new StockQuote();
        quote.setSymbol(symbol.toUpperCase());
        quote.setPrice(String.format("%.2f", finnhubQuote.getCurrentPrice()));
        double change = finnhubQuote.getChange();
        quote.setChange(String.format("%.2f", change));
        quote.setChangePercent(String.format("%.2f%%", finnhubQuote.getPercentChange()));
        return quote;
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
    public static class FinnhubQuote {
        @JsonProperty("c")
        private double currentPrice;
        
        @JsonProperty("d")
        private double change;
        
        @JsonProperty("dp")
        private double percentChange;

        public double getCurrentPrice() {
            return currentPrice;
        }

        public void setCurrentPrice(double currentPrice) {
            this.currentPrice = currentPrice;
        }

        public double getChange() {
            return change;
        }

        public void setChange(double change) {
            this.change = change;
        }

        public double getPercentChange() {
            return percentChange;
        }

        public void setPercentChange(double percentChange) {
            this.percentChange = percentChange;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class FinnhubHistoricalData {
        @JsonProperty("s")
        private String status;
        
        @JsonProperty("t")
        private long[] timestamps;
        
        @JsonProperty("c")
        private double[] closePrices;

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public long[] getTimestamps() {
            return timestamps;
        }

        public void setTimestamps(long[] timestamps) {
            this.timestamps = timestamps;
        }

        public double[] getClosePrices() {
            return closePrices;
        }

        public void setClosePrices(double[] closePrices) {
            this.closePrices = closePrices;
        }
    }

    public static class StockQuote {
        private String symbol;
        private String price;
        private String change;
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

    public static class HistoricalDataPoint {
        private long timestamp;
        private double close;

        public long getTimestamp() {
            return timestamp;
        }

        public void setTimestamp(long timestamp) {
            this.timestamp = timestamp;
        }

        public double getClose() {
            return close;
        }

        public void setClose(double close) {
            this.close = close;
        }
    }
}