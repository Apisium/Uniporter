package cn.apisium.uniporter.event;

import io.netty.channel.Channel;
import org.bukkit.event.HandlerList;

/**
 * When ever detected SSL protocol channel will be created, this event will be called.
 * <p>
 * This is typically called when server reads the connection starts with byte 22, as stated in SSL specs.
 *
 * @author Baleine_2000
 */
public class SSLChannelCreatedEvent extends ChannelCreatedEvent {
    private static final HandlerList handlerList = new HandlerList();

    public static HandlerList getHandlerList() {
        return handlerList;
    }

    @Override
    public HandlerList getHandlers() {
        return handlerList;
    }

    public SSLChannelCreatedEvent(Channel channel) {
        super(channel);
    }
}
