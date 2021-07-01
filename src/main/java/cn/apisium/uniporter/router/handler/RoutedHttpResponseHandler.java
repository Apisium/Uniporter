package cn.apisium.uniporter.router.handler;

import cn.apisium.uniporter.router.api.message.RoutedHttpResponse;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.ChannelPromise;
import io.netty.handler.codec.http.FullHttpResponse;

/**
 * A handler to process routed http response.
 * <p>
 * Currently only adds extra headers specified in route configuration to the response.
 *
 * @author Baleine_2000
 */
public class RoutedHttpResponseHandler extends ChannelOutboundHandlerAdapter {
    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        if (msg instanceof RoutedHttpResponse) {
            // Add header
            RoutedHttpResponse routed = (RoutedHttpResponse) msg;
            msg = routed.getResponse();
            FullHttpResponse finalMsg = (FullHttpResponse) msg;
            routed.getRoute().getHeader().forEach((k, v) -> (finalMsg).headers().add(k, v));
        }
        super.write(ctx, msg, promise);
    }
}
