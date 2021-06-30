package cn.apisium.uniporter.server;

import cn.apisium.uniporter.event.HttpChannelCreatedEvent;
import org.bukkit.Bukkit;

public class SimpleHttpServer extends SimpleServer {
    public SimpleHttpServer(int port) {
        super(port, (c) ->
                Bukkit.getPluginManager().callEvent(new HttpChannelCreatedEvent(c)));
    }
}
