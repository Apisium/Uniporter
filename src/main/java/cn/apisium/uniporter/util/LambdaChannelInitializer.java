package cn.apisium.uniporter.util;

import io.netty.channel.Channel;

/**
 * An Uniporter-styled channel initializer
 *
 * @author Baleine_2000
 */
public interface LambdaChannelInitializer {
    void initialize(Channel channel);
}
