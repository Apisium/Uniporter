package cn.apisium.uniporter.router.api.message;

import cn.apisium.uniporter.router.api.UniporterHttpHandler;
import cn.apisium.uniporter.router.api.Route;
import io.netty.handler.codec.http.FullHttpRequest;

public class RoutedHttpRequest {
    String path;
    FullHttpRequest request;
    Route route;
    UniporterHttpHandler handler;

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
