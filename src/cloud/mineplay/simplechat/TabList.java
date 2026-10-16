package cloud.mineplay.simplechat;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

public class TabList {
	
	private final SimpleChat plugin;
    private final PlaceHolderMgr formatter;
    private final String headerTemplate;
    private final String footerTemplate;
    private BukkitTask task;
	
	public TabList(SimpleChat plugin, PlaceHolderMgr formatter, String headerTemplate, String footerTemplate) {
        this.plugin = plugin;
        this.formatter = formatter;
        this.headerTemplate = headerTemplate;
        this.footerTemplate = footerTemplate;
    }

	public void start() {
        task = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            for (Player player : Bukkit.getOnlinePlayers()) {
                // Format header/footer using placeholders
                String header = formatter.format(player, headerTemplate);
                String footer = formatter.format(player, footerTemplate);
                player.setPlayerListHeaderFooter(header, footer);

                // Format and update tab name (nickname, prefix, etc.)
                String tabName = formatter.format(player, "<prefix> <nickname>");
                player.setPlayerListName(tabName);
            }
        }, 0L, 40L); // every 2 seconds
    }

    public void stop() {
        if (task != null) {
            task.cancel();
        }
    }

}
