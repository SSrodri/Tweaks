package org.tweak.tweaks.mixin.client;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.SubtitlesHud;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.tweak.tweaks.client.config.TweaksConfig;
import org.tweak.tweaks.client.hud.TweakedSubtitles;

@Mixin(SubtitlesHud.class)
public class SubtitlesHudMixin {
    @Inject(method = "render", at = @At("HEAD"), cancellable = true)
    private void tweaks$render(DrawContext context, CallbackInfo ci) {
        if (TweaksConfig.get().subtitles.enabled) {
            ci.cancel();
            TweakedSubtitles.INSTANCE.render(context);
        }
    }
}
