package com.beatnetwork.core.cooldown;

import com.beatnetwork.core.CorePlugin;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.bukkit.entity.Player;

public final class CooldownManager {

    private final CorePlugin plugin;
    private final Map<UUID, Map<String, Long>> cooldowns = new HashMap<>();

    public CooldownManager(CorePlugin plugin) {
        this.plugin = plugin;
    }

    public boolean tryUse(Player player, String key, String displayName, int cooldownSeconds) {
        long remainingMillis = getRemainingMillis(player, key);

        if (remainingMillis > 0) {
            player.sendMessage(plugin.messages().configMessage(
                    "messages.abilities.cooldown",
                    Map.of(
                            "ability", displayName,
                            "time", formatRemaining(remainingMillis)
                    )
            ));
            return false;
        }

        if (cooldownSeconds > 0) {
            cooldowns
                    .computeIfAbsent(player.getUniqueId(), ignored -> new HashMap<>())
                    .put(key, System.currentTimeMillis() + (cooldownSeconds * 1000L));
        }

        return true;
    }

    public long getRemainingMillis(Player player, String key) {
        Map<String, Long> playerCooldowns = cooldowns.get(player.getUniqueId());

        if (playerCooldowns == null) {
            return 0L;
        }

        long expiresAt = playerCooldowns.getOrDefault(key, 0L);
        long remaining = expiresAt - System.currentTimeMillis();

        if (remaining <= 0L) {
            playerCooldowns.remove(key);
            if (playerCooldowns.isEmpty()) {
                cooldowns.remove(player.getUniqueId());
            }
            return 0L;
        }

        return remaining;
    }

    public void clear() {
        cooldowns.clear();
    }

    private String formatRemaining(long remainingMillis) {
        double seconds = Math.max(0.1D, remainingMillis / 1000.0D);
        return String.format("%.1fs", seconds);
    }
}
