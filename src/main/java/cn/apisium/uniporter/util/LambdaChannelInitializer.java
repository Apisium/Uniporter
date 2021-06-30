package cn.apisium.uniporter.util;

import io.netty.channel.Channel;

public interface LambdaChannelInitializer {
    void initialize(Channel channel);
}
