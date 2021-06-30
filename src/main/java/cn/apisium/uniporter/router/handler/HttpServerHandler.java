package cn.apisium.uniporter.router.handler;


import cn.apisium.uniporter.Constants;
import cn.apisium.uniporter.Uniporter;
import cn.apisium.uniporter.router.api.Route;
import cn.apisium.uniporter.router.api.UniporterHttpHandler;
import cn.apisium.uniporter.router.api.message.RoutedHttpRequest;
import cn.apisium.uniporter.router.exception.IllegalHttpStateException;
import cn.apisium.uniporter.util.PathResolver;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpResponseStatus;
import org.bukkit.Bukkit;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.URL;

public class HttpServerHandler extends SimpleChannelInboundHandler<FullHttpRequest> {

    private static void sendNoRouter(String path, ChannelHandlerContext context) {
        IllegalHttpStateException.send(context, HttpResponseStatus.NOT_FOUND, String.format("没有找到对应的路由。<br>%s",
                path));
    }


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
            SocketAddress address = context.channel().localAddress();
            String logicalPort = ":minecraft";
            int port;
            if (address instanceof InetSocketAddress
                    && (port = ((InetSocketAddress) address).getPort()) != Bukkit.getPort()) {
                logicalPort = ":" + port;
            }

            Route route = Uniporter.getRouteConfig().findRoute(logicalPort, path);
            UniporterHttpHandler handler =
                    Uniporter.getRouteConfig().getHandler(route.getHandler()).orElseThrow(IllegalHttpStateException::new);
            if (!route.isGzip() && context.channel().pipeline().names().contains(Constants.GZIP_HANDLER_ID)) {
                context.channel().pipeline().remove(Constants.GZIP_HANDLER_ID);
            }
            context.fireChannelRead(new RoutedHttpRequest(path, request, route, handler));
        } catch (IllegalHttpStateException exception) {
            exception.printStackTrace();
            sendNoRouter(path, context);
        } catch (Throwable e) {
            e.printStackTrace();
            IllegalHttpStateException.send(context, e);
        }
    }
}
