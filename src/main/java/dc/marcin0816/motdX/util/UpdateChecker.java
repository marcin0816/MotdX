package dc.marcin0816.motdX.util;

import dc.marcin0816.motdX.MotdX;
import org.bukkit.Bukkit;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class UpdateChecker {

    private static final Pattern TAG_PATTERN = Pattern.compile("\"tag_name\":\\s*\"([^\"]+)\"");

    private final MotdX plugin;
    private final HttpClient http = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .build();

    public UpdateChecker(MotdX plugin) {
        this.plugin = plugin;
    }

    public void check() {
        String owner = plugin.getConfig().getString("update-checker.github-owner", "");
        String repo = plugin.getConfig().getString("update-checker.github-repo", "");
        if (owner.isEmpty() || repo.isEmpty()) return;

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                var request = HttpRequest.newBuilder()
                        .uri(URI.create("https://api.github.com/repos/" + owner + "/" + repo + "/releases/latest"))
                        .header("Accept", "application/vnd.github+json")
                        .timeout(Duration.ofSeconds(5))
                        .GET()
                        .build();

                var response = http.send(request, HttpResponse.BodyHandlers.ofString());
                if (response.statusCode() != 200) return;

                Matcher matcher = TAG_PATTERN.matcher(response.body());
                if (!matcher.find()) return;

                String latest = matcher.group(1).replaceFirst("^v", "");
                String current = plugin.getPluginMeta().getVersion();

                if (!latest.equalsIgnoreCase(current)) {
                    plugin.getLogger().info("================================================");
                    plugin.getLogger().info("  New version available: " + latest);
                    plugin.getLogger().info("  Current version: " + current);
                    plugin.getLogger().info("  https://github.com/" + owner + "/" + repo + "/releases/latest");
                    plugin.getLogger().info("================================================");
                }
            } catch (IOException | InterruptedException e) {
                plugin.getLogger().warning("Update check failed: " + e.getMessage());
            }
        });
    }
}
