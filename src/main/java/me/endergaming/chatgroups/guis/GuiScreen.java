package me.endergaming.chatgroups.guis;

import com.marcusslover.plus.lib.item.Item;
import com.marcusslover.plus.lib.text.Text;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class GuiScreen {

	private final Inventory inventory;
	private final Player player;

	private final Map<Integer, GuiItem> items = new LinkedHashMap<>();

	private Consumer<InventoryCloseEvent> closeHandler;

	private boolean needUpdate = false;
	private final int internalId;

	public GuiScreen(Player player, String name, int size) {
		this.inventory = Bukkit.createInventory(player, size, Text.of(name).comp());
		this.player = player;
		this.internalId = GuiManager.getNextId();
	}

	public GuiScreen(Player player, Component name, int size) {
		this.inventory = Bukkit.createInventory(player, size, name);
		this.player = player;
		this.internalId = GuiManager.getNextId();
	}

	public GuiScreen(Player player, String name, InventoryType type) {
		this.inventory = Bukkit.createInventory(player, type, name);
		this.player = player;
		this.internalId = GuiManager.getNextId();
	}

	private GuiScreen(Player player, Inventory inventory) {
		this.player = player;
		this.inventory = inventory;
		this.internalId = GuiManager.getNextId();
	}

	public void show() {
		GuiManager.addPlayer(this.player.getUniqueId(), this);

		this.player.openInventory(this.inventory);
	}

	public Player getPlayer() {
		return this.player;
	}

	public Inventory getInventory() {
		return this.inventory;
	}

	public GuiItem createItem(ItemStack itemStack) {
		return new GuiItem(this, itemStack);
	}

	/**
	 * Creates a new GuiItem with the given configuration.
	 * @param slot The slot to place the item in.
	 * @param itemStack The item to use.
	 * @param clickHandler The click handler to use.
	 */
	public void createItemAnd(int slot, Supplier<@NotNull ItemStack> itemStack, BiConsumer<InventoryClickEvent, GuiItem> clickHandler) {
		var item = new GuiItem(this, itemStack.get());

		this.setItem(slot, item);
		item.onClick(clickHandler);
	}

	/**
	 * Creates a new GuiItem with the given configuration.
	 * @param slot The slot to place the item in.
	 * @param itemStack The item to use.
	 */
	public void createItemAnd(int slot, Supplier<@NotNull ItemStack> itemStack) {
		var item = new GuiItem(this, itemStack.get());

		this.setItem(slot, item);
	}

	public GuiItem setItem(int slot, GuiItem item) {
		if (slot >= this.inventory.getSize()) {
			return item;
		}

		this.items.put(slot, item);
		this.inventory.setItem(slot, item.getItemStack());

		return item;
	}

	public GuiItem getItem(int slot) {
		return this.items.get(slot);
	}

	public void emptySlot(int slot) {
		this.items.remove(slot);
		this.inventory.setItem(slot, new ItemStack(Material.AIR));
	}

	public void onClose(Consumer<InventoryCloseEvent> closeHandler) {
		this.closeHandler = closeHandler;
	}

	void forceUpdate() {
		this.needUpdate = true;
	}

	void update() {
		if (!this.needUpdate) {
			return;
		}

		synchronized (this.items) {
			for (Map.Entry<Integer, GuiItem> entry : this.items.entrySet()) {
				this.inventory.setItem(entry.getKey(), entry.getValue().getItemStack());
			}
		}

		this.player.updateInventory();
		this.needUpdate = false;
	}

	void handleClick(InventoryClickEvent event) {
		GuiItem item = this.items.get(event.getRawSlot());

		if (item != null) {
			item.handleClick(event);
		}
	}

	void handleInteract(PlayerInteractEvent event) {
		GuiItem item = this.items.get(event.getPlayer().getInventory().getHeldItemSlot());

		if (item != null) {
			event.setCancelled(true);
			item.handleInteract(event);
		}
	}

	public boolean handleClose(InventoryCloseEvent event) {
		if (this.closeHandler != null) {
			this.closeHandler.accept(event);
		}

		return event.getPlayer().getOpenInventory().getTopInventory().equals(this.inventory);
	}

	public static GuiScreen wrap(Player player) {
		return GuiManager.getPlayer(player.getUniqueId(), () -> new GuiScreen(player, player.getOpenInventory().getBottomInventory()));
	}

	protected GuiItem createBackItem() {
		return this.createItem(Item.of(Material.ARROW, 1).name(ChatColor.AQUA + "Go Back!").get());
	}

	public void clear() {
		this.items.clear();
		this.inventory.clear();
	}

	public int getInternalId() {
		return this.internalId;
	}
}