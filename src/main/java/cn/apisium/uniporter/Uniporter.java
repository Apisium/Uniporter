package cn.apisium.uniporter;

import cn.apisium.uniporter.router.api.Config;
import cn.apisium.uniporter.router.api.Route;
import cn.apisium.uniporter.router.api.UniporterHttpHandler;
import cn.apisium.uniporter.router.defaults.DefaultStaticHandler;
import cn.apisium.uniporter.router.listener.RouterChannelCreator;
import cn.apisium.uniporter.util.ReflectionFinder;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.bukkit.Bukkit;
import org.bukkit.plugin.PluginLoadOrder;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.java.annotation.plugin.*;
import org.bukkit.plugin.java.annotation.plugin.author.Author;

import java.io.File;
import java.util.HashMap;
import java.util.List;

@Plugin(name = "Uniporter", version = "1.0")
@Description("A netty wrapper for Minecraft, which allows running multiple protocols in same port.")
@Author("Baleine_2000")
@LoadOrder(PluginLoadOrder.STARTUP)
@Website("https://apisium.cn")
@ApiVersion(ApiVersion.Target.v1_13)
public final class Uniporter extends JavaPlugin {

    private static Uniporter instance;

    public static Uniporter getInstance() {
        return instance;
    }

    private static Config config;

    public static Config getRouteConfig() {
        return config;
    }

    public static boolean isDebug() {
        return getRouteConfig().isDebug();
    }

    public static void registerRoute(Route route) {
        getRouteConfig().registerRoute(route);
    }

    public static void registerHandler(String id, UniporterHttpHandler handler) {
        registerHandler(id, handler, false);
    }

    public static void registerHandler(String id, UniporterHttpHandler handler, boolean isAutoRoute) {
        getRouteConfig().registerHandler(id, handler);
        if (isAutoRoute) {
            registerRoute(new Route(String.format("/%s", id), id, true, new HashMap<>(),
                    new HashMap<>()));
        }
    }

    public void attachChannelHandler() {
        try {
            List<?> futures = ReflectionFinder.findChannelFutures();
            assert futures != null;
            futures.stream()
                    .filter(f -> f instanceof ChannelFuture)
                    .map(f -> (ChannelFuture) f).findFirst()
                    .ifPresent(future -> future.channel().pipeline().addFirst(new ChannelInboundHandlerAdapter() {
                        @Override
                        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                            if (msg instanceof NioSocketChannel) {
                                NioSocketChannel channel = (NioSocketChannel) msg;
                                if (channel.pipeline().names().contains(Constants.DECODER_ID)) {
                                    channel.pipeline().remove(Constants.DECODER_ID);
                                }
                                channel.pipeline().addLast(Constants.DECODER_ID, new Decoder());
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
        instance = this;
        config = new Config(new File(this.getDataFolder(), "route.yml"));
        this.attachChannelHandler();

        Bukkit.getPluginManager().registerEvents(new RouterChannelCreator(), this);

        // Register default static handler
        registerHandler("static", new DefaultStaticHandler());

        getLogger().info("Uniporter initialized");

        // Uncomment below to see how example works.
        // Uniporter.registerHandler("helloworld", new HttpHelloSender(), true);
    }

    @Override
    public void onDisable() {
        getLogger().info("Uniporter disabled.");
    }
}
