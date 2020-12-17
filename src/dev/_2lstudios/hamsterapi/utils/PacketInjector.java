package dev._2lstudios.hamsterapi.utils;

import java.lang.reflect.InvocationTargetException;
import java.nio.channels.ClosedChannelException;

import org.bukkit.Server;

import dev._2lstudios.hamsterapi.hamsterplayer.HamsterPlayer;
import dev._2lstudios.hamsterapi.handlers.HamsterChannelHandler;
import dev._2lstudios.hamsterapi.handlers.HamsterDecoderHandler;
import io.netty.channel.Channel;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.codec.ByteToMessageDecoder;

public class PacketInjector {
	final Server server;
	private static final String HAMSTER_DECODER_NAME = "hapi_decoder";
	private static final String HAMSTER_CHANNEL_NAME = "hapi_channel";

	public PacketInjector(final Server server) {
		this.server = server;
	}

	public void inject(final HamsterPlayer hamsterPlayer)
			throws IllegalAccessException, InvocationTargetException, NoSuchMethodException, NoSuchFieldException,
			ClosedChannelException {
		hamsterPlayer.setup();

		final Channel channel = hamsterPlayer.getChannel();

		if (!channel.isActive()) {
			throw new ClosedChannelException();
		}

		// We uninject before injecting to prevent double injection.
		uninject(hamsterPlayer);

		final ChannelPipeline pipeline = channel.pipeline();
		final ByteToMessageDecoder hamsterDecoderHandler = new HamsterDecoderHandler(server, hamsterPlayer);
		final ChannelDuplexHandler hamsterChannelHandler = new HamsterChannelHandler(server, hamsterPlayer);

		if (pipeline.get("decompress") != null) {
			pipeline.addAfter("decompress", HAMSTER_DECODER_NAME, hamsterDecoderHandler);
		} else if (pipeline.get("splitter") != null) {
			pipeline.addAfter("splitter", HAMSTER_DECODER_NAME, hamsterDecoderHandler);
		} else {
			throw new IllegalStateException(
					"No ChannelHandler was found on the pipeline to inject " + HAMSTER_DECODER_NAME);
		}

		if (pipeline.get("decoder") != null) {
			pipeline.addAfter("decoder", HAMSTER_CHANNEL_NAME, hamsterChannelHandler);
		} else {
			throw new IllegalStateException(
					"No ChannelHandler was found on the pipeline to inject " + hamsterChannelHandler);
		}
	}

	public void uninject(final HamsterPlayer hamsterPlayer) {
		final Channel channel = hamsterPlayer.getChannel();

		if (channel != null && channel.isActive()) {
			final ChannelPipeline pipeline = hamsterPlayer.getChannel().pipeline();

			if (pipeline.get(HAMSTER_DECODER_NAME) != null) {
				pipeline.remove(HAMSTER_DECODER_NAME);
			}

			if (pipeline.get(HAMSTER_CHANNEL_NAME) != null) {
				pipeline.remove(HAMSTER_CHANNEL_NAME);
			}
		}
	}
}
