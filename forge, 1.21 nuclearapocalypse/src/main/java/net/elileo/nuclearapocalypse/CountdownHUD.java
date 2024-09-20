package net.elileo.nuclearapocalypse;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;


@Mod.EventBusSubscriber
public class CountdownHUD {

    private static final CountdownTimer countdownTimer = new CountdownTimer();

    @SubscribeEvent
    public static void onRenderGuiOverlay(RenderGameOverlayEvent.Pre event) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft != null && minecraft.world != null && minecraft.screen != null) {
            GuiGraphics gui = Minecraft.getInstance().screen.getGuiGraphics();

            if (!countdownTimer.isFinished()) {
                String timeString = countdownTimer.getTimeString();
                int screenWidth = minecraft.getWindow().getGuiScaledWidth();
                int x = (screenWidth / 2) - minecraft.font.width(timeString) / 2;
                int y = 10;

                RenderSystem.enableBlend();
                gui.drawString(minecraft.font, timeString, x, y, 0xFFFFFF);
                RenderSystem.disableBlend();
            }
        }
    }