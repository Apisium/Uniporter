package cn.apisium.uniporter.router.api;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpRequest;

/**
 * Handles a http request
 *
 * @author Baleine_2000
 */
public interface UniporterHttpHandler {
    /**
     * Handles a http request
     *
     * @param path    the path user is accessing
     * @param route   the route matches this request
     * @param context the Netty context
     * @param request the http request
     */
    void handle(String path, Route route, ChannelHandlerContext context, FullHttpRequest request);

    default boolean needReFire() {
        return false;
    }

    default boolean hijackAggregator() {
        return false;
    }

    default void hijack(ChannelHandlerContext context, HttpRequest request) {
    }
}
