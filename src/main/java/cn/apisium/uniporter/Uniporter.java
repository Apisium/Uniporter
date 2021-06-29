package cn.apisium.uniporter;

import cn.apisium.uniporter.util.ReflectionFinder;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.bukkit.plugin.PluginLoadOrder;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.java.annotation.plugin.*;
import org.bukkit.plugin.java.annotation.plugin.author.Author;

import java.util.List;

@Plugin(name = "Uniporter", version = "1.0")
@Description("A netty wrapper for Minecraft, which allows running multiple protocols in same port.")
@Author("Baleine_2000")
@LoadOrder(PluginLoadOrder.STARTUP)
@Website("https://apisium.cn")
@ApiVersion(ApiVersion.Target.v1_13)
public final class Uniporter extends JavaPlugin {

    @Override
    public void onLoad() {
        try {
            List<?> futures = ReflectionFinder.findChannelFutures();

            assert futures != null;
            futures.stream().filter(f -> f instanceof ChannelFuture)
                    .map(f -> (ChannelFuture) f).findFirst()
                    .ifPresent(future -> future.channel().pipeline().addFirst(new ChannelInboundHandlerAdapter() {
                        @Override
                        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                            if (msg instanceof NioSocketChannel) {
                                NioSocketChannel channel = (NioSocketChannel) msg;
                                channel.pipeline().addLast("uniporterde", new Decoder());
                                super.channelRead(ctx, channel);
                            } else {
                                super.channelRead(ctx, msg);
                            }
                        }
                    }));
        } catch (Throwable e) {
            e.printStackTrace();
            getLogger().info("Failed to attach channel.");
        }
    }

    @Override
    public void onEnable() {
        // Uncomment below to see how example works.
        // Bukkit.getPluginManager().registerEvents(new HelloWorldChannelCreateListener(), this);
    }

    @Override
    public void onDisable() {

    }
}
