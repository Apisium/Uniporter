package cn.apisium.uniporter.router.api.message;

import cn.apisium.uniporter.router.api.Route;
import cn.apisium.uniporter.router.api.UniporterHttpHandler;
import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.DecoderResult;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpVersion;

public class RoutedHttpFullRequest extends RoutedHttpRequest implements FullHttpRequest {
    /**
     * Create this routed request which will be passed to later processes.
     *
     * @param path    user accessed url path after host
     * @param request the specified request
     * @param route   the route detected from this request
     * @param handler the registered handler to handle this request
     */
    public RoutedHttpFullRequest(String path, FullHttpRequest request, Route route, UniporterHttpHandler handler) {
        super(path, request, route, handler);
    }

    public FullHttpRequest copy() {
        return request.copy();
    }

    public FullHttpRequest duplicate() {
        return request.duplicate();
    }

    public FullHttpRequest retainedDuplicate() {
        return request.retainedDuplicate();
    }

    public FullHttpRequest replace(ByteBuf content) {
        return request.replace(content);
    }

    public FullHttpRequest retain(int increment) {
        return request.retain(increment);
    }

    public FullHttpRequest retain() {
        return request.retain();
    }

    public FullHttpRequest touch() {
        return request.touch();
    }

    public FullHttpRequest touch(Object hint) {
        return request.touch(hint);
    }

    public FullHttpRequest setProtocolVersion(HttpVersion version) {
        return request.setProtocolVersion(version);
    }

    public FullHttpRequest setMethod(HttpMethod method) {
        return request.setMethod(method);
    }

    public FullHttpRequest setUri(String uri) {
        return request.setUri(uri);
    }

    @Deprecated
    public HttpMethod getMethod() {
        return request.getMethod();
    }

    public HttpMethod method() {
        return request.method();
    }

    @Deprecated
    public String getUri() {
        return request.getUri();
    }

    public String uri() {
        return request.uri();
    }

    @Deprecated
    public HttpVersion getProtocolVersion() {
        return request.getProtocolVersion();
    }

    public HttpVersion protocolVersion() {
        return request.protocolVersion();
    }

    public HttpHeaders headers() {
        return request.headers();
    }

    @Deprecated
    public DecoderResult getDecoderResult() {
        return request.getDecoderResult();
    }

    public DecoderResult decoderResult() {
        return request.decoderResult();
    }

    public void setDecoderResult(DecoderResult result) {
        request.setDecoderResult(result);
    }

    public HttpHeaders trailingHeaders() {
        return request.trailingHeaders();
    }

    public ByteBuf content() {
        return request.content();
    }

    public int refCnt() {
        return request.refCnt();
    }

    public boolean release() {
        return request.release();
    }

    public boolean release(int decrement) {
        return request.release(decrement);
    }
}
