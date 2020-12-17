package dev._2lstudios.hamsterapi.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

import dev._2lstudios.hamsterapi.hamsterplayer.HamsterPlayer;
import dev._2lstudios.hamsterapi.hamsterplayer.HamsterPlayerManager;
import dev._2lstudios.hamsterapi.utils.PacketInjector;

public class PlayerQuitListener implements Listener {
    private final HamsterPlayerManager hamsterPlayerManager;
    private final PacketInjector packetInjector;

    public PlayerQuitListener(final HamsterPlayerManager hamsterPlayerManager, final PacketInjector packetInjector) {
        this.hamsterPlayerManager = hamsterPlayerManager;
        this.packetInjector = packetInjector;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerQuit(final PlayerQuitEvent event) {
        final Player player = event.getPlayer();
        final HamsterPlayer hamsterPlayer = hamsterPlayerManager.get(player);

        packetInjector.uninject(hamsterPlayer);
        hamsterPlayerManager.remove(player);
    }
}