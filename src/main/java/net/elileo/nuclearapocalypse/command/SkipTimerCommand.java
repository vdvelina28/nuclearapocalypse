package net.elileo.nuclearapocalypse.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import net.elileo.nuclearapocalypse.event.CountdownTimerHandler;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber
public class SkipTimerCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {

        dispatcher.register(net.minecraft.commands.Commands.literal("skiptimer")
                .executes(SkipTimerCommand::execute));
    }

    private static int execute(CommandContext<CommandSourceStack> context) {

        CountdownTimerHandler.skipTimer();
        context.getSource().sendSuccess(() -> Component.literal("Skipped the timer to 30 seconds left!"), true);
        return 1;
    }

    @SubscribeEvent
    public static void onCommandRegister(RegisterCommandsEvent event) {

        register(event.getDispatcher());
    }
}
