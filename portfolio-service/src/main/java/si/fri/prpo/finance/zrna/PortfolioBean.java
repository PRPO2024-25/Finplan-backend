package si.fri.prpo.finance.zrna;

import si.fri.prpo.finance.entitete.Portfolio;
import javax.enterprise.context.ApplicationScoped;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;
import java.math.BigDecimal;
import java.util.List;
import java.util.logging.Logger;
import javax.ws.rs.NotFoundException;
import java.util.Date;
import java.util.Map;
import java.util.HashMap;
import javax.persistence.TypedQuery;
import si.fri.prpo.finance.entitete.Stock;
import java.util.ArrayList;
import javax.inject.Inject;
import java.math.RoundingMode;

@ApplicationScoped
public class PortfolioBean {

    private static final Logger LOG = Logger.getLogger(PortfolioBean.class.getName());

    @PersistenceContext(unitName = "kumuluzee-samples-jpa")
    private EntityManager em;

    @Inject
    private YahooFinanceService yahooFinanceService;

    public List<Portfolio> getAllPortfolios() {
        return em.createNamedQuery("Portfolio.getAll", Portfolio.class).getResultList();
    }

    @Transactional
    public Portfolio createPortfolio(Portfolio portfolio) {
        LOG.info("Creating portfolio for user: " + portfolio.getUserId());
        
        portfolio.setCreatedAt(new Date());
        
        if (portfolio.getCashBalance() == null) {
            portfolio.setCashBalance(BigDecimal.ZERO);
        }
        
        em.persist(portfolio);
        em.flush();
        
        LOG.info("Portfolio created with ID: " + portfolio.getId());
        return portfolio;
    }

    public Portfolio getPortfolio(Integer id) {
        return em.find(Portfolio.class, id);
    }

    public List<Portfolio> getUserPortfolios(Long userId) {
        return em.createNamedQuery("Portfolio.getByUserId", Portfolio.class)
                .setParameter("userId", userId)
                .getResultList();
    }

    @Transactional
    public void updateBalance(Integer portfolioId, BigDecimal amount) {
        Portfolio portfolio = getPortfolio(portfolioId);
        if (portfolio == null) {
            throw new NotFoundException("Portfolio not found");
        }
        
        BigDecimal newBalance = portfolio.getCashBalance().add(amount);
        if (newBalance.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Insufficient funds");
        }
        
        portfolio.setCashBalance(newBalance);
        em.merge(portfolio);
    }

    public boolean canProcessAmount(Integer portfolioId, BigDecimal amount) {
        LOG.info("Checking if portfolio " + portfolioId + " can process amount: " + amount);
        Portfolio portfolio = getPortfolio(portfolioId);
        return portfolio != null && hasEnoughBalance(portfolioId, amount);
    }

    public Portfolio updatePortfolio(Portfolio portfolio) {
        return em.merge(portfolio);
    }

    public Portfolio getPortfolioById(Integer id) {
        return em.find(Portfolio.class, id);
    }

    @Transactional
    public void deletePortfolio(Integer id) {
        Portfolio portfolio = getPortfolio(id);
        if (portfolio != null) {
            em.remove(portfolio);
        }
    }

    //function to check if portfolio has enough balance
    public boolean hasEnoughBalance(Integer portfolioId, BigDecimal amount) {
        Portfolio portfolio = getPortfolio(portfolioId);
        return portfolio != null && portfolio.getCashBalance().compareTo(amount) >= 0;
    }
    
    @Transactional
    public void withdrawMoney(Integer portfolioId, BigDecimal amount) {
        LOG.info("Withdrawing " + amount + " from portfolio " + portfolioId);
        Portfolio portfolio = getPortfolio(portfolioId);
        if (portfolio == null) {
            throw new NotFoundException("Portfolio not found");
        }
        
        if (!hasEnoughBalance(portfolioId, amount)) {
            throw new IllegalArgumentException("Insufficient funds");
        }
        
        portfolio.setCashBalance(portfolio.getCashBalance().subtract(amount));
        em.merge(portfolio);
        LOG.info("Withdrawal successful. New balance: " + portfolio.getCashBalance());
    }

    @Transactional
    public void depositMoney(Integer portfolioId, BigDecimal amount) {
        LOG.info("Depositing " + amount + " to portfolio " + portfolioId);
        Portfolio portfolio = getPortfolio(portfolioId);
        if (portfolio == null) {
            throw new NotFoundException("Portfolio not found");
        }
        
        portfolio.setCashBalance(portfolio.getCashBalance().add(amount));
        em.merge(portfolio);
        LOG.info("Deposit successful. New balance: " + portfolio.getCashBalance());
    }

    public BigDecimal getBalance(Integer portfolioId) {
        Portfolio portfolio = getPortfolio(portfolioId);
        if (portfolio == null) {
            throw new NotFoundException("Portfolio not found");
        }
        return portfolio.getCashBalance();
    }

    //add logic to update the portfolio balance
    @Transactional
    public void processTransfer(Integer portfolioId, BigDecimal amount, boolean isSender) {
        Portfolio portfolio = getPortfolio(portfolioId);
        if (portfolio == null) {
            throw new NotFoundException("Portfolio not found");
        }
        
        BigDecimal newBalance;
        if (isSender) {
            newBalance = portfolio.getCashBalance().subtract(amount);
        } else {
            newBalance = portfolio.getCashBalance().add(amount);
        }
        
        portfolio.setCashBalance(newBalance);
        em.merge(portfolio);
    }

    public List<Portfolio> getPortfoliosByUserId(Integer userId) {
        TypedQuery<Portfolio> query = em.createQuery(
            "SELECT p FROM Portfolio p WHERE p.userId = :userId", 
            Portfolio.class
        );
        query.setParameter("userId", userId);
        return query.getResultList();
    }

    public Map<String, Object> getPortfolioStats(Integer portfolioId) {
        Portfolio portfolio = getPortfolio(portfolioId);
        if (portfolio == null) {
            throw new NotFoundException("Portfolio not found");
        }

        Map<String, Object> stats = new HashMap<>();
        stats.put("currentBalance", portfolio.getCashBalance());
        stats.put("createdAt", portfolio.getCreatedAt());
        stats.put("userId", portfolio.getUserId());
        //get the stocks in the portfolio
        List<Stock> stocks = portfolio.getStocks();
        stats.put("stocks", stocks);
        return stats;
    }

    public long getPortfolioCountForUser(Long userId) {
        return em.createQuery("SELECT COUNT(p) FROM Portfolio p WHERE p.userId = :userId", Long.class)
                 .setParameter("userId", userId)
                 .getSingleResult();
    }

    @Transactional
    public Portfolio createSimplePortfolio(Long userId) {
        try {
            // Create new portfolio without setting ID
            Portfolio portfolio = new Portfolio();
            portfolio.setUserId(userId);
            portfolio.setCashBalance(BigDecimal.ZERO);
            portfolio.setCreatedAt(new Date());
            portfolio.setStocks(new ArrayList<>());
            
            // Let JPA handle the ID generation
            em.persist(portfolio);
            em.flush();
            em.refresh(portfolio); // Refresh to get the generated ID
            
            LOG.info("Created portfolio with ID: " + portfolio.getId());
            return portfolio;
        } catch (Exception e) {
            LOG.severe("Error creating portfolio: " + e.getMessage());
            throw e;
        }
    }

    @Transactional
    public Stock addStockToPortfolio(Integer portfolioId, String ticker, Integer quantity, BigDecimal purchasePrice) throws Exception {
        // Verify stock exists and get market data
        Stock stockData = yahooFinanceService.getStockByTicker(ticker.toUpperCase());
        
        Portfolio portfolio = getPortfolio(portfolioId);
        if (portfolio == null) {
            throw new NotFoundException("Portfolio not found");
        }

        if (portfolio.getStocks() == null) {
            portfolio.setStocks(new ArrayList<>());
        }

        // Check for existing stock
        Stock existingStock = portfolio.getStocks().stream()
                .filter(s -> s.getSymbol().equals(ticker.toUpperCase()))
                .findFirst()
                .orElse(null);

        if (existingStock != null) {
            // Update existing stock
            existingStock.setQuantity(existingStock.getQuantity() + quantity);
            existingStock.setPurchasePrice(
                    calculateAveragePurchasePrice(existingStock, quantity, purchasePrice)
            );
            existingStock.setCurrentPrice(stockData.getCurrentPrice());
            existingStock.setPriceLastUpdated(new Date());
            
            updatePortfolio(portfolio);
            return existingStock;
        } else {
            // Create new stock
            Stock newStock = new Stock();
            newStock.setSymbol(stockData.getSymbol());
            newStock.setName(stockData.getName());
            newStock.setQuantity(quantity);
            newStock.setPurchasePrice(purchasePrice);
            newStock.setCurrentPrice(stockData.getCurrentPrice());
            newStock.setPriceLastUpdated(new Date());
            newStock.setPortfolio(portfolio);

            portfolio.getStocks().add(newStock);
            updatePortfolio(portfolio);
            return newStock;
        }
    }

    private BigDecimal calculateAveragePurchasePrice(Stock stock, Integer newQuantity, BigDecimal newPrice) {
        return (stock.getPurchasePrice()
                .multiply(new BigDecimal(stock.getQuantity()))
                .add(newPrice.multiply(new BigDecimal(newQuantity))))
                .divide(new BigDecimal(stock.getQuantity() + newQuantity), 2, RoundingMode.HALF_UP);
    }

    @Transactional
    public Portfolio createPortfolio(Long userId) {
        Portfolio portfolio = new Portfolio();
        portfolio.setUserId(userId);
        portfolio.setCashBalance(BigDecimal.ZERO);
        portfolio.setCreatedAt(new Date());
        portfolio.setStocks(new ArrayList<>());
        
        LOG.info("Creating portfolio for user: " + userId);
        em.persist(portfolio);
        em.flush();
        LOG.info("Portfolio created with ID: " + portfolio.getId());
        
        return portfolio;
    }

    @Transactional
    public List<Stock> updateAllStockPrices() {
        LOG.info("Starting to update all stock prices");
        List<Stock> updatedStocks = new ArrayList<>();
        
        // Get all unique stocks from the database
        List<Stock> allStocks = em.createQuery("SELECT DISTINCT s FROM Stock s", Stock.class)
                .getResultList();
        
        for (Stock stock : allStocks) {
            try {
                // Get current market data for each stock
                Stock marketData = yahooFinanceService.getStockByTicker(stock.getSymbol());
                
                // Update the stock price
                stock.setCurrentPrice(marketData.getCurrentPrice());
                stock.setPriceLastUpdated(new Date());
                
                em.merge(stock);
                updatedStocks.add(stock);
                
                LOG.info("Updated price for " + stock.getSymbol() + " to " + stock.getCurrentPrice());
            } catch (Exception e) {
                LOG.warning("Failed to update price for " + stock.getSymbol() + ": " + e.getMessage());
            }
        }
        
        return updatedStocks;
    }
}
    