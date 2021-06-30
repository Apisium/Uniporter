package cn.apisium.uniporter.router;

import cn.apisium.uniporter.router.exception.IllegalHttpStateException;
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
    List<String> indexes = new ArrayList<>();
    HashMap<String, Route> routes = new HashMap<>();
    HashMap<String, HttpHandler> handlers = new HashMap<>();

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
                registerRoute(new Route(
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
    }

    public void registerHandler(String id, HttpHandler handler) {
        handlers.put(id, handler);
    }

    public void registerRoute(Route route) {
        routes.putIfAbsent(route.path, route);
    }

    public Optional<HttpHandler> getHandler(String id) {
        return Optional.ofNullable(handlers.get(id));
    }

    public Route findRoute(String path) throws IllegalHttpStateException {
        return routes.computeIfAbsent(path, p -> routes.keySet().stream()
                .filter(path::startsWith)
                .max(Comparator.comparingInt(String::length))
                .map(routes::get)
                .orElseThrow(() -> new IllegalHttpStateException(HttpResponseStatus.NOT_FOUND)));
    }
}
