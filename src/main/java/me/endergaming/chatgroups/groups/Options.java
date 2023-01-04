package me.endergaming.chatgroups.groups;

import lombok.Getter;
import lombok.experimental.Accessors;
import org.bukkit.Material;

/* Access control for GUI */
public enum Options {
    DISTANCE_IGNORED("Distance Ignored", Material.YELLOW_CONCRETE),
    AUDIBLE_TO_OTHERS("Audible To Others", Material.DIAMOND_BLOCK),
    RANGE("Range", Material.OAK_SIGN),
    MUTED("Muted", Material.WHITE_WOOL),
    PLAYERS("Players", Material.PLAYER_HEAD),
    NONE("", Material.AIR),
    ;

    public static String ITEM_TAG = "option";
    public static String DEFAULT_TAG = "error";

    @Getter
    @Accessors(fluent = true) final String display;

    @Getter
    @Accessors(fluent = true) final Material material;

    Options(String display, Material material) {
        this.display = display;
        this.material = material;
    }
}
