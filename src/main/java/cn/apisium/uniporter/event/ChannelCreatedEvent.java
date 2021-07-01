package cn.apisium.uniporter.event;

import io.netty.channel.Channel;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class ChannelCreatedEvent extends Event {
    private static final HandlerList handlerList = new HandlerList();

    public static HandlerList getHandlerList() {
        return handlerList;
    }

    @Override
    public HandlerList getHandlers() {
        return handlerList;
    }

    private final Channel channel;

    public Channel getChannel() {
        return channel;
    }

    public ChannelCreatedEvent(Channel channel) {
        super();
        this.channel = channel;
    }
}
