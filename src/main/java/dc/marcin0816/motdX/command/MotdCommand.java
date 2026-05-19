package dc.marcin0816.motdX.command;

import dc.marcin0816.motdX.MotdX;
import dc.marcin0816.motdX.model.MotdFrame;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.stream.Stream;

public class MotdCommand implements CommandExecutor, TabCompleter {

    private final MotdX plugin;
    private final MiniMessage mm = MiniMessage.miniMessage();

    public MotdCommand(MotdX plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String @NotNull [] args) {
        if (args.length == 0) {
            sendHelp(sender);
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "reload" -> {
                if (!sender.hasPermission("motdx.reload")) { noPerms(sender); return true; }
                plugin.reloadConfig();
                plugin.getMotdManager().reload();
                plugin.getActionBarManager().reload();
                plugin.getIconManager().reload();
                sender.sendMessage(mm.deserialize(
                        "<green>MotdX reloaded! Loaded <yellow>" + plugin.getMotdManager().getFrameCount() +
                        "<green> MOTD frames, <yellow>" + plugin.getIconManager().getIconCount() + "<green> icon(s)."
                ));
            }
            case "maintenance" -> {
                if (!sender.hasPermission("motdx.maintenance")) { noPerms(sender); return true; }
                var manager = plugin.getMotdManager();
                boolean now = !manager.isMaintenanceActive();
                manager.setMaintenanceActive(now);
                sender.sendMessage(mm.deserialize(now
                        ? "<yellow>Maintenance mode: <red>ENABLED"
                        : "<yellow>Maintenance mode: <green>DISABLED"
                ));
            }
            case "info" -> {
                if (!sender.hasPermission("motdx.info")) { noPerms(sender); return true; }
                var m = plugin.getMotdManager();
                sender.sendMessage(mm.deserialize(
                        "<gold><bold>MotdX<reset> <dark_gray>| <gray>Info<newline>" +
                        "<dark_gray>• <gray>Frames: <yellow>" + m.getFrameCount() + "<newline>" +
                        "<dark_gray>• <gray>Mode: <yellow>" + m.getMode() + "<newline>" +
                        "<dark_gray>• <gray>Maintenance: <yellow>" + m.isMaintenanceActive() + "<newline>" +
                        "<dark_gray>• <gray>Icons: <yellow>" + plugin.getIconManager().getIconCount() + "<newline>" +
                        "<dark_gray>• <gray>PlaceholderAPI: <yellow>" + plugin.hasPapi()
                ));
            }
            case "preview" -> {
                if (!sender.hasPermission("motdx.preview")) { noPerms(sender); return true; }
                MotdFrame frame = plugin.getMotdManager().peekCurrent();
                if (frame == null) {
                    sender.sendMessage(mm.deserialize("<red>No MOTD frames loaded."));
                    return true;
                }
                int online = Bukkit.getOnlinePlayers().size();
                int max = Bukkit.getMaxPlayers();
                String line1 = frame.line1().replace("{online}", String.valueOf(online)).replace("{max}", String.valueOf(max));
                String line2 = frame.line2().replace("{online}", String.valueOf(online)).replace("{max}", String.valueOf(max));
                sender.sendMessage(mm.deserialize(
                        "<gold><bold>MotdX<reset> <dark_gray>| <gray>Preview<newline>" +
                        "<dark_gray>┌ <reset>" + line1 + "<newline>" +
                        "<dark_gray>└ <reset>" + line2
                ));
            }
            case "set" -> {
                if (!sender.hasPermission("motdx.set")) { noPerms(sender); return true; }
                if (args.length < 2) {
                    sender.sendMessage(mm.deserialize("<red>Usage: /motdx set <sequential|random|shuffle>"));
                    return true;
                }
                String newMode = args[1].toLowerCase();
                if (!List.of("sequential", "random", "shuffle").contains(newMode)) {
                    sender.sendMessage(mm.deserialize("<red>Unknown mode. Use: <yellow>sequential<red>, <yellow>random<red>, <yellow>shuffle"));
                    return true;
                }
                plugin.getMotdManager().setMode(newMode);
                sender.sendMessage(mm.deserialize("<green>Animation mode set to: <yellow>" + newMode));
            }
            default -> sendHelp(sender);
        }
        return true;
    }

    private void sendHelp(CommandSender sender) {
        sender.sendMessage(mm.deserialize(
                "<gold><bold>MotdX<reset> <dark_gray>| <gray>Commands<newline>" +
                "<yellow>/motdx reload <dark_gray>- <gray>Reload config<newline>" +
                "<yellow>/motdx maintenance <dark_gray>- <gray>Toggle maintenance mode<newline>" +
                "<yellow>/motdx preview <dark_gray>- <gray>Preview current MOTD<newline>" +
                "<yellow>/motdx set <mode> <dark_gray>- <gray>Set animation mode<newline>" +
                "<yellow>/motdx info <dark_gray>- <gray>Show plugin info"
        ));
    }

    private void noPerms(CommandSender sender) {
        sender.sendMessage(mm.deserialize("<red>You don't have permission!"));
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String @NotNull [] args) {
        if (args.length == 1) {
            return Stream.of("reload", "maintenance", "preview", "set", "info")
                    .filter(s -> s.startsWith(args[0].toLowerCase()))
                    .toList();
        }
        if (args.length == 2 && args[0].equalsIgnoreCase("set")) {
            return Stream.of("sequential", "random", "shuffle")
                    .filter(s -> s.startsWith(args[1].toLowerCase()))
                    .toList();
        }
        return List.of();
    }
}
