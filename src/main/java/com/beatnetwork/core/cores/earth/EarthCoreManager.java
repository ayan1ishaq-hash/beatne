package com.beatnetwork.core.cores.earth;

import com.beatnetwork.core.CorePlugin;
import com.beatnetwork.core.cooldown.CooldownManager;
import com.beatnetwork.core.cores.earth.normal.NormalEarthCore;
import com.beatnetwork.core.cores.earth.upgrade2.GeoWarriorCore;
import com.beatnetwork.core.cores.earth.upgrade3.AncientTitanCore;
import java.util.List;
import java.util.Optional;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.components.CustomModelDataComponent;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitTask;

public final class EarthCoreManager implements Listener {

    private final CorePlugin plugin;
    private final NamespacedKey coreTypeKey;
    private final NamespacedKey coreTierKey;
    private final CooldownManager cooldowns;
    private final NormalEarthCore normalCore;
    private final GeoWarriorCore geoWarriorCore;
    private final AncientTitanCore ancientTitanCore;

    private BukkitTask passiveTask;

    public EarthCoreManager(CorePlugin plugin) {
        this.plugin = plugin;
        this.coreTypeKey = new NamespacedKey(plugin, "core_type");
        this.coreTierKey = new NamespacedKey(plugin, "core_tier");
        this.cooldowns = new CooldownManager(plugin);
        this.normalCore = new NormalEarthCore(plugin, this, cooldowns);
        this.geoWarriorCore = new GeoWarriorCore(plugin, this, cooldowns);
        this.ancientTitanCore = new AncientTitanCore(plugin, this, cooldowns);
    }

    public void startPassiveTask() {
        shutdownPassiveTaskOnly();

        passiveTask = plugin.getServer().getScheduler().runTaskTimer(plugin, () -> {
            for (Player player : plugin.getServer().getOnlinePlayers()) {
                getOffhandTier(player).ifPresent(tier -> abilityFor(tier).tickPassive(player));
            }
        }, 20L, 20L);
    }

    public void shutdown() {
        shutdownPassiveTaskOnly();
        cooldowns.clear();
    }

    private void shutdownPassiveTaskOnly() {
        if (passiveTask != null) {
            passiveTask.cancel();
            passiveTask = null;
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (!isRightClick(event.getAction()) || event.getHand() == null) {
            return;
        }

        Player player = event.getPlayer();
        ItemStack usedItem = event.getItem();

        // Ancient Titan active is special: it is used with any Stone Sword while the
        // Ancient Titan Earth Core paper is held in the off hand.
        if (!player.isSneaking()
                && event.getHand() == EquipmentSlot.HAND
                && usedItem != null
                && usedItem.getType() == Material.STONE_SWORD
                && getOffhandTier(player).filter(tier -> tier == EarthTier.UPGRADE_3).isPresent()) {
            event.setCancelled(true);
            ancientTitanCore.handleStoneSwordActive(player);
            return;
        }

        Optional<EarthTier> usedCoreTier = getEarthTier(usedItem);

        if (usedCoreTier.isEmpty()) {
            return;
        }

        // Prevent an off-hand core from firing accidentally while the player is using
        // a normal main-hand item. With an empty main hand, off-hand paper still works.
        if (!player.isSneaking()
                && event.getHand() == EquipmentSlot.OFF_HAND
                && player.getInventory().getItemInMainHand().getType() != Material.AIR) {
            return;
        }

        event.setCancelled(true);

        if (player.isSneaking()) {
            abilityFor(usedCoreTier.get()).handleUltimate(player);
        } else {
            abilityFor(usedCoreTier.get()).handleActive(player);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerDamaged(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }

        getOffhandTier(player).ifPresent(tier -> abilityFor(tier).handleDamageTaken(player, event));
    }

    public ItemStack createCoreItem(EarthTier tier) {
        ItemStack item = new ItemStack(Material.PAPER);
        ItemMeta meta = item.getItemMeta();

        meta.displayName(displayNameFor(tier));
        meta.lore(loreFor(tier));
        applyModelId(meta, tier.modelId());

        PersistentDataContainer data = meta.getPersistentDataContainer();
        data.set(coreTypeKey, PersistentDataType.STRING, "earth");
        data.set(coreTierKey, PersistentDataType.INTEGER, tier.level());

        item.setItemMeta(meta);
        return item;
    }

    private void applyModelId(ItemMeta meta, int modelId) {
        CustomModelDataComponent customModelData = meta.getCustomModelDataComponent();
        customModelData.setFloats(List.of((float) modelId));
        meta.setCustomModelDataComponent(customModelData);
    }

    public Optional<EarthTier> getOffhandTier(Player player) {
        return getEarthTier(player.getInventory().getItemInOffHand());
    }

    public Optional<EarthTier> getEarthTier(ItemStack item) {
        if (item == null || item.getType() != Material.PAPER || !item.hasItemMeta()) {
            return Optional.empty();
        }

        ItemMeta meta = item.getItemMeta();
        PersistentDataContainer data = meta.getPersistentDataContainer();
        String coreType = data.get(coreTypeKey, PersistentDataType.STRING);
        Integer tierLevel = data.get(coreTierKey, PersistentDataType.INTEGER);

        if (!"earth".equalsIgnoreCase(coreType) || tierLevel == null) {
            return Optional.empty();
        }

        return EarthTier.fromLevel(tierLevel);
    }

    private AbstractEarthCore abilityFor(EarthTier tier) {
        return switch (tier) {
            case NORMAL -> normalCore;
            case UPGRADE_2 -> geoWarriorCore;
            case UPGRADE_3 -> ancientTitanCore;
        };
    }

    private Component displayNameFor(EarthTier tier) {
        return switch (tier) {
            case NORMAL -> plugin.messages().deserialize("<green><bold>Earth Core</bold> <gray>(Normal)</gray>");
            case UPGRADE_2 -> plugin.messages().deserialize("<dark_green><bold>Earth Core</bold> <gray>(Upgrade 2)</gray>");
            case UPGRADE_3 -> plugin.messages().deserialize("<gold><bold>Earth Core</bold> <gray>(Upgrade 3)</gray>");
        };
    }

    private List<Component> loreFor(EarthTier tier) {
        return switch (tier) {
            case NORMAL -> List.of(
                    plugin.messages().deserialize("<gray>" + tier.loreName() + "</gray>"),
                    plugin.messages().deserialize("<green>Passive:</green> Stone Skin"),
                    plugin.messages().deserialize("<yellow>Right Click:</yellow> Ember Touch"),
                    plugin.messages().deserialize("<gold>Sneak + Right Click:</gold> Water Shield"),
                    plugin.messages().deserialize("<dark_gray>Model ID: " + tier.modelId() + "</dark_gray>"),
                    plugin.messages().deserialize("<dark_gray>Hold in off hand for passives.</dark_gray>")
            );
            case UPGRADE_2 -> List.of(
                    plugin.messages().deserialize("<gray>" + tier.loreName() + "</gray>"),
                    plugin.messages().deserialize("<green>Passive:</green> Stone Skin II, Lava Coat"),
                    plugin.messages().deserialize("<yellow>Right Click:</yellow> Rock Slam"),
                    plugin.messages().deserialize("<gold>Sneak + Right Click:</gold> Mud Trap"),
                    plugin.messages().deserialize("<dark_gray>Model ID: " + tier.modelId() + "</dark_gray>"),
                    plugin.messages().deserialize("<dark_gray>Hold in off hand for passives.</dark_gray>")
            );
            case UPGRADE_3 -> List.of(
                    plugin.messages().deserialize("<gray>" + tier.loreName() + "</gray>"),
                    plugin.messages().deserialize("<green>Passive:</green> Stone Skin II, Lava Coat,"),
                    plugin.messages().deserialize("<green>Passive:</green> Inferno Ground, Tidal Armor"),
                    plugin.messages().deserialize("<yellow>Right Click Stone Sword:</yellow> Tidal Armor"),
                    plugin.messages().deserialize("<gold>Sneak + Right Click Paper:</gold> Earthquake"),
                    plugin.messages().deserialize("<dark_gray>Model ID: " + tier.modelId() + "</dark_gray>"),
                    plugin.messages().deserialize("<dark_gray>Hold in off hand for passives.</dark_gray>")
            );
        };
    }

    private boolean isRightClick(Action action) {
        return action == Action.RIGHT_CLICK_AIR || action == Action.RIGHT_CLICK_BLOCK;
    }
}
