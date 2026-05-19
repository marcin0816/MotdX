package dc.marcin0816.motdX.manager;

import dc.marcin0816.motdX.MotdX;
import org.bukkit.Bukkit;
import org.bukkit.util.CachedServerIcon;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

public class IconManager {

    private final MotdX plugin;
    private final List<CachedServerIcon> icons = new ArrayList<>();
    private final List<CachedServerIcon> shuffleQueue = new ArrayList<>();
    private final Random random = new Random();

    private boolean enabled;
    private String mode;
    private int sequentialIndex = 0;

    public IconManager(MotdX plugin) {
        this.plugin = plugin;
    }

    public void reload() {
        icons.clear();
        shuffleQueue.clear();
        sequentialIndex = 0;

        var cfg = plugin.getConfig();
        enabled = cfg.getBoolean("icons.enabled", false);
        mode = cfg.getString("icons.mode", "sync").toLowerCase();

        if (!enabled) return;

        File iconsDir = new File(plugin.getDataFolder(), "icons");
        if (!iconsDir.exists()) {
            if (iconsDir.mkdirs()) {
                plugin.getLogger().info("Created icons/ directory — place 64x64 PNG files there.");
            }
            return;
        }

        File[] files = iconsDir.listFiles((ignored, name) -> name.toLowerCase().endsWith(".png"));
        if (files == null || files.length == 0) {
            plugin.getLogger().warning("icons/ is empty — no icons loaded.");
            return;
        }

        Arrays.sort(files, Comparator.comparing(File::getName));

        for (File file : files) {
            try {
                icons.add(Bukkit.loadServerIcon(file));
            } catch (Exception e) {
                plugin.getLogger().warning("Failed to load icon " + file.getName() + ": " + e.getMessage());
            }
        }

        plugin.getLogger().info("Loaded " + icons.size() + " server icon(s).");

        if (mode.equals("shuffle") && !icons.isEmpty()) {
            shuffleQueue.addAll(icons);
            Collections.shuffle(shuffleQueue, random);
        }
    }

    public CachedServerIcon next(int motdFrameIndex) {
        if (!enabled || icons.isEmpty()) return null;

        return switch (mode) {
            case "random" -> icons.get(random.nextInt(icons.size()));
            case "shuffle" -> nextShuffle();
            case "sync" -> icons.get(motdFrameIndex % icons.size());
            default -> icons.get(sequentialIndex++ % icons.size());
        };
    }

    private CachedServerIcon nextShuffle() {
        if (shuffleQueue.isEmpty()) {
            shuffleQueue.addAll(icons);
            Collections.shuffle(shuffleQueue, random);
        }
        return shuffleQueue.removeFirst();
    }

    public int getIconCount() {
        return icons.size();
    }
}
