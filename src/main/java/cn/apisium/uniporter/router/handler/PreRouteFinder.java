package cn.apisium.uniporter.router.handler;

import cn.apisium.uniporter.Constants;
import cn.apisium.uniporter.Uniporter;
import cn.apisium.uniporter.router.api.Route;
import cn.apisium.uniporter.router.api.UniporterHttpHandler;
import cn.apisium.uniporter.router.exception.IllegalHttpStateException;
import cn.apisium.uniporter.router.util.RouteResolver;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.HttpRequest;

public class PreRouteFinder extends SimpleChannelInboundHandler<HttpRequest> implements RouteResolver {
    public PreRouteFinder() {
        super(false);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext context, HttpRequest request) {
        try {
            Route route;
            UniporterHttpHandler handler = null;

            if (context.channel().pipeline().names().contains(Constants.PRE_ROUTE_ID)) {
                context.channel().pipeline().remove(Constants.PRE_ROUTE_ID);
            }

            try {
                route = this.getRoute(context, request.headers(), findPath(request.uri()));
                handler = Uniporter.getRouteConfig().getHandler(route.getHandler()).orElse(null);
            } catch (Throwable ignore) {
            }

            if (handler == null || !handler.hijackAggregator()) {
                context.fireChannelRead(request);
                return;
            }

            handler.hijack(context, request);
            context.channel().pipeline().fireChannelRead(request);
        } catch (Throwable e) {
            IllegalHttpStateException.send(context, e);
        }
    }
}
