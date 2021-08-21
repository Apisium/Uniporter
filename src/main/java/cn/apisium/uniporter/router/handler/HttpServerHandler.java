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
import cn.apisium.uniporter.router.util.RouteResolver;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpResponseStatus;
import org.bukkit.Bukkit;

/**
 * Handles raw Http request and try to convert to routed Http request.
 */
public class HttpServerHandler extends SimpleChannelInboundHandler<FullHttpRequest> implements RouteResolver {
    @Override
    protected void channelRead0(ChannelHandlerContext context, FullHttpRequest request) {
        // Parse path
        String path;
        try {
            path = this.findPath(request.uri());
        } catch (IllegalHttpStateException exception) {
            exception.send(context);
            return;
        } catch (Throwable e) {
            IllegalHttpStateException.send(context, e);
            return;
        }

        try {
            // Find the route corresponding to this request and calls the handler registered
            Route route = this.getRoute(context, request.headers(), path);
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
            if (Uniporter.isDebug()) {
                exception.printStackTrace();
            }
            sendNoRouter(path, context);
        } catch (Throwable e) {
            // This is an unexpected error, should not happened
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
        IllegalHttpStateException.send(context, HttpResponseStatus.NOT_FOUND, String.format("No such route:<br>%s",
                path));
    }
}
