package me.endergaming.chatgroups.guis.screens;

import com.marcusslover.plus.lib.item.Item;
import com.marcusslover.plus.lib.sound.Note;
import lombok.Getter;
import me.endergaming.chatgroups.groups.Group;
import me.endergaming.chatgroups.interfaces.BaseScreen;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class PlayerManagementScreen extends BaseScreen<PlayerManagementScreen> {
    @Getter
    Group group;

    long lastClick = 0L;

    private PlayerManagementScreen(Player player, @NotNull Group group) {
        super(player, null, "Players &8(%s)".formatted(group.id()), 4);
        this.group = group;

        /* Setup GUI */
        this.setup();
    }

    @Override
    protected void setupItems() {
        /* Add option buttons */
        var members = this.group.members().stream().toList();
        for (int i = 0; i < this.group.members().size(); i++) {
            var id = members.get(i);
            var offline = Bukkit.getOfflinePlayer(id);
            var name = offline.getName() == null ? id.toString() : offline.getName();

            Item head = Item.of(Material.PLAYER_HEAD, 1, "&e%s".formatted(name));
            head.skull(offline);
            head.lore(List.of("", "&fShift-Click to remove from group"));

            this.screen().createItemAnd(i, head::get, (event, item) -> {
                if (event.isShiftClick()) {
                    this.group.members().remove(id);

                    Note.of(Sound.ENTITY_CREEPER_HURT, 1, 1).play(this.player());

                    this.refresh();
                }
            });
        }

        /* Fill empty */
        var filler = Item.of(Material.GRAY_STAINED_GLASS_PANE, 1, " ").get();
        for (int i = 0; i < this.screen().getInventory().getSize(); i++) {
            if (this.screen().getInventory().getItem(i) != null) {
                continue;
            }

            this.screen().createItemAnd(i, () -> filler);
        }
    }

    public static PlayerManagementScreen wrap(Player player, Group group) {
        return new PlayerManagementScreen(player, group);
    }
}
