package cn.apisium.uniporter.router.api;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Data class contains all information to determine a route.
 *
 * @author Baleine_2000
 */
public class Route {
    String path; // Route path
    String handler; // Handler id that process this route
    boolean gzip; // Is the route gzip enabled
    Map<String, Object> options; // Route's extra options
    Map<String, String> header; // Route's extra headers

    final List<Pattern> hosts; // All compiled hosts regexp

    /**
     * Create a route listen to all hosts.
     *
     * @param path    route path
     * @param handler handler id that process this route
     * @param gzip    is gzip enabled
     * @param options extra options, currently supports options.ssl and options.path for static handler
     * @param header  extra headers
     */
    public Route(String path, String handler, boolean gzip, Map<String, Object> options, Map<String, String> header) {
        this(path, handler, gzip, Collections.emptyList(), options, header);
    }

    /**
     * Create a route.
     *
     * @param path    route path
     * @param handler handler id that process this route
     * @param gzip    is gzip enabled
     * @param hosts   non-compiled regexes which matches hosts
     * @param options extra options, currently supports options.ssl and options.path for static handler
     * @param header  extra headers
     */
    public Route(String path, String handler, boolean gzip, List<String> hosts, Map<String, Object> options,
                 Map<String, String> header) {
        this.path = path;
        this.handler = handler;
        this.gzip = gzip;
        this.header = header;
        this.options = options;
        this.hosts = hosts.stream().map(Pattern::compile).collect(Collectors.toList());
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

    @SuppressWarnings("unused")
    public void setGzip(boolean gzip) {
        this.gzip = gzip;
    }

    public Map<String, String> getHeader() {
        return header;
    }

    @SuppressWarnings("unused")
    public void setHeader(Map<String, String> header) {
        this.header = header;
    }

    public Map<String, Object> getOptions() {
        return options;
    }

    @SuppressWarnings("unused")
    public void setOptions(Map<String, Object> options) {
        this.options = options;
    }
}
