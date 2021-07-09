package cn.apisium.uniporter.event;

import cn.apisium.uniporter.router.api.Route;
import io.netty.channel.Channel;
import io.netty.handler.codec.http.FullHttpResponse;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;

public class RouteDetectedEvent extends ChannelEvent implements Cancellable {
    private static final HandlerList handlerList = new HandlerList();

    public static HandlerList getHandlerList() {
        return handlerList;
    }

    @Override
    public HandlerList getHandlers() {
        return handlerList;
    }

    boolean needReFire = false;
    boolean cancelled = false;

    Route route;
    String path;
    FullHttpResponse response = null;

    public RouteDetectedEvent(Channel channel, Route route, String path) {
        super(channel);
        this.route = route;
        this.path = path;
    }

    public boolean isNeedReFire() {
        return needReFire;
    }

    public void setNeedReFire(boolean needReFire) {
        this.needReFire = needReFire;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean canceled) {
        this.cancelled = canceled;
    }

    public FullHttpResponse getResponse() {
        return response;
    }

    public void setResponse(FullHttpResponse response) {
        this.response = response;
    }
}
