package cloud.mineplay.simplechat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.server.ServerCommandEvent;

import cloud.mineplay.simplechat.enums.ChatState;
import net.md_5.bungee.api.ChatColor;

public class CommandManager implements Listener, CommandExecutor, TabCompleter{
	
	/*
	 * Get rid of minecraft's default /tell & /say which bypass formatting.
	 */

	@EventHandler
    public void onPlayerCommand(PlayerCommandPreprocessEvent event) {
        String message = event.getMessage().toLowerCase();
        if (message.startsWith("/say ") || message.equals("/say")
            || message.startsWith("/minecraft:say ") || message.equals("/minecraft:say")
            || message.startsWith("/tell ") || message.equals("/tell")
            || message.startsWith("/minecraft:tell ") || message.equals("/minecraft:tell")) {
            event.setCancelled(true);
            event.getPlayer().sendMessage("§cThis command is disabled.");
        }
    }
	
	@EventHandler
    public void onServerCommand(ServerCommandEvent event) {
        String cmd = event.getCommand().toLowerCase();
        if (cmd.startsWith("say ") || cmd.equals("say")
            || cmd.startsWith("minecraft:say ") || cmd.equals("minecraft:say")
            || cmd.startsWith("tell ") || cmd.equals("tell")
            || cmd.startsWith("minecraft:tell ") || cmd.equals("minecraft:tell")) {
            event.setCancelled(true);
            event.getSender().sendMessage("This command is disabled.");
        }
    }
	
	/*
	 * Our commands:
	 */
	
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		
        if (command.getName().equalsIgnoreCase("m")) {
        	 if (!(sender instanceof Player)) {
                 sender.sendMessage("This command can only be used by players.");
                 return true;
             }

             Player player = (Player) sender;
            if (args.length < 2) {
                player.sendMessage(ChatColor.RED + "Usage: /m <player> <message>");
                return true;
            }

            Player target = Bukkit.getPlayerExact(args[0]);
            if (target == null || !target.isOnline()) {
                player.sendMessage(ChatColor.RED + "That player is not online.");
                return true;
            }

            if (target.equals(player)) {
                player.sendMessage(ChatColor.RED + "You cannot message yourself.");
                return true;
            }

            String message = String.join(" ", Arrays.copyOfRange(args, 1, args.length));

            player.sendMessage(ChatColor.GRAY + "To " + target.getName() + ": " + ChatColor.WHITE + message);
            target.sendMessage(ChatColor.GRAY + "From " + player.getName() + ": " + ChatColor.WHITE + message);

            // Update last messaged
            SimpleChat.getInstace().getLastMessaged().put(player.getUniqueId(), target.getUniqueId());
            SimpleChat.getInstace().getLastMessaged().put(target.getUniqueId(), player.getUniqueId());

            return true;
        }

        if (command.getName().equalsIgnoreCase("r")) {
        	 if (!(sender instanceof Player)) {
                 sender.sendMessage("This command can only be used by players.");
                 return true;
             }

             Player player = (Player) sender;
            if (args.length < 1) {
                player.sendMessage(ChatColor.RED + "Usage: /r <message>");
                return true;
            }

            UUID targetId = SimpleChat.getInstace().getLastMessaged().get(player.getUniqueId());
            if (targetId == null) {
                player.sendMessage(ChatColor.RED + "You have no one to reply to.");
                return true;
            }

            Player target = Bukkit.getPlayer(targetId);
            if (target == null || !target.isOnline()) {
                player.sendMessage(ChatColor.RED + "That player is no longer online.");
                return true;
            }

            String message = String.join(" ", args);

            player.sendMessage(ChatColor.GRAY + "To " + target.getName() + ": " + ChatColor.WHITE + message);
            target.sendMessage(ChatColor.GRAY + "From " + player.getName() + ": " + ChatColor.WHITE + message);

            // Update last messaged again
            SimpleChat.getInstace().getLastMessaged().put(player.getUniqueId(), target.getUniqueId());
            SimpleChat.getInstace().getLastMessaged().put(target.getUniqueId(), player.getUniqueId());

            return true;
        }
        
        if(command.getName().equalsIgnoreCase("lockdown")) {
        	
        	if(sender instanceof Player) {
        		Player p = (Player) sender;
        		if(!p.hasPermission("simplechat.lockdown")) {
        			return true;
        		}
        	}
        	
        	if(args.length != 1) {
        		if(SimpleChat.getInstace().getChatState() == ChatState.OPEN) {
        			SimpleChat.getInstace().setChatState(ChatState.LOCKED);
        			Bukkit.broadcastMessage(ChatColor.GOLD + sender.getName() + ChatColor.DARK_RED + " LOCKED CHAT!");
        			return true;
        		}else if(SimpleChat.getInstace().getChatState() == ChatState.LOCKED || SimpleChat.getInstace().getChatState() == ChatState.STAFF) {
        			SimpleChat.getInstace().setChatState(ChatState.OPEN);
        			Bukkit.broadcastMessage(ChatColor.GOLD + sender.getName() + ChatColor.DARK_GREEN + " UNLOCKED CHAT!");
        			return true;
        		}
        	}else if(args.length == 1 && args[0].equalsIgnoreCase("staff")) {
        		SimpleChat.getInstace().setChatState(ChatState.STAFF);
        		Bukkit.broadcastMessage(ChatColor.GOLD + sender.getName() + ChatColor.DARK_RED + " LOCKED CHAT TO STAFF ONLY!");
        		return true;
        	}
        }
        
        if (command.getName().equalsIgnoreCase("nick") || command.getName().equalsIgnoreCase("nickname")) {

            if (args.length == 0) {
                sender.sendMessage(ChatColor.RED + "Usage: /nick <nickname> | /nick clear [player] | /nick <player> <nickname>");
                return true;
            }
            
            Nicknames nicknameMgr = SimpleChat.getInstace().getNicknameMgr();

            // /nick clear
            if (args[0].equalsIgnoreCase("clear")) {
                if (args.length == 1) {
                    // /nick clear (self)
                    if (!(sender instanceof Player)) {
                        sender.sendMessage("Only players can clear their own nickname.");
                        return true;
                    }
                    Player player = (Player) sender;

                    if (!player.hasPermission("simplechat.nick")) {
                        player.sendMessage(ChatColor.RED + "You don't have permission.");
                        return true;
                    }

                    nicknameMgr.clearNickname(player);
                    player.sendMessage(ChatColor.GREEN + "Your nickname has been cleared.");
                    return true;
                }

                if (args.length == 2) {
                    // /nick clear <player>
                    if (!sender.hasPermission("simplechat.nick.others")) {
                        sender.sendMessage(ChatColor.RED + "You don't have permission to clear others' nicknames.");
                        return true;
                    }

                    Player target = Bukkit.getPlayerExact(args[1]);
                    if (target == null || !target.isOnline()) {
                        sender.sendMessage(ChatColor.RED + "Player not found.");
                        return true;
                    }

                    nicknameMgr.clearNickname(target);
                    sender.sendMessage(ChatColor.GREEN + "Cleared nickname for " + target.getName() + ".");
                    target.sendMessage(ChatColor.YELLOW + "Your nickname has been cleared by an admin.");
                    return true;
                }

                sender.sendMessage(ChatColor.RED + "Usage: /nick clear [player]");
                return true;
            }

            // /nick <player> <nickname>
            if (args.length >= 2) {
                if (!sender.hasPermission("simplechat.nick.others")) {
                    sender.sendMessage(ChatColor.RED + "You don't have permission to set others' nicknames.");
                    return true;
                }

                Player target = Bukkit.getPlayerExact(args[0]);
                if (target == null || !target.isOnline()) {
                    sender.sendMessage(ChatColor.RED + "Player not found.");
                    return true;
                }

                String nickname = String.join(" ", java.util.Arrays.copyOfRange(args, 1, args.length));
                nicknameMgr.setNickname(target, nickname);

                sender.sendMessage(ChatColor.GREEN + "Set nickname for " + target.getName() + " to " + nickname);
                target.sendMessage(ChatColor.YELLOW + "Your nickname has been set to " + nickname);
                return true;
            }

            // /nick <nickname> (self)
            if (!(sender instanceof Player)) {
                sender.sendMessage("Only players can set their own nickname.");
                return true;
            }

            Player p = (Player) sender;

            if (!p.hasPermission("simplechat.nick")) {
                p.sendMessage(ChatColor.RED + "You don't have permission.");
                return true;
            }

            String nickname = String.join(" ", args);
            nicknameMgr.setNickname(p, nickname);

            p.sendMessage(ChatColor.GREEN + "Your nickname has been set to " + nickname);
            return true;
        }
        
        if(command.getName().equalsIgnoreCase("realname")) {
        	if (!sender.hasPermission("simplechat.realname")) {
                sender.sendMessage(ChatColor.RED + "You don't have permission to use this command.");
                return true;
            }
        	
        	if (args.length != 1) {
                sender.sendMessage(ChatColor.RED + "Usage: /realname <nickname or player>");
                return true;
            }
        	
        	String input = args[0];
        	
        	// Try to find online players by display name
            for (Player online : Bukkit.getOnlinePlayers()) {
                String strippedNick = ChatColor.stripColor(online.getDisplayName());

                if (strippedNick.equalsIgnoreCase(input) || online.getName().equalsIgnoreCase(input)) {
                    sender.sendMessage(ChatColor.YELLOW + "Real name of " + strippedNick + " is: " + ChatColor.GREEN + online.getName());
                    return true;
                }
            }

            sender.sendMessage(ChatColor.RED + "Player not found online.");
            return true;
        }
        
        if (command.getName().equalsIgnoreCase("simplechat")) {
	        if (args.length == 1 && args[0].equalsIgnoreCase("reload")) {

	            if (!sender.hasPermission("simplechat.reload")) {
	                sender.sendMessage(ChatColor.RED + "You don't have permission to reload SimpleChat.");
	                return true;
	            }

	            SimpleChat.getInstace().reloadConfig();

	            SimpleChat.getInstace().loadChatFormat();
	            SimpleChat.getInstace().reloadTabList();

	            sender.sendMessage(ChatColor.GREEN + "[SimpleChat] Configuration reloaded.");
	            return true;
	        }

	        sender.sendMessage(ChatColor.YELLOW + "Usage: /simplechat reload");
	        return true;
	    }

        return true;
    }
	
	public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
	    List<String> completions = new ArrayList<>();
	    String cmdName = command.getName().toLowerCase();

	    if (cmdName.equalsIgnoreCase("nick") || cmdName.equalsIgnoreCase("nickname")) {
	        if (args.length == 1) {
	            if ("clear".startsWith(args[0].toLowerCase())) {
	                completions.add("clear");
	            }

	            if (sender.hasPermission("simplechat.nick.others")) {
	                for (Player p : Bukkit.getOnlinePlayers()) {
	                    if (p.getName().toLowerCase().startsWith(args[0].toLowerCase())) {
	                        completions.add(p.getName());
	                    }
	                }
	            }

	            return completions;
	        }

	        if (args.length == 2) {
	            if (args[0].equalsIgnoreCase("clear") && sender.hasPermission("simplechat.nick.others")) {
	                for (Player p : Bukkit.getOnlinePlayers()) {
	                    if (p.getName().toLowerCase().startsWith(args[1].toLowerCase())) {
	                        completions.add(p.getName());
	                    }
	                }
	            }

	            return completions;
	        }

	        return completions;
	    }

	    if (cmdName.equalsIgnoreCase("realname")) {
	        if (args.length == 1 && sender.hasPermission("simplechat.realname")) {
	            String typed = args[0].toLowerCase();
	            for (Player p : Bukkit.getOnlinePlayers()) {
	                String name = p.getName();
	                String display = ChatColor.stripColor(p.getDisplayName());

	                if (name.toLowerCase().startsWith(typed) || display.toLowerCase().startsWith(typed)) {
	                    completions.add(name);
	                }
	            }
	        }

	        return completions;
	    }

	    if (cmdName.equals("m") || cmdName.equalsIgnoreCase("message")) {
	        if (args.length == 1) {
	            String typed = args[0].toLowerCase();
	            for (Player p : Bukkit.getOnlinePlayers()) {
	                if (!p.getName().equalsIgnoreCase(sender.getName()) && p.getName().toLowerCase().startsWith(typed)) {
	                    completions.add(p.getName());
	                }
	            }
	        }

	        return completions;
	    }

	    return completions;
	}

}
