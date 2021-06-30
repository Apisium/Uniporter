package cn.apisium.uniporter.router.listener;

import cn.apisium.uniporter.Constants;
import cn.apisium.uniporter.event.ChannelCreatedEvent;
import cn.apisium.uniporter.router.handler.HttpServerHandler;
import cn.apisium.uniporter.router.handler.RoutedHttpResponseHandler;
import cn.apisium.uniporter.router.handler.RoutedHttpRequestHandler;
import io.netty.channel.Channel;
import io.netty.handler.codec.http.HttpContentCompressor;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;
import io.netty.handler.stream.ChunkedWriteHandler;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class HttpRouterChannelCreator implements Listener {
    @EventHandler
    public void onCreated(ChannelCreatedEvent event) {
        Channel ch = event.getChannel();
        ch.pipeline().addLast("uniporter-http-decoder", new HttpRequestDecoder());
        ch.pipeline().addLast("uniporter-http-aggregator", new HttpObjectAggregator(1024 * 1024));
        ch.pipeline().addLast("uniporter-http-encoder", new HttpResponseEncoder());
        ch.pipeline().addLast("uniporter-http-chunked", new ChunkedWriteHandler());
        ch.pipeline().addLast(Constants.GZIP_HANDLER_ID, new HttpContentCompressor());
        ch.pipeline().addLast("uniporter-http-server", new HttpServerHandler());
        ch.pipeline().addLast("uniporter-http-routed-outbound", new RoutedHttpResponseHandler());
        ch.pipeline().addLast("uniporter-http-routed-inbound", new RoutedHttpRequestHandler());
    }
}
