package cn.apisium.uniporter.router;

import cn.apisium.uniporter.Uniporter;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.DecoderException;

import javax.net.ssl.SSLHandshakeException;

/**
 * Ignore common SSL exceptions.
 *
 * @author Baleine_2000
 */
public class ExceptionIgnorer extends ChannelDuplexHandler {
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        // Ignore those if not in debug to prevent console spam
        boolean ignore = !Uniporter.isDebug();

        // Ignore handshake exceptions for those trying to connect with HTTP instead of SSL
        if (cause instanceof SSLHandshakeException) {
            ignore = true;
        } else if (cause instanceof DecoderException &&
                cause.getCause() instanceof SSLHandshakeException) {
            ignore = true;
        }

        // Print detail if not ignored
        if (!ignore) {
            cause.printStackTrace();
        }
    }
}
