package tv.ender.chatgroups.interfaces;

import tv.ender.chatgroups.exceptions.GroupAlreadyExistsException;
import tv.ender.chatgroups.exceptions.InvalidGroupIdException;
import tv.ender.chatgroups.exceptions.InvalidUserException;
import tv.ender.chatgroups.groups.Group;

import java.util.Map;
import java.util.UUID;
import java.util.stream.Stream;

public interface GroupManager {
    Group register(Group group) throws GroupAlreadyExistsException;

    void unregister(Group group);

    void unregister(String id) throws InvalidGroupIdException;

    Map<String, Group> groups();

    Stream<Group> findGroups(UUID uuid);

    void joinGroup(String user, String id) throws InvalidUserException, InvalidGroupIdException;

    void leaveGroup(String user, String id) throws InvalidUserException, InvalidGroupIdException;
}
