package cn.apisium.uniporter;

import cn.apisium.uniporter.acme.Authorizer;
import cn.apisium.uniporter.router.api.Config;
import cn.apisium.uniporter.router.api.Route;
import cn.apisium.uniporter.router.api.UniporterHttpHandler;
import cn.apisium.uniporter.router.defaults.DefaultStaticHandler;
import cn.apisium.uniporter.router.listener.RouterChannelCreator;
import cn.apisium.uniporter.util.ReflectionFinder;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.handler.codec.http.*;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.PluginLoadOrder;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.java.annotation.command.Command;
import org.bukkit.plugin.java.annotation.command.Commands;
import org.bukkit.plugin.java.annotation.dependency.SoftDependency;
import org.bukkit.plugin.java.annotation.permission.Permission;
import org.bukkit.plugin.java.annotation.permission.Permissions;
import org.bukkit.plugin.java.annotation.plugin.*;
import org.bukkit.plugin.java.annotation.plugin.author.Author;

import java.io.File;
import java.util.*;
import java.util.stream.Stream;

/**
 * Uniporter plugin class, also contains some API methods.
 *
 * @author Baleine_2000
 */
@Plugin(name = "Uniporter", version = "@@RELEASE_VERSION@@")
@Description("A netty wrapper for Minecraft, which allows running multiple protocols in same port.")
@Author("Baleine_2000")
@LoadOrder(PluginLoadOrder.STARTUP)
@Website("https://apisium.cn")
@ApiVersion(ApiVersion.Target.v1_13)
@Commands(@Command(name = "uniporter", permission = "uniporter.use", usage = "/uniporter"))
@Permissions(@Permission(name = "uniporter.use"))
@SoftDependency("ProtocolLib")
public final class Uniporter extends JavaPlugin {
    private static final String PREFIX = ChatColor.YELLOW + "[Uniporter] ";
    private static final HashMap<String, UniporterHttpHandler> handlers = new HashMap<>();
    private static final ArrayList<RouteWithOptions> pluginRoutes = new ArrayList<>();
    private static Uniporter instance;
    private static Config config;
    private static boolean debug; // Is this debug environment
    private static boolean useNativeTransport;

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
        return debug;
    }

    /**
     * @return is in debug environment
     */
    public static boolean isUseNativeTransport() {
        return useNativeTransport;
    }

    /**
     * Register a route listen to minecraft port.
     *
     * @param route the route need to be registered
     */
    public static void registerRoute(Route route) {
        getRouteConfig().registerRoute(":minecraft", true, route);
        pluginRoutes.add(new RouteWithOptions(":minecraft", true, route));
    }

    /**
     * Register a route, with given port to listen to, and will ssl be used
     *
     * @param port  port it listen to
     * @param ssl   use ssl or not
     * @param route the route need to be registered
     */
    @SuppressWarnings("unused")
    public static void registerRoute(int port, boolean ssl, Route route) {
        getRouteConfig().registerRoute(":" + port, ssl, route);
        pluginRoutes.add(new RouteWithOptions(":" + port, ssl, route));
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
        registerHandler(id, handler, isAutoRoute, true);
    }

    /**
     * Register handler for later use.
     *
     * @param id          the unique handler id, the very last handler registered with same id will be used
     * @param handler     the handler who will process the http request
     * @param isAutoRoute will the handler register a route corresponding to its id immediately
     * @param gzip        whether gzip is enabled
     */
    public static void registerHandler(String id, UniporterHttpHandler handler, boolean isAutoRoute, boolean gzip) {
        handlers.put(id, handler);
        if (isAutoRoute) {
            registerRoute(new Route("/" + id, id, gzip, Collections.emptyMap(), Collections.emptyMap()));
        }
    }

    @SuppressWarnings("unused")
    public static Map<String, UniporterHttpHandler> getHandlers() {
        return Collections.unmodifiableMap(handlers);
    }

    /**
     * Remove a registered handler.
     *
     * @param id the unique handler id
     */
    @SuppressWarnings("unused")
    public static void removeHandler(String id) {
        handlers.remove(id);
    }

    /**
     * Find a possible handler represents by the given id
     *
     * @param id unique handler id
     * @return possible handler
     */
    public static UniporterHttpHandler getHandler(String id) {
        return handlers.get(id);
    }

    /**
     * Clear channel handlers.
     *
     * @param context current Netty context
     */
    @SuppressWarnings("unused")
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

    @SuppressWarnings("unused")
    public static Set<Integer> findPortsByHandler(String handler) {
        return getRouteConfig().findPortsByHandler(handler);
    }

    @SuppressWarnings("unused")
    public static Set<Route> findRoutesByHandler(String handler) {
        return getRouteConfig().findRoutesByHandler(handler);
    }

    /**
     * Check if a port is ssl enabled or not. Note that by default, minecraft port supports both, however, this
     * method returns false if the port is the same as minecraft port.
     *
     * @param port the port that need to be checked
     * @return if the port is a ssl-only port
     */
    @SuppressWarnings("unused")
    public static boolean isSSLPort(int port) {
        return getRouteConfig().isSSLPort(port);
    }

    private static Stream<ChannelFuture> findBoostrapChannelFutures() {
        List<?> futures = ReflectionFinder.findChannelFutures();
        assert futures != null;
        return futures.stream()
                .filter(f -> f instanceof ChannelFuture)
                .map(f -> (ChannelFuture) f);
    }

    /**
     * Attach channel handler to Minecraft
     */
    @SuppressWarnings("OptionalGetWithoutIsPresent")
    private void attachChannelHandler() {
        try {
            findBoostrapChannelFutures().findFirst().get().channel().pipeline()
                    .addFirst(Constants.UNIPORTER_ID, new ChannelInboundHandlerAdapter() {
                        @Override
                        public void channelRead(ChannelHandlerContext ctx, Object msg) {
                            Channel channel = (Channel) msg;
                            if (!channel.pipeline().names().contains(Constants.DECODER_ID)) {
                                channel.pipeline().addFirst(Constants.DECODER_ID, new Decoder());
                            }
                            ctx.fireChannelRead(msg);
                        }
                    });
        } catch (Throwable e) {
            e.printStackTrace();
            getLogger().info("Failed to attach channel.");
        }
    }

    private void reload() {
        config.stop();
        reloadConfig();
        debug = this.getConfig().getBoolean("debug", false);
        useNativeTransport = this.getConfig().getBoolean("use-native-transport", true);
        config = new Config(new File(this.getDataFolder(), "route.yml"));
        pluginRoutes.forEach(it -> config.registerRoute(it.port, it.ssl, it.route));
    }

    @Override
    public void onEnable() {
        this.saveDefaultConfig();

        instance = this;
        config = new Config(new File(this.getDataFolder(), "route.yml"));
        debug = this.getConfig().getBoolean("debug", false);
        useNativeTransport = this.getConfig().getBoolean("use-native-transport", true);

        getServer().getPluginManager().registerEvents(new RouterChannelCreator(), this);

        // Register default static handler
        registerHandler("static", new DefaultStaticHandler());

        getServer().getScheduler().runTask(this, this::attachChannelHandler);

        PluginCommand command = getServer().getPluginCommand("uniporter");
        assert command != null;
        command.setTabCompleter(this);
        command.setExecutor(this);

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
        findBoostrapChannelFutures().forEach(future -> {
            if (future.channel().pipeline().get(Constants.UNIPORTER_ID) != null) {
                future.channel().pipeline().remove(Constants.UNIPORTER_ID);
            }
        });

        config.stop();
    }

    private static String formatBoolean(boolean value) {
        return (value ? ChatColor.GREEN : ChatColor.RED).toString() + value;
    }

    @Override
    public boolean onCommand(CommandSender sender, org.bukkit.command.Command command, String label, String[] args) {
        if (args.length == 0) {
            sender.sendMessage(PREFIX + ChatColor.GRAY + "Version: " + ChatColor.WHITE + getDescription().getVersion());
            sender.sendMessage(ChatColor.AQUA + "/uniporter channels");
            sender.sendMessage(ChatColor.AQUA + "/uniporter debug");
            sender.sendMessage(ChatColor.AQUA + "/uniporter handlers");
            sender.sendMessage(ChatColor.AQUA + "/uniporter reload");
            return true;
        }
        if (args.length != 1) return false;
        switch (args[0]) {
            case "reload":
                reload();
                sender.sendMessage(PREFIX + ChatColor.GREEN + "Success!");
                return true;
            case "debug":
                sender.sendMessage(PREFIX + ChatColor.GRAY + "Current is: " + formatBoolean((debug = !debug)));
                return true;
            case "handlers":
                sender.sendMessage(PREFIX + ChatColor.GRAY + "Handlers:");
                handlers.forEach((k, v) -> {
                    sender.sendMessage("  " + k + ": " + ChatColor.GRAY + v.getClass().getName());
                    sender.sendMessage(ChatColor.GRAY + "    Need re-fire: " + formatBoolean(v.needReFire()));
                    sender.sendMessage(ChatColor.GRAY + "    Hijack Aggregator: " +
                            formatBoolean(v.hijackAggregator()));
                });
                return true;
            case "channels":
                sender.sendMessage(PREFIX + ChatColor.GRAY + "Channels:");
                findBoostrapChannelFutures()
                        .forEach(future -> {
                            Channel channel = future.channel();
                            sender.sendMessage(channel.id().asShortText() + ":");
                            sender.sendMessage(ChatColor.GRAY + "  Active: " + formatBoolean(channel.isActive()));
                            sender.sendMessage(ChatColor.GRAY + "  Open: " + formatBoolean(channel.isOpen()));
                            sender.sendMessage(ChatColor.GRAY + "  Registered: " +
                                    formatBoolean(channel.isRegistered()));
                            sender.sendMessage(ChatColor.GRAY + "  Address: " + ChatColor.WHITE +
                                    channel.localAddress().toString());
                            sender.sendMessage(ChatColor.GRAY + "  Pipelines:");
                            channel.pipeline().forEach(it -> sender.sendMessage("    " + it.getKey() + ": " +
                                    ChatColor.GRAY + it.getValue().getClass().getName()));
                        });
                return true;
            default: return false;
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, org.bukkit.command.Command command,
                                      String alias, String[] args) {
        return args.length == 1 ? Arrays.asList("channels", "debug", "handlers", "reload") : Collections.emptyList();
    }

    private static final class RouteWithOptions {
        public final String port;
        public final boolean ssl;
        public final Route route;
        public RouteWithOptions(String port, boolean ssl, Route route) {
            this.port = port;
            this.ssl = ssl;
            this.route = route;
        }
    }
}
