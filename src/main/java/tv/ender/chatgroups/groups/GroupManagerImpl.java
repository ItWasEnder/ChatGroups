package tv.ender.chatgroups.groups;

import lombok.Getter;
import lombok.experimental.Accessors;
import tv.ender.chatgroups.exceptions.GroupAlreadyExistsException;
import tv.ender.chatgroups.exceptions.InvalidGroupIdException;
import tv.ender.chatgroups.exceptions.InvalidUserException;
import tv.ender.chatgroups.exceptions.NonPlayerException;
import tv.ender.chatgroups.guis.screens.GroupOptionsScreen;
import tv.ender.chatgroups.interfaces.GroupManager;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Stream;

@Accessors(fluent = true)
public class GroupManagerImpl implements GroupManager {
    @Getter
    private final Map<String, Group> groups = new HashMap<>();

    public GroupManagerImpl() {
        // Empty
    }

    @Override
    public Group register(Group group) throws GroupAlreadyExistsException {
        if (this.groups.containsKey(group.id())) {
            throw new GroupAlreadyExistsException(group.id());
        }

        this.groups.put(group.id(), group);

        return group;
    }

    @Override
    public void unregister(Group group) {
        this.groups.remove(group.id());
    }

    @Override
    public void unregister(String id) throws InvalidGroupIdException {
        this.checkGroup(id);

        this.groups.remove(id);
    }

    @Override
    public Stream<Group> findGroups(UUID uuid) {
        return this.groups.values().stream()
                .filter(group -> group.members().contains(uuid))
                .sorted(Comparator.comparingLong(Group::timeCreated).reversed());
    }

    @Override
    public void joinGroup(String user, String id) throws InvalidUserException, InvalidGroupIdException {
        this.checkGroup(id);
        this.checkUser(user);

        final Group group = this.groups.get(id);

        var offlinePlayer = Bukkit.getOfflinePlayerIfCached(user);

        assert offlinePlayer != null;
        group.members().add(offlinePlayer.getUniqueId());
    }

    @Override
    public void leaveGroup(String user, String id) throws InvalidUserException, InvalidGroupIdException {
        this.checkGroup(id);
        this.checkUser(user);

        final Group group = this.groups.get(id);

        var offlinePlayer = Bukkit.getOfflinePlayerIfCached(user);

        assert offlinePlayer != null;
        group.members().remove(offlinePlayer.getUniqueId());
    }

    public void openOptions(CommandSender target, String id) throws NonPlayerException, InvalidGroupIdException {
        this.checkGroup(id);

        if (!(target instanceof Player player)) {
            throw new NonPlayerException();
        }

        GroupOptionsScreen.wrap(player, this.groups.get(id)).show();
    }

    /* Value Checks */

    private void checkUser(String user) throws InvalidUserException {
        var offlinePlayer = Bukkit.getOfflinePlayerIfCached(user);

        if (offlinePlayer == null) {
            throw new InvalidUserException(user);
        }
    }

    private void checkGroup(String id) throws InvalidGroupIdException {
        if (!this.groups.containsKey(id)) {
            throw new InvalidGroupIdException(id);
        }
    }
}
