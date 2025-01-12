package si.fri.prpo.finance.entitete;

import javax.persistence.*;
import java.math.BigDecimal;
import java.io.Serializable;
import javax.json.bind.annotation.JsonbTransient;
import java.util.Date;
import si.fri.prpo.finance.zrna.YahooFinanceService;
import java.util.Map;


@Entity
@Table(name = "stocks")
@NamedQueries({
    @NamedQuery(name = "Stock.getAll", query = "SELECT s FROM Stock s"),
    @NamedQuery(name = "Stock.getByPortfolioId", query = "SELECT s FROM Stock s WHERE s.portfolio.id = :portfolioId")
})
public class Stock implements Serializable{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false)
    private String symbol;

    @Column(nullable = false)
    private String name;

    @Column(name = "purchase_price", nullable = false)
    private BigDecimal purchasePrice;

    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    @Column(name = "current_price")
    private BigDecimal currentPrice;

    @ManyToOne
    @JoinColumn(name = "portfolio_id")
    @JsonbTransient
    private Portfolio portfolio;

    // Add last updated timestamp
    @Column(name = "price_last_updated")
    @Temporal(TemporalType.TIMESTAMP)
    private Date priceLastUpdated;

    // Method to update current price using AlphaVantage
    public void updateCurrentPrice(YahooFinanceService yahooFinanceService) throws Exception {
        Map<String, Object> stockData = yahooFinanceService.getStockPriceAndStats(this.symbol);
        this.currentPrice = new BigDecimal(stockData.get("price").toString());
        this.priceLastUpdated = new Date();
    }

    // Add method to check if price needs updating (e.g., if it's older than 15 minutes)
    public boolean needsPriceUpdate() {
        if (this.priceLastUpdated == null) {
            return true;
        }
        long fifteenMinutesInMillis = 15 * 60 * 1000;
        return System.currentTimeMillis() - this.priceLastUpdated.getTime() > fifteenMinutesInMillis;
    }

    // Calculate total value at purchase
    public BigDecimal getTotalPurchaseValue() {
        return purchasePrice.multiply(new BigDecimal(quantity));
    }

    // Calculate current total value
    public BigDecimal getCurrentTotalValue() {
        if (currentPrice == null) {
            return getTotalPurchaseValue(); // fallback to purchase value if no current price
        }
        return currentPrice.multiply(new BigDecimal(quantity));
    }

    // Calculate profit/loss
    public BigDecimal getProfitLoss() {
        return getCurrentTotalValue().subtract(getTotalPurchaseValue());
    }

    // Getters and setters
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

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

    public BigDecimal getPurchasePrice() {
        return purchasePrice;
    }

    public void setPurchasePrice(BigDecimal purchasePrice) {
        this.purchasePrice = purchasePrice;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public BigDecimal getCurrentPrice() {
        return currentPrice;
    }

    public void setCurrentPrice(BigDecimal currentPrice) {
        this.currentPrice = currentPrice;
    }

    public Portfolio getPortfolio() {
        return portfolio;
    }

    public void setPortfolio(Portfolio portfolio) {
        this.portfolio = portfolio;
    }

    // Add getter and setter for priceLastUpdated
    public Date getPriceLastUpdated() {
        return priceLastUpdated;
    }

    public void setPriceLastUpdated(Date priceLastUpdated) {
        this.priceLastUpdated = priceLastUpdated;
    }
}