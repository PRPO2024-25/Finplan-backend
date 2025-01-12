package si.fri.prpo.finance.api.v1;

import si.fri.prpo.finance.zrna.TransakcijaZrno;
import si.fri.prpo.finance.exceptions.ValidationException;
import si.fri.prpo.finance.dto.ErrorResponse;
import javax.inject.Inject;
import javax.enterprise.context.RequestScoped;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;
import java.util.Date;
import si.fri.prpo.finance.clients.PortfolioServiceAPI;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import si.fri.prpo.finance.entitete.Transakcija;
import si.fri.prpo.finance.dto.TransactionHistoryResponse;
import java.util.stream.Collectors;

import si.fri.prpo.finance.dto.TransactionRequest;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import java.util.Map;
import com.kumuluz.ee.cors.annotations.CrossOrigin;
@Path("/transactions")
@RequestScoped
@CrossOrigin
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "transactions", description = "Transactions management endpoints")
public class TransakcijeVir {

    //private static final Logger LOGGER = Logger.getLogger(TransakcijeVir.class.getName());

    @Inject
    private TransakcijaZrno transakcijaZrno;

    @Inject
    @RestClient
    private PortfolioServiceAPI portfolioService;

    @GET
    @Operation(summary = "Get all transactions")
    @APIResponses({
        @APIResponse(responseCode = "200", description = "List of transactions"),
        @APIResponse(responseCode = "500", description = "Internal server error")
    })
    public Response getTransakcije(
            @QueryParam("minAmount") Double minAmount,
            @QueryParam("maxAmount") Double maxAmount,
            @QueryParam("startDate") String startDate,
            @QueryParam("endDate") String endDate,
            @QueryParam("status") String status,
            @QueryParam("type") String type,
            @QueryParam("sortBy") @DefaultValue("createdAt") String sortBy,
            @QueryParam("sortOrder") @DefaultValue("DESC") String sortOrder,
            @QueryParam("limit") @DefaultValue("50") Integer limit) {
        try {
            List<Transakcija> transactions = transakcijaZrno.getTransakcije(
                minAmount, maxAmount, 
                startDate != null ? new Date(Long.parseLong(startDate)) : null,
                endDate != null ? new Date(Long.parseLong(endDate)) : null,
                status, type, sortBy, sortOrder, limit);
            return Response.ok(transactions).build();
        } catch (Exception e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ErrorResponse("Error fetching transactions: " + e.getMessage(), 400))
                    .build();
        }
    }

    @POST
    @Operation(summary = "Create new portfolio transaction")
    @APIResponses({
        @APIResponse(responseCode = "201", description = "Transaction created successfully"),
        @APIResponse(responseCode = "400", description = "Invalid transaction data"),
        @APIResponse(responseCode = "404", description = "Portfolio not found"),
        @APIResponse(responseCode = "422", description = "Insufficient funds")
    })
    public Response createTransakcija(TransactionRequest request) {
        try {
            // Validate request
            if (request.getAmount() <= 0) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(new ErrorResponse("Amount must be positive", 400))
                        .build();
            }

            if (request.getSenderId().equals(request.getReceiverId())) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(new ErrorResponse("Sender and receiver cannot be the same", 400))
                        .build();
            }

            // Create transaction entity
            Transakcija transaction = new Transakcija();
            transaction.setSenderId(request.getSenderId().longValue());
            transaction.setReceiverId(request.getReceiverId().longValue());
            transaction.setAmount(request.getAmount());
            transaction.setDescription(request.getDescription());
            
            // Process transaction
            Transakcija created = transakcijaZrno.createTransakcija(transaction);
            return Response.status(Response.Status.CREATED).entity(created).build();
            
        } catch (ValidationException e) {
            if (e.getMessage().contains("not found")) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity(new ErrorResponse(e.getMessage(), 404))
                        .build();
            } else if (e.getMessage().contains("Insufficient funds")) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(new ErrorResponse(e.getMessage(), 400))
                        .build();
            } else {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(new ErrorResponse(e.getMessage(), 400))
                        .build();
            }
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(new ErrorResponse("Internal server error: " + e.getMessage(), 500))
                    .build();
        }
    }


    @GET
    @Path("/{id}")
    @Operation(summary = "Get transaction by ID")
    @APIResponses({
        @APIResponse(responseCode = "200", description = "Transaction found"),
        @APIResponse(responseCode = "404", description = "Transaction not found")
    })
    public Response getTransakcija(@PathParam("id") Integer id) {
        Transakcija transaction = transakcijaZrno.getTransakcija(id);
        if (transaction == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(new ErrorResponse("Transaction not found", 404))
                    .build();
        }
        return Response.ok(transaction).build();
    }

    @GET
    @Path("/portfolio/{portfolioId}")
    @Operation(summary = "Get portfolio's transaction history with filters")
    public Response getPortfolioTransactions(
            @PathParam("portfolioId") Long portfolioId,
            @QueryParam("startDate") String startDate,
            @QueryParam("endDate") String endDate,
            @QueryParam("type") String type,  // "INCOMING" or "OUTGOING"
            @QueryParam("minAmount") Double minAmount,
            @QueryParam("maxAmount") Double maxAmount,
            @QueryParam("limit") @DefaultValue("50") Integer limit,
            @QueryParam("offset") @DefaultValue("0") Integer offset) {
        
        try {
            List<Transakcija> transactions = transakcijaZrno.getFilteredTransactions(
                portfolioId, startDate, endDate, type, minAmount, maxAmount, limit, offset
            );
            
            List<TransactionHistoryResponse> response = transactions.stream()
                .map(t -> new TransactionHistoryResponse(t, portfolioId))
                .collect(Collectors.toList());
                
            return Response.ok(response).build();
        } catch (Exception e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ErrorResponse(e.getMessage(), 400))
                    .build();
        }
    }

    @GET
    @Path("/portfolio/{portfolioId}/summary")
    @Operation(summary = "Get portfolio's transaction summary")
    public Response getPortfolioTransactionSummary(
            @PathParam("portfolioId") Long portfolioId,
            @QueryParam("startDate") String startDate,
            @QueryParam("endDate") String endDate) {
        
        try {
            Map<String, Object> summary = transakcijaZrno.getTransactionSummary(portfolioId, startDate, endDate);
            return Response.ok(summary).build();
        } catch (Exception e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ErrorResponse(e.getMessage(), 400))
                    .build();
        }
    }
    //end point to get all transaction of a portfolio
    @GET
    @Path("/portfolio/{portfolioId}/transactions")
    public Response getPortfolioTransactions(@PathParam("portfolioId") Long portfolioId) {
        List<Transakcija> transactions = transakcijaZrno.getTransakcijeByPortfolioId(portfolioId);
        return Response.ok(transactions).build();
    }
}
