package cn.apisium.uniporter.router;

import cn.apisium.uniporter.Uniporter;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.DecoderException;

import javax.net.ssl.SSLHandshakeException;

public class ExceptionIgnorer extends ChannelDuplexHandler {
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        boolean ignore = !Uniporter.isDebug();
        if (cause instanceof SSLHandshakeException) {
            ignore = true;
        } else if (cause instanceof DecoderException &&
                cause.getCause() instanceof SSLHandshakeException) {
            ignore = true;
        }
        if (!ignore) {
            cause.printStackTrace();
        }
    }
}
