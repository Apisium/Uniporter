package cn.apisium.uniporter.example.handlers;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;

import java.nio.charset.StandardCharsets;

public class HttpHelloSender extends ChannelDuplexHandler {
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        sayHello(ctx, (ByteBuf) msg);
    }

    private void sayHello(ChannelHandlerContext context, ByteBuf b) {
        b.clear();
        b.writeBytes(("HTTP/1.1 " + "200 OK").getBytes(StandardCharsets.UTF_8));
        b.writeBytes("\r\n".getBytes(StandardCharsets.UTF_8));
        b.writeBytes(("Content-Type: text/html; charset=utf-8").getBytes(StandardCharsets.UTF_8));
        b.writeBytes("\r\n".getBytes(StandardCharsets.UTF_8));
        b.writeBytes(("Connection: close").getBytes(StandardCharsets.UTF_8));
        b.writeBytes("\r\n".getBytes(StandardCharsets.UTF_8));
        b.writeBytes("\r\n".getBytes(StandardCharsets.UTF_8));
        b.writeBytes("Hello world from Uniporter!".getBytes(StandardCharsets.UTF_8));

        context.writeAndFlush(b);
        context.close();
    }
}
