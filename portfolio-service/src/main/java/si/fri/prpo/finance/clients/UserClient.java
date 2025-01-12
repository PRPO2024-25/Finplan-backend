package si.fri.prpo.finance.clients;

import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;
import javax.enterprise.context.Dependent;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/v1/users")
@RegisterRestClient(configKey="user-service")
@Dependent
public interface UserClient {
    
    @GET
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    Response getUserById(@PathParam("id") Long id);

    @GET
    @Path("/{id}/validate")
    Response validateUserAccess(@PathParam("id") Integer userId, @QueryParam("portfolioId") Integer portfolioId);

    @GET
    @Path("/{id}/portfolios")
    Response getUserPortfolios(@PathParam("id") Integer userId);

    @DELETE
    @Path("/{id}/portfolio/{portfolioId}")
    Response unlinkPortfolio(@PathParam("id") Integer userId, @PathParam("portfolioId") Integer portfolioId);

    @GET
    @Path("/{id}/info")
    Response getUserInfo(@PathParam("id") Long userId);
} 