package cn.apisium.uniporter.router;

import java.util.Map;

public class Route {
    String path;
    String handler;
    boolean gzip;
    Map<String, Object> options;
    Map<String, String> header;

    public Route(String path, String handler, boolean gzip, Map<String, Object> options, Map<String, String> header) {
        this.path = path;
        this.handler = handler;
        this.gzip = gzip;
        this.header = header;
        this.options = options;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getHandler() {
        return handler;
    }

    public void setHandler(String handler) {
        this.handler = handler;
    }

    public boolean isGzip() {
        return gzip;
    }

    public void setGzip(boolean gzip) {
        this.gzip = gzip;
    }

    public Map<String, String> getHeader() {
        return header;
    }

    public void setHeader(Map<String, String> header) {
        this.header = header;
    }

    public Map<String, Object> getOptions() {
        return options;
    }

    public void setOptions(Map<String, Object> options) {
        this.options = options;
    }
}
