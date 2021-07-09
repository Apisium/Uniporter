package cn.apisium.uniporter.router.handler;

import cn.apisium.uniporter.Constants;
import cn.apisium.uniporter.Uniporter;
import cn.apisium.uniporter.event.RouteDetectedEvent;
import cn.apisium.uniporter.router.api.Route;
import cn.apisium.uniporter.router.api.UniporterHttpHandler;
import cn.apisium.uniporter.router.api.message.RoutedHttpFullRequest;
import cn.apisium.uniporter.router.api.message.RoutedHttpRequest;
import cn.apisium.uniporter.router.api.message.RoutedHttpResponse;
import cn.apisium.uniporter.router.exception.IllegalHttpStateException;
import cn.apisium.uniporter.util.PathResolver;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpResponseStatus;
import org.bukkit.Bukkit;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.URL;

/**
 * Handles raw Http request and try to convert to routed Http request.
 */
public class HttpServerHandler extends SimpleChannelInboundHandler<FullHttpRequest> {
    @Override
    protected void channelRead0(ChannelHandlerContext context, FullHttpRequest request) throws Exception {
        // Only used to get formatted path, not the actual url
        URL url = new URL(String.format("https://localhost/%s", request.uri()));

        // Parse path
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
            // Check port and calculate the internal logical port
            SocketAddress address = context.channel().localAddress();
            String logicalPort = ":minecraft";
            int port;
            if (address instanceof InetSocketAddress
                    && (port = ((InetSocketAddress) address).getPort()) != Bukkit.getPort()) {
                logicalPort = ":" + port;
            }

            // Find the route corresponding to this request and calls the handler registered
            Route route = Uniporter.getRouteConfig()
                    .findRoute(logicalPort, request.headers().get("Host", ""), path);
            UniporterHttpHandler handler =
                    Uniporter.getRouteConfig()
                            .getHandler(route.getHandler())
                            .orElseThrow(IllegalHttpStateException::new);

            // Check if the response need to be gzipped or not
            if (!route.isGzip() && context.channel().pipeline().names().contains(Constants.GZIP_HANDLER_ID)) {
                context.channel().pipeline().remove(Constants.GZIP_HANDLER_ID);
            }

            RouteDetectedEvent event = new RouteDetectedEvent(context.channel(), route, path);
            Bukkit.getPluginManager().callEvent(event);

            if (event.isCancelled()) {
                IllegalHttpStateException.send(context, HttpResponseStatus.BAD_GATEWAY, "Cancelled");
            } else if (event.getResponse() != null) {
                context.writeAndFlush(new RoutedHttpResponse(path, event.getResponse(), route))
                        .addListener(ChannelFutureListener.CLOSE);
            } else if (event.isNeedReFire() || handler.needReFire()) {
                context.channel().pipeline().remove(Constants.GZIP_HANDLER_ID);
                context.channel().pipeline().remove(Constants.SERVER_HANDLER_ID);
                context.channel().pipeline().remove(Constants.ROUTED_REQUEST_HANDLER_ID);
                context.channel().pipeline().remove(Constants.ROUTED_RESPONSE_HANDLER_ID);
                request.retain();
                handler.handle(path, route, context, request);
                context.channel().pipeline().fireChannelRead(new RoutedHttpFullRequest(path, request, route, handler));
            } else {
                // Send the routed request to next channel handler
                context.fireChannelRead(new RoutedHttpRequest(path, request, route, handler));
            }
        } catch (IllegalHttpStateException exception) {
            // This means router is not found, so send that
            exception.printStackTrace();
            sendNoRouter(path, context);
        } catch (Throwable e) {
            // This is an unexpected error, should not happened
            e.printStackTrace();
            IllegalHttpStateException.send(context, e);
        }
    }

    /**
     * Send no router exception via {@link IllegalHttpStateException}.
     *
     * @param path    current accessing path
     * @param context current request context
     */
    private static void sendNoRouter(String path, ChannelHandlerContext context) {
        IllegalHttpStateException.send(context, HttpResponseStatus.NOT_FOUND, String.format("没有找到对应的路由。<br>%s",
                path));
    }
}
