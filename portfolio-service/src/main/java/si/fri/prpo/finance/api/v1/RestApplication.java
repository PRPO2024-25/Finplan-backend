package si.fri.prpo.finance.api.v1;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;
import com.kumuluz.ee.discovery.annotations.RegisterService;
import org.eclipse.microprofile.openapi.annotations.OpenAPIDefinition;
import org.eclipse.microprofile.openapi.annotations.info.Info;
import org.eclipse.microprofile.openapi.annotations.servers.Server;
import java.util.Set;
import java.util.HashSet;
import si.fri.prpo.finance.config.JsonConfig;
import com.kumuluz.ee.cors.annotations.CrossOrigin;

@ApplicationPath("/v1")
@CrossOrigin
@RegisterService(value = "portfolio-service", environment = "dev", version = "1.0.0")
@OpenAPIDefinition(
    info = @Info(
        title = "Portfolio Service API",
        version = "1.0.0",
        description = "API for managing portfolios and transactions"
    ),
    servers = {
        @Server(url = "http://localhost:8080")
    }
)
public class RestApplication extends Application {
    @Override
    public Set<Class<?>> getClasses() {
        Set<Class<?>> classes = new HashSet<>();
        classes.add(PortfolioResource.class);
        classes.add(StockResource.class);
        classes.add(JsonConfig.class);
        return classes;
    }
}