package gateway.router;

import gateway.FilterAndRouterInitializer;

import java.util.List;
import java.util.Map;

public interface HttpEndpointRouter {
    
    String route(FilterAndRouterInitializer filterAndRoute);
    
}
