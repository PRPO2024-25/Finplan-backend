package si.fri.prpo.finance.clients;

import javax.enterprise.context.ApplicationScoped;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

@Path("/v1/users")
@RegisterRestClient(configKey="user-service")
@ApplicationScoped
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public interface UserServiceAPI {
    @GET
    @Path("/{id}")
    Response getUser(@PathParam("id") Long id);
} 