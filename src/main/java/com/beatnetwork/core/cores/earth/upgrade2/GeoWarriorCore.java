package com.beatnetwork.core.cores.earth.upgrade2;

import com.beatnetwork.core.CorePlugin;
import com.beatnetwork.core.cooldown.CooldownManager;
import com.beatnetwork.core.cores.earth.AbstractEarthCore;
import com.beatnetwork.core.cores.earth.EarthCoreManager;
import com.beatnetwork.core.cores.earth.EarthTargetUtil;
import com.beatnetwork.core.cores.earth.EarthTier;
import java.util.List;
import java.util.Map;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

public final class GeoWarriorCore extends AbstractEarthCore {

    public GeoWarriorCore(CorePlugin plugin, EarthCoreManager manager, CooldownManager cooldowns) {
        super(plugin, manager, cooldowns);
    }

    @Override
    public EarthTier tier() {
        return EarthTier.UPGRADE_2;
    }

    @Override
    public void tickPassive(Player player) {
        // Stone Skin: Resistance II on earth blocks.
        if (configBoolean("abilities.earth.upgrade2.stone-skin.enabled", true)) {
            applyStoneSkin(player, 1);
        }
    }

    @Override
    public void handleDamageTaken(Player player, EntityDamageEvent event) {
        if (!(event instanceof EntityDamageByEntityEvent entityDamageByEntityEvent)) {
            return;
        }

        // Lava Coat: attackers catch fire when they hit you.
        Entity attacker = EarthTargetUtil.resolveAttacker(entityDamageByEntityEvent.getDamager());

        if (attacker instanceof LivingEntity livingAttacker && EarthTargetUtil.isValidEnemy(player, livingAttacker)) {
            int fireSeconds = configInt("abilities.earth.upgrade2.lava-coat.fire-seconds", 3);
            livingAttacker.setFireTicks(Math.max(livingAttacker.getFireTicks(), secondsToTicks(fireSeconds)));
        }
    }

    @Override
    public void handleActive(Player player) {
        int cooldown = configInt("abilities.earth.upgrade2.rock-slam.cooldown-seconds", 15);
        double radius = configDouble("abilities.earth.upgrade2.rock-slam.radius", 5.0D);
        int slownessSeconds = configInt("abilities.earth.upgrade2.rock-slam.slowness-seconds", 4);
        double upwardVelocity = configDouble("abilities.earth.upgrade2.rock-slam.upward-velocity", 1.1D);
        double outwardVelocity = configDouble("abilities.earth.upgrade2.rock-slam.outward-velocity", 0.35D);

        List<LivingEntity> enemies = EarthTargetUtil.nearbyEnemies(player, radius);

        if (enemies.isEmpty()) {
            send(player, "messages.abilities.no-enemies");
            return;
        }

        if (!tryCooldown(player, "rock-slam", "Rock Slam", cooldown)) {
            return;
        }

        for (LivingEntity enemy : enemies) {
            Vector direction = enemy.getLocation().toVector().subtract(player.getLocation().toVector());

            if (direction.lengthSquared() > 0.0D) {
                direction.normalize().multiply(outwardVelocity);
            }

            direction.setY(upwardVelocity);
            enemy.setVelocity(direction);
            addEffect(enemy, PotionEffectType.SLOWNESS, slownessSeconds, 1);
        }

        send(player, "messages.abilities.earth.rock-slam", Map.of("count", String.valueOf(enemies.size())));
    }

    @Override
    public void handleUltimate(Player player) {
        int cooldown = configInt("abilities.earth.upgrade2.mud-trap.cooldown-seconds", 25);
        double radius = configDouble("abilities.earth.upgrade2.mud-trap.radius", 6.0D);
        int duration = configInt("abilities.earth.upgrade2.mud-trap.duration-seconds", 5);

        List<LivingEntity> enemies = EarthTargetUtil.nearbyEnemies(player, radius);

        if (enemies.isEmpty()) {
            send(player, "messages.abilities.no-enemies");
            return;
        }

        if (!tryCooldown(player, "mud-trap", "Mud Trap", cooldown)) {
            return;
        }

        for (LivingEntity enemy : enemies) {
            addEffect(enemy, PotionEffectType.SLOWNESS, duration, 2);
        }

        send(player, "messages.abilities.earth.mud-trap", Map.of("count", String.valueOf(enemies.size())));
    }
}
