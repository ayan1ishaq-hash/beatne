package com.beatnetwork.core.cores.earth;

import java.util.EnumSet;
import java.util.Set;
import org.bukkit.Material;

public final class EarthBlockUtil {

    private static final Set<Material> EARTH_BLOCKS = EnumSet.of(
            Material.GRASS_BLOCK,
            Material.DIRT,
            Material.COARSE_DIRT,
            Material.ROOTED_DIRT,
            Material.PODZOL,
            Material.MYCELIUM,
            Material.MUD,
            Material.MUDDY_MANGROVE_ROOTS,
            Material.CLAY,
            Material.GRAVEL,
            Material.SAND,
            Material.RED_SAND,
            Material.STONE,
            Material.COBBLESTONE,
            Material.MOSSY_COBBLESTONE,
            Material.ANDESITE,
            Material.DIORITE,
            Material.GRANITE,
            Material.DEEPSLATE,
            Material.COBBLED_DEEPSLATE,
            Material.TUFF,
            Material.CALCITE,
            Material.DRIPSTONE_BLOCK,
            Material.SANDSTONE,
            Material.RED_SANDSTONE,
            Material.MOSS_BLOCK
    );

    private EarthBlockUtil() {
    }

    public static boolean isEarthBlock(Material material) {
        return EARTH_BLOCKS.contains(material);
    }
}
