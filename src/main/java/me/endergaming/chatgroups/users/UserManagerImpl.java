package me.endergaming.chatgroups.users;

import de.maxhenkel.voicechat.api.VoicechatConnection;
import de.maxhenkel.voicechat.api.events.PlayerConnectedEvent;
import de.maxhenkel.voicechat.api.events.PlayerDisconnectedEvent;
import lombok.Getter;
import me.endergaming.chatgroups.interfaces.UserManager;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class UserManagerImpl implements UserManager {
    @Getter
    private final Map<UUID, VoicechatConnection> connections = new HashMap<>();

    public UserManagerImpl() {
        // Empty
    }

    public void register(PlayerConnectedEvent event) {
        this.connections.put(event.getConnection().getPlayer().getUuid(), event.getConnection());
    }

    public void unregister(PlayerDisconnectedEvent event) {
        this.connections.remove(event.getPlayerUuid());
    }

    @Override
    public VoicechatConnection getConnection(UUID uuid) {
        return this.connections.get(uuid);
    }

    @Override
    public boolean isConnected(UUID uuid) {
        return this.connections.containsKey(uuid);
    }
}
