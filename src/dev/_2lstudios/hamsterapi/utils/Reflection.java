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

	public Class<?> getClass(final String className) throws ClassNotFoundException {
		final Class<?> craftBukkitClass = Class.forName(className);
			
		this.classes.put(key, craftBukkitClass);

		return craftBukkitClass;
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

	public Class<?> getNewNMSClass117(String key) {
		try {
			return getClass("net.minecraft.server." + key);
		} catch (final ClassNotFoundException e) {
			/* Ignored */
		}
	}
	
	public Class<?> getNMSClass(String key) {
		if (this.classes.containsKey(key)) {
			return this.classes.get(key);
		}

		try {
			return getClass("net.minecraft.server." + this.version + "." + key);
		} catch (final ClassNotFoundException e) {
			return getNewNMSClass(key);
		}

		return null;
	}

	public Class<?> getCraftBukkitClass(String key) {
		if (this.classes.containsKey(key)) {
			return this.classes.get(key);
		}

		try {
			getClass("org.bukkit.craftbukkit." + this.version + "." + key);
		} catch (final ClassNotFoundException e) {
			/* Ignored */
		}

		return null;
	}
}
