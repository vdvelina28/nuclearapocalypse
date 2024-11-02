package net.elileo.nuclearapocalypse.effect;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.network.chat.Component; // Import for Component

public class RadiationEffect {

    private static final int RADIATION_DURATION = 600; // Duration in ticks (30 seconds)
    private static final int RADIATION_AMPLIFIER = 0; // Level of effect, 0 for weakness

    public static void applyRadiation(ServerPlayer player) {

        if (player != null) {
            player.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, RADIATION_DURATION, RADIATION_AMPLIFIER, false, true));
            player.sendSystemMessage(Component.literal("You have been exposed to radiation!")); // Fixed line
        }
    }
}