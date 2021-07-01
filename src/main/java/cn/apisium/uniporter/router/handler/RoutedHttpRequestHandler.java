package cn.apisium.uniporter.router.handler;

import cn.apisium.uniporter.router.api.message.RoutedHttpRequest;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

/**
 * A handler to handle routed http requests and calls the corresponding handler to do actual process.
 * <p>
 * This can be improved in future to achieve more functions
 *
 * @author Baleine_2000
 */
public class RoutedHttpRequestHandler extends SimpleChannelInboundHandler<RoutedHttpRequest> {
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, RoutedHttpRequest msg) throws Exception {
        msg.getHandler().handle(msg.getPath(), msg.getRoute(), ctx, msg.getRequest());
    }
}
