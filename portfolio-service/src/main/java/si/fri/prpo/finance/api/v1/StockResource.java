package si.fri.prpo.finance.api.v1;

import si.fri.prpo.finance.entitete.Stock;
import si.fri.prpo.finance.zrna.StockBean;
import si.fri.prpo.finance.dto.ErrorResponse;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;
import java.util.Map;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.logging.Logger;
import si.fri.prpo.finance.zrna.YahooFinanceService;
import javax.transaction.Transactional;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
    
@Path("/stocks")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@ApplicationScoped
@Tag(name = "Stock Management", description = "Endpoints for managing portfolio stocks")
public class StockResource {

    @Inject
    private StockBean stockBean;

    @Inject
    private YahooFinanceService yahooService;

    private static final Logger LOGGER = Logger.getLogger(StockResource.class.getName());

    @GET
    @Operation(summary = "Get all stocks")
    @APIResponses({
        @APIResponse(responseCode = "200", description = "List of all stocks retrieved successfully"),
        @APIResponse(responseCode = "500", description = "Internal server error")
    })
    public Response getAllStocks() {
        try {
            List<Stock> stocks = stockBean.getAllStocks();
            return Response.ok(stocks)
                    .header("X-Total-Count", stocks.size())
                    .build();
        } catch (Exception e) {
            LOGGER.severe("Error retrieving stocks: " + e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(new ErrorResponse("Error retrieving stocks", 500))
                    .build();
        }
    }

    @GET
    @Path("/{id}")
    @Operation(summary = "Get stock by ID")
    public Response getStockById(@PathParam("id") Integer id) {
        try {
            Stock stock = stockBean.getStockById(id);
            if (stock == null) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity(new ErrorResponse("Stock not found", 404))
                        .build();
            }
            return Response.ok(stock).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(new ErrorResponse("Error retrieving stock", 500))
                    .build();
        }
    }

    @GET
    @Path("/search/{query}")
    @Operation(summary = "Search for stocks")
    public Response searchStocks(@PathParam("query") String query) {
        try {
            List<Stock> results = stockBean.searchStocks(query);
            return Response.ok(results).build();
        } catch (Exception e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ErrorResponse("Error searching stocks: " + e.getMessage(), 400))
                    .build();
        }
    }

    @GET
    @Path("/price/{symbol}")
    @Operation(summary = "Get current stock price and daily stats")
    public Response getStockPrice(@PathParam("symbol") String symbol) {
        try {
            Map<String, Object> priceData = stockBean.getStockPriceAndStats(symbol);
            return Response.ok(priceData).build();
        } catch (Exception e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ErrorResponse("Error fetching stock price: " + e.getMessage(), 400))
                    .build();
        }
    }

    @GET
    @Path("/portfolio/{portfolioId}")
    @Operation(summary = "Get portfolio stocks")
    public Response getPortfolioStocks(@PathParam("portfolioId") Integer portfolioId) {
        try {
            List<Stock> stocks = stockBean.getStocksByPortfolioId(portfolioId);
            return Response.ok(stocks).build();
            
        } catch (Exception e) {
            LOGGER.severe("Error getting portfolio stocks: " + e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(new ErrorResponse("Error retrieving stocks: " + e.getMessage(), 500))
                    .build();
        }
    }

    @POST
    @Path("/portfolio/{portfolioId}")
    @Transactional
    @Operation(summary = "Add stock to portfolio")
    @APIResponses({
        @APIResponse(responseCode = "201", description = "Stock added successfully"),
        @APIResponse(responseCode = "400", description = "Invalid request"),
        @APIResponse(responseCode = "404", description = "Portfolio not found"),
        @APIResponse(responseCode = "409", description = "Stock already exists in portfolio")
    })
    public Response addStockToPortfolio(
            @PathParam("portfolioId") Integer portfolioId,
            @QueryParam("symbol") String symbol) {
        try {
            if (symbol == null) {
                return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ErrorResponse("Stock symbol is required", 400))
                    .build();
            }

            Stock newStock = stockBean.addStockToPortfolio( new Stock(), portfolioId);
            return Response.status(Response.Status.CREATED)
                    .entity(newStock)
                    .header("Location", "/stocks/" + newStock.getId())
                    .build();

        } catch (NotFoundException e) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(new ErrorResponse(e.getMessage(), 404))
                    .build();
        } catch (Exception e) {
            LOGGER.severe("Error adding stock: " + e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(new ErrorResponse("Error adding stock", 500))
                    .build();
        }
    }

    @PUT
    @Path("/{id}/price")
    @Operation(summary = "Update stock price")
    public Response updateStockPrice(
            @PathParam("id") Integer id,
            Map<String, BigDecimal> request) {
        try {
            BigDecimal newPrice = request.get("price");
            if (newPrice == null) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(new ErrorResponse("Price is required", 400))
                        .build();
            }
            stockBean.updateStockPrice(id, newPrice);
            return Response.ok().build();
        } catch (NotFoundException e) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(new ErrorResponse(e.getMessage(), 404))
                    .build();
        }
    }

    @DELETE
    @Path("/{id}")
    @Operation(summary = "Remove stock")
    public Response removeStock(@PathParam("id") Integer id) {
        try {
            stockBean.removeStock(id);
            return Response.ok().build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(new ErrorResponse("Error removing stock", 500))
                    .build();
        }
    }

    @PUT
    @Path("/update-all")
    @Transactional
    @Operation(summary = "Update all stock prices")
    @APIResponses({
        @APIResponse(responseCode = "200", description = "Stocks updated successfully"),
        @APIResponse(responseCode = "500", description = "Error updating stocks")
    })
    public Response updateAllStockPrices() {
        try {
            LOGGER.info("=== Starting Stock Price Update ===");
            List<Stock> stocks = stockBean.getAllStocks();
            int updatedCount = 0;

            for (Stock stock : stocks) {
                try {
                    Map<String, Object> yahooData = yahooService.getStockPriceAndStats(stock.getSymbol());
                    BigDecimal newPrice = new BigDecimal(yahooData.get("price").toString());
                    stock.setCurrentPrice(newPrice);
                    stock.setPriceLastUpdated(new Date());
                    stockBean.createStock(stock);
                    updatedCount++;
                } catch (Exception e) {
                    LOGGER.warning("Failed to update " + stock.getSymbol() + ": " + e.getMessage());
                }
            }

            Map<String, Object> response = new HashMap<>();
            response.put("updatedStocks", updatedCount);
            response.put("totalStocks", stocks.size());
            response.put("timestamp", new Date().toString());

            return Response.ok(response)
                    .header("X-Updated-Count", updatedCount)
                    .header("X-Total-Count", stocks.size())
                    .header("Last-Modified", new Date().toString())
                    .build();

        } catch (Exception e) {
            LOGGER.severe("Error updating stock prices: " + e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(new ErrorResponse("Error updating stock prices", 500))
                    .build();
        }
    }

    @POST
    @Path("/create")
    @Transactional
    @Operation(summary = "Create a new stock")
    @APIResponses({
        @APIResponse(responseCode = "201", description = "Stock created successfully"),
        @APIResponse(responseCode = "400", description = "Invalid stock data"),
        @APIResponse(responseCode = "409", description = "Stock already exists")
    })
    public Response createStock(
            @QueryParam("ticker") String ticker,
            @QueryParam("quantity") Integer quantity,
            @QueryParam("purchaseAmount") BigDecimal purchaseAmount) {
        try {
            // Basic validation
            if (ticker == null || quantity == null || purchaseAmount == null) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(new ErrorResponse("All fields are required", 400))
                        .build();
            }

            // Create new stock
            Stock stock = new Stock();
            stock.setSymbol(ticker);
            stock.setName(ticker);
            stock.setQuantity(quantity);
            stock.setPurchasePrice(purchaseAmount);
            stock.setCurrentPrice(purchaseAmount);
            stock.setPriceLastUpdated(new Date());

            // Save stock
            Stock created = stockBean.createStock(stock);
            return Response.status(Response.Status.CREATED)
                    .entity(created)
                    .build();
            
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(new ErrorResponse("Error creating stock: " + e.getMessage(), 500))
                    .build();
        }
    }
}