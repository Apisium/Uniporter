package cn.apisium.uniporter.event;

import io.netty.channel.Channel;
import org.bukkit.event.HandlerList;

public class HttpChannelCreatedEvent extends ChannelCreatedEvent {
    private static HandlerList handlerList = new HandlerList();

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
