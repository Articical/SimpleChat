package cloud.mineplay.simplechat;

import java.util.Optional;
import java.util.UUID;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.cacheddata.CachedMetaData;
import net.luckperms.api.model.user.User;
import net.luckperms.api.node.types.InheritanceNode;
import net.luckperms.api.query.QueryOptions;

public class Perms {
	
	public static User loadUser(Player player) {
		LuckPerms lpm = SimpleChat.lpermsAPI;

		if (!player.isOnline()) {
			throw new IllegalStateException("Player is offline");
		}

		return lpm.getUserManager().getUser(player.getUniqueId());
	}
	
	public static boolean hasGroup(Player player, String groupName) {
        LuckPerms luckPerms = LuckPermsProvider.get();

        User user = luckPerms.getUserManager().getUser(player.getUniqueId());
        if (user == null) {
            return false;
        }

        Optional<QueryOptions> queryOptionsOptional = luckPerms.getContextManager().getQueryOptions(user);
        if (!queryOptionsOptional.isPresent()) {
            return false;
        }

        QueryOptions queryOptions = queryOptionsOptional.get();
        return user.getInheritedGroups(queryOptions)
                   .stream()
                   .anyMatch(group -> group.getName().equalsIgnoreCase(groupName));
    }
	
	 public static void addGroupToPlayer(UUID uuid, String groupName) {
	        LuckPerms luckPerms = LuckPermsProvider.get();

	        // Load the user asynchronously
	        luckPerms.getUserManager().loadUser(uuid).thenAcceptAsync(user -> {
	            // Create a group inheritance node
	            InheritanceNode node = InheritanceNode.builder(groupName).build();

	            // Add the node to the user
	            user.data().add(node);

	            // Save the user back to LuckPerms
	            luckPerms.getUserManager().saveUser(user);
	        });
	    }

	public static String getPrefix(Player p) {
		User user = Perms.loadUser(p);

		CachedMetaData metaData = user.getCachedData().getMetaData();
		String prefix = "";
		if (!(metaData.getPrefix() == null)) {
			prefix = ChatColor.translateAlternateColorCodes('&', prefix);
		}
		return prefix;
	}

}
