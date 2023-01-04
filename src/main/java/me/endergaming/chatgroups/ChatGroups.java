package me.endergaming.chatgroups;

import com.marcusslover.plus.lib.text.Text;
import de.maxhenkel.voicechat.api.BukkitVoicechatService;
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
import me.endergaming.chatgroups.interfaces.ChatGroupsAPI;
import me.endergaming.chatgroups.interfaces.GroupManager;
import me.endergaming.chatgroups.interfaces.UserManager;
import me.endergaming.chatgroups.users.UserManagerImpl;
import org.bukkit.Bukkit;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

public class ChatGroups implements ChatGroupsAPI, VoicechatPlugin {
    private static ChatGroups instance;
    private final GroupManagerImpl groupManager;
    private final UserManagerImpl userManager;
    private VoicechatApi api;

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
        if (instance == null) {
            instance = new ChatGroups();
            Bukkit.getServicesManager().load(BukkitVoicechatService.class).registerPlugin(instance);
        }

        return instance;
    }

    @Override
    public double getBroadcastRange() {
        return this.api != null ? this.api.getVoiceChatDistance() : 24.0;
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
        this.api = api;
    }

    private void onEntitySoundPacket(EntitySoundPacketEvent event) {
        UUID receiver = event.getReceiverConnection().getPlayer().getUuid();
        UUID sender = event.getSenderConnection().getPlayer().getUuid();
        boolean isListeningRestricted = false;
        boolean isInSameGroup = false;

        for (var group : this.groupManager.groups().values()) {
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

        var user = event.getSenderConnection().getPlayer();
        UUID userId = event.getSenderConnection().getPlayer().getUuid();
        var groupUser = this.userManager.users.get(userId);

        final Set<UUID> listeners = new HashSet<>();
        final Set<UUID> rangedListeners = new HashSet<>();

        /* Check if dominant group is muting this player also if player is just muted */
        var first = this.groupManager.findGroups(userId).findFirst();
        if ((first.isPresent() && first.get().muted()) || (groupUser != null && groupUser.muted())) {
            event.cancel();

            Text.of("&cYou are muted!").sendActionBar(
                    Objects.requireNonNull(Bukkit.getPlayer(userId)));

            return;
        }

        var groups = this.groupManager.findGroups(userId);
        groups.forEach(group -> {
            if (group.distanceIgnored()) {
                group.members().forEach(m -> {
                    if (m != userId) {
                        listeners.add(m);
                    }
                });
            } else if (group.range() != api.getBroadcastRange()) {
                var pos = user.getPosition();

                /* Get all nearby members */
                var nearby = api.getPlayersInRange(user.getServerLevel(), pos, group.range(),
                        player -> group.members().contains(player.getUuid()));

                nearby.forEach(serverPlayer -> {
                    if (serverPlayer.getUuid() != userId) {
                        rangedListeners.add(serverPlayer.getUuid());
                    }
                });
            }
        });

        if (!listeners.isEmpty()) {
            event.cancel();

            StaticSoundPacket packet = event.getPacket().staticSoundPacketBuilder().build();
            listeners.forEach(uuid -> {
                if (userId.equals(uuid)) {
                    return;
                }

                VoicechatConnection connection = this.userManager.connections().get(uuid);

                if (connection != null) {
                    event.getVoicechat().sendStaticSoundPacketTo(connection, packet);
                }
            });
        }

        if (!rangedListeners.isEmpty()) {
            event.cancel();

            var pos = event.getSenderConnection().getPlayer().getPosition();

            rangedListeners.forEach(uuid -> {
                if (userId.equals(uuid)) {
                    return;
                }

                VoicechatConnection connection = this.userManager.connections().get(uuid);

                var builder = event.getPacket().locationalSoundPacketBuilder();

                builder.position(pos);
                builder.distance(first.isEmpty() ? (float) api.getBroadcastRange() : (float) first.get().range());

                if (connection != null) {
                    api.sendLocationalSoundPacketTo(connection, builder.build());
                }
            });
        }
    }
}
