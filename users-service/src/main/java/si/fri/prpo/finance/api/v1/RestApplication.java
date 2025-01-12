package si.fri.prpo.finance.api.v1;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;
import org.eclipse.microprofile.openapi.annotations.OpenAPIDefinition;
import org.eclipse.microprofile.openapi.annotations.info.Info;
import org.eclipse.microprofile.openapi.annotations.servers.Server;
import com.kumuluz.ee.cors.annotations.CrossOrigin;
@ApplicationPath("/v1")
@CrossOrigin
@OpenAPIDefinition(
    info = @Info(
        title = "Users API",
        version = "1.0.0",
        description = "API for managing users and their accounts"
    ),
    servers = {
        @Server(url = "http://localhost:8081")
    }
)
public class RestApplication extends Application {
}
