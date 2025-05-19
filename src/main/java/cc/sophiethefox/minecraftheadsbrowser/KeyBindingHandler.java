package cc.sophiethefox.minecraftheadsbrowser;

import cc.sophiethefox.minecraftheadsbrowser.gui.MCHeadsScreen;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import org.lwjgl.glfw.GLFW;

public class KeyBindingHandler {
    private static KeyBinding openGuiKey;

    public static void register() {
        openGuiKey = KeyBindingHelper.registerKeyBinding(
                new KeyBinding("key.minecraft-heads.opengui", GLFW.GLFW_KEY_H, "key.categories.misc")
        );

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (openGuiKey.wasPressed()) {
                if (client.player != null) {
                    MinecraftClient.getInstance().setScreen(
                            new MCHeadsScreen()
                    );
                }
            }
        });
    }
}
