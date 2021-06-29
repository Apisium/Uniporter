package cn.apisium.uniporter.router;


import cn.apisium.uniporter.Uniporter;
import cn.apisium.uniporter.router.exception.IllegalHttpStateException;
import cn.apisium.uniporter.util.PathResolver;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpResponseStatus;

import java.net.URL;

public class HttpServerHandler extends SimpleChannelInboundHandler<FullHttpRequest> {

    @Override
    protected void channelRead0(ChannelHandlerContext context, FullHttpRequest request) throws Exception {
        URL url = new URL(String.format("https://localhost/%s", request.uri()));

        String path;
        try {
            path = PathResolver.resolvePath(url.getPath().substring(1)).replaceAll("[\\\\]", "/");
        } catch (IllegalHttpStateException exception) {
            exception.send(context);
            return;
        } catch (Throwable e) {
            IllegalHttpStateException.send(context, e);
            return;
        }

        try {
            Route route = Uniporter.getRouteConfig().findRoute(path);
            HttpHandler handler =
                    Uniporter.getRouteConfig().getHandler(route.getHandler()).orElseThrow(IllegalHttpStateException::new);
            handler.handle(path, route, context, request);
        } catch (IllegalHttpStateException exception) {
            exception.printStackTrace();
            sendNoRouter(path, context);
        } catch (Throwable e) {
            IllegalHttpStateException.send(context, e);
        }
    }

    private static void sendNoRouter(String path, ChannelHandlerContext context) {
        IllegalHttpStateException.send(context, HttpResponseStatus.NOT_FOUND, String.format("没有找到对应的路由。<br>%s",
                path));
    }
}
