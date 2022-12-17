package me.endergaming.chatgroups.interfaces;

import de.maxhenkel.voicechat.api.VoicechatConnection;

import java.util.UUID;

public interface UserManager {
    VoicechatConnection getConnection(UUID uuid);

    boolean isConnected(UUID uuid);
}
