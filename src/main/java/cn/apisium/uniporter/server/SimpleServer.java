package cn.apisium.uniporter.server;

import cn.apisium.uniporter.util.LambdaChannelInitializer;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;

import java.net.InetSocketAddress;

public class SimpleServer {
    int port;
    LambdaChannelInitializer initializer;
    ChannelFuture future;

    public int getPort() {
        return port;
    }

    public SimpleServer(int port, LambdaChannelInitializer initializer) {
        this.port = port;
        this.initializer = initializer;
    }

    public void start() throws Exception {
        ServerBootstrap bootstrap = new ServerBootstrap();
        bootstrap.group(new NioEventLoopGroup())
                .channel(NioServerSocketChannel.class)
                .childHandler(new ChannelInitializer<Channel>() {
                    @Override
                    protected void initChannel(Channel ch) throws Exception {
                        initializer.initialize(ch);
                    }
                });
        future = bootstrap.bind(new InetSocketAddress(port)).await();
    }

    public ChannelFuture getFuture() {
        return future;
    }
}
