package cloud.mineplay.simplechat;

import org.bukkit.entity.Player;

import net.luckperms.api.LuckPerms;
import net.md_5.bungee.api.ChatColor;

public class PlaceHolderMgr {
	
	private LuckPerms lpApi = null;
	
	public PlaceHolderMgr(LuckPerms lpApi) {
		this.lpApi = lpApi;
	}
	
	public String format(Player player, String input) {
        String output = input;

        // Replace custom placeholders
        output = output.replace("<player>", player.getName());

        /*if (player.getDisplayName() != null) {
            output = output.replace("<nickname>", ChatColor.stripColor(player.getDisplayName()));
        }*/
        
        String nickname = player.getDisplayName() != null ? player.getDisplayName() : "";
        if (!player.hasPermission("simplechat.color.nickname")) {
            nickname = ChatColor.stripColor(nickname);
        } else {
            nickname = ChatColor.translateAlternateColorCodes('&', nickname);
        }
        
        output = output.replace("<nickname>", nickname);

        if (lpApi != null) {
            String prefix = lpApi.getUserManager().getUser(player.getUniqueId())
                          .getCachedData().getMetaData().getPrefix();
            String suffix = lpApi.getUserManager().getUser(player.getUniqueId())
                          .getCachedData().getMetaData().getSuffix();

            output = output.replace("<prefix>", prefix != null ? prefix : "");
            output = output.replace("<suffix>", suffix != null ? suffix : "");
        }

        output = output.replace("<name>", player.getName() != null ? player.getName() : "");

        double tpsValue = SimpleChat.getInstace().tpscalc.getTps();
        String coloredTps = getColoredTps(tpsValue);
        output = output.replace("<tps>", coloredTps != null ? coloredTps : ChatColor.RED + "0.0");
        String pingDisplay = getColoredPing(player.getPing());
        output = output.replace("<ping>", pingDisplay != null ? pingDisplay : "N/A");
        
        return output;
    }
	
	public String getColoredPing(int ping) {
	    if (ping < 50) return "" + ChatColor.GREEN + ping + "ms";
	    if (ping < 150) return "" + ChatColor.YELLOW + ping + "ms";
	    return "" + ChatColor.RED + ping + "ms" + ChatColor.RESET;
	}
	
	public String getColoredTps(double tps) {
	    ChatColor color;

	    if (tps >= 19.5) {
	        color = ChatColor.GREEN;
	    } else if (tps >= 17.0) {
	        color = ChatColor.YELLOW;
	    } else {
	        color = ChatColor.RED;
	    }

	    return color + String.format("%.2f", Math.min(tps, 20.0)) + ChatColor.RESET;
	}

}
