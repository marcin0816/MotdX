package dc.marcin0816.motdX.listener;

import dc.marcin0816.motdX.MotdX;
import dc.marcin0816.motdX.util.PremiumDataStore;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.title.Title;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerJoinEvent;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

public class PremiumCheckListener implements Listener {

    private final MotdX plugin;
    private final PremiumDataStore dataStore;
    private final HttpClient http = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .build();

    public PremiumCheckListener(MotdX plugin, PremiumDataStore dataStore) {
        this.plugin = plugin;
        this.dataStore = dataStore;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        var player = event.getPlayer();
        if (dataStore.hasUsed(player.getUniqueId())) return;

        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            if (!isPremium(player.getName())) return;
            var cfg = plugin.getConfig();
            String titleText = cfg.getString("premium-check.title",
                    "<gold><bold>Wykryto konto premium!");
            String subtitleText = cfg.getString("premium-check.subtitle",
                    "<yellow>Użyj <white>/premium<yellow> aby uzyskać automatyczne logowanie");
            plugin.getServer().getScheduler().runTask(plugin, () -> {
                if (!player.isOnline()) return;
                var mm = MiniMessage.miniMessage();
                player.showTitle(Title.title(
                        mm.deserialize(titleText),
                        mm.deserialize(subtitleText)
                ));
            });
        });
    }

    @EventHandler
    public void onCommand(PlayerCommandPreprocessEvent event) {
        String cmd = event.getMessage().toLowerCase();
        if (cmd.equals("/premium") || cmd.startsWith("/premium ")) {
            dataStore.markUsed(event.getPlayer().getUniqueId());
        }
    }

    private boolean isPremium(String username) {
        try {
            var req = HttpRequest.newBuilder()
                    .uri(URI.create("https://api.mojang.com/users/profiles/minecraft/" + username))
                    .timeout(Duration.ofSeconds(5))
                    .GET()
                    .build();
            return http.send(req, HttpResponse.BodyHandlers.discarding()).statusCode() == 200;
        } catch (IOException | InterruptedException e) {
            plugin.getLogger().warning("Premium check failed for " + username + ": " + e.getMessage());
            return false;
        }
    }
}
