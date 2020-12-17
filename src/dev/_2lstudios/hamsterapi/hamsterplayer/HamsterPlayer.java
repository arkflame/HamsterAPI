package dev._2lstudios.hamsterapi.hamsterplayer;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.bukkit.Server;
import org.bukkit.entity.Player;

import dev._2lstudios.hamsterapi.HamsterAPI;
import dev._2lstudios.hamsterapi.utils.Reflection;
import io.netty.channel.Channel;

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

	public void setup()
			throws IllegalAccessException, InvocationTargetException, NoSuchMethodException, NoSuchFieldException {
		if (setup) {
			return;
		}

		final Reflection reflection = hamsterAPI.getReflection();
		final Object handler = player.getClass().getDeclaredMethod("getHandle").invoke(player);

		this.playerConnection = reflection.getField(handler, "playerConnection");
		this.networkManager = reflection.getField(playerConnection, "networkManager");
		this.channel = (Channel) reflection.getField(networkManager, "channel");
		this.iChatBaseComponentClass = reflection.getNMSClass("IChatBaseComponent");
		this.sendPacketMethod = this.playerConnection.getClass().getDeclaredMethod("sendPacket",
				reflection.getNMSClass("Packet"));
		this.toChatBaseComponent = iChatBaseComponentClass.getDeclaredClasses()[0].getDeclaredMethod("a", String.class);
		hamsterAPI.getLogger().info("Succesfully setup player " + player.getName() + "!");

		setup = true;
	}

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
}
