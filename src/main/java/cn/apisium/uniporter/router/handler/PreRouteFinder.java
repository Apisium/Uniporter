package cn.apisium.uniporter.router.handler;

import cn.apisium.uniporter.router.util.RouteResolver;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.HttpMessage;

public class PreRouteFinder extends SimpleChannelInboundHandler<HttpMessage> implements RouteResolver {
    @Override
    protected void channelRead0(ChannelHandlerContext context, HttpMessage request) throws Exception {

    }
}
