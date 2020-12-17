package dev._2lstudios.hamsterapi.tasks;

import java.nio.channels.ClosedChannelException;
import java.util.Collection;
import java.util.HashSet;
import java.util.logging.Logger;

import org.bukkit.entity.Player;

import dev._2lstudios.hamsterapi.HamsterAPI;
import dev._2lstudios.hamsterapi.hamsterplayer.HamsterPlayer;

public class PacketInjectorQueue implements Runnable {
    private final HamsterAPI hamsterAPI;
    private final Logger logger;
    private final Collection<HamsterPlayer> pendingInject = new HashSet<>();

    public PacketInjectorQueue(final HamsterAPI hamsterAPI) {
        this.hamsterAPI = hamsterAPI;
        this.logger = hamsterAPI.getLogger();
    }

    public boolean queue(final HamsterPlayer hamsterPlayer) {
        final Player player = hamsterPlayer.getPlayer();

        if (player == null || !player.isOnline()) {
            return true;
        }

        try {
            hamsterAPI.getPacketInjector().inject(hamsterPlayer);
        } catch (final ClosedChannelException e) {
            logger.info("Cancelled injection because the channel is closed!");
            return true;
        } catch (final Exception exception) {
            pendingInject.add(hamsterPlayer);
            logger.info("Retrying injection to player " + hamsterPlayer.getPlayer().getName() + "! Reason: "
                    + exception.getClass().getName());
            return false;
        }

        logger.info("Succesfully injected player " + hamsterPlayer.getPlayer().getName() + "!");
        return true;
    }

    @Override
    public void run() {
        for (final HamsterPlayer hamsterPlayer : new HashSet<>(pendingInject)) {
            if (queue(hamsterPlayer)) {
                pendingInject.remove(hamsterPlayer);
            }
        }
    }
}
