package gateway.filter;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpRequest;

import java.util.Map;

public interface HttpRequestFilter {

    void filter(FullHttpRequest fullRequest, ChannelHandlerContext ctx);
    
}



