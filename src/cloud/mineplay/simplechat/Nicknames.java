package cloud.mineplay.simplechat;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class Nicknames implements Listener{
	
	private File nicknameFile;
	private FileConfiguration nicknameConfig;
	private final Map<UUID, String> nicknamesMap = new HashMap<>();
	
	private int autoSaveTaskId = -1;
	
	public Nicknames() {
		createNicknameFile();
	}
	
	public void createNicknameFile() {
		nicknameFile = new File(SimpleChat.getInstace().getDataFolder(), "nicknames.yml");
		if(!nicknameFile.exists()) {
			nicknameFile.getParentFile().mkdirs();
			try {
				nicknameFile.createNewFile();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		nicknameConfig = YamlConfiguration.loadConfiguration(nicknameFile);
	}
	
	public void saveNicknameAsync(UUID uuid, String nickname) {
	    // Sync part: safely update memory
	    Bukkit.getScheduler().runTask(SimpleChat.getInstace(), () -> {
	        nicknameConfig.set("nicknames." + uuid.toString(), nickname);

	        // Async part: safely save to disk after memory update
	        Bukkit.getScheduler().runTaskAsynchronously(SimpleChat.getInstace(), () -> {
	            try {
	                nicknameConfig.save(nicknameFile);
	            } catch (IOException e) {
	                e.printStackTrace();
	            }
	        });
	    });
	}
	
	public void loadNickname(Player player) {
		
		String nick = null;
		
		if(nicknamesMap.containsKey(player.getUniqueId())) {
			nick = nicknamesMap.get(player.getUniqueId());
		}else {
			nick = nicknameConfig.getString("nicknames." + player.getUniqueId());
		}
		if(nick != null) {
			player.setDisplayName(nick);
			if(player.hasPermission("simplechat.color.nickname")) {
				nick = ChatColor.translateAlternateColorCodes('&', nick);
			}
			player.setPlayerListName(nick);
		}
	}
	
	public void changeNickname(Player player, String nickname) {
		if(!nicknamesMap.containsKey(player.getUniqueId())) {
			nicknamesMap.put(player.getUniqueId(), nickname);
			player.setDisplayName(nickname);
		}
	}
	
	public void saveNicknamesToFile(Boolean shutdown) {
        for (Map.Entry<UUID, String> entry : nicknamesMap.entrySet()) {
            nicknameConfig.set("nicknames." + entry.getKey().toString(), entry.getValue());
        }
        try {
            nicknameConfig.save(nicknameFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
        if(shutdown) {
        	return;
        }
        Bukkit.getScheduler().runTask(SimpleChat.getInstace(), () -> {
            nicknamesMap.keySet().removeIf(uuid -> Bukkit.getPlayer(uuid) == null);
        });
    }
	
	public void saveNicknamesToFileAsync() {
	    // Take a full snapshot (includes online + offline)
	    Map<UUID, String> snapshot = new HashMap<>(nicknamesMap);

	    // Save the snapshot asynchronously
	    Bukkit.getScheduler().runTaskAsynchronously(SimpleChat.getInstace(), () -> {
	        FileConfiguration config = YamlConfiguration.loadConfiguration(nicknameFile);

	        for (Map.Entry<UUID, String> entry : snapshot.entrySet()) {
	            config.set("nicknames." + entry.getKey().toString(), entry.getValue());
	        }

	        try {
	            config.save(nicknameFile);
	        } catch (IOException e) {
	            e.printStackTrace();
	        }

	        // Clean up memory after saving (run this part on the main thread)
	        Bukkit.getScheduler().runTask(SimpleChat.getInstace(), () -> {
	            nicknamesMap.keySet().removeIf(uuid -> Bukkit.getPlayer(uuid) == null);
	        });
	    });
	}
	
	public void setNickname(Player player, String nickname) {
	    nicknamesMap.put(player.getUniqueId(), nickname);
	    if(player.hasPermission("simplechat.color.nickname")) {
	    	player.setDisplayName(ChatColor.translateAlternateColorCodes('&', nickname));
	    	player.setPlayerListName(ChatColor.translateAlternateColorCodes('&', nickname));
	    }else {
	    	player.setDisplayName(nickname);
	    	player.setPlayerListName(nickname);
	    }
	}

	public void clearNickname(Player player) {
	    nicknamesMap.remove(player.getUniqueId());
	    nicknameConfig.set("nicknames." + player.getUniqueId().toString(), null); // Remove from config
	    player.setDisplayName(player.getName());
	    player.setPlayerListName(player.getName());
	}
	
	public void startAutoSaveTask(long intervalTicks) {
	    if (autoSaveTaskId != -1) {
	        Bukkit.getScheduler().cancelTask(autoSaveTaskId); // avoid duplicates
	    }

	    autoSaveTaskId = Bukkit.getScheduler().runTaskTimerAsynchronously(
	        SimpleChat.getInstace(),
	        this::saveNicknamesToFileAsync,
	        intervalTicks, intervalTicks
	    ).getTaskId();
	}
	
	public void stopAutoSaveTask() {
	    if (autoSaveTaskId != -1) {
	        Bukkit.getScheduler().cancelTask(autoSaveTaskId);
	        autoSaveTaskId = -1;
	    }
	}
	
	@EventHandler
	public void onJoin(PlayerJoinEvent e) {
		loadNickname(e.getPlayer());
	}

}
