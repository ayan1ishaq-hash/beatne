package com.beatnetwork.core;

import com.beatnetwork.core.command.CoreCommand;
import com.beatnetwork.core.cores.earth.EarthCoreManager;
import com.beatnetwork.core.listener.PlayerJoinListener;
import com.beatnetwork.core.util.MessageUtil;
import java.util.List;
import org.bukkit.plugin.java.JavaPlugin;

public final class CorePlugin extends JavaPlugin {

    private static CorePlugin instance;

    private MessageUtil messages;
    private EarthCoreManager earthCoreManager;

    @Override
    public void onEnable() {
        instance = this;

        saveDefaultConfig();
        messages = new MessageUtil(this);
        earthCoreManager = new EarthCoreManager(this);

        registerCommands();
        registerListeners();

        earthCoreManager.startPassiveTask();

        getLogger().info("Core v" + getPluginMeta().getVersion() + " has been enabled.");
        debug("Debug mode is enabled.");
    }

    @Override
    public void onDisable() {
        if (earthCoreManager != null) {
            earthCoreManager.shutdown();
        }

        getLogger().info("Core has been disabled.");
        instance = null;
    }

    private void registerCommands() {
        registerCommand(
                "core",
                "Main BeatNetwork Core command.",
                List.of("coreplugin"),
                new CoreCommand(this)
        );
    }

    private void registerListeners() {
        getServer().getPluginManager().registerEvents(new PlayerJoinListener(this), this);
        getServer().getPluginManager().registerEvents(earthCoreManager, this);
    }

    public void reloadCore() {
        reloadConfig();
        messages = new MessageUtil(this);
    }

    public MessageUtil messages() {
        return messages;
    }

    public EarthCoreManager earthCores() {
        return earthCoreManager;
    }

    public boolean isDebugEnabled() {
        return getConfig().getBoolean("settings.debug", false);
    }

    public void debug(String message) {
        if (isDebugEnabled()) {
            getLogger().info("[Debug] " + message);
        }
    }

    public static CorePlugin getInstance() {
        return instance;
    }
}
