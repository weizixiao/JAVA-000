package gateway.router;

import gateway.FilterAndRouterInitializer;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpRequest;

import java.util.List;
import java.util.Map;

public class HttpRouterHandle implements HttpEndpointRouter {
    FullHttpRequest fullRequest;
    ChannelHandlerContext ctx;
    public HttpRouterHandle(FullHttpRequest fullRequest, ChannelHandlerContext ctx) {
        this.fullRequest = fullRequest;
        this.ctx = ctx;
    }

    @Override
    public String route(FilterAndRouterInitializer filterAndRoute) {
        String client = fullRequest.headers().get("nio");
        String proxyServer = filterAndRoute.getRoutes().get(client);
        return "http://" + proxyServer;
    }
}
