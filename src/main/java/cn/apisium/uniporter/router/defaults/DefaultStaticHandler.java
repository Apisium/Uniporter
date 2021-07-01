package cn.apisium.uniporter.router.defaults;

import cn.apisium.uniporter.Uniporter;
import cn.apisium.uniporter.router.api.Route;
import cn.apisium.uniporter.router.api.UniporterHttpHandler;
import cn.apisium.uniporter.router.api.message.RoutedHttpResponse;
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

/**
 * A default static resource handler which process unspecified routes.
 *
 * @author Baleine_2000
 */
public class DefaultStaticHandler implements UniporterHttpHandler {
    @Override
    public void handle(String path, Route route, ChannelHandlerContext context, FullHttpRequest request) {
        // Find the correct path user is accessing, the path is filtered and *should* be absolute.
        path = PathResolver.resolvePath(path.substring(route.getPath().length()));
        String basePath = route.getOptions().computeIfAbsent("path",
                (key) -> (Uniporter.getInstance().getDataFolder().getAbsolutePath() +
                        "/static")).toString();
        if (!(Paths.get(basePath)).isAbsolute()) {
            basePath = Uniporter.getInstance().getDataFolder().getAbsolutePath() + "/" + basePath;
            route.getOptions().put("path", basePath = Paths.get(basePath).toAbsolutePath().toString());
        }

        // Get the actual file
        File file = new File(basePath + path);

        // If it is a file, try to find its index from all possible indexes
        if (file.isDirectory()) {
            for (String defaultName : Uniporter.getRouteConfig().getIndexes()) {
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
                // Create the response
                FullHttpResponse response = new DefaultFullHttpResponse(
                        HttpVersion.HTTP_1_1,
                        HttpResponseStatus.OK,
                        Unpooled.copiedBuffer(Files.readAllBytes(target)));
                // Correct its MIME
                String mime = Files.probeContentType(target);
                if (mime.equalsIgnoreCase("text/html")) {
                    mime += "; charset=UTF-8";
                }
                response.headers().set(HttpHeaderNames.CONTENT_TYPE, mime);
                // Write and close the connection, okay, I know it is not suitable for download large file.
                // PR welcome :)
                context.writeAndFlush(new RoutedHttpResponse(path, response, route))
                        .addListener(ChannelFutureListener.CLOSE);
            } catch (IOException e) {
                // If error occurs, send a 500 error
                IllegalHttpStateException.send(context, e);
            }
        }
    }
}
