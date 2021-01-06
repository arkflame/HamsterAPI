package dev._2lstudios.hamsterapi.hamsterplayer;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.channels.ClosedChannelException;
import java.util.logging.Logger;

import org.bukkit.Server;
import org.bukkit.entity.Player;

import dev._2lstudios.hamsterapi.HamsterAPI;
import dev._2lstudios.hamsterapi.handlers.HamsterChannelHandler;
import dev._2lstudios.hamsterapi.handlers.HamsterDecoderHandler;
import dev._2lstudios.hamsterapi.enums.HamsterHandler;
import dev._2lstudios.hamsterapi.utils.Reflection;
import io.netty.channel.Channel;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.codec.ByteToMessageDecoder;

public class HamsterPlayer {
	private final Player player;
	private final HamsterAPI hamsterAPI;
	private Object playerConnection;
	private Object networkManager;
	private Channel channel;
	private Class<?> iChatBaseComponentClass;
	private Method toChatBaseComponent;
	private Method sendPacketMethod;
	private boolean setup = false;
	private boolean injected = false;

	HamsterPlayer(final Player player) {
		this.player = player;
		this.hamsterAPI = HamsterAPI.getInstance();
	}

	public Player getPlayer() {
		return this.player;
	}

	public void sendActionbar(final String text) {
		final Reflection reflection = hamsterAPI.getReflection();

		try {
			Object chatAction = toChatBaseComponent.invoke(null, "{ \"text\":\"" + text + "\" }");
			Object packet = reflection.getNMSClass("PacketPlayOutChat")
					.getConstructor(iChatBaseComponentClass, byte.class).newInstance(chatAction, (byte) 2);

			sendPacket(packet);
		} catch (final Exception e) {
			e.printStackTrace();
		}
	}

	public void sendTitle(String title, String subtitle, int fadeInTime, int showTime, int fadeOutTime) {
		final Reflection reflection = hamsterAPI.getReflection();

		try {
			Object chatTitle = toChatBaseComponent.invoke(null, "{ \"text\":\"" + title + "\" }");
			Constructor<?> titleConstructor = reflection.getNMSClass("PacketPlayOutTitle").getConstructor(
					reflection.getNMSClass("PacketPlayOutTitle").getDeclaredClasses()[0], iChatBaseComponentClass,
					int.class, int.class, int.class);

			Object packet = titleConstructor
					.newInstance(reflection.getNMSClass("PacketPlayOutTitle").getDeclaredClasses()[0]
							.getDeclaredField("TITLE").get(null), chatTitle, fadeInTime, showTime, fadeOutTime);

			Object chatSubTitle = toChatBaseComponent.invoke(null, "{ \"text\":\"" + subtitle + "\" }");
			Constructor<?> timingTitleConstructor = reflection.getNMSClass("PacketPlayOutTitle").getConstructor(
					reflection.getNMSClass("PacketPlayOutTitle").getDeclaredClasses()[0], iChatBaseComponentClass,
					int.class, int.class, int.class);

			Object timingPacket = timingTitleConstructor
					.newInstance(
							reflection.getNMSClass("PacketPlayOutTitle").getDeclaredClasses()[0]
									.getDeclaredField("SUBTITLE").get(null),
							chatSubTitle, fadeInTime, showTime, fadeOutTime);

			sendPacket(packet);
			sendPacket(timingPacket);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void sendServer(final String serverName) {
		hamsterAPI.getBungeeMessenger().sendPluginMessage("ConnectOther", player.getName(), serverName);
	}

	public void closeChannel() {
		if (channel != null && channel.isActive()) {
			channel.close();
		}
	}

	public void disconnect(final String reason) {
		final Reflection reflection = hamsterAPI.getReflection();
		final Server server = hamsterAPI.getServer();

		try {
			final Object chatKick = toChatBaseComponent.invoke(null, "{ \"text\":\"" + reason + "\" }");
			final Object packet = reflection.getNMSClass("PacketPlayOutKickDisconnect")
					.getConstructor(iChatBaseComponentClass).newInstance(chatKick);

			sendPacket(packet);
		} catch (final Exception e) {
			e.printStackTrace();
		}

		hamsterAPI.getBungeeMessenger().sendPluginMessage("kickPlayer", player.getName(), reason);

		if (server.isPrimaryThread()) {
			player.kickPlayer(reason);
		} else {
			server.getScheduler().runTask(hamsterAPI, () -> {
				player.kickPlayer(reason);
			});
		}
	}

	public void sendPacket(final Object packet) {
		try {
			sendPacketMethod.invoke(playerConnection, packet);
		} catch (final Exception e) {
			e.printStackTrace();
		}
	}

	public Object getPlayerConnection() {
		return playerConnection;
	}

	public Object getNetworkManager() {
		return networkManager;
	}

	public Channel getChannel() {
		return channel;
	}

	public void uninject() {
		if (injected && channel != null && channel.isActive()) {
			final ChannelPipeline pipeline = channel.pipeline();

			if (pipeline.get(HamsterHandler.HAMSTER_DECODER) != null) {
				pipeline.remove(HamsterHandler.HAMSTER_DECODER);
			}

			if (pipeline.get(HamsterHandler.HAMSTER_CHANNEL) != null) {
				pipeline.remove(HamsterHandler.HAMSTER_CHANNEL);
			}
		}
	}

	public void setup()
			throws IllegalAccessException, InvocationTargetException, NoSuchMethodException, NoSuchFieldException {
		if (!setup) {
			final Reflection reflection = hamsterAPI.getReflection();
			final Object handler = player.getClass().getDeclaredMethod("getHandle").invoke(player);

			this.playerConnection = reflection.getField(handler, "playerConnection");
			this.networkManager = reflection.getField(playerConnection, "networkManager");
			this.channel = (Channel) reflection.getField(networkManager, "channel");
			this.iChatBaseComponentClass = reflection.getNMSClass("IChatBaseComponent");
			this.sendPacketMethod = this.playerConnection.getClass().getDeclaredMethod("sendPacket",
					reflection.getNMSClass("Packet"));
			this.toChatBaseComponent = iChatBaseComponentClass.getDeclaredClasses()[0].getDeclaredMethod("a",
					String.class);
			this.setup = true;

			hamsterAPI.getLogger().info("Succesfully setup player " + player.getName() + "!");
		}
	}

	public void inject() throws IllegalAccessException, InvocationTargetException, NoSuchMethodException,
			NoSuchFieldException, ClosedChannelException {
		if (!injected) {
			setup();

			if (!channel.isActive()) {
				throw new ClosedChannelException();
			}

			final ChannelPipeline pipeline = channel.pipeline();
			final ByteToMessageDecoder hamsterDecoderHandler = new HamsterDecoderHandler(this);
			final ChannelDuplexHandler hamsterChannelHandler = new HamsterChannelHandler(this);

			if (pipeline.get("decompress") != null) {
				pipeline.addAfter("decompress", HamsterHandler.HAMSTER_DECODER, hamsterDecoderHandler);
			} else if (pipeline.get("splitter") != null) {
				pipeline.addAfter("splitter", HamsterHandler.HAMSTER_DECODER, hamsterDecoderHandler);
			} else {
				throw new IllegalAccessException(
						"No ChannelHandler was found on the pipeline to inject " + HamsterHandler.HAMSTER_DECODER);
			}

			if (pipeline.get("decoder") != null) {
				pipeline.addAfter("decoder", HamsterHandler.HAMSTER_CHANNEL, hamsterChannelHandler);
			} else {
				throw new IllegalAccessException(
						"No ChannelHandler was found on the pipeline to inject " + hamsterChannelHandler);
			}

			this.injected = true;
		}
	}

	public boolean trySetup(final Logger logger) {
		try {
			setup();
		} catch (final IllegalAccessException | InvocationTargetException | NoSuchMethodException
				| NoSuchFieldException e) {
			logger.info("Failed to setup " + player.getName() + "! Reason: " + e.getMessage());

			return false;
		}

		return true;
	}

	public boolean tryInject(final Logger logger) {
		try {
			inject();
		} catch (final IllegalAccessException | InvocationTargetException | NoSuchMethodException | NoSuchFieldException
				| ClosedChannelException e) {
			logger.info("Failed to inject " + player.getName() + "! Reason: " + e.getMessage());

			return false;
		}

		return true;
	}

	public boolean trySetupInject(final Logger logger) {
		return trySetup(logger) && tryInject(logger);
	}
}
