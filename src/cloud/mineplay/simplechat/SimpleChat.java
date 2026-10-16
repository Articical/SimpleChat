package cloud.mineplay.simplechat;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import cloud.mineplay.simplechat.enums.ChatState;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;

public class SimpleChat extends JavaPlugin{
	
	private static SimpleChat instance;
	
	private String chatFormat = null;
	private String tabHeader = null;
	public TpsCalc tpscalc;
	private TabList tabListManager;
	private String tabFooter = null;
	private Boolean tabEnabled;
	private ChatState chatState = ChatState.OPEN;
	
	private final Map<UUID, UUID> lastMessaged = new HashMap<>();
	
	public static LuckPerms lpermsAPI;
	
	private Nicknames nicknameMgr;
	
	@Override
	public void onEnable() {
		
		instance = this;
		
		int pluginId = 26481;
    	@SuppressWarnings("unused")
		Metrics metrics = new Metrics(this, pluginId);
		
		saveDefaultConfig();
		 // Inject new config defaults if missing
	    getConfig().addDefault("nickname-save-interval-seconds", 300);
	    getConfig().options().copyDefaults(true);
	    saveConfig();
		
		try {
		    lpermsAPI = LuckPermsProvider.get();
		} catch (IllegalStateException e) {
		    getLogger().warning("[SimpleChat] LuckPerms API not found! Some features may not work.");
		}
		
		nicknameMgr = new Nicknames();
		
		long intervalSeconds = getConfig().getLong("nickname-save-interval-seconds", 300); // default 5 min
		nicknameMgr.startAutoSaveTask(intervalSeconds * 20L);
		
		String rawFormat = getConfig().getString("chat-format", "<prefix> <nickname> &6>>&r <message>");
		chatFormat = ChatColor.translateAlternateColorCodes('&', rawFormat);
		tabEnabled = getConfig().getBoolean("tab-enabled");
		if(tabEnabled) {
			tabHeader = ChatColor.translateAlternateColorCodes('&', getConfig().getString("tab-header", ""));
			tabFooter = ChatColor.translateAlternateColorCodes('&', getConfig().getString("tab-footer", ""));
			PlaceHolderMgr formatter = new PlaceHolderMgr(lpermsAPI);
		    tabListManager = new TabList(this, formatter, tabHeader, tabFooter);
		    tabListManager.start();
		}
		
		CommandManager cmdMgr = new CommandManager();

		PluginCommand nickCmd = getCommand("nick");
		if (nickCmd != null) {
		    nickCmd.setExecutor(cmdMgr);
		    nickCmd.setTabCompleter(cmdMgr);
		}

		PluginCommand mCmd = getCommand("m");
		if (mCmd != null) {
		    mCmd.setExecutor(cmdMgr);
		    mCmd.setTabCompleter(cmdMgr);
		}

		PluginCommand rCmd = getCommand("r");
		if (rCmd != null) {
		    rCmd.setExecutor(cmdMgr);
		    rCmd.setTabCompleter(cmdMgr);
		}
		
		PluginCommand realNameCmd = getCommand("realname");
		if (realNameCmd != null) {
		    realNameCmd.setExecutor(cmdMgr);
		    realNameCmd.setTabCompleter(cmdMgr);
		}
		
		PluginCommand simpleChatCmd = getCommand("simpleChat");
		if (simpleChatCmd != null) {
		    simpleChatCmd.setExecutor(cmdMgr);
		}
		
		PluginCommand lockdownCmd = getCommand("lockdown");
		if (lockdownCmd != null) {
		    lockdownCmd.setExecutor(cmdMgr);
		}
		
		Bukkit.getPluginManager().registerEvents(new EventManager(), instance);
		Bukkit.getPluginManager().registerEvents(new CommandManager(), instance);
		Bukkit.getPluginManager().registerEvents(nicknameMgr, instance);
		
		Bukkit.getScheduler().runTaskTimer(this, () -> nicknameMgr.saveNicknamesToFileAsync(), 6000L, 6000L);
		
		tpscalc = new TpsCalc(this);
		
		Bukkit.getLogger().info("SimpleChat enabled, config loaded!");
		
		for(Player p : Bukkit.getOnlinePlayers()) {
			nicknameMgr.loadNickname(p);
		}
		
	}
	
	@Override
	public void onDisable() {
		
		nicknameMgr.stopAutoSaveTask();
		
		nicknameMgr.saveNicknamesToFile(true);
		if (tabListManager != null) tabListManager.stop();
		
		Bukkit.getLogger().info("SimpleChat disabled!");
		
	}
	
	public void loadChatFormat() {
	    String rawFormat = getConfig().getString("chat-format", "<prefix> <nickname> &6>>&r <message>");
	    chatFormat = ChatColor.translateAlternateColorCodes('&', rawFormat);

	    tabEnabled = getConfig().getBoolean("tab-enabled");

	    if (tabEnabled) {
	        tabHeader = ChatColor.translateAlternateColorCodes('&', getConfig().getString("tab-header", ""));
	        tabFooter = ChatColor.translateAlternateColorCodes('&', getConfig().getString("tab-footer", ""));
	    }
	}
	
	public void reloadTabList() {
	    if (tabListManager != null) {
	        tabListManager.stop();
	    }

	    if (tabEnabled) {
	        PlaceHolderMgr formatter = new PlaceHolderMgr(lpermsAPI);
	        tabListManager = new TabList(this, formatter, tabHeader, tabFooter);
	        tabListManager.start();
	    }
	}

	/**
	 * @return the instace
	 */
	public static SimpleChat getInstace() {
		return instance;
	}

	/**
	 * @return the chatFormat
	 */
	public String getChatFormat() {
		return chatFormat;
	}

	/**
	 * @return the tabHeader
	 */
	public String getTabHeader() {
		return tabHeader;
	}

	/**
	 * @return the tabFooter
	 */
	public String getTabFooter() {
		return tabFooter;
	}

	/**
	 * @return the chatState
	 */
	public ChatState getChatState() {
		return chatState;
	}

	/**
	 * @param chatState the chatState to set
	 */
	public void setChatState(ChatState chatState) {
		this.chatState = chatState;
	}

	/**
	 * @return the lastMessaged
	 */
	public Map<UUID, UUID> getLastMessaged() {
		return lastMessaged;
	}

	/**
	 * @return the nicknameMgr
	 */
	public Nicknames getNicknameMgr() {
		return nicknameMgr;
	}

}
