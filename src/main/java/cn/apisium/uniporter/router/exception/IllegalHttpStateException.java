package cn.apisium.uniporter.router.exception;

import cn.apisium.uniporter.Uniporter;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.*;

import java.nio.charset.StandardCharsets;

public class IllegalHttpStateException extends IllegalArgumentException {
    private HttpResponseStatus status;

    public HttpResponseStatus getStatus() {
        return status;
    }

    public void setStatus(HttpResponseStatus status) {
        this.status = status;
    }

    public IllegalHttpStateException() {
        this(HttpResponseStatus.NOT_FOUND);
    }

    public IllegalHttpStateException(HttpResponseStatus status) {
        this.status = status;
    }

    public IllegalHttpStateException(String s, HttpResponseStatus status) {
        super(s);
        this.status = status;
    }

    public IllegalHttpStateException(String message, Throwable cause, HttpResponseStatus status) {
        super(message, cause);
        this.status = status;
    }

    public IllegalHttpStateException(Throwable cause, HttpResponseStatus status) {
        super(cause);
        this.status = status;
    }

    public IllegalHttpStateException(Throwable cause) {
        super(Uniporter.isDebug() ? cause.getMessage() : "Internal Server Error", cause);
        this.status = HttpResponseStatus.INTERNAL_SERVER_ERROR;
    }

    public static void send(ChannelHandlerContext context, Throwable cause) {
        send(context, HttpResponseStatus.INTERNAL_SERVER_ERROR, Uniporter.isDebug() ? cause.getMessage() :
                "Internal Server Error");
    }

    public void send(ChannelHandlerContext context) {
        send(context, getStatus(), getMessage());
    }

    public static void send(ChannelHandlerContext context, HttpResponseStatus status, String message) {
        String msg = "<html><head><title>%s %s - Uniporter</title></head><body>%s</body></html>";
        FullHttpResponse response = new DefaultFullHttpResponse(
                HttpVersion.HTTP_1_1,
                status,
                Unpooled.copiedBuffer(String.format(msg, status.code(), status.reasonPhrase(), message),
                        StandardCharsets.UTF_8));
        response.headers().set(HttpHeaderNames.CONTENT_TYPE, "text/html; charset=UTF-8");
        context.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
    }
}
