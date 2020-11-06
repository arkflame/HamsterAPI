package dev._2lstudios.hamsterapi.events;

import dev._2lstudios.hamsterapi.hamsterplayer.HamsterPlayer;
import dev._2lstudios.hamsterapi.wrappers.ByteBufWrapper;
import io.netty.channel.ChannelHandlerContext;

public class PacketDecodeEvent extends PacketEvent {
    private final ByteBufWrapper byteBuf;

    public PacketDecodeEvent(final ChannelHandlerContext channelHandlerContext, final HamsterPlayer hamsterPlayer,
            final ByteBufWrapper byteBuf, final boolean async) {
        super(channelHandlerContext, hamsterPlayer, async);

        this.byteBuf = byteBuf;
    }

    public ByteBufWrapper getByteBuf() {
        return this.byteBuf;
    }
}