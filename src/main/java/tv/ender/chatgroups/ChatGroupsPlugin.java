package tv.ender.chatgroups;

import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;
import tv.ender.chatgroups.api.ChatGroupsAPI;

import java.util.logging.Logger;

public final class ChatGroupsPlugin extends JavaPlugin {
    @Override
    public void onEnable() {
        /* Setup commands */
        ChatGroupsAPI.get().register(this);
    }

    @Override
    public void onDisable() {
        /* Unregister everything */
        HandlerList.unregisterAll(this);
    }

    public static Logger logger() {
        return Logger.getLogger("ChatGroups");
    }
}
