package cn.apisium.uniporter;

import cn.apisium.uniporter.event.ChannelCreatedEvent;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import org.bukkit.Bukkit;

import java.util.*;

public class Decoder extends ByteToMessageDecoder {
    boolean initialized = false;

    private static final Set<Character> httpMethods = new HashSet<>(Arrays.asList('G', 'H', 'P', 'D', 'C', 'O', 'T'));

    @Override
    protected void decode(ChannelHandlerContext context, ByteBuf buf, List<Object> list) throws Exception {
        buf.retain();
        buf.markReaderIndex();
        if (buf.isReadable() && !initialized && httpMethods.contains((char) buf.readByte())) {
            registerProtocol(context);
            context.channel().pipeline().fireChannelRead(buf);
        } else {
            buf.resetReaderIndex();
            context.channel().pipeline().remove("uniporterde");
            context.fireChannelRead(buf);
        }
    }

    private void registerProtocol(ChannelHandlerContext context) {
        try {
            List<String> original = context.channel().pipeline().names();
            List<String> names = new ArrayList<>(original.size());
            names.addAll(original);
            names.remove("DefaultChannelPipeline$TailContext#0");
            names.forEach(context.channel().pipeline()::remove);
            Bukkit.getPluginManager().callEvent(new ChannelCreatedEvent(context.channel()));
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }
}
