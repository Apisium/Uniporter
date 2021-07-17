package cn.apisium.uniporter;

import cn.apisium.uniporter.acme.Authorizer;
import cn.apisium.uniporter.router.api.Config;
import cn.apisium.uniporter.router.api.Route;
import cn.apisium.uniporter.router.api.UniporterHttpHandler;
import cn.apisium.uniporter.router.defaults.DefaultStaticHandler;
import cn.apisium.uniporter.router.listener.RouterChannelCreator;
import cn.apisium.uniporter.util.ReflectionFinder;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.*;
import org.bukkit.Bukkit;
import org.bukkit.plugin.PluginLoadOrder;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.java.annotation.plugin.*;
import org.bukkit.plugin.java.annotation.plugin.author.Author;

import java.io.File;
import java.util.HashMap;
import java.util.List;

/**
 * Uniporter plugin class, also contains some API methods.
 *
 * @author Baleine_2000
 */
@Plugin(name = "Uniporter", version = "1.1")
@Description("A netty wrapper for Minecraft, which allows running multiple protocols in same port.")
@Author("Baleine_2000")
@LoadOrder(PluginLoadOrder.STARTUP)
@Website("https://apisium.cn")
@ApiVersion(ApiVersion.Target.v1_13)
public final class Uniporter extends JavaPlugin {
    private static Uniporter instance;
    private static Config config;

    /**
     * @return Plugin's instance
     */
    public static Uniporter getInstance() {
        return instance;
    }

    /**
     * @return The route config
     */
    public static Config getRouteConfig() {
        return config;
    }

    /**
     * @return is in debug environment
     */
    public static boolean isDebug() {
        return getRouteConfig().isDebug();
    }

    /**
     * Register a route listen to minecraft port.
     *
     * @param route the route need to be registered
     */
    public static void registerRoute(Route route) {
        getRouteConfig().registerRoute(route);
    }

    /**
     * Register a route, with given port to listen to, and will ssl be used
     *
     * @param port  port it listen to
     * @param ssl   use ssl or not
     * @param route the route need to be registered
     */
    public static void registerRoute(int port, boolean ssl, Route route) {
        getRouteConfig().registerRoute(":" + port, ssl, route);
    }

    /**
     * Register handler for later use.
     *
     * @param id      the unique handler id, the very last handler registered with same id will be used
     * @param handler the handler who will process the http request
     */
    public static void registerHandler(String id, UniporterHttpHandler handler) {
        registerHandler(id, handler, false);
    }

    /**
     * Register handler for later use.
     *
     * @param id          the unique handler id, the very last handler registered with same id will be used
     * @param handler     the handler who will process the http request
     * @param isAutoRoute will the handler register a route corresponding to its id immediately
     */
    public static void registerHandler(String id, UniporterHttpHandler handler, boolean isAutoRoute) {
        getRouteConfig().registerHandler(id, handler);
        if (isAutoRoute) {
            registerRoute(new Route(String.format("/%s", id), id, true, new HashMap<>(),
                    new HashMap<>()));
        }
    }

    /**
     * Remove a registered handler.
     *
     * @param id the unique handler id
     */
    public static void removeHandler(String id) {
        getRouteConfig().removeHandler(id);
    }

    /**
     * Clear channel handlers.
     *
     * @param context current Netty context
     */
    public static void clearNettyHandler(ChannelHandlerContext context) {
        Decoder.clearHandler(context);
    }

    public static void send(ChannelHandlerContext context, String mime, byte[] data) {
        FullHttpResponse response = new DefaultFullHttpResponse(
                HttpVersion.HTTP_1_1,
                HttpResponseStatus.OK,
                Unpooled.copiedBuffer(data));
        response.headers().set(HttpHeaderNames.CONTENT_TYPE, mime);
        context.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
    }

    /**
     * Attach channel handler to Minecraft
     */
    private void attachChannelHandler() {
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
                                if (!channel.pipeline().names().contains(Constants.DECODER_ID)) {
                                    channel.pipeline().addLast(Constants.DECODER_ID, new Decoder());
                                }
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
        this.saveDefaultConfig();

        instance = this;
        config = new Config(new File(this.getDataFolder(), "route.yml"));
        this.attachChannelHandler();

        Bukkit.getPluginManager().registerEvents(new RouterChannelCreator(), this);

        // Register default static handler
        registerHandler("static", new DefaultStaticHandler());

        getLogger().info("Uniporter initialized");

        if (this.getConfig().getBoolean("eula") && this.getConfig().getBoolean("order")) {
            boolean success = false;
            int count = 5;
            while (!success && count > 0) {
                try {
                    count--;
                    new Authorizer(this).order();
                    success = true;
                } catch (Throwable e) {
                    e.printStackTrace();
                }
            }

            if (Authorizer.server != null) {
                Authorizer.server.getFuture().channel().close();
                Authorizer.server.getFuture().channel().closeFuture().syncUninterruptibly();
            }
            getRouteConfig().keyStoreExist = getRouteConfig().getKeyStore().exists();
            this.getConfig().set("order", false);
            this.saveConfig();
        }

        // Uncomment below to see how example works.
        // Uniporter.registerHandler("helloworld", new HttpHelloSender(), true);
        // Uniporter.registerHandler("helloworld-re-fire", new HttpReFireHelloSender(), true);
        // Uniporter.registerHandler("hijack", new HttpHijackSender(), true);
        // Uniporter.registerHandler("mix", new HttpHijackMixedSender(), true);
    }

    @Override
    public void onDisable() {
        // Close all previous opened servers
        getRouteConfig().getAdditionalServers().values().forEach(server -> {
            server.getFuture().addListener(ChannelFutureListener.CLOSE);
            server.getFuture().syncUninterruptibly();
        });

        getLogger().info("Uniporter disabled.");
    }
}
