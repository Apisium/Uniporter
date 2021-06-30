package cn.apisium.uniporter.router.api;

import cn.apisium.uniporter.Uniporter;
import cn.apisium.uniporter.router.exception.IllegalHttpStateException;
import cn.apisium.uniporter.server.SimpleHttpServer;
import cn.apisium.uniporter.server.SimpleHttpsServer;
import cn.apisium.uniporter.server.SimpleServer;
import io.netty.handler.codec.http.HttpResponseStatus;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.*;
import java.util.stream.Collectors;

public class Config {
    ConfigurationSection section;

    String sslKeyStorePath;
    String sslKeyStorePassword;

    List<String> indexes;
    HashMap<String, Route> routes = new HashMap<>();
    HashMap<String, SimpleServer> additionalServers = new HashMap<>();
    HashMap<String, UniporterHttpHandler> handlers = new HashMap<>();

    boolean keyStoreExist = false;

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

    public HashMap<String, Route> getRoutes() {
        return routes;
    }

    public HashMap<String, UniporterHttpHandler> getHandlers() {
        return handlers;
    }

    public boolean isDebug() {
        return section.getBoolean("debug", false);
    }

    public Config(File file) {
        if (!file.exists()) {
            try {
                if (file.getParentFile().mkdirs() && file.createNewFile()) {
                    Files.write(file.toPath(), "server:\n    example:\n        /:\n            handler: static\n"
                            .getBytes(StandardCharsets.UTF_8), StandardOpenOption.CREATE);
                } else {
                    throw new IllegalStateException("config not created");
                }
            } catch (IOException e) {
                throw new IllegalStateException(e);
            }
        }
        section = YamlConfiguration.loadConfiguration(file);
        indexes = section.getStringList("indexes");
        if (indexes.isEmpty()) {
            indexes.add("index.html");
        }

        sslKeyStorePath = section.getString("keystore.path", "keystore.jks");
        sslKeyStorePassword = section.getString("keystore.password", "uniporter");

        keyStoreExist = getKeyStore().exists();

        ConfigurationSection servers =
                section.contains("server") ? section.getConfigurationSection("server") : section.createSection(
                        "server");
        assert servers != null;
        servers.getKeys(false).forEach(key -> {
            ConfigurationSection server = servers.getConfigurationSection(key);
            assert server != null;
            server.getKeys(false).forEach(path -> {
                ConfigurationSection routeConfig = server.getConfigurationSection(path);
                assert routeConfig != null;
                registerRoute(key, routeConfig.getBoolean("options.ssl", false), new Route(
                        path,
                        routeConfig.getString("handler", "static"),
                        routeConfig.getBoolean("gzip", true),
                        Optional.ofNullable(routeConfig.get("options", null))
                                .filter(o -> o instanceof ConfigurationSection)
                                .map(o -> (ConfigurationSection) o)
                                .map(o -> o.getValues(true))
                                .orElse(new HashMap<>()),
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
        additionalServers.values().forEach(server -> {
            try {
                server.start();
                Uniporter.getInstance().getLogger().info(String.format("Server on port %s started.", server.getPort()));
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    public void registerHandler(String id, UniporterHttpHandler handler) {
        handlers.put(id, handler);
    }

    public void registerRoute(Route route) {
        registerRoute(":minecraft", true, route);
    }

    public void registerRoute(String logicalPort, boolean ssl, Route route) {
        if (!logicalPort.startsWith(":")) {
            // TODO: parse server ip
            return;
        }
        routes.putIfAbsent(route.getPath() + logicalPort, route);
        if (!logicalPort.equalsIgnoreCase(":minecraft")) {
            try {
                int port = Integer.parseInt(logicalPort.substring(1));
                additionalServers.computeIfAbsent(logicalPort, (key) -> ssl ? new SimpleHttpsServer(port) :
                        new SimpleHttpServer(port));
            } catch (Throwable ignore) {
            }
        }

    }

    public Optional<UniporterHttpHandler> getHandler(String id) {
        return Optional.ofNullable(handlers.get(id));
    }

    public Route findRoute(String logicalPort, String path) throws IllegalHttpStateException {
        path = path + logicalPort;
        String finalPath = path;
        return routes.computeIfAbsent(path, p -> routes.keySet().stream()
                .filter(finalPath::startsWith)
                .max(Comparator.comparingInt(String::length))
                .map(routes::get)
                .orElseThrow(() -> new IllegalHttpStateException(HttpResponseStatus.NOT_FOUND)));
    }

    public Route findRoute(String path) throws IllegalHttpStateException {
        return findRoute(":minecraft", path);
    }
}
