package dc.marcin0816.motdX.listener;

import dc.marcin0816.motdX.MotdX;
import dc.marcin0816.motdX.model.MotdFrame;
import com.destroystokyo.paper.event.server.PaperServerListPingEvent;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.util.CachedServerIcon;

public class MotdListener implements Listener {

    private final MotdX plugin;
    private final MiniMessage mm = MiniMessage.miniMessage();

    public MotdListener(MotdX plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPing(PaperServerListPingEvent event) {
        MotdFrame frame = plugin.getMotdManager().next(event.getNumPlayers(), event.getMaxPlayers());
        if (frame == null) return;

        int online = event.getNumPlayers();
        int max = event.getMaxPlayers();

        String line1 = applyPlaceholders(frame.line1(), online, max);
        String line2 = applyPlaceholders(frame.line2(), online, max);

        if (plugin.hasPapi()) {
            line1 = me.clip.placeholderapi.PlaceholderAPI.setPlaceholders(null, line1);
            line2 = me.clip.placeholderapi.PlaceholderAPI.setPlaceholders(null, line2);
        }

        event.motd(mm.deserialize(line1 + "\n" + line2));

        if (frame.playersOnline() != null) event.setNumPlayers(frame.playersOnline());
        if (frame.playersMax() != null) event.setMaxPlayers(frame.playersMax());

        CachedServerIcon icon = plugin.getIconManager().next(plugin.getMotdManager().getLastUsedIndex());
        if (icon != null) event.setServerIcon(icon);
    }

    private String applyPlaceholders(String text, int online, int max) {
        return text
                .replace("{online}", String.valueOf(online))
                .replace("{max}", String.valueOf(max));
    }
}
