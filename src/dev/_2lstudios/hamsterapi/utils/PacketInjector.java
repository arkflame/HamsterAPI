package dev._2lstudios.hamsterapi.utils;

import java.util.logging.Logger;

import org.bukkit.Server;

import dev._2lstudios.hamsterapi.hamsterplayer.HamsterPlayer;
import dev._2lstudios.hamsterapi.handlers.HamsterChannelHandler;
import dev._2lstudios.hamsterapi.handlers.HamsterDecoderHandler;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.codec.ByteToMessageDecoder;

public class PacketInjector {
	final Server server;
	private final Logger logger;
	private final String hamsterDecoderName;
	private final String hamsterChannelName;

	public PacketInjector(final Server server, final Logger logger) {
		this.server = server;
		this.logger = logger;
		this.hamsterDecoderName = "hapi_decoder";
		this.hamsterChannelName = "hapi_channel";
	}

	public void inject(final HamsterPlayer hamsterPlayer) {
		// We uninject before injecting to prevent double injection.
		uninject(hamsterPlayer);

		final ChannelPipeline pipeline = hamsterPlayer.getChannel().pipeline();
		final ByteToMessageDecoder hamsterDecoderHandler = new HamsterDecoderHandler(server, hamsterPlayer);
		final ChannelDuplexHandler hamsterChannelHandler = new HamsterChannelHandler(server, hamsterPlayer);

		if (pipeline.get("decompress") != null) {
			pipeline.addAfter("decompress", hamsterDecoderName, hamsterDecoderHandler);
		} else if (pipeline.get("splitter") != null) {
			pipeline.addAfter("splitter", hamsterDecoderName, hamsterDecoderHandler);
		} else {
			throw new NullPointerException(
					"No ChannelHandler was found on the pipeline to inject " + hamsterDecoderName);
		}

		if (pipeline.get("decoder") != null) {
			pipeline.addAfter("decoder", hamsterChannelName, hamsterChannelHandler);
		} else {
			throw new NullPointerException(
					"No ChannelHandler was found on the pipeline to inject " + hamsterChannelHandler);
		}

		logger.info("Succesfully injected player " + hamsterPlayer.getPlayer().getName() + "!");
	}

	public void uninject(final HamsterPlayer hamsterPlayer) {
		try {
			final ChannelPipeline pipeline = hamsterPlayer.getChannel().pipeline();

			if (pipeline.get(hamsterDecoderName) != null) {
				pipeline.remove(hamsterDecoderName);
			}

			if (pipeline.get(hamsterChannelName) != null) {
				pipeline.remove(hamsterChannelName);
			}
		} catch (final Exception exception) {
			exception.printStackTrace();
		}
	}
}
