package com.beatnetwork.core.command;

import com.beatnetwork.core.CorePlugin;
import com.beatnetwork.core.cores.earth.EarthTier;
import io.papermc.paper.command.brigadier.BasicCommand;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public final class CoreCommand implements BasicCommand {

    private static final List<String> SUBCOMMANDS = List.of("help", "reload", "version", "give");
    private static final List<String> CORE_TYPES = List.of("earth");
    private static final List<String> EARTH_TIERS = List.of("normal", "upgrade2", "upgrade3");

    private final CorePlugin plugin;

    public CoreCommand(CorePlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void execute(CommandSourceStack source, String[] args) {
        CommandSender sender = source.getSender();

        if (args.length == 0 || args[0].equalsIgnoreCase("help")) {
            sendHelp(sender);
            return;
        }

        switch (args[0].toLowerCase(Locale.ROOT)) {
            case "reload" -> handleReload(sender);
            case "version" -> handleVersion(sender);
            case "give" -> handleGive(sender, args);
            default -> sender.sendMessage(plugin.messages().configMessage("messages.unknown-command"));
        }
    }

    private void sendHelp(CommandSender sender) {
        sender.sendMessage(plugin.messages().configMessage("messages.help.header", false));
        sender.sendMessage(plugin.messages().configMessage("messages.help.reload", false));
        sender.sendMessage(plugin.messages().configMessage("messages.help.version", false));
        sender.sendMessage(plugin.messages().configMessage("messages.help.give", false));
    }

    private void handleReload(CommandSender sender) {
        if (!sender.hasPermission("core.admin")) {
            sender.sendMessage(plugin.messages().configMessage("messages.no-permission"));
            return;
        }

        plugin.reloadCore();
        sender.sendMessage(plugin.messages().configMessage("messages.reloaded"));
    }

    private void handleVersion(CommandSender sender) {
        sender.sendMessage(plugin.messages().configMessage(
                "messages.version",
                Map.of("version", plugin.getPluginMeta().getVersion())
        ));
    }

    private void handleGive(CommandSender sender, String[] args) {
        if (!sender.hasPermission("core.admin")) {
            sender.sendMessage(plugin.messages().configMessage("messages.no-permission"));
            return;
        }

        if (args.length < 3 || !args[1].equalsIgnoreCase("earth")) {
            sender.sendMessage(plugin.messages().configMessage("messages.commands.give-usage"));
            return;
        }

        EarthTier tier = EarthTier.fromInput(args[2]).orElse(null);

        if (tier == null) {
            sender.sendMessage(plugin.messages().configMessage("messages.commands.invalid-earth-tier"));
            return;
        }

        Player target;

        if (args.length >= 4) {
            target = Bukkit.getPlayerExact(args[3]);
        } else if (sender instanceof Player player) {
            target = player;
        } else {
            sender.sendMessage(plugin.messages().configMessage("messages.commands.player-required"));
            return;
        }

        if (target == null) {
            sender.sendMessage(plugin.messages().configMessage("messages.commands.player-not-found"));
            return;
        }

        ItemStack item = plugin.earthCores().createCoreItem(tier);
        Map<Integer, ItemStack> leftovers = target.getInventory().addItem(item);

        leftovers.values().forEach(leftover -> target.getWorld().dropItemNaturally(target.getLocation(), leftover));

        sender.sendMessage(plugin.messages().configMessage(
                "messages.commands.given-earth-core",
                Map.of(
                        "player", target.getName(),
                        "tier", tier.displayName(),
                        "model", String.valueOf(tier.modelId())
                )
        ));

        if (!sender.equals(target)) {
            target.sendMessage(plugin.messages().configMessage(
                    "messages.commands.received-earth-core",
                    Map.of(
                            "tier", tier.displayName(),
                            "model", String.valueOf(tier.modelId())
                    )
            ));
        }
    }

    @Override
    public Collection<String> suggest(CommandSourceStack source, String[] args) {
        if (args.length == 1) {
            return filterStartsWith(SUBCOMMANDS, args[0]);
        }

        if (args.length == 2 && args[0].equalsIgnoreCase("give")) {
            return filterStartsWith(CORE_TYPES, args[1]);
        }

        if (args.length == 3 && args[0].equalsIgnoreCase("give") && args[1].equalsIgnoreCase("earth")) {
            return filterStartsWith(EARTH_TIERS, args[2]);
        }

        if (args.length == 4 && args[0].equalsIgnoreCase("give")) {
            return filterStartsWith(Bukkit.getOnlinePlayers().stream().map(Player::getName).toList(), args[3]);
        }

        return List.of();
    }

    private List<String> filterStartsWith(List<String> options, String input) {
        String normalizedInput = input.toLowerCase(Locale.ROOT);
        List<String> matches = new ArrayList<>();

        for (String option : options) {
            if (option.toLowerCase(Locale.ROOT).startsWith(normalizedInput)) {
                matches.add(option);
            }
        }

        return matches;
    }
}
