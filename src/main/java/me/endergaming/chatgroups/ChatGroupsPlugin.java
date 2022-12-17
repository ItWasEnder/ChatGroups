package me.endergaming.chatgroups;

import com.marcusslover.plus.lib.command.CommandManager;
import me.endergaming.chatgroups.command.ChatGroupsCommand;
import me.endergaming.chatgroups.groups.GroupManagerImpl;
import me.endergaming.chatgroups.guis.GuiManager;
import org.bukkit.Bukkit;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.Nullable;

import java.util.logging.Logger;

public final class ChatGroupsPlugin extends JavaPlugin {
    CommandManager commandManager;

    @Override
    public void onEnable() {
        /* Setup commands */
        this.commandManager = CommandManager.get(this);
        this.commandManager.register(new ChatGroupsCommand((GroupManagerImpl) ChatGroups.get().getGroupManager()));

        /* Setup managers */
        ChatGroups.get();
        GuiManager.enable();
    }

    @Override
    public void onDisable() {
        /* Unregister everything */
        HandlerList.unregisterAll(this);
    }

    public static Logger logger() {
        return getInstance().getLogger();
    }

    public static @Nullable Plugin getInstance() {
        return Bukkit.getPluginManager().getPlugin("ChatGroups");
    }
}
