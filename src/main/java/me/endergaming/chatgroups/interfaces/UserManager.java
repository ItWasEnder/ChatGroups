package me.endergaming.chatgroups.interfaces;

import de.maxhenkel.voicechat.api.VoicechatConnection;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public interface UserManager {
    @Nullable GroupUser getUser(UUID uuid);

    @Nullable VoicechatConnection getConnection(UUID uuid);

    boolean isConnected(UUID uuid);
}
