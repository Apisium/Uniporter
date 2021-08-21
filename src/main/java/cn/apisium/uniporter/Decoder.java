package cn.apisium.uniporter;

import cn.apisium.uniporter.event.HttpChannelCreatedEvent;
import cn.apisium.uniporter.event.SSLChannelCreatedEvent;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import org.bukkit.Bukkit;

import java.util.ArrayList;
import java.util.List;

/**
 * The decoder to determine the actual protocol that the client is using.
 *
 * @author Baleine_2000
 */
public class Decoder extends ByteToMessageDecoder {
    @Override
    protected void decode(ChannelHandlerContext context, ByteBuf buf, List<Object> list) {
        buf.retain();
        buf.markReaderIndex();
        boolean handled = false;
        if (buf.isReadable()) {
            byte head = buf.readByte();
            if (head == 22 && Uniporter.getRouteConfig().isKeyStoreExist()) {
                // If the packet starts with byte 22 (SSL handshake byte), pass to SSL handlers
                registerSSL(context);
                handled = true;
            } else if (Constants.HTTP_METHODS.contains((char) head)) {
                // If the packet starts with Http methods bytes, pass to Http handlers
                registerHttp(context);
                handled = true;
            }
            if (handled) {
                // Re-read this byte
                buf.resetReaderIndex();
                context.channel().pipeline().fireChannelRead(buf);
            }
        }

        // If not handled, means it is probably Minecraft protocol, pass to them
        if (!handled) {
            buf.resetReaderIndex();
            context.channel().pipeline().remove(Constants.DECODER_ID);
            context.fireChannelRead(buf);
        }
    }

    /**
     * Clear the channel and register Http stuffs.
     *
     * @param context current Netty context
     */
    protected void registerHttp(ChannelHandlerContext context) {
        clearHandler(context);
        Bukkit.getPluginManager().callEvent(new HttpChannelCreatedEvent(context.channel()));
    }

    /**
     * Clear the channel and register SSL stuffs.
     *
     * @param context current Netty context
     */
    protected void registerSSL(ChannelHandlerContext context) {
        clearHandler(context);
        Bukkit.getPluginManager().callEvent(new SSLChannelCreatedEvent(context.channel()));
    }

    /**
     * Clear Netty channel handlers.
     *
     * @param context current Netty context
     */
    public static void clearHandler(ChannelHandlerContext context) {
        try {
            List<String> original = context.channel().pipeline().names();
            List<String> names = new ArrayList<>(original.size());
            names.addAll(original);
            // Keep the tail handler
            names.remove(Constants.DEFAULT_TAIL_ID);
            names.forEach(context.channel().pipeline()::remove);
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }
}
