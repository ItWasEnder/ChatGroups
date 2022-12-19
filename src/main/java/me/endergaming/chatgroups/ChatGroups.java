package me.endergaming.chatgroups;

import de.maxhenkel.voicechat.api.Position;
import de.maxhenkel.voicechat.api.VoicechatApi;
import de.maxhenkel.voicechat.api.VoicechatConnection;
import de.maxhenkel.voicechat.api.VoicechatPlugin;
import de.maxhenkel.voicechat.api.events.EntitySoundPacketEvent;
import de.maxhenkel.voicechat.api.events.EventRegistration;
import de.maxhenkel.voicechat.api.events.MicrophonePacketEvent;
import de.maxhenkel.voicechat.api.events.PlayerConnectedEvent;
import de.maxhenkel.voicechat.api.events.PlayerDisconnectedEvent;
import de.maxhenkel.voicechat.api.packets.StaticSoundPacket;
import me.endergaming.chatgroups.groups.GroupManagerImpl;
import me.endergaming.chatgroups.groups.Options;
import me.endergaming.chatgroups.interfaces.ChatGroupsAPI;
import me.endergaming.chatgroups.interfaces.GroupManager;
import me.endergaming.chatgroups.interfaces.UserManager;
import me.endergaming.chatgroups.users.UserManagerImpl;
import me.endergaming.chatgroups.utils.OptionsMap;
import org.bukkit.Location;

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

    @Override
    public void initialize(VoicechatApi api) {
        VoicechatPlugin.super.initialize(api);
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

        var api = event.getVoicechat();

        UUID sender = event.getSenderConnection().getPlayer().getUuid();
        final Set<UUID> listeners = new HashSet<>();
        final Set<UUID> rangedListeners = new HashSet<>();

        var groups = this.groupManager.findGroups(sender);

//        /* Obtain dominant group options */
//        OptionsMap overrides = new OptionsMap();
//        groups.findFirst().ifPresent(group -> {
//            overrides.put(Options.AUDIBLE_TO_OTHERS, group.audibleToOthers());
//            overrides.put(Options.DISTANCE_IGNORED, group.distanceIgnored());
//            overrides.put(Options.MUTED, group.muted());
//            overrides.put(Options.RANGE, group.range());
//        });

        groups.forEach(group -> {
            if (group.muted()) {
                event.cancel();
            }

            if (group.distanceIgnored()) {
                group.members().forEach(m -> {
                    if (m != sender) {
                        listeners.add(m);
                    }
                });
            }

            var user = event.getSenderConnection().getPlayer();

            if (group.range() != api.getBroadcastRange()) {
                var pos = user.getPosition();

                var nearby = api.getPlayersInRange(user.getServerLevel(), pos, group.range(), player -> {
                    return !isInRange(pos, player.getPosition(), api.getBroadcastRange());
                });

                nearby.forEach(player -> {
                    if (player.getUuid() != sender) {
                        rangedListeners.add(player.getUuid());
                    }
                });
            }
        });

        if (event.isCancelled()) {
            return;
        }

        event.getVoicechat().getPlayersInRange(null, null, 200);
        event.getVoicechat().getBroadcastRange();


        if (!listeners.isEmpty()) {
            StaticSoundPacket packet = event.getPacket().toStaticSoundPacket();
            listeners.forEach(uuid -> {
                if (sender.equals(uuid)) {
                    return;
                }

                VoicechatConnection connection = this.userManager.getConnections().get(uuid);

                if (connection != null) {
                    event.getVoicechat().sendStaticSoundPacketTo(connection, packet);
                }
            });
        }

        if (!rangedListeners.isEmpty()) {
            var pos = event.getSenderConnection().getPlayer().getPosition();

            rangedListeners.forEach(uuid -> {
                if (sender.equals(uuid)) {
                    return;
                }

                VoicechatConnection connection = this.userManager.getConnections().get(uuid);

                if (connection != null) {
                    api.sendLocationalSoundPacketTo(connection, event.getPacket().toLocationalSoundPacket(pos));
                }
            });
        }
    }

    public static boolean isInRange(Position pos1, Position pos2, double range) {
        return Math.abs(pos1.getX() - pos2.getX()) <= range && Math.abs(pos1.getY() - pos2.getY()) <= range && Math.abs(pos1.getZ() - pos2.getZ()) <= range;
    }
}
