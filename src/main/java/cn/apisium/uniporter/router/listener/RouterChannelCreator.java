package cn.apisium.uniporter.router.listener;

import cn.apisium.uniporter.Constants;
import cn.apisium.uniporter.event.HttpChannelCreatedEvent;
import cn.apisium.uniporter.event.SSLChannelCreatedEvent;
import cn.apisium.uniporter.router.ExceptionIgnorer;
import cn.apisium.uniporter.router.handler.HttpServerHandler;
import cn.apisium.uniporter.router.handler.PreRouteFinder;
import cn.apisium.uniporter.router.handler.RoutedHttpRequestHandler;
import cn.apisium.uniporter.router.handler.RoutedHttpResponseHandler;
import cn.apisium.uniporter.util.SSLFactory;
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

/**
 * Listen on channel events, and attach corresponding handlers to the channel.
 *
 * @author Baleine_2000
 */
public class RouterChannelCreator implements Listener {
    @EventHandler
    public void onCreated(HttpChannelCreatedEvent event) {
        ChannelPipeline pipeline = event.getChannel().pipeline();

        // Set up normal http server
        pipeline.addLast(Constants.DECODER_HANDLER_ID, new HttpRequestDecoder());
        pipeline.addLast(Constants.ENCODER_HANDLER_ID, new HttpResponseEncoder());
        pipeline.addLast(Constants.PRE_ROUTE_ID, new PreRouteFinder());
        pipeline.addLast(Constants.AGGREGATOR_HANDLER_ID, new HttpObjectAggregator(1024 * 1024));
        pipeline.addLast(new ChunkedWriteHandler());
        pipeline.addLast(Constants.GZIP_HANDLER_ID, new HttpContentCompressor());
        pipeline.addLast(Constants.SERVER_HANDLER_ID, new HttpServerHandler());

        // Below are used to add header for routed requests
        pipeline.addLast(Constants.ROUTED_RESPONSE_HANDLER_ID, new RoutedHttpResponseHandler());
        pipeline.addLast(Constants.ROUTED_REQUEST_HANDLER_ID, new RoutedHttpRequestHandler());
    }

    @EventHandler
    public void onSSLDetected(SSLChannelCreatedEvent event) {
        ChannelPipeline pipeline = event.getChannel().pipeline();

        // Set up normal SSL server
        pipeline.addFirst(new SslHandler(SSLFactory.createEngine()));
        pipeline.addLast(new ExceptionIgnorer());

        // Call http stuffs to create HTTPS server, this can be modified to support other protocol
        Bukkit.getPluginManager().callEvent(new HttpChannelCreatedEvent(event.getChannel()));
    }
}
