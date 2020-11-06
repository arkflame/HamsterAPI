package dev._2lstudios.hamsterapi;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Properties;
import java.util.logging.Logger;

import org.bukkit.Server;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import dev._2lstudios.hamsterapi.hamsterplayer.HamsterPlayer;
import dev._2lstudios.hamsterapi.hamsterplayer.HamsterPlayerManager;
import dev._2lstudios.hamsterapi.listeners.PlayerJoinListener;
import dev._2lstudios.hamsterapi.listeners.PlayerQuitListener;
import dev._2lstudios.hamsterapi.messengers.BungeeMessenger;
import dev._2lstudios.hamsterapi.tasks.InjectTask;
import dev._2lstudios.hamsterapi.utils.BufferIO;
import dev._2lstudios.hamsterapi.utils.PacketInjector;
import dev._2lstudios.hamsterapi.utils.Reflection;

public class HamsterAPI extends JavaPlugin {
	private static HamsterAPI instance;
	private Reflection reflection;
	private BufferIO bufferIO;
	private BungeeMessenger bungeeMessenger;
	private HamsterPlayerManager hamsterPlayerManager;
	private PacketInjector packetInjector;

	private static synchronized void setInstance(final HamsterAPI hamsterAPI) {
		HamsterAPI.instance = hamsterAPI;
	}

	public static synchronized HamsterAPI getInstance() {
		return instance;
	}

	private void initialize() {
		final Logger logger = getLogger();
		final Server server = getServer();
		final Properties properties = getProperties();
		final String bukkitVersion = server.getBukkitVersion().split("[-]")[0].replaceFirst("[.]", "");
		final int compressionThreshold = (int) properties.getOrDefault("network_compression_threshold", 256);

		setInstance(this);

		this.reflection = new Reflection(server.getClass().getPackage().getName().split("\\.")[3]);
		this.bufferIO = new BufferIO(this.reflection, bukkitVersion, compressionThreshold);
		this.hamsterPlayerManager = new HamsterPlayerManager();
		this.packetInjector = new PacketInjector(server, logger);
		this.bungeeMessenger = new BungeeMessenger(this);
	}

	private Properties getProperties() {
		final File propertiesFile = new File("./server.properties");
		final Properties properties = new Properties();

		try (final InputStream inputStream = new FileInputStream(propertiesFile)) {
			properties.load(inputStream);
		} catch (final Exception e) {
			e.printStackTrace();
		}

		return properties;
	}

	@Override
	public void onEnable() {
		final Server server = getServer();
		final PluginManager pluginManager = server.getPluginManager();
		final InjectTask injectTask = new InjectTask();

		initialize();

		server.getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");
		pluginManager.registerEvents(new PlayerJoinListener(injectTask), this);
		pluginManager.registerEvents(new PlayerQuitListener(), this);

		for (final Player player : server.getOnlinePlayers()) {
			injectTask.inject(player);
		}

		server.getScheduler().runTaskTimer(this, injectTask, 20, 20);
	}

	@Override
	public void onDisable() {
		final Server server = getServer();

		server.getScheduler().cancelTasks(this);

		for (final Player player : server.getOnlinePlayers()) {
			final HamsterPlayer hamsterPlayer = this.hamsterPlayerManager.get(player);

			this.packetInjector.uninject(hamsterPlayer);
			this.hamsterPlayerManager.remove(player);
		}
	}

	public BufferIO getBufferIO() {
		return this.bufferIO;
	}

	public BungeeMessenger getBungeeMessenger() {
		return this.bungeeMessenger;
	}

	public HamsterPlayerManager getHamsterPlayerManager() {
		return this.hamsterPlayerManager;
	}

	public PacketInjector getPacketInjector() {
		return this.packetInjector;
	}

	public Reflection getReflection() {
		return this.reflection;
	}
}
