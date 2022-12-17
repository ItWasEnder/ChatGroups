package me.endergaming.chatgroups.guis.screens;

import com.marcusslover.plus.lib.item.Item;
import com.marcusslover.plus.lib.sound.Note;
import lombok.Getter;
import me.endergaming.chatgroups.groups.Group;
import me.endergaming.chatgroups.groups.Options;
import me.endergaming.chatgroups.interfaces.BaseScreen;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedHashSet;
import java.util.List;

public class GroupOptionsScreen extends BaseScreen<GroupOptionsScreen> {
    @Getter
    Group group;

    long lastClick = 0L;

    private final LinkedHashSet<Integer> prettySlots = new LinkedHashSet<>() {{
        this.addAll(List.of(11, 12, 13, 14, 15, 20, 21, 22, 23, 24));
    }};

    private GroupOptionsScreen(Player player, @NotNull Group group) {
        super(player, null, "Group Options &8(%s)".formatted(group.id()), 4);
        this.group = group;

        /* Setup GUI */
        this.setup();
    }

    @Override
    protected void setupItems() {
        /* Fill empty */
        var filler = Item.of(Material.GRAY_STAINED_GLASS_PANE, 1, " ").get();
        for (int i = 0; i < this.screen().getInventory().getSize(); i++) {
            if (this.prettySlots.contains(i)) {
                continue;
            }

            this.screen().createItemAnd(i, () -> filler);
        }

        /* Add option buttons */
        Integer[] slots = this.prettySlots.toArray(new Integer[0]);
        for (Options option : Options.values()) {
            if (option.equals(Options.NONE)) {
                continue;
            }

            if (option.ordinal() > slots.length) {
                break;
            }

            int slot = slots[option.ordinal()];

            this.screen().createItemAnd(slot, () -> {
                var item = Item.of(option.material(), 1, option.display());

                try {
                    item.lore(List.of(" ", "&7Status: &d%s".formatted(this.getSetting(option))));
                } catch (Exception e) {
                    e.printStackTrace();
                }

                return item.get();
            }, (event, screen) -> {
                if (System.currentTimeMillis() - this.lastClick < 250L) {
                    return;
                }

                this.lastClick = System.currentTimeMillis();

                try {
                    this.modifySetting(option);

                    Note.of(Sound.BLOCK_NOTE_BLOCK_PLING).play(this.player());
                } catch (Exception e) {
                    e.printStackTrace();
                }

                this.refresh();
            });
        }
    }

    private void modifySetting(Options option) {
        switch (option) {
            case AUDIBLE_TO_OTHERS -> {
                this.group.audibleToOthers(!this.group.audibleToOthers());
            }

            case DISTANCE_IGNORED -> {
                this.group.distanceIgnored(!this.group.distanceIgnored());
            }

            case MUTED -> {
                this.group.muted(!this.group.muted());
            }

            case RANGE -> {
                /* TODO: Create input method for this */
            }

            default -> throw new IllegalArgumentException("Unexpected value: " + option);
        }
    }

    private String getSetting(Options option) throws IllegalArgumentException {
        switch (option) {
            case AUDIBLE_TO_OTHERS -> {
                return String.valueOf(this.group.audibleToOthers());
            }

            case DISTANCE_IGNORED -> {
                return String.valueOf(this.group.distanceIgnored());
            }

            case MUTED -> {
                return String.valueOf(this.group.muted());
            }

            case RANGE -> {
                return String.valueOf(this.group.range());
            }

            default -> throw new IllegalArgumentException("Unexpected value: " + option);
        }
    }


    public static GroupOptionsScreen wrap(Player player, Group group) {
        return new GroupOptionsScreen(player, group);
    }
}
