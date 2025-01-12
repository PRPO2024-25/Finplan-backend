package si.fri.prpo.finance.api.v1;

import si.fri.prpo.finance.entitete.Portfolio;
import si.fri.prpo.finance.zrna.PortfolioBean;


import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;
import si.fri.prpo.finance.Errors.ErrorResponse;
import java.math.BigDecimal;
import java.util.Map;
import javax.validation.Valid;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import java.math.RoundingMode;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import si.fri.prpo.finance.clients.UserClient;
import java.util.HashMap;
import si.fri.prpo.finance.dto.TransactionRequest;
import javax.transaction.Transactional;
import java.util.Date;
import java.util.ArrayList;
import si.fri.prpo.finance.dto.AmountRequest;
import si.fri.prpo.finance.entitete.Stock;
import si.fri.prpo.finance.zrna.YahooFinanceService;


@Path("/portfolios")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@ApplicationScoped
@Tag(name = "Portfolio Management", description = "Endpoints for managing user portfolios")
public class PortfolioResource {

    @Inject
    private PortfolioBean portfolioBean;

    @Inject
    @RestClient
    private UserClient userClient;

    @Inject
    private YahooFinanceService yahooFinanceService;


    @GET
    @Path("/{id}")
    @Operation(summary = "Get portfolio by ID")
    @APIResponses({
        @APIResponse(responseCode = "200", description = "Portfolio found"),
        @APIResponse(responseCode = "404", description = "Portfolio not found")
    })
    public Response getPortfolio(@PathParam("id") Integer id) {
        try {
            Portfolio portfolio = portfolioBean.getPortfolio(id);
            if (portfolio == null) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity(new ErrorResponse("Portfolio not found", 404))
                        .build();
            }
            return Response.ok(portfolio).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(new ErrorResponse("Error retrieving portfolio", 500))
                    .build();
        }
    }

    @GET
    @Operation(summary = "Get all portfolios")
    public Response getAllPortfolios() {
        try {
            List<Portfolio> portfolios = portfolioBean.getAllPortfolios();
            return Response.ok(portfolios).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(new ErrorResponse("Error retrieving portfolios", 500))
                    .build();
        }
    }

    @GET
    @Path("/{id}/balance")
    @Operation(
        summary = "Get portfolio balance",
        description = "Retrieves the current cash balance of a portfolio"
    )
    @APIResponses({
        @APIResponse(
            responseCode = "200",
            description = "Balance retrieved successfully",
            content = @Content(schema = @Schema(
                implementation = Map.class,
                example = "{\"balance\": 1000.00}"
            ))
        ),
        @APIResponse(
            responseCode = "404",
            description = "Portfolio not found",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
        )
    })
    public Response getBalance(
        @Parameter(description = "Portfolio ID", required = true)
        @PathParam("id") Integer id
    ) {
        try {
            BigDecimal balance = portfolioBean.getBalance(id);
            return Response.ok(Map.of("balance", balance)).build();
        } catch (NotFoundException e) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(new ErrorResponse(e.getMessage(), 404))
                    .build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(new ErrorResponse("Error retrieving balance", 500))
                    .build();
        }
    }

    @GET
    @Path("/{id}/can-process")
    @Operation(summary = "Check if portfolio can process amount")
    public Response canProcessAmount(
            @PathParam("id") Integer id,
            @QueryParam("amount") BigDecimal amount) {
        try {
            boolean canProcess = portfolioBean.canProcessAmount(id, amount);
            return Response.ok(Map.of("canProcess", canProcess)).build();
        } catch (Exception e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ErrorResponse(e.getMessage(), 400))
                    .build();
        }
    }

    @POST
    @Path("/{id}/withdraw")
    @Operation(summary = "Withdraw money from portfolio")
    @APIResponses({
        @APIResponse(responseCode = "200", description = "Withdrawal successful"),
        @APIResponse(responseCode = "400", description = "Invalid amount or insufficient funds"),
        @APIResponse(responseCode = "404", description = "Portfolio not found")
    })
    public Response withdrawMoney(
            @PathParam("id") Integer id,
            @Valid AmountRequest request) {
        try {
            if (request.getAmount() == null) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(new ErrorResponse("Amount is required", 400))
                        .build();
            }
            portfolioBean.withdrawMoney(id, request.getAmount());
            return Response.ok().build();
        } catch (NotFoundException e) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(new ErrorResponse(e.getMessage(), 404))
                    .build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ErrorResponse(e.getMessage(), 400))
                    .build();
        }
    }

    @POST
    @Path("/{id}/deposit")
    @Operation(summary = "Deposit money to portfolio")
    @APIResponses({
        @APIResponse(responseCode = "200", description = "Deposit successful"),
        @APIResponse(responseCode = "400", description = "Invalid amount"),
        @APIResponse(responseCode = "404", description = "Portfolio not found")
    })
    public Response depositMoney(
            @PathParam("id") Integer id,
            @Valid AmountRequest request) {
        try {
            if (request.getAmount() == null) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(new ErrorResponse("Amount is required", 400))
                        .build();
            }
            portfolioBean.depositMoney(id, request.getAmount());
            return Response.ok().build();
        } catch (NotFoundException e) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(new ErrorResponse(e.getMessage(), 404))
                    .build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(new ErrorResponse(e.getMessage(), 500))
                    .build();
        }
    }
    //add post for transfer so that when i create a new transaction, i can update the portfolio balance, this should update for both the sender and receiver
    @POST
    @Path("/{id}/transfer")
    @Operation(summary = "Process transfer for portfolio")
    @APIResponses({
        @APIResponse(responseCode = "200", description = "Transfer processed successfully"),
        @APIResponse(responseCode = "400", description = "Invalid transfer request"),
        @APIResponse(responseCode = "404", description = "Portfolio not found")
    })
    public Response processTransfer(
            @PathParam("id") Integer id,
            @QueryParam("amount") BigDecimal amount) {
        try {
            System.out.println("Processing transfer for portfolio: " + id + " with amount: " + amount);
            portfolioBean.processTransfer(id, amount, false);
            System.out.println("Transfer processed successfully");
            return Response.ok().build();
        } catch (NotFoundException e) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(new ErrorResponse(e.getMessage(), 404))
                    .build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ErrorResponse(e.getMessage(), 400))
                    .build();
        } catch (Exception e) {
            System.err.println("Error processing transfer: " + e.getMessage());
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(new ErrorResponse("Error processing transfer: " + e.getMessage(), 500))
                    .build();
        }
    }



    @GET
    @Path("/user/{userId}")
    @Operation(summary = "Get user's portfolios")
    public Response getUserPortfolios(@PathParam("userId") Integer userId) {
        try {
            // Validate user exists
            Response userResponse = userClient.getUserById(userId.longValue());
            if (userResponse.getStatus() != 200) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity(new ErrorResponse("User not found", 404))
                        .build();
            }

            List<Portfolio> portfolios = portfolioBean.getPortfoliosByUserId(userId);
            return Response.ok(portfolios).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(new ErrorResponse(e.getMessage(), 500))
                    .build();
        }
    }

    @GET
    @Path("/{id}/stats")
    @Operation(summary = "Get portfolio statistics")
    @APIResponses({
        @APIResponse(responseCode = "200", description = "Statistics retrieved successfully"),
        @APIResponse(responseCode = "404", description = "Portfolio not found")
    })
    public Response getPortfolioStats(@PathParam("id") Integer id) {
        try {
            Portfolio portfolio = portfolioBean.getPortfolio(id);
            if (portfolio == null) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity(new ErrorResponse("Portfolio not found", 404))
                        .build();
            }

            // Get user info for the portfolio owner
            Response userResponse = userClient.getUserInfo(portfolio.getUserId());
            if (userResponse.getStatus() != 200) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity(new ErrorResponse("Portfolio owner information not found", 404))
                        .build();
            }

            // Get portfolio count for this user
            long portfolioCount = portfolioBean.getPortfolioCountForUser(portfolio.getUserId());

            // Combine all data
            Map<String, Object> response = new HashMap<>();
            response.put("portfolio", portfolio);
            response.put("owner", userResponse.readEntity(Map.class));
            response.put("portfolioCount", portfolioCount);

            return Response.ok(response).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(new ErrorResponse("Error retrieving portfolio statistics", 500))
                    .build();
        }
    }

    @GET
    @Path("/{id}/owner")
    @Operation(summary = "Get portfolio with owner information and portfolio count")
    @APIResponses({
        @APIResponse(responseCode = "200", description = "Portfolio, owner info and portfolio count found"),
        @APIResponse(responseCode = "404", description = "Portfolio or owner not found")
    })
    public Response getPortfolioWithOwner(@PathParam("id") Integer portfolioId) {
        try {
            // First get the portfolio
            Portfolio portfolio = portfolioBean.getPortfolio(portfolioId);
            if (portfolio == null) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity(new ErrorResponse("Portfolio not found", 404))
                        .build();
            }

            // Get owner information from user service
            Response userResponse = userClient.getUserInfo(portfolio.getUserId().longValue());
            if (userResponse.getStatus() != 200) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity(new ErrorResponse("Portfolio owner information not found", 404))
                        .build();
            }

            // Get portfolio count for this user
            long portfolioCount = portfolioBean.getPortfolioCountForUser(portfolio.getUserId());


            // Combine all data
            Map<String, Object> response = new HashMap<>();
            response.put("portfolio", portfolio);
            response.put("owner", userResponse.readEntity(Map.class));
            response.put("portfolioCount", portfolioCount);

            return Response.ok(response).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(new ErrorResponse("Error retrieving portfolio with owner info", 500))
                    .build();
        }
    }

    @POST
    @Path("/transaction")
    @Transactional
    @Operation(summary = "Process transaction between portfolios")
    public Response processTransaction(TransactionRequest request) {
        try {
            // 1. Validate both portfolios exist
            Portfolio senderPortfolio = portfolioBean.getPortfolio(request.getSenderId());
            Portfolio receiverPortfolio = portfolioBean.getPortfolio(request.getReceiverId());
            
            System.out.println("=== Starting Transaction Processing ===");
            System.out.println("Sender Portfolio ID: " + request.getSenderId());
            System.out.println("Receiver Portfolio ID: " + request.getReceiverId());
            
            if (senderPortfolio == null || receiverPortfolio == null) {
                System.out.println("Error: One or both portfolios not found");
                return Response.status(Response.Status.NOT_FOUND)
                        .entity(new ErrorResponse("One or both portfolios not found", 404))
                        .build();
            }

            // Check balance before getting user information
            System.out.println("Checking sender balance. Required amount: " + request.getAmount());
            if (senderPortfolio.getCashBalance().compareTo(BigDecimal.valueOf(request.getAmount())) < 0) {
                System.out.println("Error: Insufficient funds. Current balance: " + senderPortfolio.getCashBalance());
                Map<String, Object> errorDetails = new HashMap<>();
                errorDetails.put("currentBalance", senderPortfolio.getCashBalance());
                errorDetails.put("requiredAmount", request.getAmount());
                errorDetails.put("message", "Insufficient funds for transfer");
                
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(errorDetails)
                        .build();
            }

            // 2. Get user information for both parties
            System.out.println("Getting sender user info for ID: " + senderPortfolio.getUserId());
            Response senderResponse = userClient.getUserInfo(senderPortfolio.getUserId().longValue());
            System.out.println("Getting receiver user info for ID: " + receiverPortfolio.getUserId());
            Response receiverResponse = userClient.getUserInfo(receiverPortfolio.getUserId().longValue());

            if (senderResponse.getStatus() != 200 || receiverResponse.getStatus() != 200) {
                System.out.println("Error: Could not retrieve user information");
                System.out.println("Sender response status: " + senderResponse.getStatus());
                System.out.println("Receiver response status: " + receiverResponse.getStatus());
                return Response.status(Response.Status.NOT_FOUND)
                        .entity(new ErrorResponse("Could not retrieve user information", 404))
                        .build();
            }

            // 3. Process the transaction
            System.out.println("Checking sender balance. Required amount: " + request.getAmount());
            if (senderPortfolio.getCashBalance().compareTo(BigDecimal.valueOf(request.getAmount())) < 0) {
                System.out.println("Error: Insufficient funds. Current balance: " + senderPortfolio.getCashBalance());
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(new ErrorResponse("Insufficient funds", 400))
                        .build();
            }

            // 4. Update balances
            System.out.println("Processing transfer...");
            senderPortfolio.setCashBalance(senderPortfolio.getCashBalance().subtract(BigDecimal.valueOf(request.getAmount())));
            receiverPortfolio.setCashBalance(receiverPortfolio.getCashBalance().add(BigDecimal.valueOf(request.getAmount())));
            
            portfolioBean.updatePortfolio(senderPortfolio);
            portfolioBean.updatePortfolio(receiverPortfolio);
            System.out.println("Transfer completed successfully");

            // 5. Create enriched response with user information
            Map<String, Object> senderInfo = senderResponse.readEntity(Map.class);
            Map<String, Object> receiverInfo = receiverResponse.readEntity(Map.class);

            // Create enriched description
            String formattedDescription = String.format("Transfer from %s %s (%s) to %s %s (%s)",
                senderInfo.get("firstName"),
                senderInfo.get("lastName"),
                senderInfo.get("email"),
                receiverInfo.get("firstName"),
                receiverInfo.get("lastName"),
                receiverInfo.get("email")
            );

            Map<String, Object> response = new HashMap<>();
            response.put("transactionAmount", request.getAmount());
            response.put("formattedDescription", formattedDescription);
            response.put("timestamp", new Date().toString());
            response.put("sender", Map.of(
                "portfolioId", senderPortfolio.getId(),
                "username", senderInfo.get("username"),
                "firstName", senderInfo.get("firstName"),
                "lastName", senderInfo.get("lastName"),
                "email", senderInfo.get("email")
            ));
            response.put("receiver", Map.of(
                "portfolioId", receiverPortfolio.getId(),
                "username", receiverInfo.get("username"),
                "firstName", receiverInfo.get("firstName"),
                "lastName", receiverInfo.get("lastName"),
                "email", receiverInfo.get("email")
            ));

            System.out.println("Transaction description: " + formattedDescription);
            System.out.println("=== Transaction Complete ===");
            return Response.ok(response).build();
        } catch (Exception e) {
            System.err.println("Error processing transaction: " + e.getMessage());
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(new ErrorResponse("Error processing transaction", 500))
                    .build();
        }
    }

    @POST
    @Path("/user/{userId}")
    @Operation(summary = "Create default portfolio for user")
    public Response createPortfolioForUser(@PathParam("userId") Long userId) {
        try {
            Portfolio portfolio = new Portfolio();
            portfolio.setUserId(userId);
            portfolio.setCashBalance(new BigDecimal("0.00"));
            portfolio.setCreatedAt(new Date());
            portfolio.setStocks(new ArrayList<>());
            
            Portfolio created = portfolioBean.createPortfolio(portfolio);
            return Response.status(Response.Status.CREATED)
                    .entity(created)
                    .build();
                
        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(new ErrorResponse("Error creating portfolio", 500))
                    .build();
        }
    }

    @DELETE
    @Path("/{id}")
    @Operation(summary = "Delete portfolio")
    @APIResponses({
        @APIResponse(responseCode = "204", description = "Portfolio successfully deleted"),
        @APIResponse(responseCode = "404", description = "Portfolio not found")
    })
    public Response deletePortfolio(@PathParam("id") Integer id) {
        try {
            Portfolio portfolio = portfolioBean.getPortfolio(id);
            if (portfolio == null) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity(new ErrorResponse("Portfolio not found", 404))
                        .build();
            }

            portfolioBean.deletePortfolio(id);
            return Response.noContent().build();
            
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(new ErrorResponse("Error deleting portfolio", 500))
                    .build();
        }
    }

    @GET
    @Path("/{id}/stocks")
    @Operation(summary = "Get stocks for portfolio")
    @APIResponses({
        @APIResponse(responseCode = "200", description = "Stocks retrieved successfully"),
        @APIResponse(responseCode = "404", description = "Portfolio not found")
    })
    public Response getPortfolioStocks(@PathParam("id") Integer id) {
        try {
            Portfolio portfolio = portfolioBean.getPortfolio(id);
            if (portfolio == null) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity(new ErrorResponse("Portfolio not found", 404))
                        .build();
            }

            List<Stock> stocks = portfolio.getStocks();
            if (stocks == null) {
                stocks = new ArrayList<>();
            }

            return Response.ok(stocks).build();
            
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(new ErrorResponse("Error retrieving stocks", 500))
                    .build();
        }
    }

    @POST
    @Path("/{id}/stocks")
    @Operation(summary = "Add stock to portfolio")
    public Response addStockToPortfolio(
            @PathParam("id") Integer portfolioId,
            @QueryParam("ticker") String ticker,
            @QueryParam("quantity") Integer quantity,
            @QueryParam("purchasePrice") BigDecimal purchasePrice) {
        try {
            // Input validation
            if (ticker == null || ticker.isEmpty()) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(new ErrorResponse("Ticker symbol is required", 400))
                        .build();
            }
            if (quantity == null || quantity <= 0) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(new ErrorResponse("Quantity must be greater than 0", 400))
                        .build();
            }
            if (purchasePrice == null || purchasePrice.compareTo(BigDecimal.ZERO) <= 0) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(new ErrorResponse("Purchase price must be greater than 0", 400))
                        .build();
            }

            Stock stock = portfolioBean.addStockToPortfolio(portfolioId, ticker, quantity, purchasePrice);
            return Response.status(Response.Status.CREATED)
                    .entity(stock)
                    .build();
                
        } catch (NotFoundException e) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(new ErrorResponse(e.getMessage(), 404))
                    .build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ErrorResponse(e.getMessage(), 400))
                    .build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(new ErrorResponse("Error adding stock: " + e.getMessage(), 500))
                    .build();
        }
    }

    @POST
    @Path("/create")
    @Operation(summary = "Create a simple portfolio")
    public Response createSimplePortfolio(@QueryParam("userId") Long userId) {
        try {
            if (userId == null) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(new ErrorResponse("userId is required", 400))
                        .build();
            }
            
            Portfolio created = portfolioBean.createPortfolio(userId);
            return Response.status(Response.Status.CREATED)
                    .entity(created)
                    .build();
                
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(new ErrorResponse("Error creating portfolio: " + e.getMessage(), 500))
                    .build();
        }
    }

    @PUT
    @Path("/stocks/update-prices")
    @Operation(summary = "Update all stock prices")
    @APIResponses({
        @APIResponse(responseCode = "200", description = "Stock prices updated successfully"),
        @APIResponse(responseCode = "500", description = "Error updating stock prices")
    })
    public Response updateAllStockPrices() {
        try {
            List<Stock> updatedStocks = portfolioBean.updateAllStockPrices();
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Successfully updated stock prices");
            response.put("updatedStocks", updatedStocks.size());
            
            return Response.ok(response).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(new ErrorResponse("Error updating stock prices: " + e.getMessage(), 500))
                    .build();
        }
    }
} 