package me.endergaming.chatgroups;

import de.maxhenkel.voicechat.api.VoicechatConnection;
import de.maxhenkel.voicechat.api.VoicechatPlugin;
import de.maxhenkel.voicechat.api.events.EntitySoundPacketEvent;
import de.maxhenkel.voicechat.api.events.EventRegistration;
import de.maxhenkel.voicechat.api.events.MicrophonePacketEvent;
import de.maxhenkel.voicechat.api.events.PlayerConnectedEvent;
import de.maxhenkel.voicechat.api.events.PlayerDisconnectedEvent;
import de.maxhenkel.voicechat.api.packets.StaticSoundPacket;
import me.endergaming.chatgroups.groups.GroupManagerImpl;
import me.endergaming.chatgroups.interfaces.ChatGroupsAPI;
import me.endergaming.chatgroups.interfaces.GroupManager;
import me.endergaming.chatgroups.interfaces.UserManager;
import me.endergaming.chatgroups.users.UserManagerImpl;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class ChatGroups implements ChatGroupsAPI, VoicechatPlugin {
    private static ChatGroups instance;
    private final GroupManagerImpl groupManager;
    private final UserManagerImpl userManager;

    private ChatGroups() {
        this.groupManager = new GroupManagerImpl();
        this.userManager = new UserManagerImpl();
    }

    @Override
    public GroupManager getGroupManager() {
        return this.groupManager;
    }

    @Override
    public UserManager getUserManager() {
        return this.userManager;
    }

    public static ChatGroupsAPI get() {
        return instance == null ? instance = new ChatGroups() : instance;
    }

    @Override
    public String getPluginId() {
        return "chat_groups_api";
    }

    @Override
    public void registerEvents(EventRegistration registration) {
        registration.registerEvent(PlayerConnectedEvent.class, this.userManager::register);
        registration.registerEvent(PlayerDisconnectedEvent.class, this.userManager::unregister);
        registration.registerEvent(EntitySoundPacketEvent.class, this::onEntitySoundPacket);
        registration.registerEvent(MicrophonePacketEvent.class, this::onMicrophonePacket);
    }

    private void onEntitySoundPacket(EntitySoundPacketEvent event) {
        UUID receiver = event.getReceiverConnection().getPlayer().getUuid();
        UUID sender = event.getSenderConnection().getPlayer().getUuid();
        boolean isListeningRestricted = false;
        boolean isInSameGroup = false;
        for (var group : this.groupManager.getGroups().values()) {
            boolean isSenderInGroup = group.members().contains(sender);
            boolean isReceiverInGroup = group.members().contains(receiver);
            isListeningRestricted |= isReceiverInGroup || (isSenderInGroup && !group.audibleToOthers());
            if (isSenderInGroup && isReceiverInGroup) {
                if (group.distanceIgnored()) {
                    /* Static sound packets have already been sent to players in this group */
                    event.cancel();
                    return;
                }
                isInSameGroup = true;
            }
        }
        if (isListeningRestricted && !isInSameGroup) {
            event.cancel();
        }
    }

    private void onMicrophonePacket(MicrophonePacketEvent event) {

        if (event.getSenderConnection() == null) {
            return;
        }
        UUID sender = event.getSenderConnection().getPlayer().getUuid();
        Set<UUID> listeners = null;
        for (var group : this.groupManager.getGroups().values()) {
            if (!group.distanceIgnored()) {
                continue;
            }
            if (group.members().contains(sender)) {
                if (listeners == null) {
                    listeners = new HashSet<>();
                }
                listeners.addAll(group.members());
            }
        }
        if (listeners != null) {
            StaticSoundPacket packet = event.getPacket().toStaticSoundPacket();
            listeners.forEach((listener) -> {
                if (sender.equals(listener)) {
                    return;
                }
                VoicechatConnection connection = this.userManager.getConnections().get(listener);
                if (connection != null) {
                    event.getVoicechat().sendStaticSoundPacketTo(connection, packet);
                }
            });
        }
    }
}
