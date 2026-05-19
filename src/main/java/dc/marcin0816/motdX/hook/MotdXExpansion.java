package dc.marcin0816.motdX.hook;

import dc.marcin0816.motdX.MotdX;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class MotdXExpansion extends PlaceholderExpansion {

    private final MotdX plugin;

    public MotdXExpansion(MotdX plugin) {
        this.plugin = plugin;
    }

    @Override
    public @NotNull String getIdentifier() {
        return "motdx";
    }

    @Override
    public @NotNull String getAuthor() {
        return "marcin0816";
    }

    @Override
    public @NotNull String getVersion() {
        return plugin.getPluginMeta().getVersion();
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public @Nullable String onRequest(OfflinePlayer player, @NotNull String params) {
        var m = plugin.getMotdManager();
        return switch (params.toLowerCase()) {
            case "maintenance" -> String.valueOf(m.isMaintenanceActive());
            case "mode" -> m.getMode();
            case "frames" -> String.valueOf(m.getFrameCount());
            default -> null;
        };
    }
}
