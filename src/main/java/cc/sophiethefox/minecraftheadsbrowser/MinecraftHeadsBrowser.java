package cc.sophiethefox.minecraftheadsbrowser;

import cc.sophiethefox.minecraftheadsbrowser.commands.HeadsCommand;
import cc.sophiethefox.minecraftheadsbrowser.config.MCHeadsConfig;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;

public class MinecraftHeadsBrowser implements ModInitializer {
    public static final String MOD_ID = "minecraft-heads-browser";
    public static HeadManager headManager = new HeadManager();

    public static final MCHeadsConfig CONFIG = MCHeadsConfig.createAndLoad();

    @Override
    public void onInitialize() {
        headManager.fetchAllHeads();
        KeyBindingHandler.register();

        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            HeadsCommand.register(dispatcher);
        });
    }
}