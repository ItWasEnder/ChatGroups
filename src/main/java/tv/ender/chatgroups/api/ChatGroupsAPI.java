package tv.ender.chatgroups.api;

import org.bukkit.plugin.java.JavaPlugin;
import tv.ender.chatgroups.interfaces.GroupManager;
import tv.ender.chatgroups.interfaces.UserManager;

public interface ChatGroupsAPI {
    GroupManager getGroupManager();

    UserManager getUserManager();

    double getBroadcastRange();

    /**
     * Registers the plugin to the ChatGroups API
     *
     * @param plugin The plugin to register
     * @throws IllegalStateException If the api is already registered
     */
    void register(JavaPlugin plugin);

    static ChatGroupsAPI get() {
        return ChatGroups.get();
    }

    JavaPlugin getRegisteringPlugin();
}
