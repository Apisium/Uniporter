package cn.apisium.uniporter.router;

import cn.apisium.uniporter.Uniporter;
import cn.apisium.uniporter.router.exception.IllegalHttpStateException;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class DefaultStaticHandler implements HttpHandler {
    @Override
    public void handle(String path, Route route, ChannelHandlerContext context, FullHttpRequest request) {
        File file =
                new File(route.getOptions().getOrDefault("path",
                        Uniporter.getInstance().getDataFolder().getAbsolutePath() +
                                "/static").toString() + path);
        Path target = file.getAbsoluteFile().toPath();
        if (!file.exists()) {
            IllegalHttpStateException.send(context, HttpResponseStatus.NOT_FOUND, "File not found");
        } else if (file.isDirectory()) {
            IllegalHttpStateException.send(context, HttpResponseStatus.FORBIDDEN, "Forbidden");
        } else {
            try {
                FullHttpResponse response = new DefaultFullHttpResponse(
                        HttpVersion.HTTP_1_1,
                        HttpResponseStatus.OK,
                        Unpooled.copiedBuffer(Files.readAllBytes(target)));
                response.headers().set(HttpHeaderNames.CONTENT_TYPE, "text/html; charset=UTF-8");
                context.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
            } catch (IOException e) {
                IllegalHttpStateException.send(context, e);
            }
        }
    }
}
