package me.endergaming.chatgroups.users;

import lombok.Builder;
import lombok.ToString;
import me.endergaming.chatgroups.interfaces.GroupUser;

import java.util.UUID;

@Builder
@ToString
public class GroupUserImpl implements GroupUser {
    private String name;
    private final UUID uuid;
    private boolean muted;

    public void name(String name) {
        this.name = name;
    }

    @Override
    public String name() {
        return this.name;
    }

    @Override
    public UUID uuid() {
        return this.uuid;
    }

    @Override
    public boolean muted() {
        return this.muted;
    }

    @Override
    public void muted(boolean isMuted) {
        this.muted = isMuted;
    }

    @Override
    public int hashCode() {
        return this.uuid.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }

        if (obj == this) {
            return true;
        }

        if (obj instanceof GroupUserImpl gUser) {
            return gUser.uuid().equals(this.uuid);
        }

        return false;
    }
}
