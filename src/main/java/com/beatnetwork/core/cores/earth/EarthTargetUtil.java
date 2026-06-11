package com.beatnetwork.core.cores.earth;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.bukkit.FluidCollisionMode;
import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.projectiles.ProjectileSource;
import org.bukkit.util.RayTraceResult;

public final class EarthTargetUtil {

    private EarthTargetUtil() {
    }

    public static List<LivingEntity> nearbyEnemies(Player player, double radius) {
        List<LivingEntity> enemies = new ArrayList<>();
        Collection<Entity> nearby = player.getWorld().getNearbyEntities(player.getLocation(), radius, radius, radius);

        for (Entity entity : nearby) {
            if (entity instanceof LivingEntity livingEntity && isValidEnemy(player, livingEntity)) {
                if (livingEntity.getLocation().distanceSquared(player.getLocation()) <= radius * radius) {
                    enemies.add(livingEntity);
                }
            }
        }

        return enemies;
    }

    public static LivingEntity rayTraceEnemy(Player player, double range) {
        Location eye = player.getEyeLocation();
        double maxDistance = range;

        RayTraceResult blockResult = player.getWorld().rayTraceBlocks(
                eye,
                eye.getDirection(),
                range,
                FluidCollisionMode.NEVER,
                true
        );

        if (blockResult != null && blockResult.getHitPosition() != null) {
            maxDistance = Math.max(0.0D, blockResult.getHitPosition().distance(eye.toVector()));
        }

        RayTraceResult entityResult = player.getWorld().rayTraceEntities(
                eye,
                eye.getDirection(),
                maxDistance,
                0.5D,
                entity -> entity instanceof LivingEntity livingEntity && isValidEnemy(player, livingEntity)
        );

        if (entityResult == null || !(entityResult.getHitEntity() instanceof LivingEntity livingEntity)) {
            return null;
        }

        return livingEntity;
    }

    public static Entity resolveAttacker(Entity damager) {
        if (damager instanceof org.bukkit.entity.Projectile projectile) {
            ProjectileSource source = projectile.getShooter();
            return source instanceof Entity entity ? entity : null;
        }

        return damager;
    }

    public static boolean isValidEnemy(Player player, LivingEntity entity) {
        if (entity.equals(player) || entity.isDead() || !entity.isValid()) {
            return false;
        }

        return !(entity instanceof ArmorStand);
    }
}
