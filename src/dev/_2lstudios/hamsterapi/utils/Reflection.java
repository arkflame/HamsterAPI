package dev._2lstudios.hamsterapi.utils;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

public class Reflection {
	private final String version;
	private final Map<String, Class<?>> classes = new HashMap<>();

	public Reflection(String version) {
		this.version = version;
	}

	public Object getField(final Object object, final String fieldName)
			throws NoSuchFieldException, IllegalAccessException {
		if (object == null) {
			throw new IllegalAccessException("Tried to access field from a null object");
		}

		final Object fieldValue;
		final Field field = object.getClass().getField(fieldName);
		final boolean accessible = field.isAccessible();

		field.setAccessible(true);
		fieldValue = field.get(object);
		field.setAccessible(accessible);

		return fieldValue;
	}

	public Object getFieldByClass(final Class<?> requiredClass, final Object object)
			throws IllegalArgumentException, IllegalAccessException {
		final Class<?> objectClass = object.getClass();

		for (final Field field : objectClass.getFields()) {
			if (requiredClass.isAssignableFrom(field.getType())) {
				return field.get(object);
			}
		}

		return null;
	}

	public Class<?> getNMSClass(String key) {
		return this.getNMSClass(key, true);
	}

	public Class<?> getNMSClass(String key, boolean byVersion) {
		if (this.classes.containsKey(key)) {
			return this.classes.get(key);
		}

		try {
			String name = "net.minecraft.";

			if (byVersion) {
				name += this.version + ".";
			}

			Class<?> nmsClass = Class.forName(name + key);

			this.classes.put(key, nmsClass);

			return nmsClass;
		} catch (final ClassNotFoundException e) {
			// Ignored
		}

		return null;
	}

	public Class<?> getCraftBukkitClass(String key) {
		if (this.classes.containsKey(key)) {
			return this.classes.get(key);
		}

		try {
			Class<?> craftBukkitClass = Class.forName("org.bukkit.craftbukkit." + this.version + "." + key);
			this.classes.put(key, craftBukkitClass);

			return craftBukkitClass;
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}

		return null;
	}
}
