package gateway.inbound;

import gateway.FilterAndRouterInitializer;
import gateway.filter.HttpRequestFilterHandle;
import gateway.outbound.httpclient4.HttpOutboundHandler;
import gateway.router.HttpEndpointRouter;
import gateway.router.HttpRouterHandle;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.util.ReferenceCountUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class HttpInboundHandler extends ChannelInboundHandlerAdapter {
    private static Logger logger = LoggerFactory.getLogger(HttpInboundHandler.class);
    private HttpOutboundHandler handler;

    public HttpInboundHandler() {
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) {
        ctx.flush();
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        try {
            //logger.info("channelRead流量接口请求开始，时间为{}", startTime);
            FullHttpRequest fullRequest = (FullHttpRequest) msg;
//            String uri = fullRequest.uri();
//            //logger.info("接收到的请求url为{}", uri);
//            if (uri.contains("/test")) {
//                handlerTest(fullRequest, ctx);
//            }
            FilterAndRouterInitializer filterAndRoute = new FilterAndRouterInitializer();
            HttpRequestFilterHandle requestFilter = null;
            if (filterAndRoute.isFilterStatus()) {
                requestFilter = new HttpRequestFilterHandle(filterAndRoute);
                requestFilter.filter(fullRequest, ctx);
            }
            // 无权限拦截返回
            if (!requestFilter.getFlag()) return;
            HttpRouterHandle router = new HttpRouterHandle(fullRequest, ctx);
            String proxyServer = router.route(filterAndRoute);
            System.out.println("proxyServer:" + proxyServer);
            handler = new HttpOutboundHandler(proxyServer);
            handler.handle(fullRequest, ctx);

        } catch(Exception e) {
            e.printStackTrace();
        } finally {
            ReferenceCountUtil.release(msg);
        }
    }

//    private void handlerTest(FullHttpRequest fullRequest, ChannelHandlerContext ctx) {
//        FullHttpResponse response = null;
//        try {
//            String value = "hello,kimmking";
//            response = new DefaultFullHttpResponse(HTTP_1_1, OK, Unpooled.wrappedBuffer(value.getBytes("UTF-8")));
//            response.headers().set("Content-Type", "application/json");
//            response.headers().setInt("Content-Length", response.content().readableBytes());
//
//        } catch (Exception e) {
//            logger.error("处理测试接口出错", e);
//            response = new DefaultFullHttpResponse(HTTP_1_1, NO_CONTENT);
//        } finally {
//            if (fullRequest != null) {
//                if (!HttpUtil.isKeepAlive(fullRequest)) {
//                    ctx.write(response).addListener(ChannelFutureListener.CLOSE);
//                } else {
//                    response.headers().set(CONNECTION, KEEP_ALIVE);
//                    ctx.write(response);
//                }
//            }
//        }
//    }
//
//    @Override
//    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
//        cause.printStackTrace();
//        ctx.close();
//    }
}
