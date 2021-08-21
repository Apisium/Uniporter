package cn.apisium.uniporter.util;

import io.netty.channel.ChannelFuture;
import org.bukkit.Bukkit;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Objects;

/**
 * A dirty reflection helper which find the Minecraft's Netty channel.
 *
 * @author Baleine_2000
 */
public class ReflectionFinder {
    /**
     * Find the list of Minecraft's Netty channels, usually (1.12.2 to 1.17) it contains only one channel.
     *
     * @return list of Minecraft's Netty channels
     */
    public static List<?> findChannelFutures() {
        Object connection = getServerConnection();
        assert connection != null;
        Class<?> clazz = connection.getClass();
        for (Field field : clazz.getDeclaredFields()) {
            if (!field.getType().isAssignableFrom(List.class)) {
                continue;
            }
            field.setAccessible(true);
            List<?> list;
            try {
                list = (List<?>) field.get(connection);
                if (list.stream().anyMatch(o -> o instanceof ChannelFuture)) {
                    return list;
                }
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    // Internal helper methods
    private static Class<?> getCraftServerClass() {
        return Bukkit.getServer().getClass();
    }

    private static Method getServerMethodFromClass(Class<?> clazz) throws NoSuchMethodException {
        return clazz.getMethod("getServer");
    }

    private static Method getServerConnectionMethodFromClass(Class<?> clazz) throws NoSuchMethodException {
        return clazz.getMethod("getServerConnection");
    }

    private static Object getServerConnection() {
        Object server = getMinecraftServer();
        try {
            assert server != null;
            return getServerConnectionMethodFromClass(Objects.requireNonNull(getMinecraftServerClass())).invoke(server);
        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static Class<?> getMinecraftServerClass() {
        try {
            Class<?> clazz = getServerMethodFromClass(getCraftServerClass()).getReturnType();
            if (clazz.getName().contains("DedicatedServer")) {
                clazz = clazz.getSuperclass();
            }
            return clazz;
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static Object getMinecraftServer() {
        Class<?> serverClass = getCraftServerClass();
        try {
            return getServerMethodFromClass(serverClass).invoke(Bukkit.getServer());
        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            e.printStackTrace();
        }
        return null;
    }
}
