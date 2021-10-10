package dev._2lstudios.hamsterapi.utils;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

public class Reflection {
	private final String version;
	private final Map<String, Class<?>> classes = new HashMap<>();
	private final Map<Class<?>, Map<Class<?>, Map<Integer, Field>>> classFields = new HashMap<>();

	public Reflection(final String version) {
		this.version = version;
	}

	public Class<?> getClass(final String className) throws ClassNotFoundException {
		if (this.classes.containsKey(className)) {
			return this.classes.get(className);
		}

		final Class<?> craftBukkitClass = Class.forName(className);

		this.classes.put(className, craftBukkitClass);

		return craftBukkitClass;
	}

	private Object getValue(final Field field, final Object object) throws IllegalArgumentException, IllegalAccessException {
		final boolean accessible = field.isAccessible();

		field.setAccessible(true);

		final Object value = field.get(object);

		field.setAccessible(accessible);

		return value;
	}

	public Object getField(final Object object, final Class<?> fieldType, final int number) throws IllegalAccessException {
		if (object == null) {
			throw new IllegalAccessException("Tried to access field from a null object");
		}

		final Class<?> objectClass = object.getClass();
		final Map<Class<?>, Map<Integer, Field>> typeFields = classFields.getOrDefault(objectClass, new HashMap<>());
		final Map<Integer, Field> fields = typeFields.getOrDefault(fieldType, new HashMap<>());

		classFields.put(objectClass, typeFields);
		typeFields.put(fieldType, fields);

		if (!fields.isEmpty() && fields.containsKey(number)) {
			return getValue(fields.get(number), object);
		}

		int index = 0;

		for (final Field field : objectClass.getFields()) {
			if (fieldType.equals(field.getType()) && index++ >= number) {
				final Object value = getValue(field, object);

				fields.put(number, field);

				return value;
			}
		}

		return null;
	}

	public Object getField(final Object object, final Class<?> fieldType) throws IllegalAccessException {
		return getField(object, fieldType, 0);
	}

	private Class<?> getNewNetMinecraftClass(String key) {
		try {
			return getClass("net.minecraft." + key);
		} catch (final ClassNotFoundException e) {
			/* Ignored */
		}

		return null;
	}

	private Class<?> getNetMinecraftClass(String key) {
		try {
			final int lastDot = key.lastIndexOf(".");
			final String lastKey = key.substring(lastDot > 0 ? lastDot + 1 : 0, key.length());

			return getClass("net.minecraft.server." + this.version + "." + lastKey);
		} catch (final ClassNotFoundException e) {
			/* Ignored */
		}

		return getNewNetMinecraftClass(key);
	}

	private Class<?> getNewCraftBukkitClass(String key) {
		try {
			return getClass("org.bukkit.craftbukkit." + this.version + "." + key);
		} catch (final ClassNotFoundException e) {
			/* Ignored */
		}

		return null;
	}

	private Class<?> getCraftBukkitClass(String key) {
		try {
			final int lastDot = key.lastIndexOf(".");
			final String lastKey = key.substring(lastDot > 0 ? lastDot + 1 : 0, key.length());

			return getClass("org.bukkit.craftbukkit." + this.version + "." + lastKey);
		} catch (final ClassNotFoundException e) {
			/* Ignored */
		}

		return getNewCraftBukkitClass(key);
	}

	public Class<?> getItemStack() {
		return getNetMinecraftClass("world.item.ItemStack");
	}

	public Class<?> getMinecraftKey() {
		return getNetMinecraftClass("resources.MinecraftKey");
	}

	public Class<?> getEnumProtocol() {
		return getNetMinecraftClass("network.EnumProtocol");
	}

	public Class<?> getEnumProtocolDirection() {
		return getNetMinecraftClass("network.protocol.EnumProtocolDirection");
	}

	public Class<?> getNetworkManager() {
		return getNetMinecraftClass("network.NetworkManager");
	}

	public Class<?> getPacketDataSerializer() {
		return getNetMinecraftClass("network.PacketDataSerializer");
	}

	public Class<?> getPacket() {
		return getNetMinecraftClass("network.protocol.Packet");
	}

	public Class<?> getIChatBaseComponent() {
		return getNetMinecraftClass("network.chat.IChatBaseComponent");
	}

	public Class<?> getPacketPlayOutKickDisconnect() {
		return getNetMinecraftClass("network.protocol.game.PacketPlayOutKickDisconnect");
	}

	public Class<?> getPacketPlayOutTitle() {
		return getNetMinecraftClass("network.protocol.game.PacketPlayOutTitle");
	}

	public Class<?> getPacketPlayOutChat() {
		return getNetMinecraftClass("network.protocol.game.PacketPlayOutChat");
	}

	public Class<?> getPlayerConnection() {
		return getNetMinecraftClass("server.network.PlayerConnection");
	}

	public Class<?> getClientboundSetTitlesAnimationPacket() {
		return getNetMinecraftClass("network.protocol.game.ClientboundSetTitlesAnimationPacket");
	}

	public Class<?> getClientboundSetTitleTextPacket() {
		return getNetMinecraftClass("network.protocol.game.ClientboundSetTitleTextPacket");
	}

	public Class<?> getClientboundSetSubtitleTextPacket() {
		return getNetMinecraftClass("network.protocol.game.ClientboundSetSubtitleTextPacket");
	}

	public Class<?> getChatMessageType() {
		return getNetMinecraftClass("network.chat.ChatMessageType");
	}

	public Class<?> getCraftItemStack() {
		return getCraftBukkitClass("inventory.CraftItemStack");
	}
}
