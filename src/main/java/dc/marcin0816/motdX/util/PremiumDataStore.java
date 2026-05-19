package dc.marcin0816.motdX.util;

import dc.marcin0816.motdX.MotdX;

import java.io.*;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class PremiumDataStore {

    private final File file;
    private final Set<UUID> used = ConcurrentHashMap.newKeySet();

    public PremiumDataStore(MotdX plugin) {
        this.file = new File(plugin.getDataFolder(), "premium-used.txt");
        load();
    }

    public boolean hasUsed(UUID uuid) {
        return used.contains(uuid);
    }

    public void markUsed(UUID uuid) {
        if (used.add(uuid)) {
            save();
        }
    }

    private void load() {
        if (!file.exists()) return;
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;
                try {
                    used.add(UUID.fromString(line));
                } catch (IllegalArgumentException ignored) {}
            }
        } catch (IOException ignored) {}
    }

    private void save() {
        File parent = file.getParentFile();
        if (!parent.exists() && !parent.mkdirs()) return;
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            for (UUID uuid : used) {
                writer.write(uuid.toString());
                writer.newLine();
            }
        } catch (IOException ignored) {}
    }
}
