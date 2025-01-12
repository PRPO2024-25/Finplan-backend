package si.fri.prpo.finance.dto;

import java.math.BigDecimal;
import javax.json.bind.annotation.JsonbProperty;

public class StockDTO {
    @JsonbProperty("symbol")
    private String symbol;

    @JsonbProperty("name")
    private String name;

    @JsonbProperty("quantity")
    private Integer quantity;

    @JsonbProperty("purchasePrice")
    private BigDecimal purchasePrice;

    @JsonbProperty("currentPrice")
    private BigDecimal currentPrice;

    // Default constructor
    public StockDTO() {
    }

    // Constructor with fields
    public StockDTO(String symbol, String name, Integer quantity, BigDecimal purchasePrice, BigDecimal currentPrice) {
        this.symbol = symbol;
        this.name = name;
        this.quantity = quantity;
        this.purchasePrice = purchasePrice;
        this.currentPrice = currentPrice;
    }

    // Getters and Setters
    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public BigDecimal getPurchasePrice() {
        return purchasePrice;
    }

    public void setPurchasePrice(BigDecimal purchasePrice) {
        this.purchasePrice = purchasePrice;
    }

    public BigDecimal getCurrentPrice() {
        return currentPrice;
    }

    public void setCurrentPrice(BigDecimal currentPrice) {
        this.currentPrice = currentPrice;
    }

    // Optional: Override toString() method for debugging
    @Override
    public String toString() {
        return "StockDTO{" +
                "symbol='" + symbol + '\'' +
                ", name='" + name + '\'' +
                ", quantity=" + quantity +
                ", purchasePrice=" + purchasePrice +
                ", currentPrice=" + currentPrice +
                '}';
    }
} 