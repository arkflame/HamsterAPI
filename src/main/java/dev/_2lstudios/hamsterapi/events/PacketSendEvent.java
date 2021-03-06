package dev._2lstudios.hamsterapi.events;

import dev._2lstudios.hamsterapi.hamsterplayer.HamsterPlayer;
import dev._2lstudios.hamsterapi.wrappers.PacketWrapper;
import io.netty.channel.ChannelHandlerContext;

public class PacketSendEvent extends PacketEvent {
    private final PacketWrapper packet;

    public PacketSendEvent(final ChannelHandlerContext channelHandlerContext, final HamsterPlayer hamsterPlayer,
            final PacketWrapper packet, final boolean async) {
        super(channelHandlerContext, hamsterPlayer, async);

        this.packet = packet;
    }

    public PacketWrapper getPacket() {
        return this.packet;
    }
}