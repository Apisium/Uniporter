package cn.apisium.uniporter.router.api;

import cn.apisium.uniporter.Uniporter;
import cn.apisium.uniporter.router.exception.IllegalHttpStateException;
import cn.apisium.uniporter.server.SimpleHttpServer;
import cn.apisium.uniporter.server.SimpleHttpsServer;
import cn.apisium.uniporter.server.SimpleServer;
import io.netty.handler.codec.http.HttpResponseStatus;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Configurations of the router, also contains come helper methods.
 *
 * @author Baleine_2000
 */
public class Config {
    ConfigurationSection section; // Plugin's config instance

    String sslKeyStorePath; // The JKS format key store path, relative to plugins data folder
    String sslKeyStorePassword; // The JKS format key store password

    List<String> indexes; // The default file that the plugin will try to read when accessing folder path

    final HashMap<String, HashMap<String, HashSet<Route>>> routes = new HashMap<>();
    // All routes, first key is port, second is path
    final HashMap<String, HashMap<String, Route>> routeCache = new HashMap<>();
    // Cached routes, first key is host, second is <code>host + "#" + path + "#" + port</code>
    final HashMap<String, SimpleServer> additionalServers = new HashMap<>();
    // All servers with ports other than :minecraft, key is its port

    final HashMap<String, UniporterHttpHandler> handlers = new HashMap<>(); // All registered http handlers

    public boolean keyStoreExist; // Is the key store exist, if its not, ssl will be disabled
    boolean debug; // Is this debug environment

    public HashMap<String, HashMap<String, Route>> getRouteCache() {
        return routeCache;
    }

    public HashMap<String, SimpleServer> getAdditionalServers() {
        return additionalServers;
    }

    public File getKeyStore() {
        return new File(Uniporter.getInstance().getDataFolder(), getSslKeyStorePath()).getAbsoluteFile();
    }

    public boolean isKeyStoreExist() {
        return keyStoreExist;
    }

    public String getSslKeyStorePath() {
        return sslKeyStorePath;
    }

    public String getSslKeyStorePassword() {
        return sslKeyStorePassword;
    }

    public ConfigurationSection getSection() {
        return section;
    }

    public List<String> getIndexes() {
        return indexes;
    }

    public HashMap<String, HashMap<String, HashSet<Route>>> getRoutes() {
        return routes;
    }

    public HashMap<String, UniporterHttpHandler> getHandlers() {
        return handlers;
    }

    public boolean isDebug() {
        return debug;
    }

    /**
     * Load configuration from given file.
     *
     * @param file the yml file contains the configuration, if its not exist, new file will be created.
     */
    public Config(File file) {
        // Create default config if not exist
        if (!file.exists()) {
            try {
                if ((file.getParentFile().exists() || file.getParentFile().mkdirs()) && file.createNewFile()) {
                    InputStream defaultConfig = Objects.requireNonNull(
                            this.getClass().getClassLoader().getResourceAsStream("route.yml"));
                    byte[] contents = new byte[defaultConfig.available()];
                    if (defaultConfig.read(contents) > 0) {
                        Files.write(file.toPath(), contents, StandardOpenOption.CREATE);
                    }
                } else {
                    throw new IllegalStateException("config not created");
                }
            } catch (IOException e) {
                throw new IllegalStateException(e);
            }
        }
        section = YamlConfiguration.loadConfiguration(file);

        debug = section.getBoolean("debug", false);

        indexes = section.getStringList("indexes");
        if (indexes.isEmpty()) {
            indexes.add("index.html");
        }

        // Load ssl keys
        sslKeyStorePath = section.getString("keystore.path", "keystore.jks");
        sslKeyStorePassword = section.getString("keystore.password", "uniporter");
        keyStoreExist = getKeyStore().exists();

        // Parse servers
        ConfigurationSection servers =
                section.contains("server") ? section.getConfigurationSection("server") : section.createSection(
                        "server");
        assert servers != null;
        servers.getKeys(false).forEach(logicalPort -> {
            // Parse "server.*"
            ConfigurationSection server = servers.getConfigurationSection(logicalPort);
            assert server != null;
            server.getKeys(false).forEach(path -> {
                // Parse "server.*.*", which is detailed route config
                ConfigurationSection routeConfig = server.getConfigurationSection(path);
                assert routeConfig != null;
                registerRoute(logicalPort,
                        // Check if is ssl or not
                        routeConfig.getBoolean("options.ssl", false),
                        new Route(path,
                                // The configured handler, static resource processor will be used by default
                                routeConfig.getString("handler", "static"),
                                // If it needs gzip, true by default
                                routeConfig.getBoolean("gzip", true),
                                // Listen hosts, to all hosts by default
                                routeConfig.getStringList("hosts"),
                                // Extra options, currently is options.ssl and options.path
                                Optional.ofNullable(routeConfig.get("options", null))
                                        .filter(o -> o instanceof ConfigurationSection)
                                        .map(o -> (ConfigurationSection) o)
                                        .map(o -> o.getValues(true))
                                        .orElse(new HashMap<>()),
                                // Extra headers need to be added at the end, empty by default
                                Optional.ofNullable(routeConfig.get("headers", null))
                                        .filter(o -> o instanceof ConfigurationSection)
                                        .map(o -> (ConfigurationSection) o)
                                        .map(o -> o.getValues(false))
                                        .map(o ->
                                                o.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey,
                                                        e -> e.getValue().toString()))
                                        )
                                        .orElse(new HashMap<>())));
            });
        });

        // Run all non-minecraft-port servers
        additionalServers.values().forEach(server -> {
            try {
                server.start();
                Uniporter.getInstance().getLogger().info(String.format("Server on port %s started.",
                        server.getPort()));
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    /**
     * Register handler for later use.
     *
     * @param id      the unique handler id, the very last handler registered with same id will be used
     * @param handler the handler who will process the http request
     */
    public void registerHandler(String id, UniporterHttpHandler handler) {
        handlers.put(id, handler);
    }

    /**
     * Remove a registered handler.
     *
     * @param id the unique handler id
     */
    public void removeHandler(String id) {
        handlers.remove(id);
    }


    /**
     * Register a route, with default ssl enable and listen to minecraft port.
     *
     * @param route the route need to be registered
     */
    public void registerRoute(Route route) {
        registerRoute(":minecraft", true, route);
    }


    /**
     * Register a route, with given port to listen to, and will ssl be used
     *
     * @param logicalPort in format of <code>":" + port</code>
     * @param ssl         use ssl or not
     * @param route       the route need to be registered
     */
    public void registerRoute(String logicalPort, boolean ssl, Route route) {
        if (!logicalPort.startsWith(":")) {
            return;
        }
        // Create non-exist maps
        HashSet<Route> portedRoutes =
                routes.computeIfAbsent(logicalPort, (key) -> new HashMap<>())
                        .computeIfAbsent(route.getPath(), (key) -> new HashSet<>());
        portedRoutes.add(route);

        // If the route listen to port other than minecraft port, create corresponding server
        if (!logicalPort.equalsIgnoreCase(":minecraft")) {
            try {
                int port = Integer.parseInt(logicalPort.substring(1));
                if (Bukkit.getPort() != port)
                    additionalServers.computeIfAbsent(logicalPort, (key) -> ssl && isKeyStoreExist() ?
                            new SimpleHttpsServer(port) :
                            new SimpleHttpServer(port));
            } catch (Throwable ignore) {
                // No error should happen here, otherwise it is intended
            }
        }

    }

    /**
     * Find a possible handler represents by the given id
     *
     * @param id unique handler id
     * @return possible handler
     */
    public Optional<UniporterHttpHandler> getHandler(String id) {
        return Optional.ofNullable(handlers.get(id));
    }

    /**
     * Find a most suitable route which listens to minecraft with empty hosts
     *
     * @param path the path user is accessing
     * @return the most suitable route
     * @throws IllegalHttpStateException if no route can be found
     */
    public Route findRoute(String path) throws IllegalHttpStateException {
        return findRoute(":minecraft", "", path);
    }

    /**
     * Find a most suitable route from given information. Result will be cached for same request
     *
     * @param logicalPort the port user is accessing
     * @param host        the host user is accessing
     * @param path        the path user is accessing
     * @return the most suitable route
     * @throws IllegalHttpStateException if no route can be found
     */
    public Route findRoute(String logicalPort, String host, String path) throws IllegalHttpStateException {
        HashMap<String, Route> cachedRoutes = routeCache.computeIfAbsent(host, (key) -> new HashMap<>());
        String cacheKey = host + "#" + path + "#" + logicalPort; // The cache key
        return cachedRoutes.computeIfAbsent(cacheKey, p -> this.getRoutes()
                .computeIfAbsent(logicalPort, (key) -> new HashMap<>())
                .values().stream()
                // Find all routes sets can handle the given path
                .filter(sets -> sets.stream().anyMatch(route ->
                        path.equals(route.getPath()) || path.startsWith(route.getPath() + "/")))
                // Combine to a large stream
                .flatMap(Set::stream)
                // Filters non related path routers
                .filter(route -> path.equals(route.getPath()) || path.startsWith(route.getPath() + "/"))
                // Filters non related host routers
                // 0 size means it handles all hosts
                .filter(route -> route.hosts.size() == 0
                        // Find match without port numbers
                        || route.hosts.stream().anyMatch(
                        pattern -> pattern.matcher(host.substring(0, host.lastIndexOf(":"))).find())
                        // Find match with port numbers as a fallback
                        || route.hosts.stream().anyMatch(pattern -> pattern.matcher(host).find()))
                // Find the most precise route by path length
                .max(Comparator.comparingInt((route) -> route.path.length()))
                // Throws error if nothing is found
                .orElseThrow(() -> new IllegalHttpStateException(HttpResponseStatus.NOT_FOUND)));
    }
}
