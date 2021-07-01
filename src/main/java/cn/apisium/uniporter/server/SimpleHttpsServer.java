package cn.apisium.uniporter.server;

import cn.apisium.uniporter.event.SSLChannelCreatedEvent;
import org.bukkit.Bukkit;

/**
 * A simple http server, created with default http and ssl channel handlers.
 *
 * @author Baleine_2000
 */
public class SimpleHttpsServer extends SimpleServer {
    /**
     * Create the server
     *
     * @param port the port listen to
     */
    public SimpleHttpsServer(int port) {
        super(port, (c) ->
                Bukkit.getPluginManager().callEvent(new SSLChannelCreatedEvent(c)));
    }
}
