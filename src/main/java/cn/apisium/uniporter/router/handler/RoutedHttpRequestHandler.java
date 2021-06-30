package cn.apisium.uniporter.router.handler;

import cn.apisium.uniporter.router.api.message.RoutedHttpRequest;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

public class RoutedHttpRequestHandler extends SimpleChannelInboundHandler<RoutedHttpRequest> {
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, RoutedHttpRequest msg) throws Exception {
        msg.getHandler().handle(msg.getPath(), msg.getRoute(), ctx, msg.getRequest());
    }
}
