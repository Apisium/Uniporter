package cn.apisium.uniporter.router.listener;

import cn.apisium.uniporter.Constants;
import cn.apisium.uniporter.event.HttpChannelCreatedEvent;
import cn.apisium.uniporter.event.SSLChannelCreatedEvent;
import cn.apisium.uniporter.router.ExceptionIgnorer;
import cn.apisium.uniporter.router.handler.HttpServerHandler;
import cn.apisium.uniporter.router.handler.RoutedHttpRequestHandler;
import cn.apisium.uniporter.router.handler.RoutedHttpResponseHandler;
import cn.apisium.uniporter.util.SslFactory;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.codec.http.HttpContentCompressor;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;
import io.netty.handler.ssl.SslHandler;
import io.netty.handler.stream.ChunkedWriteHandler;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class RouterChannelCreator implements Listener {
    @EventHandler
    public void onCreated(HttpChannelCreatedEvent event) {
        ChannelPipeline pipeline = event.getChannel().pipeline();
        pipeline.addLast(new HttpRequestDecoder());
        pipeline.addLast(new HttpObjectAggregator(1024 * 1024));
        pipeline.addLast(new HttpResponseEncoder());
        pipeline.addLast(new ChunkedWriteHandler());
        pipeline.addLast(Constants.GZIP_HANDLER_ID, new HttpContentCompressor());
        pipeline.addLast(new HttpServerHandler());
        pipeline.addLast(new RoutedHttpResponseHandler());
        pipeline.addLast(new RoutedHttpRequestHandler());
    }

    @EventHandler
    public void onSSLDetected(SSLChannelCreatedEvent event) {
        ChannelPipeline pipeline = event.getChannel().pipeline();
        pipeline.addFirst(new SslHandler(SslFactory.createEngine()));
        pipeline.addLast(new ExceptionIgnorer());
        Bukkit.getPluginManager().callEvent(new HttpChannelCreatedEvent(event.getChannel()));
    }

}
