package tv.ender.chatgroups.users;

import de.maxhenkel.voicechat.api.VoicechatConnection;
import de.maxhenkel.voicechat.api.events.PlayerConnectedEvent;
import de.maxhenkel.voicechat.api.events.PlayerDisconnectedEvent;
import lombok.Getter;
import lombok.experimental.Accessors;
import tv.ender.chatgroups.ChatGroupsPlugin;
import tv.ender.chatgroups.interfaces.GroupUser;
import tv.ender.chatgroups.interfaces.UserManager;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Accessors(fluent = true)
public class UserManagerImpl implements UserManager {
    @Getter
    private final Map<UUID, VoicechatConnection> connections = new HashMap<>();
    @Getter
    public final ConcurrentHashMap<UUID, GroupUserImpl> users = new ConcurrentHashMap<>();

    public UserManagerImpl() {
        // Empty
    }

    public void register(PlayerConnectedEvent event) {
        var conn = event.getConnection();
        var player = conn.getPlayer();

        this.connections.put(player.getUuid(), conn);

        this.users.computeIfAbsent(player.getUuid(), this::createUser);
    }

    public void unregister(PlayerDisconnectedEvent event) {
        this.connections.remove(event.getPlayerUuid());
    }

    @Override
    @Nullable
    public GroupUser getUser(UUID uuid) {
        return this.users.computeIfAbsent(uuid, this::createUser);
    }

    private GroupUserImpl createUser(UUID uuid) {
        var bukkitPlayer = Bukkit.getOfflinePlayer(uuid);

        if (bukkitPlayer.getName() == null || bukkitPlayer.getName().isEmpty()) {
            ChatGroupsPlugin.logger().warning("Player " + uuid + " does not have data on the server");
            return null;
        }

        return GroupUserImpl.builder()
                .name(bukkitPlayer.getName())
                .uuid(bukkitPlayer.getUniqueId())
                .muted(false)
                .build();
    }

    @Override
    @Nullable
    public VoicechatConnection getConnection(UUID uuid) {
        return this.connections.get(uuid);
    }

    @Override
    public boolean isConnected(UUID uuid) {
        return this.connections.containsKey(uuid);
    }
}
