package tv.ender.chatgroups.interfaces;

import lombok.experimental.Accessors;

import java.util.UUID;

@Accessors(fluent = true, chain = true)
public interface GroupUser {
    String name();

    UUID uuid();

    boolean muted();

    void muted(boolean isMuted);
}
