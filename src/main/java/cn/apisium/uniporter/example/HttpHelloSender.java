package cn.apisium.uniporter.example;

import cn.apisium.uniporter.Uniporter;
import cn.apisium.uniporter.router.HttpHandler;
import cn.apisium.uniporter.router.Route;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.*;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;

public class HttpHelloSender implements HttpHandler {
    public void register() {
        Uniporter.registerHandler("hello world example", this);
        Uniporter.registerRoute(new Route("/helloworld", "hello world example", false, new HashMap<>(),
                new HashMap<>()));
    }

    @Override
    public void handle(String path, Route route, ChannelHandlerContext context, FullHttpRequest request) {
        StringBuilder builder = new StringBuilder();
        builder.append("Hello world from Uniporter!");
        builder.append("<br>");
        builder.append("<br>This is: ");
        builder.append(request.uri());
        builder.append("<br>");
        builder.append("<br>");
        builder.append("Have fun!");

        FullHttpResponse response = new DefaultFullHttpResponse(
                HttpVersion.HTTP_1_1,
                HttpResponseStatus.OK,
                Unpooled.copiedBuffer(builder.toString().getBytes(StandardCharsets.UTF_8)));
        response.headers().set(HttpHeaderNames.CONTENT_TYPE, "text/html; charset=UTF-8");
        context.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
    }
}
