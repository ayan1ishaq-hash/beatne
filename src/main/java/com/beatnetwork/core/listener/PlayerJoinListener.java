package com.beatnetwork.core.listener;

import com.beatnetwork.core.CorePlugin;
import java.util.Map;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public final class PlayerJoinListener implements Listener {

    private final CorePlugin plugin;

    public PlayerJoinListener(CorePlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        if (!plugin.getConfig().getBoolean("join-message.enabled", false)) {
            return;
        }

        event.getPlayer().sendMessage(plugin.messages().configMessage(
                "join-message.message",
                Map.of("player", event.getPlayer().getName()),
                false
        ));
    }
}
