package cn.apisium.uniporter.router;

import cn.apisium.uniporter.Uniporter;
import cn.apisium.uniporter.router.exception.IllegalHttpStateException;
import cn.apisium.uniporter.util.PathResolver;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class DefaultStaticHandler implements HttpHandler {
    @Override
    public void handle(String path, Route route, ChannelHandlerContext context, FullHttpRequest request) {
        path = PathResolver.resolvePath(path.substring(route.path.length()));
        String basePath = route.getOptions().computeIfAbsent("path",
                (key) -> (Uniporter.getInstance().getDataFolder().getAbsolutePath() +
                        "/static")).toString();
        if (!(Paths.get(basePath)).isAbsolute()) {
            basePath = Uniporter.getInstance().getDataFolder().getAbsolutePath() + "/" + basePath;
            route.getOptions().put("path", basePath = Paths.get(basePath).toAbsolutePath().toString());
        }

        File file = new File(basePath + path);

        if (file.isDirectory()) {
            for (String defaultName : Uniporter.getRouteConfig().indexes) {
                File temp = new File(file, defaultName);
                if (temp.exists()) {
                    file = temp;
                    break;
                }
            }
        }

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
                String mime = Files.probeContentType(target);
                if (mime.equalsIgnoreCase("text/html")) {
                    mime += "; charset=UTF-8";
                }
                response.headers().set(HttpHeaderNames.CONTENT_TYPE, mime);
                context.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
            } catch (IOException e) {
                IllegalHttpStateException.send(context, e);
            }
        }
    }
}
