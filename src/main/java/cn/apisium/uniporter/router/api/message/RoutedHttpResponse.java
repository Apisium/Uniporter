package cn.apisium.uniporter.router.api.message;

import cn.apisium.uniporter.router.api.Route;
import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.DecoderResult;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;

public class RoutedHttpResponse implements FullHttpResponse {
    String path;
    FullHttpResponse response;
    Route route;

    public RoutedHttpResponse(String path, FullHttpResponse response, Route route) {
        this.path = path;
        this.response = response;
        this.route = route;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public FullHttpResponse getResponse() {
        return response;
    }

    public void setResponse(FullHttpResponse response) {
        this.response = response;
    }

    public Route getRoute() {
        return route;
    }

    public void setRoute(Route route) {
        this.route = route;
    }

    @Override
    public FullHttpResponse copy() {
        return response.copy();
    }

    @Override
    public FullHttpResponse duplicate() {
        return response.duplicate();
    }

    @Override
    public FullHttpResponse retainedDuplicate() {
        return response.retainedDuplicate();
    }

    @Override
    public FullHttpResponse replace(ByteBuf content) {
        return response.replace(content);
    }

    @Override
    public FullHttpResponse retain(int increment) {
        return response.retain(increment);
    }

    @Override
    public FullHttpResponse retain() {
        return response.retain();
    }

    @Override
    public FullHttpResponse touch() {
        return response.touch();
    }

    @Override
    public FullHttpResponse touch(Object hint) {
        return response.touch(hint);
    }

    @Override
    public FullHttpResponse setProtocolVersion(HttpVersion version) {
        return response.setProtocolVersion(version);
    }

    @Override
    public FullHttpResponse setStatus(HttpResponseStatus status) {
        return response.setStatus(status);
    }

    @Override
    @Deprecated
    public HttpResponseStatus getStatus() {
        return response.getStatus();
    }

    @Override
    public HttpResponseStatus status() {
        return response.status();
    }

    @Override
    @Deprecated
    public HttpVersion getProtocolVersion() {
        return response.getProtocolVersion();
    }

    @Override
    public HttpVersion protocolVersion() {
        return response.protocolVersion();
    }

    @Override
    public HttpHeaders headers() {
        return response.headers();
    }

    @Override
    @Deprecated
    public DecoderResult getDecoderResult() {
        return response.getDecoderResult();
    }

    @Override
    public DecoderResult decoderResult() {
        return response.decoderResult();
    }

    @Override
    public void setDecoderResult(DecoderResult result) {
        response.setDecoderResult(result);
    }

    @Override
    public HttpHeaders trailingHeaders() {
        return response.trailingHeaders();
    }

    @Override
    public ByteBuf content() {
        return response.content();
    }

    @Override
    public int refCnt() {
        return response.refCnt();
    }

    @Override
    public boolean release() {
        return response.release();
    }

    @Override
    public boolean release(int decrement) {
        return response.release(decrement);
    }
}
