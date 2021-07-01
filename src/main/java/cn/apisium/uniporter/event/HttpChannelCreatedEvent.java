package cn.apisium.uniporter.event;

import io.netty.channel.Channel;
import org.bukkit.event.HandlerList;

/**
 * When ever detected Http protocol channel will be created, this event will be called.
 * <p>
 * Note that it is also called when {@link SSLChannelCreatedEvent} is called, as to create a Https server.
 *
 * @author Baleine_2000
 */
public class HttpChannelCreatedEvent extends ChannelCreatedEvent {
    private static final HandlerList handlerList = new HandlerList();

    public static HandlerList getHandlerList() {
        return handlerList;
    }

    @Override
    public HandlerList getHandlers() {
        return handlerList;
    }

    public HttpChannelCreatedEvent(Channel channel) {
        super(channel);
    }
}
