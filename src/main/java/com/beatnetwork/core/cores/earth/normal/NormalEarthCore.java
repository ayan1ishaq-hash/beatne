package com.beatnetwork.core.cores.earth.normal;

import com.beatnetwork.core.CorePlugin;
import com.beatnetwork.core.cooldown.CooldownManager;
import com.beatnetwork.core.cores.earth.AbstractEarthCore;
import com.beatnetwork.core.cores.earth.EarthCoreManager;
import com.beatnetwork.core.cores.earth.EarthTargetUtil;
import com.beatnetwork.core.cores.earth.EarthTier;
import java.util.Map;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;

public final class NormalEarthCore extends AbstractEarthCore {

    public NormalEarthCore(CorePlugin plugin, EarthCoreManager manager, CooldownManager cooldowns) {
        super(plugin, manager, cooldowns);
    }

    @Override
    public EarthTier tier() {
        return EarthTier.NORMAL;
    }

    @Override
    public void tickPassive(Player player) {
        // Stone Skin: Resistance I on dirt/stone/grass and other earth blocks.
        if (configBoolean("abilities.earth.normal.stone-skin.enabled", true)) {
            applyStoneSkin(player, 0);
        }
    }

    @Override
    public void handleActive(Player player) {
        int cooldown = configInt("abilities.earth.normal.ember-touch.cooldown-seconds", 10);
        double range = configDouble("abilities.earth.normal.ember-touch.range", 5.0D);
        int fireSeconds = configInt("abilities.earth.normal.ember-touch.fire-seconds", 5);

        LivingEntity target = EarthTargetUtil.rayTraceEnemy(player, range);

        if (target == null) {
            send(player, "messages.abilities.no-target");
            return;
        }

        if (!tryCooldown(player, "ember-touch", "Ember Touch", cooldown)) {
            return;
        }

        target.setFireTicks(Math.max(target.getFireTicks(), secondsToTicks(fireSeconds)));
        send(player, "messages.abilities.earth.ember-touch", Map.of("target", target.getName()));
    }

    @Override
    public void handleUltimate(Player player) {
        int cooldown = configInt("abilities.earth.normal.water-shield.cooldown-seconds", 20);
        int duration = configInt("abilities.earth.normal.water-shield.duration-seconds", 8);

        if (!tryCooldown(player, "water-shield", "Water Shield", cooldown)) {
            return;
        }

        // Water Shield: Resistance II + Slowness II.
        addEffect(player, PotionEffectType.RESISTANCE, duration, 1);
        addEffect(player, PotionEffectType.SLOWNESS, duration, 1);
        send(player, "messages.abilities.earth.water-shield", Map.of("duration", String.valueOf(duration)));
    }
}
