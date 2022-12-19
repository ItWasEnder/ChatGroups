package me.endergaming.chatgroups.interfaces;

public interface ChatGroupsAPI {
    GroupManager getGroupManager();

    UserManager getUserManager();

    double getBroadcastRange();
}
