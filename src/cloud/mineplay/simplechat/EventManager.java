package cloud.mineplay.simplechat;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import cloud.mineplay.simplechat.enums.ChatState;
import net.luckperms.api.cacheddata.CachedMetaData;
import net.luckperms.api.model.user.User;
import net.md_5.bungee.api.ChatColor;

public class EventManager implements Listener{
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onAsyncChat(AsyncPlayerChatEvent e) {
		
		if (e.isCancelled()) {
	        return; // respect cancellation by others
	    }
		
		SimpleChat simpleChat = SimpleChat.getInstace();
		
		// Handle chat state
		ChatState currentState = simpleChat.getChatState();
		
		if(currentState == ChatState.LOCKED && !e.getPlayer().hasPermission("simplechat.lockdown.bypass")) {
			e.setCancelled(true);
			e.getPlayer().sendMessage(ChatColor.GOLD + "[" + ChatColor.DARK_PURPLE + "SimpleChat" + ChatColor.GOLD + "]" + ChatColor.DARK_RED + " Chat is currently locked!");
			return;
		}else if( currentState == ChatState.STAFF && !e.getPlayer().hasPermission("simplechat.staff")) {
			e.setCancelled(true);
			e.getPlayer().sendMessage(ChatColor.GOLD + "[" + ChatColor.DARK_PURPLE + "SimpleChat" + ChatColor.GOLD + "]" + ChatColor.DARK_RED + " Chat is currently staff only!");
		}
		
		// Luck Perms Shizz
		
		User user = Perms.loadUser(e.getPlayer());

		CachedMetaData metaData = user.getCachedData().getMetaData();
		String prefix = "";
		if (!(metaData.getPrefix() == null)) {
			prefix = metaData.getPrefix();
		}
		
		String suffix = "";
		if(!(metaData.getSuffix() == null)) {
			suffix = metaData.getSuffix();
		}
		
		// Lp Shizz Done ^
		
		Player p = e.getPlayer();
		
		String format = ChatColor.translateAlternateColorCodes('&', simpleChat.getChatFormat());
		
		String nickname = p.getDisplayName();
		
		String name = p.getName();
		
		String message = e.getMessage();
		
		// Translate color codes conditionally
		prefix = ChatColor.translateAlternateColorCodes('&', prefix);
		suffix = ChatColor.translateAlternateColorCodes('&', suffix);
		
		if (p.hasPermission("simplechat.color.nickname")) {
		    nickname = ChatColor.translateAlternateColorCodes('&', nickname);
		}
		if (p.hasPermission("simplechat.color.message")) {
		    message = ChatColor.translateAlternateColorCodes('&', message);
		}
		
		String formatted = format.replace("<prefix>", prefix)
				.replace("<suffix>", suffix)
				.replace("<nickname>", nickname)
				.replace("<name>", name)
				.replace("<message>", message);
		
		e.setFormat(formatted);
		
	}

}
