package si.fri.prpo.finance.zrna;

import javax.enterprise.context.ApplicationScoped;
import java.math.BigDecimal;
import java.util.*;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.logging.Logger;
import org.apache.http.HttpResponse;
import si.fri.prpo.finance.entitete.Stock;

@ApplicationScoped
public class YahooFinanceService {
    
    private static final Logger LOGGER = Logger.getLogger(YahooFinanceService.class.getName());
    private static final String BASE_URL = "https://query1.finance.yahoo.com/v8/finance/chart/";
    
    private final ObjectMapper objectMapper = new ObjectMapper();

    public Map<String, Object> getStockPriceAndStats(String symbol) throws Exception {
        String url = BASE_URL + symbol;
        
        LOGGER.info("Fetching stock data for symbol: " + symbol);
        
        try (CloseableHttpClient client = HttpClients.createDefault()) {
            HttpGet request = new HttpGet(url);
            HttpResponse httpResponse = client.execute(request);
            String response = EntityUtils.toString(httpResponse.getEntity());
            
            LOGGER.info("API Response status: " + httpResponse.getStatusLine());
            
            Map<String, Object> result = objectMapper.readValue(response, 
                objectMapper.getTypeFactory().constructMapType(Map.class, String.class, Object.class));
            
            // Parse Yahoo Finance response
            @SuppressWarnings("unchecked")
            Map<String, Object> chart = (Map<String, Object>) result.get("chart");
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> results = (List<Map<String, Object>>) chart.get("result");
            
            if (results == null || results.isEmpty()) {
                LOGGER.severe("No data found for symbol: " + symbol);
                throw new IllegalArgumentException("Stock not found: " + symbol);
            }
            
            Map<String, Object> stockData = new HashMap<>();
            @SuppressWarnings("unchecked")
            Map<String, Object> quote = (Map<String, Object>) results.get(0).get("meta");
            
            // Get the current price
            BigDecimal price = new BigDecimal(quote.get("regularMarketPrice").toString());
            
            stockData.put("symbol", symbol);
            stockData.put("price", price);
            stockData.put("currency", quote.get("currency"));
            stockData.put("exchange", quote.get("exchangeName"));
            stockData.put("lastUpdated", new Date(Long.parseLong(quote.get("regularMarketTime").toString()) * 1000));
            
            LOGGER.info("Successfully processed stock data for " + symbol);
            return stockData;
            
        } catch (Exception e) {
            LOGGER.severe("Error processing stock data for " + symbol + ": " + e.getMessage());
            throw new IllegalArgumentException("Error fetching stock data: " + e.getMessage());
        }
    }

    public List<Map<String, String>> searchStocks(String query) throws Exception {
        // For now, we'll keep this simple. Yahoo has a more complex search API
        // that we can implement if needed
        List<Map<String, String>> results = new ArrayList<>();
        Map<String, String> stock = new HashMap<>();
        stock.put("symbol", query.toUpperCase());
        stock.put("name", query.toUpperCase());
        results.add(stock);
        return results;
    }

    public Map<String, String> getComprehensiveStockInfo(String symbol) throws Exception {
        Map<String, Object> basicData = getStockPriceAndStats(symbol);
        
        Map<String, String> stockInfo = new HashMap<>();
        stockInfo.put("symbol", symbol);
        stockInfo.put("name", symbol); // Yahoo basic endpoint doesn't provide company name
        stockInfo.put("price", basicData.get("price").toString());
        stockInfo.put("currency", basicData.get("currency").toString());
        stockInfo.put("exchange", basicData.get("exchange").toString());
        
        return stockInfo;
    }

    //function to get stock by ticker
    public Stock getStockByTicker(String ticker) throws Exception {
        String url = BASE_URL + ticker;
        
        LOGGER.info("Fetching stock data for ticker: " + ticker);
        
        try (CloseableHttpClient client = HttpClients.createDefault()) {
            HttpGet request = new HttpGet(url);
            HttpResponse httpResponse = client.execute(request);
            String response = EntityUtils.toString(httpResponse.getEntity());
            
            Map<String, Object> result = objectMapper.readValue(response, 
                objectMapper.getTypeFactory().constructMapType(Map.class, String.class, Object.class));
            
            @SuppressWarnings("unchecked")
            Map<String, Object> chart = (Map<String, Object>) result.get("chart");
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> results = (List<Map<String, Object>>) chart.get("result");
            
            if (results == null || results.isEmpty()) {
                throw new IllegalArgumentException("Stock not found: " + ticker);
            }
            
            @SuppressWarnings("unchecked")
            Map<String, Object> quote = (Map<String, Object>) results.get(0).get("meta");
            
            Stock stock = new Stock();
            stock.setSymbol(ticker);
            stock.setName(ticker); // Yahoo basic API doesn't provide company name
            stock.setCurrentPrice(new BigDecimal(quote.get("regularMarketPrice").toString()));
            stock.setPriceLastUpdated(new Date(Long.parseLong(quote.get("regularMarketTime").toString()) * 1000));
            
            // Set purchase price to current price initially
            stock.setPurchasePrice(stock.getCurrentPrice());
            
            LOGGER.info("Successfully created stock object for " + ticker);
            return stock;
            
        } catch (Exception e) {
            LOGGER.severe("Error fetching stock data for " + ticker + ": " + e.getMessage());
            throw new IllegalArgumentException("Error fetching stock data: " + e.getMessage());
        }
    }
} 