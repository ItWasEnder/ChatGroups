package tv.ender.chatgroups.guis;

import com.marcusslover.plus.lib.task.Task;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

public class GuiManager implements Listener {
    private static GuiManager instance;
    private final Map<UUID, GuiScreen> screens;
    private final Map<UUID, GuiScreen> player_screens;

    private GuiManager(JavaPlugin plugin) {
        this.screens = new ConcurrentHashMap<>();
        this.player_screens = new ConcurrentHashMap<>();

        Task.syncRepeating(plugin, () -> {
            instance.screens.values().forEach(GuiScreen::update);
            instance.player_screens.values().forEach(GuiScreen::update);
        }, 1L, 1L);

        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    public static void addScreen(UUID uuid, GuiScreen screen) {
        instance.screens.put(uuid, screen);
    }

    public static void addPlayer(UUID uuid, GuiScreen screen) {
        instance.player_screens.put(uuid, screen);
    }

    /**
     * @param uuid     The UUID of the player to get.
     * @param supplier The supplier to use if an instance doesn't exist.
     * @return The GuiScreen that represents the player's inventory.
     */
    static GuiScreen getPlayer(UUID uuid, Supplier<GuiScreen> supplier) {
        return instance.player_screens.getOrDefault(uuid, supplier.get());
    }

    public static GuiScreen getScreen(UUID uuid) {
        GuiScreen screen = instance.screens.get(uuid);

        if (screen == null) {
            screen = instance.player_screens.get(uuid);
        }

        return screen;
    }

    @EventHandler(priority = EventPriority.LOW)
    private void onClick(InventoryClickEvent event) {
        GuiScreen screen = GuiManager.getScreen(event.getWhoClicked().getUniqueId());

        if (screen != null) {
            event.setCancelled(true);
            screen.handleClick(event);
        }
    }

    @EventHandler
    private void onInteract(PlayerInteractEvent event) {
        Action action = event.getAction();

        if (action != Action.RIGHT_CLICK_AIR && action != Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        GuiScreen screen = GuiManager.getScreen(event.getPlayer().getUniqueId());

        if (screen != null) {
            screen.handleInteract(event);
        }
    }

    @EventHandler
    private void onClose(InventoryCloseEvent event) {
        GuiScreen screen = getScreen(event.getPlayer().getUniqueId());

        if (screen == null) {
            return;
        }

        if (!screen.getInventory().equals(event.getInventory())) {
            return;
        }

        if (screen.handleClose(event)) {
            instance.screens.remove(screen.getPlayer().getUniqueId());
            instance.player_screens.remove(screen.getPlayer().getUniqueId());
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    private void onQuit(PlayerQuitEvent event) {
        instance.screens.remove(event.getPlayer().getUniqueId());
        instance.player_screens.remove(event.getPlayer().getUniqueId());
    }

    protected static int getNextId() {
        int max = 0;

        for (GuiScreen screen : instance.screens.values()) {
            if (screen.getInternalId() > max) {
                max = screen.getInternalId();
            }
        }

        return max + 1;
    }

    public static void enable(JavaPlugin plugin) {
        if (GuiManager.instance == null) {
            GuiManager.instance = new GuiManager(plugin);
        }
    }
}