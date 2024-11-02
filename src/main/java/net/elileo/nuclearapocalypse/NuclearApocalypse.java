package net.elileo.nuclearapocalypse;

import com.mojang.logging.LogUtils;
import net.elileo.nuclearapocalypse.command.SkipTimerCommand;
import net.elileo.nuclearapocalypse.event.CountdownTimerHandler;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

@Mod(NuclearApocalypse.MOD_ID)
public class NuclearApocalypse {
    public static final String MOD_ID = "nuclearapocalypse";
    private static final Logger LOGGER = LogUtils.getLogger();

    public NuclearApocalypse() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();


        // Register common setup and creative tab listeners
        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(this::addCreative);

        // Register the mod to the global event bus
        MinecraftForge.EVENT_BUS.register(this);
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        // Common setup logic (if any) can go here
    }

    @SubscribeEvent
    public static void registerCommands(RegisterCommandsEvent event) {
        SkipTimerCommand.register(event.getDispatcher()); // Register the command here
    }

    private void addCreative(BuildCreativeModeTabContentsEvent event) {
        // Your creative tab logic here
    }

    @Mod.EventBusSubscriber(modid = MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientModEvents {
        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event) {
            LOGGER.info("HELLO FROM CLIENT SETUP");
            LOGGER.info("MINECRAFT NAME >> {}", Minecraft.getInstance().getUser().getName());
        }
    }
    @Mod.EventBusSubscriber(modid = MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
    public static class ModEvents {

        @SubscribeEvent
        public static void onServerStarting(ServerStartingEvent event) {
            // Register commands or other necessary setups here
            SkipTimerCommand.register(event.getServer().getCommands().getDispatcher());



        }

    }
}
