package cc.sophiethefox.minecraftheadsbrowser.commands;

import cc.sophiethefox.minecraftheadsbrowser.gui.MCHeadsScreen;
import com.mojang.brigadier.CommandDispatcher;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.MinecraftClient;

public class HeadsCommand {
    public static void register(CommandDispatcher<FabricClientCommandSource> dispatcher) {
        dispatcher.register(ClientCommandManager.literal("heads").executes(context -> {
                    if (MinecraftClient.getInstance().player != null) {
                        MinecraftClient.getInstance().setScreen(new MCHeadsScreen());
                    }
                    return 1;
                })
        );
    }
}
