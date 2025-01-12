package si.fri.prpo.finance.config;

import javax.json.bind.JsonbConfig;
import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.Provider;
import javax.json.bind.Jsonb;
import javax.json.bind.JsonbBuilder;
import javax.json.bind.config.PropertyVisibilityStrategy;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Locale;

@Provider
public class JsonConfig implements ContextResolver<Jsonb> {
    @Override
    public Jsonb getContext(Class<?> type) {
        JsonbConfig config = new JsonbConfig()
            .withDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
            .withPropertyVisibilityStrategy(new PropertyVisibilityStrategy() {
                @Override
                public boolean isVisible(Field field) {
                    return true;
                }

                @Override
                public boolean isVisible(Method method) {
                    return true;
                }
            });
        return JsonbBuilder.create(config);
    }
} 