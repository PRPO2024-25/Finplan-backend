package si.fri.prpo.finance.api.mappers;


import javax.json.Json;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

@Provider
public class GeneralExceptionMapper implements ExceptionMapper<Exception> {
    
    @Override
    public Response toResponse(Exception exception) {
        if (exception instanceof IllegalArgumentException) {
            return Response
                .status(Response.Status.BAD_REQUEST)
                .entity(Json.createObjectBuilder()
                    .add("error", exception.getMessage())
                    .build())
                .build();
        }
        
        // Handle other exceptions...
        return Response
            .status(Response.Status.INTERNAL_SERVER_ERROR)
            .entity(Json.createObjectBuilder()
                .add("error", "An unexpected error occurred")
                .build())
            .build();
    }
} 