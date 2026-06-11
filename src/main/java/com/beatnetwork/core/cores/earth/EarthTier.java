package com.beatnetwork.core.cores.earth;

import java.util.Arrays;
import java.util.Locale;
import java.util.Optional;

public enum EarthTier {

    NORMAL(0, "normal", "Base Earth Core", "Tier 0 - Base Earth Core", 10001),
    UPGRADE_2(1, "upgrade2", "Geo Warrior", "Tier 1 - Geo Warrior", 10002),
    UPGRADE_3(2, "upgrade3", "Ancient Titan", "Tier 2 - Ancient Titan", 10003);

    private final int level;
    private final String id;
    private final String displayName;
    private final String loreName;
    private final int modelId;

    EarthTier(int level, String id, String displayName, String loreName, int modelId) {
        this.level = level;
        this.id = id;
        this.displayName = displayName;
        this.loreName = loreName;
        this.modelId = modelId;
    }

    public int level() {
        return level;
    }

    public String id() {
        return id;
    }

    public String displayName() {
        return displayName;
    }

    public String loreName() {
        return loreName;
    }

    public int modelId() {
        return modelId;
    }

    public boolean isAtLeast(EarthTier other) {
        return level >= other.level;
    }

    public static Optional<EarthTier> fromLevel(int level) {
        return Arrays.stream(values())
                .filter(tier -> tier.level == level)
                .findFirst();
    }

    public static Optional<EarthTier> fromInput(String input) {
        String normalized = input.toLowerCase(Locale.ROOT).replace("-", "").replace("_", "").replace(" ", "");

        return switch (normalized) {
            case "normal", "base", "tier0", "0", "earth" -> Optional.of(NORMAL);
            case "upgrade2", "up2", "geo", "geowarrior", "tier1", "1" -> Optional.of(UPGRADE_2);
            case "upgrade3", "up3", "ancient", "ancienttitan", "max", "tier2", "2" -> Optional.of(UPGRADE_3);
            default -> Optional.empty();
        };
    }
}
