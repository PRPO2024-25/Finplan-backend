package si.fri.prpo.finance.health;

import org.eclipse.microprofile.health.HealthCheck;
import org.eclipse.microprofile.health.HealthCheckResponse;
import org.eclipse.microprofile.health.Liveness;
import javax.enterprise.context.ApplicationScoped;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.logging.Logger;
@Liveness  // For liveness probe
@ApplicationScoped
public class ServiceHealthCheck implements HealthCheck {

    @PersistenceContext(unitName = "kumuluzee-samples-jpa")
    private EntityManager em;

    private static final Logger LOG = Logger.getLogger(ServiceHealthCheck.class.getSimpleName());

    @Override
    public HealthCheckResponse call() {
        try {
            em.createNativeQuery("SELECT 1").getSingleResult();
            return HealthCheckResponse.named("Users-DB-Connection")
                    .up()
                    .withData("database", "PostgreSQL")
                    .withData("persistenceUnit", "kumuluzee-samples-jpa")
                    .build();
        } catch (Exception e) {
            LOG.severe("Database health check failed: " + e.getMessage());
            return HealthCheckResponse.named("Users-DB-Connection")
                    .down()
                    .withData("error", e.getMessage())
                    .build();
        }
    }
}