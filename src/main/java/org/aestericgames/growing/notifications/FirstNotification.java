package org.aestericgames.growing.notifications;

import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.event.events.player.PlayerReadyEvent;
import com.hypixel.hytale.server.core.util.NotificationUtil;

import static com.hypixel.hytale.builtin.hytalegenerator.LoggerUtil.getLogger;

public class FirstNotification {
    public static void onPlayerReady(PlayerReadyEvent event) {
        getLogger().info("First-Notification onPlayerReady called.");
        Player player = event.getPlayer();

        // TODO: Note that .getUuid is mark as deprecated and will be removed
//        var playerRef = Universe.get().getPlayer(player.getUuid());
//        var packetHandler = playerRef.getPacketHandler();

        var packetHandler = player.getPlayerConnection();

        var primaryMessage = Message.raw("FIRST NOTIFICATION WORKS!").color("#00FF00");
        var secondaryMessage = Message.raw("This is a second message.").color("#228B22");

        NotificationUtil.sendNotification(
                packetHandler,
                primaryMessage,
                secondaryMessage
        );
    }
}
