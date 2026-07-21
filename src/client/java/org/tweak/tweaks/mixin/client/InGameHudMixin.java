package org.tweak.tweaks.mixin.client;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.scoreboard.ScoreboardObjective;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.tweak.tweaks.client.config.TweaksConfig;
import org.tweak.tweaks.client.hud.ScoreboardRenderer;

@Mixin(InGameHud.class)
public class InGameHudMixin {
    @Unique
    private boolean tweaks$tabWasDown;

    @Unique
    private boolean tweaks$tabToggled;

    @Inject(
        method = "renderScoreboardSidebar(Lnet/minecraft/client/gui/DrawContext;Lnet/minecraft/scoreboard/ScoreboardObjective;)V",
        at = @At("HEAD"),
        cancellable = true
    )
    private void tweaks$renderScoreboardSidebar(DrawContext context, ScoreboardObjective objective, CallbackInfo ci) {
        if (TweaksConfig.get().scoreboard.enabled) {
            ci.cancel();
            ScoreboardRenderer.render(context, objective);
        }
    }

    @Redirect(
        method = "renderPlayerList",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/client/option/KeyBinding;isPressed()Z")
    )
    private boolean tweaks$togglePlayerList(KeyBinding keyBinding) {
        boolean down = keyBinding.isPressed();
        TweaksConfig.TabListSettings cfg = TweaksConfig.get().tabList;
        if (cfg.enabled && cfg.toggle) {
            if (down && !this.tweaks$tabWasDown) {
                this.tweaks$tabToggled = !this.tweaks$tabToggled;
            }
            this.tweaks$tabWasDown = down;
            return this.tweaks$tabToggled;
        }
        this.tweaks$tabWasDown = down;
        this.tweaks$tabToggled = false;
        return down;
    }
}
