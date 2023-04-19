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
import tv.ender.chatgroups.utils.ReadWriteLock;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Accessors(fluent = true)
public class UserManagerImpl implements UserManager {
    @Getter
    private final Map<UUID, VoicechatConnection> connections = new HashMap<>();
    @Getter
    public final Map<UUID, GroupUserImpl> users = new ConcurrentHashMap<>();
    private final ReadWriteLock lock = new ReadWriteLock();

    public UserManagerImpl() {
        // Empty
    }

    public void register(PlayerConnectedEvent event) {
        var conn = event.getConnection();
        var player = conn.getPlayer();

        this.lock.write(() -> {
            this.connections.put(player.getUuid(), conn);
            this.users.computeIfAbsent(player.getUuid(), this::createUser);
        });
    }

    public void unregister(PlayerDisconnectedEvent event) {
        this.lock.write(() -> this.connections.remove(event.getPlayerUuid()));
    }

    @Override
    @Nullable
    public GroupUser getUser(UUID uuid) {
        var user = this.lock.read(() -> this.users.get(uuid));

        if (user != null) {
            return user;
        }

        return this.lock.write(() -> this.users.put(uuid, this.createUser(uuid)));
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
        return this.lock.read(() -> this.connections.get(uuid));
    }

    @Override
    public boolean isConnected(UUID uuid) {
        return this.lock.read(() -> this.connections.containsKey(uuid));
    }
}
