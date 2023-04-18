package tv.ender.chatgroups.interfaces;

public interface ChatGroupsAPI {
    GroupManager getGroupManager();

    UserManager getUserManager();

    double getBroadcastRange();
}
