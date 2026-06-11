package com.beatnetwork.core.util;

import java.util.Map;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.plugin.java.JavaPlugin;

public final class MessageUtil {

    private final JavaPlugin plugin;
    private final MiniMessage miniMessage;

    public MessageUtil(JavaPlugin plugin) {
        this.plugin = plugin;
        this.miniMessage = MiniMessage.miniMessage();
    }

    public Component configMessage(String path) {
        return configMessage(path, Map.of(), true);
    }

    public Component configMessage(String path, boolean includePrefix) {
        return configMessage(path, Map.of(), includePrefix);
    }

    public Component configMessage(String path, Map<String, String> placeholders) {
        return configMessage(path, placeholders, true);
    }

    public Component configMessage(String path, Map<String, String> placeholders, boolean includePrefix) {
        String message = plugin.getConfig().getString(path);

        if (message == null || message.isBlank()) {
            message = "<red>Missing message: <white>" + path + "</white></red>";
        }

        if (includePrefix) {
            message = prefix() + message;
        }

        return deserialize(message, placeholders);
    }

    public Component deserialize(String message) {
        return deserialize(message, Map.of());
    }

    public Component deserialize(String message, Map<String, String> placeholders) {
        TagResolver.Builder resolverBuilder = TagResolver.builder();

        placeholders.forEach((key, value) -> resolverBuilder.resolver(Placeholder.unparsed(key, value)));

        return miniMessage.deserialize(message, resolverBuilder.build());
    }

    public String prefix() {
        return plugin.getConfig().getString("messages.prefix", "");
    }
}
