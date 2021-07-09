package cn.apisium.uniporter.event;

import io.netty.channel.Channel;
import org.bukkit.event.HandlerList;

/**
 * An abstract event that will be called whenever a channel is created.
 * <p>
 * Note that it is possible that the channel already initialized, which will not be the same as original Netty
 * behavior, this happens mostly when listen to the same port as the minecraft port because of the use of some hacks.
 *
 * @author Baleine_2000
 */
public abstract class ChannelCreatedEvent extends ChannelEvent {
    private static final HandlerList handlerList = new HandlerList();

    public ChannelCreatedEvent(Channel channel) {
        super(channel);
    }

    public static HandlerList getHandlerList() {
        return handlerList;
    }

    @Override
    public HandlerList getHandlers() {
        return handlerList;
    }

}
