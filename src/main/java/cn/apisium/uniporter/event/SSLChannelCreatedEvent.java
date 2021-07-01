package cn.apisium.uniporter.event;

import io.netty.channel.Channel;
import org.bukkit.event.HandlerList;

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
