package cn.apisium.uniporter.router.util;

import cn.apisium.uniporter.Uniporter;
import cn.apisium.uniporter.router.api.Route;
import cn.apisium.uniporter.util.PathResolver;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.HttpHeaders;
import org.bukkit.Bukkit;

import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.SocketAddress;
import java.net.URL;

public interface RouteResolver {
    default Route getRoute(ChannelHandlerContext context, HttpHeaders headers, String path){
        // Check port and calculate the internal logical port
        SocketAddress address = context.channel().localAddress();
        String logicalPort = ":minecraft";
        int port;
        if (address instanceof InetSocketAddress
                && (port = ((InetSocketAddress) address).getPort()) != Bukkit.getPort()) {
            logicalPort = ":" + port;
        }
        return Uniporter.getRouteConfig()
                .findRoute(logicalPort, headers.get("Host", ""), path);
    }

    default String findPath(String uri) {
        try {
            return findPath(new URL(String.format("https://localhost/%s", uri)));
        } catch (MalformedURLException e) {
            // Impossible
            e.printStackTrace();
        }
        return "";
    }
    default String findPath(URL url){
        return PathResolver.resolvePath(url.getPath().substring(1)).replaceAll("[\\\\]", "/");
    }
}
