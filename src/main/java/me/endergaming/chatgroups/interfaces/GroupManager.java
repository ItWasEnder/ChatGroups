package me.endergaming.chatgroups.interfaces;

import me.endergaming.chatgroups.exceptions.GroupAlreadyExistsException;
import me.endergaming.chatgroups.exceptions.InvalidGroupIdException;
import me.endergaming.chatgroups.exceptions.InvalidUserException;
import me.endergaming.chatgroups.groups.Group;

import java.util.Map;
import java.util.UUID;
import java.util.stream.Stream;

public interface GroupManager {
    Group register(Group group) throws GroupAlreadyExistsException;

    void unregister(Group group);

    void unregister(String id) throws InvalidGroupIdException;

    Map<String, Group> getGroups();

    Stream<Group> findGroups(UUID uuid);

    void joinGroup(String user, String id) throws InvalidUserException, InvalidGroupIdException;

    void leaveGroup(String user, String id) throws InvalidUserException, InvalidGroupIdException;
}
