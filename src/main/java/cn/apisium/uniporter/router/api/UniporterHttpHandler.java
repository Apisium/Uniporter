package cn.apisium.uniporter.router.api;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpRequest;

public interface UniporterHttpHandler {
    void handle(String path, Route route, ChannelHandlerContext context, FullHttpRequest request);
}
