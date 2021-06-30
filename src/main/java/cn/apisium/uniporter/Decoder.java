package cn.apisium.uniporter;

import cn.apisium.uniporter.event.HttpChannelCreatedEvent;
import cn.apisium.uniporter.event.SSLChannelCreatedEvent;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import org.bukkit.Bukkit;

import java.util.*;

public class Decoder extends ByteToMessageDecoder {
    boolean initialized = false;

    protected static final Set<Character> httpMethods = new HashSet<>(Arrays.asList('G', 'H', 'P', 'D', 'C', 'O', 'T'));

    @Override
    protected void decode(ChannelHandlerContext context, ByteBuf buf, List<Object> list) throws Exception {
        buf.retain();
        buf.markReaderIndex();
        boolean handled = false;
        if (buf.isReadable() && !initialized) {
            byte head = buf.readByte();
            if (head == 22 && Uniporter.getRouteConfig().isKeyStoreExist()) {
                registerSSL(context);
                handled = true;
            } else if (httpMethods.contains((char) head)) {
                registerHttp(context);
                handled = true;
            }
            if (handled) {
                buf.resetReaderIndex();
                context.channel().pipeline().fireChannelRead(buf);
            }
        }

        if (!handled) {
            buf.resetReaderIndex();
            context.channel().pipeline().remove(Constants.DECODER_ID);
            context.fireChannelRead(buf);
        }
    }

    protected void registerHttp(ChannelHandlerContext context) {
        clearHandler(context);
        Bukkit.getPluginManager().callEvent(new HttpChannelCreatedEvent(context.channel()));
    }

    protected void registerSSL(ChannelHandlerContext context) {
        clearHandler(context);
        Bukkit.getPluginManager().callEvent(new SSLChannelCreatedEvent(context.channel()));
    }

    protected void clearHandler(ChannelHandlerContext context) {
        try {
            List<String> original = context.channel().pipeline().names();
            List<String> names = new ArrayList<>(original.size());
            names.addAll(original);
            names.remove(Constants.DEFAULT_TAIL_ID);
            names.forEach(context.channel().pipeline()::remove);
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }
}
