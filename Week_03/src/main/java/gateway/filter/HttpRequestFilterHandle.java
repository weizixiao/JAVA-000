package gateway.filter;

import gateway.FilterAndRouterInitializer;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;

import static io.netty.handler.codec.http.HttpResponseStatus.OK;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

public class HttpRequestFilterHandle implements HttpRequestFilter {
    private FilterAndRouterInitializer filter;
    private Boolean flag = true;

    public Boolean getFlag() {
        return flag;
    }

    public void setFlag(Boolean flag) {
        this.flag = flag;
    }

    public HttpRequestFilterHandle(FilterAndRouterInitializer filter) {
        this.filter = filter;
    }

    @Override
    public void filter(FullHttpRequest fullRequest, ChannelHandlerContext ctx) {
        String[] uris = fullRequest.uri().split("\\/");
        String path = uris.length > 1 ? uris[1] : "";
        if (filter.getSecretRoutes().contains(path)) {
            if (fullRequest.headers().get("token") != null) {
                // todo 验证权限
            } else {
                // todo 无权限操作
                setUnValid(ctx);
                setFlag(false);
            }
        }
        // 设置后端服务header
        fullRequest.headers().set("nio", filter.getRoutes().containsKey(path) ? path : "default");
    }

    public void setUnValid(ChannelHandlerContext ctx) {
        FullHttpResponse response = null;
        response = new DefaultFullHttpResponse(HTTP_1_1, OK, Unpooled.wrappedBuffer("no valid".getBytes()));
        response.headers().set("Content-Type", "application/json");
        response.headers().setInt("Content-Length", 8);
        ctx.write(response);
    }
}
