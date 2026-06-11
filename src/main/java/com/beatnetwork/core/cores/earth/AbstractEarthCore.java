package com.beatnetwork.core.cores.earth;

import com.beatnetwork.core.CorePlugin;
import com.beatnetwork.core.cooldown.CooldownManager;
import java.util.Map;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public abstract class AbstractEarthCore {

    protected final CorePlugin plugin;
    protected final EarthCoreManager manager;
    protected final CooldownManager cooldowns;

    protected AbstractEarthCore(CorePlugin plugin, EarthCoreManager manager, CooldownManager cooldowns) {
        this.plugin = plugin;
        this.manager = manager;
        this.cooldowns = cooldowns;
    }

    public abstract EarthTier tier();

    public abstract void tickPassive(Player player);

    public abstract void handleActive(Player player);

    public abstract void handleUltimate(Player player);

    public void handleDamageTaken(Player player, EntityDamageEvent event) {
        // Not every tier has a damage-taken passive.
    }

    protected void applyStoneSkin(Player player, int amplifier) {
        if (!isStandingOnEarth(player)) {
            return;
        }

        addEffect(player, PotionEffectType.RESISTANCE, configInt("abilities.earth.passive-refresh-seconds", 3), amplifier);
    }

    protected boolean isStandingOnEarth(Player player) {
        Material blockBelow = player.getLocation().getBlock().getRelative(BlockFace.DOWN).getType();
        return EarthBlockUtil.isEarthBlock(blockBelow);
    }

    protected boolean isInWater(Player player) {
        Material feet = player.getLocation().getBlock().getType();
        Material eyes = player.getEyeLocation().getBlock().getType();
        return feet == Material.WATER || feet == Material.BUBBLE_COLUMN || eyes == Material.WATER || eyes == Material.BUBBLE_COLUMN;
    }

    protected void addEffect(LivingEntity entity, PotionEffectType type, int durationSeconds, int amplifier) {
        entity.addPotionEffect(new PotionEffect(
                type,
                secondsToTicks(durationSeconds),
                amplifier,
                true,
                true,
                true
        ));
    }

    protected boolean tryCooldown(Player player, String key, String displayName, int cooldownSeconds) {
        return cooldowns.tryUse(player, "earth:" + tier().id() + ":" + key, displayName, cooldownSeconds);
    }

    protected int secondsToTicks(int seconds) {
        return Math.max(1, seconds) * 20;
    }

    protected int configInt(String path, int fallback) {
        return plugin.getConfig().getInt(path, fallback);
    }

    protected double configDouble(String path, double fallback) {
        return plugin.getConfig().getDouble(path, fallback);
    }

    protected boolean configBoolean(String path, boolean fallback) {
        return plugin.getConfig().getBoolean(path, fallback);
    }

    protected void send(Player player, String path) {
        player.sendMessage(plugin.messages().configMessage(path));
    }

    protected void send(Player player, String path, Map<String, String> placeholders) {
        player.sendMessage(plugin.messages().configMessage(path, placeholders));
    }
}
