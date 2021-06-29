package cn.apisium.uniporter.util;

import io.netty.channel.ChannelFuture;
import org.bukkit.Bukkit;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Objects;

public class ReflectionFinder {
    public static Class<?> getCraftServerClass() {
        return Bukkit.getServer().getClass();
    }

    public static Method getGenericHandleMethodFromClass(Class<?> clazz) throws NoSuchMethodException {
        return clazz.getDeclaredMethod("getHandle");
    }

    public static Method getServerMethodFromClass(Class<?> clazz) throws NoSuchMethodException {
        return clazz.getDeclaredMethod("getServer");
    }

    public static Method getServerConnectionMethodFromClass(Class<?> clazz) throws NoSuchMethodException {
        return clazz.getDeclaredMethod("getServerConnection");
    }

    public static Object getServerConnection() {
        Object server = getMinecraftServer();
        try {
            assert server != null;
            return getServerConnectionMethodFromClass(Objects.requireNonNull(getMinecraftServerClass())).invoke(server);
        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Class<?> getMinecraftServerClass() {
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

    public static Object getMinecraftServer() {
        Class<?> serverClass = getCraftServerClass();
        try {
            return getServerMethodFromClass(serverClass).invoke(Bukkit.getServer());
        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            e.printStackTrace();
        }
        return null;
    }

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

}
