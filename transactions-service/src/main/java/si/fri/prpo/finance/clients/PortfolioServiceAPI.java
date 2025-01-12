package si.fri.prpo.finance.clients;

import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import javax.enterprise.context.ApplicationScoped;
import javax.ws.rs.*;
import javax.ws.rs.core.Response;
import java.math.BigDecimal;
import javax.ws.rs.core.MediaType;
import si.fri.prpo.finance.dto.TransactionRequest;
@Path("/v1/portfolios")
@RegisterRestClient(configKey="portfolio-service")
@ApplicationScoped
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public interface PortfolioServiceAPI {

    @GET
    @Path("/{id}")
    Response getPortfolio(@PathParam("id") Integer id);
    
    @GET
    @Path("/{id}/can-process")
    Response canTransfer(
        @PathParam("id") Integer id,
        @QueryParam("amount") BigDecimal amount
    );

    @POST
    @Path("/{id}/transfer")
    Response processTransfer(
        @PathParam("id") Integer id,
        @QueryParam("amount") BigDecimal amount
    );

    @POST
    @Path("/transaction")
    Response processTransaction(TransactionRequest request);
}