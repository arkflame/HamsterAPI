package dev._2lstudios.hamsterapi.tasks;

import java.util.Collection;
import java.util.HashSet;

import org.bukkit.entity.Player;

import dev._2lstudios.hamsterapi.HamsterAPI;
import dev._2lstudios.hamsterapi.hamsterplayer.HamsterPlayer;
import dev._2lstudios.hamsterapi.hamsterplayer.HamsterPlayerManager;
import dev._2lstudios.hamsterapi.utils.PacketInjector;

public class InjectTask implements Runnable {
    private Collection<Player> pendingInject = new HashSet<>();

    public boolean inject(final Player player) {
        if (player == null || !player.isOnline()) {
            return false;
        }

        final HamsterAPI hamsterAPI = HamsterAPI.getInstance();
        final HamsterPlayerManager hamsterPlayerManager = hamsterAPI.getHamsterPlayerManager();

        try {
            hamsterPlayerManager.remove(player);

            final PacketInjector packetInjector = hamsterAPI.getPacketInjector();
            final HamsterPlayer hamsterPlayer = hamsterPlayerManager.get(player);

            packetInjector.inject(hamsterPlayer);

            return true;
        } catch (final Exception exception) {
            pendingInject.add(player);
        }

        return false;
    }

    @Override
    public void run() {
        for (final Player player : new HashSet<>(pendingInject)) {
            if (!player.isOnline()) {
                pendingInject.remove(player);
            }

            if (inject(player)) {
                pendingInject.remove(player);
            }
        }
    }
}
