package si.fri.prpo.finance.zrna;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import si.fri.prpo.finance.entitete.Stock;
import si.fri.prpo.finance.entitete.Portfolio;
import si.fri.prpo.finance.exceptions.StockException;

import javax.enterprise.context.ApplicationScoped;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;
import javax.inject.Inject;
import java.math.BigDecimal;
import java.util.List;
import javax.ws.rs.NotFoundException;

import java.util.Map;
import java.util.Date;
//import java.util.Optional;


@ApplicationScoped
public class StockBean {
    private static final Logger log = LoggerFactory.getLogger(StockBean.class);

    @PersistenceContext(unitName = "kumuluzee-samples-jpa")
    private EntityManager em;

    @Inject
    private YahooFinanceService yahooFinanceService;

    public Stock findStock(String symbol) {
        log.info("Finding stock with symbol: {}", symbol);
        try {
            return em.createQuery("SELECT s FROM Stock s WHERE s.symbol = :symbol", Stock.class)
                    .setParameter("symbol", symbol)
                    .getResultStream()
                    .findFirst()
                    .orElse(null);
        } catch (Exception e) {
            log.error("Error finding stock: {}", e.getMessage());
            throw new StockException("Error finding stock", e);
        }
    }

    @Transactional
    public Stock createStockInPortfolio(
            String symbol,
            Integer quantity,
            BigDecimal purchasePrice,
            BigDecimal currentPrice,
            Portfolio portfolio) {
        
        Stock stock = new Stock();
        stock.setSymbol(symbol);
        stock.setQuantity(quantity);
        stock.setPurchasePrice(purchasePrice);
        stock.setCurrentPrice(currentPrice);
        stock.setPortfolio(portfolio);
        stock.setPriceLastUpdated(new Date());

        em.persist(stock);
        return stock;
    }

    public Map<String, Object> getStockPriceAndStats(String symbol) throws Exception {
        return yahooFinanceService.getStockPriceAndStats(symbol);
    }

    public List<Stock> getAllStocks() {
        return em.createNamedQuery("Stock.getAll", Stock.class).getResultList();
    }

    public Stock getStockById(Integer id) {

        return em.find(Stock.class, id);
    }

    public List<Stock> searchStocks(String query) {
        return em.createNamedQuery("Stock.search", Stock.class)
                .setParameter("query", query)
                .getResultList();
    }

    public List<Stock> getPortfolioStocks(Integer portfolioId) {
        return em.createNamedQuery("Stock.findByPortfolio", Stock.class)
                .setParameter("portfolioId", portfolioId)
                .getResultList();
    }

    @Transactional
    public Stock addStockToPortfolio(Stock stock, Integer portfolioId) {
        log.info("Adding stock to portfolio: " + portfolioId);
        
        Portfolio portfolio = em.find(Portfolio.class, portfolioId);
        if (portfolio == null) {
            throw new NotFoundException("Portfolio not found");
        }

        stock.setPortfolio(portfolio);
        em.persist(stock);
        return stock;
    }

    @Transactional
    public void updateStockPrice(Integer stockId, BigDecimal newPrice) {
        Stock stock = em.find(Stock.class, stockId);
        if (stock == null) {
            throw new NotFoundException("Stock not found");
        }
        stock.setCurrentPrice(newPrice);
        stock.setPriceLastUpdated(new Date());
        em.merge(stock);
    }

    @Transactional
    public void removeStock(Integer stockId) {
        Stock stock = em.find(Stock.class, stockId);
        if (stock != null) {
            em.remove(stock);
        }
    }

    @Transactional
    public List<Stock> updateAllStockPrices() {
        return em.createNamedQuery("Stock.updateAllPrices", Stock.class).getResultList();
    }

    @Transactional
    public Stock createStock(Stock stock) {
        em.persist(stock);
        return stock;
    }

    public List<Stock> getStocksByPortfolioId(Integer portfolioId) {
        try {
            return em.createQuery(
                "SELECT s FROM Stock s WHERE s.portfolio.id = :portfolioId", 
                Stock.class)
                .setParameter("portfolioId", portfolioId)
                .getResultList();
        } catch (Exception e) {
            log.error("Error retrieving stocks for portfolio: {}", e.getMessage());
            throw new StockException("Error retrieving stocks", e);
        }
    }
}
