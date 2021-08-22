package cn.apisium.uniporter.example;

import cn.apisium.uniporter.router.api.Route;
import cn.apisium.uniporter.router.api.UniporterHttpHandler;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.*;

import java.nio.charset.StandardCharsets;

/**
 * An example re-fire hello world sender.
 * <p>
 * Uncomment form plugin's main class or register by yourself to see how this works.
 *
 * @author Baleine_2000
 */
@SuppressWarnings("unused")
public class HttpReFireHelloSender implements UniporterHttpHandler {
    @Override
    public void handle(String path, Route route, ChannelHandlerContext context, FullHttpRequest request) {
        context.channel().pipeline().addLast(new SimpleChannelInboundHandler<FullHttpRequest>() {
            @Override
            protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest msg) {
                String builder = "ReFired: Hello world from Uniporter!" +
                        "<br>" +
                        "<br>This is: " +
                        request.uri() +
                        "<br>" +
                        "<br>" +
                        "Have fun!";
                FullHttpResponse response = new DefaultFullHttpResponse(
                        HttpVersion.HTTP_1_1,
                        HttpResponseStatus.OK,
                        Unpooled.copiedBuffer(builder.getBytes(StandardCharsets.UTF_8)));
                response.headers().set(HttpHeaderNames.CONTENT_TYPE, "text/html; charset=UTF-8");
                context.channel().pipeline().writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
            }
        });

    }

    @Override
    public boolean needReFire() {
        return true;
    }
}
