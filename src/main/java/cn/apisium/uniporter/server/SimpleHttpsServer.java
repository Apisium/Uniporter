package cn.apisium.uniporter.server;

import cn.apisium.uniporter.event.SSLChannelCreatedEvent;
import org.bukkit.Bukkit;

public class SimpleHttpsServer extends SimpleServer {
    public SimpleHttpsServer(int port) {
        super(port, (c) ->
                Bukkit.getPluginManager().callEvent(new SSLChannelCreatedEvent(c)));
    }
}
