package si.fri.prpo.finance.api.v1;

import com.kumuluz.ee.cors.annotations.CrossOrigin;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.parameters.RequestBody;
import si.fri.prpo.finance.entitete.User;
import si.fri.prpo.finance.zrna.UserService;

import si.fri.prpo.finance.dto.ErrorResponse;

import si.fri.prpo.finance.dto.UserLoginDTO;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;




@Path("users")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@ApplicationScoped
@CrossOrigin(
    allowOrigin = "http://localhost:3000",
    allowSubdomains = true,
    supportedMethods = "GET, POST, PUT, DELETE, OPTIONS, HEAD, TRACE",
    supportedHeaders = "Content-Type, Authorization",
    exposedHeaders = "Content-Type, Authorization"
)
@Tag(name = "users", description = "User management endpoints")
public class UserResource {

    @Context
    protected UriInfo uriInfo;

    @Inject
    private UserService userService;


    @GET
    @Operation(summary = "Get all users",
            description = "Returns a list of all users in the system")
    @APIResponses({
        @APIResponse(
            responseCode = "200",
            description = "List of users",
            content = @Content(schema = @Schema(implementation = User.class))
        ),
        @APIResponse(
            responseCode = "500",
            description = "Internal server error",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
        )
    })
    public Response getAllUsers() {
        List<User> users = userService.getUsers();
        return Response.ok(users).build();
    }

    @GET
    @Path("{userId}")
    @Operation(summary = "Get user by ID",
            description = "Returns a specific user by their ID")
    @APIResponses({
        @APIResponse(
            responseCode = "200",
            description = "User found",
            content = @Content(schema = @Schema(implementation = User.class))
        ),
        @APIResponse(
            responseCode = "404",
            description = "User not found",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
        ),
        @APIResponse(
            responseCode = "500",
            description = "Internal server error",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
        )
    })
    public Response getUser(
            @Parameter(description = "User ID", required = true)
            @PathParam("userId") Long userId) {
        User user = userService.getUser(userId);
        return user != null
                ? Response.ok(user).build()
                : Response.status(Response.Status.NOT_FOUND).build();
    }

    @POST
    @Operation(summary = "Create new user",
            description = "Creates a new user in the system")
    @APIResponses({
        @APIResponse(
            responseCode = "201",
            description = "User created successfully",
            content = @Content(schema = @Schema(implementation = User.class))
        ),
        @APIResponse(
            responseCode = "400",
            description = "Invalid user data",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
        )
    })
    public Response addUser(
            @RequestBody(required = true,
                content = @Content(schema = @Schema(implementation = User.class,
                    requiredProperties = {"firstName", "lastName", "email", "username", "password"},
                    example = "{\"firstName\": \"John\", \"lastName\": \"Doe\", " +
                             "\"email\": \"john@example.com\", \"username\": \"john_doe\", " +
                             "\"password\": \"securepass123\"}")))
            User user) {
        try {
            userService.addUser(user);
            return Response.status(Response.Status.CREATED)
                    .entity(user)
                    .build();
        } catch (Exception e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ErrorResponse(e.getMessage(), 400))
                    .build();
        }
    }


    @GET
    @Path("search")
    @Operation(summary = "Search users",
            description = "Search users by username")
    @APIResponses({
        @APIResponse(
            responseCode = "200",
            description = "User found",
            content = @Content(schema = @Schema(implementation = User.class))
        ),
        @APIResponse(
            responseCode = "404",
            description = "User not found",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
        ),
        @APIResponse(
            responseCode = "500",
            description = "Internal server error",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
        )
    })
    public Response findByUsername(
            @Parameter(description = "Username to search for", required = true)
            @QueryParam("username") String username) {
        User user = userService.findByUsername(username);
        return user != null
                ? Response.ok(user).build()
                : Response.status(Response.Status.NOT_FOUND)
                        .entity(new ErrorResponse("User not found", 404))
                        .build();
    }

    @PUT
    @Path("/{id}")
    @Operation(summary = "Update user",
            description = "Updates an existing user's information")
    @APIResponses({
        @APIResponse(
            responseCode = "200",
            description = "User updated successfully",
            content = @Content(schema = @Schema(implementation = User.class))
        ),
        @APIResponse(
            responseCode = "404",
            description = "User not found",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
        )
    })
    public Response updateUser(
            @Parameter(description = "User ID", required = true)
            @PathParam("id") Long id,
            @RequestBody(required = true,
                content = @Content(schema = @Schema(
                    requiredProperties = {"firstName", "lastName", "email", "username", "password"},
                    example = "{\"firstName\": \"John\", \"lastName\": \"Doe\", " +
                             "\"email\": \"john@example.com\", \"username\": \"john_doe\", " +
                             "\"password\": \"newpass123\"}")))
            User user) {
        try {
            user.setId(id);  // Set ID from path parameter
            User updated = userService.updateUser(id, user);
            return Response.ok(updated).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(new ErrorResponse(e.getMessage(), 404))
                    .build();
        } catch (Exception e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ErrorResponse(e.getMessage(), 400))
                    .build();
        }
    }
    @DELETE
    @Path("/{id}")
    @Operation(summary = "Delete user",
            description = "Deletes a user from the system")
    @APIResponses({
        @APIResponse(
            responseCode = "204",
            description = "User deleted successfully"
        ),
        @APIResponse(
            responseCode = "404",
            description = "User not found",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
        ),
        @APIResponse(
            responseCode = "500",
            description = "Internal server error",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
        )
    })
    public Response deleteUser(
            @Parameter(description = "User ID", required = true)
            @PathParam("id") Long id) {
        try {
            userService.deleteUser(id);
            return Response.noContent().build();
        } catch (Exception e) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(new ErrorResponse(e.getMessage(), 404))
                    .build();
        }
    }

    
    @POST
    @Path("/login")
    @Operation(summary = "User login",
            description = "Authenticates a user with username and password")
    @APIResponses({
        @APIResponse(
            responseCode = "200",
            description = "Login successful",
            content = @Content(schema = @Schema(implementation = User.class))
        ),
        @APIResponse(
            responseCode = "401",
            description = "Invalid credentials",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
        ),
        @APIResponse(
            responseCode = "500",
            description = "Internal server error",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
        )
    })
    public Response loginUser(
            @RequestBody(required = true,
                content = @Content(schema = @Schema(implementation = UserLoginDTO.class,
                    requiredProperties = {"username", "password"},
                    example = "{\"username\": \"john_doe\", \"password\": \"secret\"}")))
            UserLoginDTO userLoginDTO) {
        try {
            User user = userService.loginUser(userLoginDTO.getUsername(), userLoginDTO.getPassword());
            return Response.ok(user).build();
        } catch (Exception e) {
            return Response.status(Response.Status.UNAUTHORIZED)
                    .entity(new ErrorResponse("Invalid username or password", 401))
                    .build();
        }
    }
    @GET
    @Path("/{id}/info")
    @Operation(summary = "Get detailed user information")
    public Response getUserInfo(@PathParam("id") Long id) {
        try {
            System.out.println("=== Fetching Detailed User Info ===");
            System.out.println("Requested User ID: " + id);
            
            User user = userService.getUser(id);
            if (user == null) {
                System.out.println("Error: User not found with ID: " + id);
                return Response.status(Response.Status.NOT_FOUND)
                        .entity(new ErrorResponse("User not found", 404))
                        .build();
            }
    
            Map<String, Object> userInfo = new HashMap<>();
            userInfo.put("id", user.getId());
            userInfo.put("username", user.getUsername());
            userInfo.put("firstName", user.getFirstName());
            userInfo.put("lastName", user.getLastName());
            userInfo.put("email", user.getEmail());
            
            System.out.println("Found user: " + user.getUsername());
            System.out.println("Email: " + user.getEmail());
            System.out.println("Name: " + user.getFirstName() + " " + user.getLastName());
            System.out.println("=== User Info Request Complete ===");
            
            return Response.ok(userInfo).build();
        } catch (Exception e) {
            System.err.println("Error retrieving user info: " + e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(new ErrorResponse("Error retrieving user information", 500))
                    .build();
        }
    }

}
