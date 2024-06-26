package tv.ender.chatgroups.groups;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import tv.ender.chatgroups.api.ChatGroups;
import tv.ender.chatgroups.api.ChatGroupsAPI;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Accessors(fluent = true, chain = true)
public class Group {
    @Getter
    private final String id;

    @Getter
    private final long timeCreated;

    @Getter
    private Set<UUID> members = new HashSet<>();

    @Getter @Setter
    private boolean distanceIgnored = false;

    @Getter @Setter
    private boolean audibleToOthers = true;

    @Getter @Setter
    private boolean muted = false;

    @Getter @Setter
    private double range;

    public Group(String id, Collection<UUID> members) {
        this.id = id;
        this.members.addAll(members);
        this.timeCreated = System.currentTimeMillis();
        this.range = ChatGroupsAPI.get().getBroadcastRange();
    }

    public Group(String id) {
        this.id = id;
        this.members = new HashSet<>();
        this.timeCreated = System.currentTimeMillis();
        this.range = ChatGroupsAPI.get().getBroadcastRange();
    }
}
