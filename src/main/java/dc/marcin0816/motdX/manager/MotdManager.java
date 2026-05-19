package dc.marcin0816.motdX.manager;

import dc.marcin0816.motdX.MotdX;
import dc.marcin0816.motdX.model.MotdFrame;
import org.bukkit.Bukkit;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

public class MotdManager {

    private final MotdX plugin;
    private final List<MotdFrame> frames = new ArrayList<>();
    private final List<MotdFrame> shuffleQueue = new ArrayList<>();
    private final AtomicInteger index = new AtomicInteger(0);
    private final Random random = new Random();

    private String mode;
    private volatile int lastUsedIndex = 0;

    private boolean fullEnabled;
    private MotdFrame fullFrame;

    private boolean whitelistEnabled;
    private MotdFrame whitelistFrame;

    private boolean lowEnabled;
    private int lowThreshold;
    private MotdFrame lowFrame;

    private MotdFrame maintenanceFrame;
    private boolean maintenanceActive;

    private final List<ScheduleEntry> schedules = new ArrayList<>();

    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm");

    private record ScheduleEntry(String name, LocalTime from, LocalTime to, List<MotdFrame> frames, AtomicInteger idx) {
        MotdFrame next(String mode, Random random) {
            if (frames.isEmpty()) return null;
            return mode.equals("random")
                    ? frames.get(random.nextInt(frames.size()))
                    : frames.get(idx.getAndIncrement() % frames.size());
        }
    }

    public MotdManager(MotdX plugin) {
        this.plugin = plugin;
    }

    public void reload() {
        frames.clear();
        shuffleQueue.clear();
        schedules.clear();
        index.set(0);
        lastUsedIndex = 0;

        var cfg = plugin.getConfig();
        mode = cfg.getString("animation.mode", "sequential").toLowerCase();

        fullEnabled = cfg.getBoolean("conditions.full.enabled", false);
        fullFrame = new MotdFrame(
                cfg.getString("conditions.full.line1", "<red><bold>SERVER FULL"),
                cfg.getString("conditions.full.line2", "<gray>Please try again later"),
                null, null
        );

        whitelistEnabled = cfg.getBoolean("conditions.whitelist.enabled", false);
        whitelistFrame = new MotdFrame(
                cfg.getString("conditions.whitelist.line1", "<yellow><bold>🔒 WHITELIST</bold></yellow>"),
                cfg.getString("conditions.whitelist.line2", "<gray>Server is whitelisted"),
                null, null
        );

        lowEnabled = cfg.getBoolean("conditions.low.enabled", false);
        lowThreshold = cfg.getInt("conditions.low.threshold", 5);
        lowFrame = new MotdFrame(
                cfg.getString("conditions.low.line1", "<green><bold>► SaveCraft ◄</bold></green>"),
                cfg.getString("conditions.low.line2", "<gray>Server is almost empty — be the first!"),
                null, null
        );

        boolean maintenanceEnabled = cfg.getBoolean("conditions.maintenance.enabled", false);
        maintenanceFrame = new MotdFrame(
                cfg.getString("conditions.maintenance.line1", "<yellow><bold>⚙ MAINTENANCE ⚙"),
                cfg.getString("conditions.maintenance.line2", "<gray>Server is under maintenance"),
                null, null
        );

        if (maintenanceEnabled) maintenanceActive = true;

        //noinspection SpellCheckingInspection
        loadMotdList(cfg.getMapList("motds"), frames);

        if (mode.equals("shuffle") && !frames.isEmpty()) {
            shuffleQueue.addAll(frames);
            Collections.shuffle(shuffleQueue, random);
        }

        if (cfg.getBoolean("schedules.enabled", false)) {
            for (Map<?, ?> entry : cfg.getMapList("schedules.entries")) {
                String name = entry.get("name") instanceof String s ? s : "unnamed";
                String fromStr = entry.get("from") instanceof String s ? s : "00:00";
                String toStr = entry.get("to") instanceof String s ? s : "23:59";

                try {
                    LocalTime from = LocalTime.parse(fromStr, TIME_FMT);
                    LocalTime to = LocalTime.parse(toStr, TIME_FMT);
                    List<MotdFrame> schedFrames = new ArrayList<>();
                    //noinspection SpellCheckingInspection
                    if (entry.get("motds") instanceof List<?> raw) {
                        for (Object item : raw) {
                            if (item instanceof Map<?, ?> map) {
                                loadSingleFrame(map, schedFrames);
                            }
                        }
                    }
                    if (!schedFrames.isEmpty()) {
                        schedules.add(new ScheduleEntry(name, from, to, schedFrames, new AtomicInteger(0)));
                    }
                } catch (DateTimeParseException e) {
                    plugin.getLogger().warning("Invalid time in schedule '" + name + "': " + e.getMessage());
                }
            }
        }
    }

    public MotdFrame next(int online, int max) {
        if (maintenanceActive) return maintenanceFrame;
        if (whitelistEnabled && Bukkit.hasWhitelist()) return whitelistFrame;
        if (fullEnabled && online >= max) return fullFrame;
        if (frames.isEmpty()) return null;

        for (ScheduleEntry entry : schedules) {
            if (inTimeRange(LocalTime.now(), entry.from(), entry.to())) {
                return entry.next(mode, random);
            }
        }

        if (lowEnabled && online <= lowThreshold) return lowFrame;

        return switch (mode) {
            case "random" -> {
                lastUsedIndex = random.nextInt(frames.size());
                yield frames.get(lastUsedIndex);
            }
            case "shuffle" -> nextShuffle();
            default -> {
                lastUsedIndex = index.getAndIncrement() % frames.size();
                yield frames.get(lastUsedIndex);
            }
        };
    }

    public MotdFrame peekCurrent() {
        int online = Bukkit.getOnlinePlayers().size();
        int max = Bukkit.getMaxPlayers();
        if (maintenanceActive) return maintenanceFrame;
        if (whitelistEnabled && Bukkit.hasWhitelist()) return whitelistFrame;
        if (fullEnabled && online >= max) return fullFrame;
        if (frames.isEmpty()) return null;

        for (ScheduleEntry entry : schedules) {
            if (inTimeRange(LocalTime.now(), entry.from(), entry.to())) {
                return entry.frames().get(entry.idx().get() % entry.frames().size());
            }
        }

        if (lowEnabled && online <= lowThreshold) return lowFrame;

        return switch (mode) {
            case "random" -> frames.get(random.nextInt(frames.size()));
            case "shuffle" -> shuffleQueue.isEmpty() ? frames.getFirst() : shuffleQueue.getFirst();
            default -> frames.get(index.get() % frames.size());
        };
    }

    private MotdFrame nextShuffle() {
        if (shuffleQueue.isEmpty()) {
            shuffleQueue.addAll(frames);
            Collections.shuffle(shuffleQueue, random);
        }
        MotdFrame frame = shuffleQueue.removeFirst();
        lastUsedIndex = frames.indexOf(frame);
        return frame;
    }

    private boolean inTimeRange(LocalTime now, LocalTime from, LocalTime to) {
        if (from.isBefore(to)) {
            return !now.isBefore(from) && now.isBefore(to);
        }
        // overnight range, e.g. 22:00 to 06:00
        return !now.isBefore(from) || now.isBefore(to);
    }

    private void loadMotdList(List<Map<?, ?>> list, List<MotdFrame> target) {
        for (Map<?, ?> map : list) {
            loadSingleFrame(map, target);
        }
    }

    private void loadSingleFrame(Map<?, ?> map, List<MotdFrame> target) {
        String line1 = map.get("line1") instanceof String s ? s : "";
        String line2 = map.get("line2") instanceof String s ? s : "";

        Integer online = null;
        Integer max = null;

        if (map.containsKey("players-online") && map.get("players-online") instanceof Number n)
            online = n.intValue();
        if (map.containsKey("players-max") && map.get("players-max") instanceof Number n)
            max = n.intValue();

        target.add(new MotdFrame(line1, line2, online, max));
    }

    public void setMode(String newMode) {
        this.mode = newMode;
        index.set(0);
        if (newMode.equals("shuffle") && !frames.isEmpty()) {
            shuffleQueue.clear();
            shuffleQueue.addAll(frames);
            Collections.shuffle(shuffleQueue, random);
        }
    }

    public void setMaintenanceActive(boolean active) {
        this.maintenanceActive = active;
    }

    public boolean isMaintenanceActive() {
        return maintenanceActive;
    }

    public int getLastUsedIndex() {
        return lastUsedIndex;
    }

    public int getFrameCount() {
        return frames.size();
    }

    public String getMode() {
        return mode;
    }
}
