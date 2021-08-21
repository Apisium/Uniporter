package cn.apisium.uniporter.router.defaults;

import cn.apisium.uniporter.Constants;
import cn.apisium.uniporter.Uniporter;
import cn.apisium.uniporter.router.api.Route;
import cn.apisium.uniporter.router.api.UniporterHttpHandler;
import cn.apisium.uniporter.router.exception.IllegalHttpStateException;
import cn.apisium.uniporter.util.PathResolver;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.DefaultFileRegion;
import io.netty.handler.codec.http.*;
import io.netty.handler.ssl.SslHandler;
import io.netty.handler.stream.ChunkedFile;
import io.netty.util.concurrent.Future;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
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
                HttpResponse response = new DefaultHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK);
                RandomAccessFile raf = new RandomAccessFile(file, "r");

                // Correct its MIME
                String mime = Files.probeContentType(target);
                if (mime.equalsIgnoreCase("text/html")) {
                    mime += "; charset=UTF-8";
                }

                long length = raf.length();
                HttpUtil.setContentLength(response, length);
                response.headers().set(HttpHeaderNames.CONTENT_TYPE, mime);

                final boolean keepAlive = HttpUtil.isKeepAlive(request);
                if (!keepAlive) {
                    response.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.CLOSE);
                } else if (request.protocolVersion().equals(HttpVersion.HTTP_1_0)) {
                    response.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.KEEP_ALIVE);
                }

                context.write(response);

                Future<Void> future;
                if (context.pipeline().get(SslHandler.class) == null) {
                    if (context.pipeline().get(HttpContentCompressor.class) != null) {
                        context.pipeline().remove(HttpContentCompressor.class);
                    }
                    context.write(new DefaultFileRegion(raf.getChannel(), 0, length),
                            context.newProgressivePromise());
                    future = context.writeAndFlush(LastHttpContent.EMPTY_LAST_CONTENT);
                } else {
                    if (context.pipeline().get(HttpContentCompressor.class) != null) {
                        context.pipeline().replace(HttpContentCompressor.class, Constants.GZIP_HANDLER_ID,
                                new HttpChunkedContentCompressor());
                    }
                    future = context.writeAndFlush(new HttpChunkedInput(new ChunkedFile(raf, 0, length, 8192)),
                            context.newProgressivePromise());
                }

                if (!keepAlive) {
                    future.addListener(ChannelFutureListener.CLOSE);
                }
            } catch (IOException e) {
                // If error occurs, send a 500 error
                IllegalHttpStateException.send(context, e);
            }
        }
    }
}
