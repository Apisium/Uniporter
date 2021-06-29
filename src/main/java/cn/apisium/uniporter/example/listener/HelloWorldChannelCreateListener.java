package cn.apisium.uniporter.example.listener;

import cn.apisium.uniporter.event.ChannelCreatedEvent;
import cn.apisium.uniporter.example.handlers.HttpHelloSender;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class HelloWorldChannelCreateListener implements Listener {
    @EventHandler
    public void onCreated(ChannelCreatedEvent event) {
        event.getChannel().pipeline().addLast("hello world", new HttpHelloSender());
    }
}
