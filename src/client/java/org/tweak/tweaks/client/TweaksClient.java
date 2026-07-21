package org.tweak.tweaks.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;
import org.tweak.tweaks.client.config.TweaksConfig;
import org.tweak.tweaks.client.hud.PingMeter;
import org.tweak.tweaks.client.screen.TweaksConfigScreen;

public class TweaksClient implements ClientModInitializer {
    private static KeyBinding openConfig;

    @Override
    public void onInitializeClient() {
        TweaksConfig.load();

        KeyBinding.Category category = KeyBinding.Category.create(Identifier.of("tweaks", "tweaks"));
        openConfig = KeyBindingHelper.registerKeyBinding(
            new KeyBinding("key.tweaks.open_config", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_O, category)
        );

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            PingMeter.tick(client);
            while (openConfig.wasPressed()) {
                client.setScreen(new TweaksConfigScreen(client.currentScreen));
            }
        });
    }
}
