package cn.apisium.uniporter.server;

import cn.apisium.uniporter.Uniporter;
import cn.apisium.uniporter.util.LambdaChannelInitializer;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.epoll.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;

import java.net.InetSocketAddress;

/**
 * A simple Netty server, does nothing.
 *
 * @author Baleine_2000
 */
public class SimpleServer {
    final int port;
    final LambdaChannelInitializer initializer;

    ChannelFuture future;

    private NioEventLoopGroup bossGroup, workGroup;

    /**
     * Create the server
     *
     * @param port        the port listen to
     * @param initializer the channel initializer, but in Uniporter format not Netty format
     */
    public SimpleServer(int port, LambdaChannelInitializer initializer) {
        this.port = port;
        this.initializer = initializer;
    }

    /**
     * Start the server async
     *
     * @throws Exception if anything abnormal occurs
     */
    public void start() throws Exception {
        ServerBootstrap bootstrap = new ServerBootstrap();
        bootstrap.group(bossGroup = new NioEventLoopGroup(), workGroup = new NioEventLoopGroup())
                .channel(Epoll.isAvailable() && Uniporter.isUseNativeTransport()
                        ? EpollServerSocketChannel.class : NioServerSocketChannel.class)
                .childHandler(new ChannelInitializer<Channel>() {
                    @Override
                    protected void initChannel(Channel ch) {
                        initializer.initialize(ch);
                    }
                });
        future = bootstrap.bind(new InetSocketAddress(port)).await();
    }

    public void stop() {
        workGroup.shutdownGracefully();
        bossGroup.shutdownGracefully();
    }

    public ChannelFuture getFuture() {
        return future;
    }

    public int getPort() {
        return port;
    }
}
