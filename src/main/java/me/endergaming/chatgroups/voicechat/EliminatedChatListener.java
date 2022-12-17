package me.endergaming.chatgroups.voicechat;

import de.maxhenkel.voicechat.api.BukkitVoicechatService;
import de.maxhenkel.voicechat.api.VoicechatPlugin;
import de.maxhenkel.voicechat.api.events.EntitySoundPacketEvent;
import de.maxhenkel.voicechat.api.events.EventRegistration;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.util.UUID;

@Deprecated
public class EliminatedChatListener implements VoicechatPlugin, Listener {
    public EliminatedChatListener() {
        Bukkit.getServicesManager().load(BukkitVoicechatService.class).registerPlugin(this);
    }

    @Override
    public String getPluginId() {
        return "eliminated_chat_listener";
    }

    @Override
    public void registerEvents(EventRegistration registration) {
//        registration.registerEvent(EntitySoundPacketEvent.class, this::onEntitySoundPacket);
    }

//    @EventHandler
//    private void onAsyncPlayerChat(AsyncPlayerChatEvent event) {
//        Void container = ParticipantHandler.getInstance().getDataContainer();
//        Void participant = null;
//
//        if (participant == null) {
//            return;
//        }
//        if (participant.isVotedOut()) {
//            event.getRecipients().removeIf((listener) -> {
//                if (listener.hasPermission("survivor.admin")) {
//                    return false;
//                }
//                Participant p = container.retrieveLocally(listener.getUniqueId());
//                if (p == null) {
//                    return false;
//                }
//                return !p.isVotedOut();
//            });
//        }
//    }
//
//    private void onEntitySoundPacket(EntitySoundPacketEvent event) {
//        UUID receiver = event.getReceiverConnection().getPlayer().getUuid();
//        UUID sender = event.getSenderConnection().getPlayer().getUuid();
//        Void r = null;
//        Void s = null;
//        Player rp = (Player) event.getSenderConnection().getPlayer().getPlayer();
//        if (s.isVotedOut() && !r.isVotedOut() && !rp.hasPermission("survivor.admin")) {
//            event.cancel();
//        }
//    }
}
