package cn.apisium.uniporter.router;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpRequest;

public interface HttpHandler {
    void handle(String path, Route route, ChannelHandlerContext context, FullHttpRequest request);
}
