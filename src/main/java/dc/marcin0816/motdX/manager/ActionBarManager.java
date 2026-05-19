package dc.marcin0816.motdX.manager;

import dc.marcin0816.motdX.MotdX;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class ActionBarManager {

    private final MotdX plugin;
    private final MiniMessage mm = MiniMessage.miniMessage();

    private final List<String> frames = new ArrayList<>();
    private final AtomicInteger index = new AtomicInteger(0);
    private BukkitTask task;

    public ActionBarManager(MotdX plugin) {
        this.plugin = plugin;
    }

    public void reload() {
        stop();
        frames.clear();
        index.set(0);

        var cfg = plugin.getConfig();
        boolean enabled = cfg.getBoolean("actionbar.enabled", false);
        int interval = Math.max(1, cfg.getInt("actionbar.interval", 40));

        frames.addAll(cfg.getStringList("actionbar.frames"));

        if (enabled && !frames.isEmpty()) {
            start(interval);
        }
    }

    private void start(int interval) {
        task = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            if (Bukkit.getOnlinePlayers().isEmpty()) return;

            int online = Bukkit.getOnlinePlayers().size();
            int max = Bukkit.getMaxPlayers();
            double tps = Math.min(20.0, Bukkit.getTPS()[0]);

            String raw = frames.get(index.getAndIncrement() % frames.size())
                    .replace("{online}", String.valueOf(online))
                    .replace("{max}", String.valueOf(max))
                    .replace("{tps}", String.format("%.1f", tps));

            String permission = plugin.getConfig().getString("actionbar.permission", "motdx.actionbar");
            for (Player player : Bukkit.getOnlinePlayers()) {
                if (!player.hasPermission(permission)) continue;
                String text = raw;
                if (plugin.hasPapi()) {
                    text = me.clip.placeholderapi.PlaceholderAPI.setPlaceholders(player, text);
                }
                player.sendActionBar(mm.deserialize(text));
            }
        }, 0L, interval);
    }

    public void stop() {
        if (task != null && !task.isCancelled()) {
            task.cancel();
            task = null;
        }
    }
}
