package net.elileo.nuclearapocalypse.event;

import net.elileo.nuclearapocalypse.NuclearApocalypse;
import net.elileo.nuclearapocalypse.effect.NuclearExplosionEffect;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.core.BlockPos;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.List;

@Mod.EventBusSubscriber(modid = NuclearApocalypse.MOD_ID)
public class CountdownTimerHandler {
    private static int countdownTicks = 24000; // 20 minutes in ticks
    private static boolean nuked = false;

    // New method to skip the timer to 30 seconds
    public static void skipTimer() {
        countdownTicks = 600; // 30 seconds in ticks
    }

    @SubscribeEvent
    public static void onWorldTick(TickEvent.ServerTickEvent event) {

        if (event.phase == TickEvent.Phase.START) {
            ServerLevel serverWorld = event.getServer().getLevel(Level.OVERWORLD);

            if (!nuked) {
                if (countdownTicks > 0) {
                    countdownTicks--;

                    // Send countdown message every 600 ticks (30 seconds)
                    if (countdownTicks % 600 == 0) {
                        Component countdownMessage = formatCountdown(countdownTicks);
                        List<ServerPlayer> players = serverWorld.getPlayers(player -> true);
                        for (ServerPlayer player : players) {
                            player.sendSystemMessage(countdownMessage);
                        }
                    }
                } else {
                    nukeWorld(serverWorld);
                    nuked = true;
                }
            }
        }
    }

    @SubscribeEvent
    public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {

        ServerPlayer player = (ServerPlayer) event.getEntity();
        player.sendSystemMessage(Component.literal("The nuke will impact your world in 20 minutes."));
    }

    private static void nukeWorld(ServerLevel world) {
        if (world == null) {
            System.out.println("World is null!");
            return;
        }
        for (ServerPlayer player : world.getPlayers(player -> true)) {
            player.sendSystemMessage(Component.literal("The nuke has gone off!"));
        }

        // Get the first player in the world
        List<ServerPlayer> players = world.getPlayers(player -> true);
        if (!players.isEmpty()) {
            ServerPlayer firstPlayer = players.get(0); // Rename variable to avoid conflict

            // Calculate explosion position north of the player
            BlockPos playerPos = firstPlayer.blockPosition();
            BlockPos explosionPos = playerPos.north(50); // Adjust distance as necessary (10 blocks north)

            // Call the createExplosion method with the player
            NuclearExplosionEffect.createExplosion(world, explosionPos, firstPlayer); // Pass the player as entity
        } else {
            System.out.println("No player found for explosion.");
        }
    }

    private static Component formatCountdown(int ticks) {

        int seconds = ticks / 20;
        int minutes = seconds / 60;
        seconds = seconds % 60;

        TextColor color;
        if (minutes >= 10) {
            color = TextColor.fromRgb(0xFFFF00); // Yellow
        } else if (minutes >= 5) {
            color = TextColor.fromRgb(0xFFA500); // Orange
        } else {
            color = TextColor.fromRgb(0xFF0000); // Red
        }

        return Component.literal(String.format("%d minutes and %d seconds remaining!", minutes, seconds))
                .setStyle(Style.EMPTY.withColor(color));
    }
}
