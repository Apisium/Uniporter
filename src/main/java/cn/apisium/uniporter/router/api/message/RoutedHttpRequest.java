package cn.apisium.uniporter.router.api.message;

import cn.apisium.uniporter.router.api.Route;
import cn.apisium.uniporter.router.api.UniporterHttpHandler;
import io.netty.handler.codec.http.FullHttpRequest;

/**
 * An aggregated data class contains a {@link FullHttpRequest} and {@link Route}
 *
 * @author Baleine_2000
 */
public class RoutedHttpRequest {
    String path;
    FullHttpRequest request;
    Route route;
    UniporterHttpHandler handler;

    /**
     * Create this routed request which will be passed to later processes.
     *
     * @param path    user accessed url path after host
     * @param request the specified request
     * @param route   the route detected from this request
     * @param handler the registered handler to handle this request
     */
    public RoutedHttpRequest(String path, FullHttpRequest request, Route route, UniporterHttpHandler handler) {
        this.path = path;
        this.request = request;
        this.route = route;
        this.handler = handler;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public FullHttpRequest getRequest() {
        return request;
    }

    public void setRequest(FullHttpRequest request) {
        this.request = request;
    }

    public Route getRoute() {
        return route;
    }

    public void setRoute(Route route) {
        this.route = route;
    }

    public UniporterHttpHandler getHandler() {
        return handler;
    }

    public void setHandler(UniporterHttpHandler handler) {
        this.handler = handler;
    }
}
