package cn.apisium.uniporter.router.exception;

import cn.apisium.uniporter.Uniporter;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.*;

import java.nio.charset.StandardCharsets;

/**
 * An exception occurs when trying to response Http request but failed.
 *
 * @author Baleine_2000
 */
public class IllegalHttpStateException extends IllegalArgumentException {
    private HttpResponseStatus status; // The final response status

    /**
     * Create a file not found exception
     */
    public IllegalHttpStateException() {
        this(HttpResponseStatus.NOT_FOUND);
    }

    /**
     * Create a custom status exception.
     *
     * @param status final response status
     */
    public IllegalHttpStateException(HttpResponseStatus status) {
        this.status = status;
    }

    /**
     * Create a custom status exception with reason attached.
     *
     * @param reason error reason
     * @param status final response status
     */
    public IllegalHttpStateException(String reason, HttpResponseStatus status) {
        super(reason);
        this.status = status;
    }


    /**
     * Create a internal server error exception with cause attached.
     *
     * @param cause the cause of this exception
     */
    public IllegalHttpStateException(Throwable cause) {
        super(Uniporter.isDebug() ? cause.getMessage() : "Internal Server Error", cause);
        this.status = HttpResponseStatus.INTERNAL_SERVER_ERROR;
    }

    /**
     * Create a custom status exception with cause attached.
     *
     * @param cause  the cause of this exception
     * @param status final response status
     */
    public IllegalHttpStateException(Throwable cause, HttpResponseStatus status) {
        super(cause);
        this.status = status;
    }

    /**
     * Create a custom status exception with reason and cause attached.
     *
     * @param reason error reason
     * @param cause  the cause of this exception
     * @param status final response status
     */
    public IllegalHttpStateException(String reason, Throwable cause, HttpResponseStatus status) {
        super(reason, cause);
        this.status = status;
    }

    public HttpResponseStatus getStatus() {
        return status;
    }

    public void setStatus(HttpResponseStatus status) {
        this.status = status;
    }

    /**
     * Send this exception page.
     *
     * @param context the context of the response
     */
    public void send(ChannelHandlerContext context) {
        send(context, getStatus(), getMessage());
    }

    /**
     * Send a internal server exception, if in debug environment, detailed cause will be shown.
     *
     * @param context the context of the response
     * @param cause   the cause of this exception
     */
    public static void send(ChannelHandlerContext context, Throwable cause) {
        send(context, HttpResponseStatus.INTERNAL_SERVER_ERROR, Uniporter.isDebug() ? cause.getMessage() :
                "Internal Server Error");
        if (Uniporter.isDebug()) {
            cause.printStackTrace();
        }

    }

    /**
     * Send a custom status exception
     *
     * @param context the context of the response
     * @param status  final response status
     * @param message the message of this exception
     */
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
