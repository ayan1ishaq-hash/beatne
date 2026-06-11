package com.beatnetwork.core.cores.earth.upgrade3;

import com.beatnetwork.core.CorePlugin;
import com.beatnetwork.core.cooldown.CooldownManager;
import com.beatnetwork.core.cores.earth.AbstractEarthCore;
import com.beatnetwork.core.cores.earth.EarthCoreManager;
import com.beatnetwork.core.cores.earth.EarthTargetUtil;
import com.beatnetwork.core.cores.earth.EarthTier;
import java.util.List;
import java.util.Map;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.potion.PotionEffectType;

public final class AncientTitanCore extends AbstractEarthCore {

    private static final BlockFace[] INFERNO_DIRECTIONS = {
            BlockFace.NORTH,
            BlockFace.EAST,
            BlockFace.SOUTH,
            BlockFace.WEST
    };

    public AncientTitanCore(CorePlugin plugin, EarthCoreManager manager, CooldownManager cooldowns) {
        super(plugin, manager, cooldowns);
    }

    @Override
    public EarthTier tier() {
        return EarthTier.UPGRADE_3;
    }

    @Override
    public void tickPassive(Player player) {
        // Stone Skin: Resistance II on earth blocks.
        if (configBoolean("abilities.earth.upgrade3.stone-skin.enabled", true)) {
            applyStoneSkin(player, 1);
        }

        // Tidal Armor passive: Water Breathing + Resistance II when in water.
        if (isInWater(player)) {
            int refreshSeconds = configInt("abilities.earth.passive-refresh-seconds", 3);
            addEffect(player, PotionEffectType.WATER_BREATHING, refreshSeconds, 0);
            addEffect(player, PotionEffectType.RESISTANCE, refreshSeconds, 1);
        }
    }

    @Override
    public void handleDamageTaken(Player player, EntityDamageEvent event) {
        // Lava Coat: attackers burn for 3s when hitting you.
        if (event instanceof EntityDamageByEntityEvent entityDamageByEntityEvent) {
            Entity attacker = EarthTargetUtil.resolveAttacker(entityDamageByEntityEvent.getDamager());

            if (attacker instanceof LivingEntity livingAttacker && EarthTargetUtil.isValidEnemy(player, livingAttacker)) {
                int fireSeconds = configInt("abilities.earth.upgrade3.lava-coat.fire-seconds", 3);
                livingAttacker.setFireTicks(Math.max(livingAttacker.getFireTicks(), secondsToTicks(fireSeconds)));
            }
        }

        // Inferno Ground: fire spawns in 4 directions when you take any damage.
        spawnInfernoGround(player);
    }

    @Override
    public void handleActive(Player player) {
        // Tier 2 active uses a Stone Sword instead of the core paper.
        send(player, "messages.abilities.earth.tidal-armor-use-stone-sword");
    }

    public void handleStoneSwordActive(Player player) {
        int cooldown = configInt("abilities.earth.upgrade3.tidal-armor.cooldown-seconds", 20);
        int duration = configInt("abilities.earth.upgrade3.tidal-armor.duration-seconds", 10);

        if (!tryCooldown(player, "tidal-armor", "Tidal Armor", cooldown)) {
            return;
        }

        // Tidal Armor active: Resistance III + Regeneration II.
        addEffect(player, PotionEffectType.RESISTANCE, duration, 2);
        addEffect(player, PotionEffectType.REGENERATION, duration, 1);
        send(player, "messages.abilities.earth.tidal-armor", Map.of("duration", String.valueOf(duration)));
    }

    @Override
    public void handleUltimate(Player player) {
        int cooldown = configInt("abilities.earth.upgrade3.earthquake.cooldown-seconds", 30);
        double radius = configDouble("abilities.earth.upgrade3.earthquake.radius", 8.0D);
        int duration = configInt("abilities.earth.upgrade3.earthquake.duration-seconds", 4);

        List<LivingEntity> enemies = EarthTargetUtil.nearbyEnemies(player, radius);

        if (enemies.isEmpty()) {
            send(player, "messages.abilities.no-enemies");
            return;
        }

        if (!tryCooldown(player, "earthquake", "Earthquake", cooldown)) {
            return;
        }

        for (LivingEntity enemy : enemies) {
            addEffect(enemy, PotionEffectType.BLINDNESS, duration, 0);
            addEffect(enemy, PotionEffectType.SLOWNESS, duration, 3);
            addEffect(enemy, PotionEffectType.LEVITATION, duration, 0);
        }

        send(player, "messages.abilities.earth.earthquake", Map.of("count", String.valueOf(enemies.size())));
    }

    private void spawnInfernoGround(Player player) {
        if (!configBoolean("abilities.earth.upgrade3.inferno-ground.place-fire", true)) {
            return;
        }

        int fireSeconds = configInt("abilities.earth.upgrade3.inferno-ground.fire-seconds", 5);
        Block center = player.getLocation().getBlock();

        for (BlockFace face : INFERNO_DIRECTIONS) {
            Block fireBlock = center.getRelative(face);

            if (!canPlaceTemporaryFire(fireBlock)) {
                continue;
            }

            fireBlock.setType(Material.FIRE, false);

            plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                if (fireBlock.getType() == Material.FIRE) {
                    fireBlock.setType(Material.AIR, false);
                }
            }, secondsToTicks(fireSeconds));
        }
    }

    private boolean canPlaceTemporaryFire(Block block) {
        if (block.getType() != Material.AIR && block.getType() != Material.CAVE_AIR && block.getType() != Material.VOID_AIR) {
            return false;
        }

        return block.getRelative(BlockFace.DOWN).getType().isSolid();
    }
}
