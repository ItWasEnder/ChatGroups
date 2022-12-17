package me.endergaming.chatgroups.interfaces;

import com.marcusslover.plus.lib.task.Task;
import com.marcusslover.plus.lib.text.Text;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import me.endergaming.chatgroups.guis.GuiScreen;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

@Accessors(fluent = true)
public abstract class BaseScreen<T> {
    @Getter
    GuiScreen screen;

    @Getter
    final Player player;

    @Setter
    Consumer<InventoryCloseEvent> onExit;

    int rows = 1;

    @Getter @Setter
    String title;

    boolean closable = true;

    protected BaseScreen(Player player, Consumer<InventoryCloseEvent> onExit, String title, int rows) {
        this.player = player;
        this.onExit = onExit;
        this.rows = rows;
        this.title = title;
    }

    protected void setup() {
        if (this.screen == null) {
            this.screen = new GuiScreen(this.player, Text.of(this.title).comp(), 9 * this.rows);
        }

        this.screen.onClose(event -> {
            if (!this.closable && event.getReason() == InventoryCloseEvent.Reason.PLAYER) {
                Task.syncDelayed(this::show, 2L);

                return;
            }

            if (this.onExit != null) {
                this.onExit.accept(event);
            }
        });

        this.setupItems();
    }

    protected abstract void setupItems();

    public void refresh() {
        this.setupItems();
    }

    public void show() {
        this.refresh();

        if (this.screen != null) {
            this.screen.show();
        }
    }

    public void closable(boolean closable) {
        this.closable = closable;
    }

    public void close() {
        this.player.closeInventory();
    }
}
