package cn.apisium.uniporter.router.defaults;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import io.netty.handler.codec.http.DefaultHttpContent;
import io.netty.handler.codec.http.HttpContentCompressor;

public class HttpChunkedContentCompressor extends HttpContentCompressor {
    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        if (msg instanceof ByteBuf) {
            ByteBuf buf = (ByteBuf) msg;
            if (buf.isReadable()) {
                msg = new DefaultHttpContent(buf);
            }
        }
        super.write(ctx, msg, promise);
    }
}
