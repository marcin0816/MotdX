package dc.marcin0816.motdX;

import dc.marcin0816.motdX.command.MotdCommand;
import dc.marcin0816.motdX.hook.MotdXExpansion;
import dc.marcin0816.motdX.listener.MotdListener;
import dc.marcin0816.motdX.listener.PremiumCheckListener;
import dc.marcin0816.motdX.manager.ActionBarManager;
import dc.marcin0816.motdX.manager.IconManager;
import dc.marcin0816.motdX.manager.MotdManager;
import dc.marcin0816.motdX.util.PremiumDataStore;
import dc.marcin0816.motdX.util.UpdateChecker;
import org.bstats.bukkit.Metrics;
import org.bstats.charts.SimplePie;
import org.bukkit.plugin.java.JavaPlugin;

public final class MotdX extends JavaPlugin {

    // Register your plugin at https://bstats.org and replace this ID
    private static final int BSTATS_ID = 0;

    private MotdManager motdManager;
    private ActionBarManager actionBarManager;
    private IconManager iconManager;
    private boolean papiEnabled = false;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        motdManager = new MotdManager(this);
        motdManager.reload();

        actionBarManager = new ActionBarManager(this);
        actionBarManager.reload();

        iconManager = new IconManager(this);
        iconManager.reload();

        getServer().getPluginManager().registerEvents(new MotdListener(this), this);

        if (getConfig().getBoolean("premium-check.enabled", false)) {
            var authMe = getServer().getPluginManager().getPlugin("AuthMe");
            if (authMe == null) {
                getLogger().warning("Premium check disabled — AuthMe not found!");
            } else {
                var authMeCfg = org.bukkit.configuration.file.YamlConfiguration
                        .loadConfiguration(new java.io.File(authMe.getDataFolder(), "config.yml"));
                if (!authMeCfg.getBoolean("settings.enablePremium", false)) {
                    getLogger().info("Premium check disabled — set settings.enablePremium: true in AuthMe config.");
                } else {
                    getServer().getPluginManager().registerEvents(new PremiumCheckListener(this, new PremiumDataStore(this)), this);
                    getLogger().info("Premium check enabled (AuthMe hooked).");
                }
            }
        }

        var cmd = getCommand("motdx");
        if (cmd != null) {
            var command = new MotdCommand(this);
            cmd.setExecutor(command);
            cmd.setTabCompleter(command);
        }

        if (getServer().getPluginManager().getPlugin("PlaceholderAPI") != null) {
            papiEnabled = true;
            new MotdXExpansion(this).register();
            getLogger().info("PlaceholderAPI hooked!");
        }

        if (getConfig().getBoolean("update-checker.enabled", true)) {
            new UpdateChecker(this).check();
        }

        if (BSTATS_ID != 0) {
            Metrics metrics = new Metrics(this, BSTATS_ID);
            metrics.addCustomChart(new SimplePie("animation_mode", () -> motdManager.getMode()));
        }

        getLogger().info("================================================");
        getLogger().info("  MotdX has been enabled!");
        getLogger().info("  Frames loaded: " + motdManager.getFrameCount());
        getLogger().info("  Mode: " + motdManager.getMode());
        getLogger().info("  Icons loaded: " + iconManager.getIconCount());
        getLogger().info("  Author: marcin0816");
        getLogger().info("================================================");
    }

    @Override
    public void onDisable() {
        if (actionBarManager != null) actionBarManager.stop();
        getLogger().info("MotdX has been disabled.");
    }

    public MotdManager getMotdManager() {
        return motdManager;
    }

    public ActionBarManager getActionBarManager() {
        return actionBarManager;
    }

    public IconManager getIconManager() {
        return iconManager;
    }

    public boolean hasPapi() {
        return papiEnabled;
    }
}
