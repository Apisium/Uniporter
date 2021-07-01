package cn.apisium.uniporter.server;

import cn.apisium.uniporter.event.HttpChannelCreatedEvent;
import org.bukkit.Bukkit;

/**
 * A simple http server, created with default http channel handlers.
 *
 * @author Baleine_2000
 */
public class SimpleHttpServer extends SimpleServer {
    /**
     * Create the server
     *
     * @param port the port listen to
     */
    public SimpleHttpServer(int port) {
        super(port, (c) ->
                Bukkit.getPluginManager().callEvent(new HttpChannelCreatedEvent(c)));
    }
}
