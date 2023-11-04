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

	public Class<?> getClass(final String className) {
		return this.classes.computeIfAbsent(className, key -> {
			try {
				return Class.forName(key);
			} catch (final ClassNotFoundException e) {
				return null;
			}
		});
	}

	private Object getValue(final Field field, final Object object)
			throws IllegalArgumentException, IllegalAccessException {
		if (field.isAccessible()) {
			return field.get(object);
		}
		field.setAccessible(true);
		try {
			return field.get(object);
		} finally {
			field.setAccessible(false);
		}
	}

	public Object getField(final Object object, final Class<?> fieldType, final int number)
			throws IllegalAccessException {
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
			if (fieldType == field.getType() && index++ >= number) {
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

	private Class<?> getMinecraftClass(String key) {
		final int lastDot = key.lastIndexOf(".");
		final String lastKey = key.substring(lastDot > 0 ? lastDot + 1 : 0, key.length());
		final Class<?> legacyClass = getClass("net.minecraft.server." + this.version + "." + lastKey);
		final Class<?> newClass = getClass("net.minecraft." + key);

		return legacyClass != null ? legacyClass : newClass;
	}

	private Class<?> getCraftBukkitClass(String key) {
		final int lastDot = key.lastIndexOf(".");
		final String lastKey = key.substring(lastDot > 0 ? lastDot + 1 : 0, key.length());
		final Class<?> legacyClass = getClass("org.bukkit.craftbukkit." + this.version + "." + lastKey);
		final Class<?> newClass = getClass("org.bukkit.craftbukkit." + this.version + "." + key);

		return legacyClass != null ? legacyClass : newClass;
	}

	public Class<?> getItemStack() {
		return getMinecraftClass("world.item.ItemStack");
	}

	public Class<?> getMinecraftKey() {
		return getMinecraftClass("resources.MinecraftKey");
	}

	public Class<?> getEnumProtocol() {
		return getMinecraftClass("network.EnumProtocol");
	}

	public Class<?> getEnumProtocolDirection() {
		return getMinecraftClass("network.protocol.EnumProtocolDirection");
	}

	public Class<?> getNetworkManager() {
		return getMinecraftClass("network.NetworkManager");
	}

	public Class<?> getPacketDataSerializer() {
		return getMinecraftClass("network.PacketDataSerializer");
	}

	public Class<?> getPacket() {
		return getMinecraftClass("network.protocol.Packet");
	}

	public Class<?> getIChatBaseComponent() {
		return getMinecraftClass("network.chat.IChatBaseComponent");
	}

	public Class<?> getPacketPlayOutKickDisconnect() {
		return getMinecraftClass("network.protocol.game.PacketPlayOutKickDisconnect");
	}

	public Class<?> getPacketPlayOutTitle() {
		return getMinecraftClass("network.protocol.game.PacketPlayOutTitle");
	}

	public Class<?> getPacketPlayOutChat() {
		return getMinecraftClass("network.protocol.game.PacketPlayOutChat");
	}

	public Class<?> getPlayerConnection() {
		return getMinecraftClass("server.network.PlayerConnection");
	}

	public Class<?> getClientboundSetTitlesAnimationPacket() {
		return getMinecraftClass("network.protocol.game.ClientboundSetTitlesAnimationPacket");
	}

	public Class<?> getClientboundSetTitleTextPacket() {
		return getMinecraftClass("network.protocol.game.ClientboundSetTitleTextPacket");
	}

	public Class<?> getClientboundSetSubtitleTextPacket() {
		return getMinecraftClass("network.protocol.game.ClientboundSetSubtitleTextPacket");
	}

	public Class<?> getChatMessageType() {
		return getMinecraftClass("network.chat.ChatMessageType");
	}

	public Class<?> getCraftItemStack() {
		return getCraftBukkitClass("inventory.CraftItemStack");
	}
}
