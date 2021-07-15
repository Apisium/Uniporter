package cn.apisium.uniporter.example;

import cn.apisium.uniporter.Constants;
import cn.apisium.uniporter.router.api.Route;
import cn.apisium.uniporter.router.api.UniporterHttpHandler;
import cn.apisium.uniporter.router.exception.IllegalHttpStateException;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponseStatus;

public class HttpHijackSender implements UniporterHttpHandler {
    @Override
    public void handle(String path, Route route, ChannelHandlerContext context, FullHttpRequest request) {
    }

    @Override
    public boolean hijackAggregator() {
        return true;
    }

    @Override
    public void hijack(ChannelHandlerContext context, HttpRequest request) {
        context.pipeline().remove(Constants.PRE_ROUTE_ID);
        context.pipeline().addBefore(Constants.AGGREGATOR_HANDLER_ID, "Hijack",
                new SimpleChannelInboundHandler<Object>() {
                    @Override
                    protected void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception {
                        IllegalHttpStateException.send(context, HttpResponseStatus.OK, request.uri());
                    }
                });
    }
}
