# BeatNetwork Core

Paper server core plugin for **BeatNetwork** with the first elemental core: **Earth Core**.

## Project setup

- Plugin name: `Core`
- Server/project name: `BeatNetwork`
- Plugin version: `26.1.2`
- Main class: `com.beatnetwork.core.CorePlugin`
- Group: `com.beatnetwork`
- Java/toolchain: `25`
- Paper API dependency: `io.papermc.paper:paper-api:26.1.2.build.+`
- GitHub Actions workflow: `.github/workflows/build.yml`

## Folder layout for cores

Each core gets its own folder, and inside that core are the upgrade folders:

```text
src/main/java/com/beatnetwork/core/cores/
└── earth/
    ├── normal/
    │   └── NormalEarthCore.java
    ├── upgrade2/
    │   └── GeoWarriorCore.java
    └── upgrade3/
        └── AncientTitanCore.java
```

This keeps the setup ready for future cores like fire, water, air, etc.

## Commands

```text
/core help
/core reload
/core version
/core give earth normal [player]
/core give earth upgrade2 [player]
/core give earth upgrade3 [player]
```

Permission:

```text
core.admin
```

## Earth Core abilities

Earth Core items are custom `PAPER` items tagged with persistent data. Use `/core give earth ...` to get them.

### Normal / Tier 0 - Base Earth Core

Passive, while holding the core paper in off hand:

- **Stone Skin**: Resistance I while standing on earth blocks.

Right click with the core paper:

- **Ember Touch**: Sets a target enemy on fire.
- Cooldown: 10s
- Range: 5 blocks

Sneak + right click with the core paper:

- **Water Shield**: Resistance II + Slowness II.
- Cooldown: 20s
- Duration: 8s

### Upgrade 2 / Tier 1 - Geo Warrior

Passive, while holding the core paper in off hand:

- **Stone Skin**: Resistance II while standing on earth blocks.
- **Lava Coat**: Attackers catch fire when they hit you.

Right click with the core paper:

- **Rock Slam**: Launches nearby enemies upward and gives Slowness II.
- Cooldown: 15s
- Radius: 5 blocks

Sneak + right click with the core paper:

- **Mud Trap**: Gives nearby enemies Slowness III.
- Cooldown: 25s
- Duration: 5s
- Radius: 6 blocks

### Upgrade 3 / Tier 2 - Ancient Titan

Passive, while holding the core paper in off hand:

- **Stone Skin**: Resistance II while standing on earth blocks.
- **Lava Coat**: Attackers burn for 3s when hitting you.
- **Inferno Ground**: Temporary fire spawns north/east/south/west when you take damage.
- **Tidal Armor passive**: Water Breathing + Resistance II while in water.

Right click with a stone sword while holding the Upgrade 3 Earth Core paper in off hand:

- **Tidal Armor active**: Resistance III + Regeneration II.
- Cooldown: 20s
- Duration: 10s

Sneak + right click with the core paper:

- **Earthquake**: Blindness I + Slowness IV + Levitation I to nearby enemies.
- Cooldown: 30s
- Duration: 4s
- Radius: 8 blocks

## Config

Ability cooldowns, durations, ranges, messages, and Inferno Ground fire placement are in:

```text
src/main/resources/config.yml
```

## Local build

Use Java 25.

```bash
gradle build
```

The plugin jar will be created at:

```text
build/libs/BeatNetwork-Core-26.1.2.jar
```

Put that jar into your Paper server's `plugins/` folder and restart the server.

## GitHub build

Push this project to GitHub. The workflow at `.github/workflows/build.yml` will build the jar automatically on push, pull request, or manual workflow run.

After the workflow finishes, download the jar from the workflow artifacts named:

```text
BeatNetwork-Core-jar
```
