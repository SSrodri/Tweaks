package org.tweak.tweaks.mixin.client;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.PlayerListHud;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.client.option.GameOptions;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.ScoreboardObjective;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Text;
import org.jspecify.annotations.Nullable;
import org.joml.Matrix3x2fStack;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.tweak.tweaks.client.config.TweaksConfig;
import org.tweak.tweaks.client.hud.PingMeter;
import org.tweak.tweaks.client.hud.TabListLayout;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Mixin(PlayerListHud.class)
public abstract class PlayerListHudMixin {
    @Shadow
    private @Nullable Text header;

    @Shadow
    private @Nullable Text footer;

    @Inject(method = "render", at = @At("HEAD"))
    private void tweaks$pushTransform(DrawContext context, int scaledWindowWidth, Scoreboard scoreboard, ScoreboardObjective objective, CallbackInfo ci) {
        Matrix3x2fStack matrices = context.getMatrices();
        matrices.pushMatrix();
        TweaksConfig.TabListSettings cfg = TweaksConfig.get().tabList;
        if (!cfg.enabled) {
            return;
        }
        TabListLayout.Bounds bounds = TabListLayout.measure(
            (PlayerListHud) (Object) this,
            scaledWindowWidth,
            scoreboard,
            objective,
            cfg.hideHeader ? null : this.header,
            cfg.hideFooter ? null : this.footer
        );
        if (bounds == null) {
            return;
        }

        float scale = cfg.scale;
        int screenHeight = context.getScaledWindowHeight();
        // TOP keeps vanilla's 9px margin so default settings match vanilla exactly.
        float x = cfg.offsetX + switch (cfg.horizontalAnchor) {
            case LEFT -> 5.0F - scale * bounds.left();
            case CENTER -> (scaledWindowWidth - scale * bounds.width()) / 2.0F - scale * bounds.left();
            case RIGHT -> scaledWindowWidth - 5.0F - scale * bounds.width() - scale * bounds.left();
        };
        float y = cfg.offsetY + switch (cfg.verticalAnchor) {
            case TOP -> 9.0F - scale * bounds.top();
            case CENTER -> (screenHeight - scale * bounds.height()) / 2.0F - scale * bounds.top();
            case BOTTOM -> screenHeight - 5.0F - scale * bounds.height() - scale * bounds.top();
        };
        matrices.translate(x, y);
        matrices.scale(scale, scale);
    }

    @Inject(method = "render", at = @At("RETURN"))
    private void tweaks$popTransform(DrawContext context, int scaledWindowWidth, Scoreboard scoreboard, ScoreboardObjective objective, CallbackInfo ci) {
        context.getMatrices().popMatrix();
    }

    @Inject(method = "collectPlayerEntries", at = @At("RETURN"), cancellable = true)
    private void tweaks$youOnTop(CallbackInfoReturnable<List<PlayerListEntry>> cir) {
        TweaksConfig.TabListSettings cfg = TweaksConfig.get().tabList;
        if (!cfg.enabled || !cfg.youOnTop) {
            return;
        }
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) {
            return;
        }
        UUID selfId = client.player.getUuid();
        String selfName = client.player.getGameProfile().name();
        List<PlayerListEntry> list = cir.getReturnValue();

        // Exact profile match (vanilla servers).
        for (int i = 0; i < list.size(); i++) {
            if (tweaks$isSelf(list.get(i), selfId, selfName)) {
                tweaks$moveToFront(cir, list, i);
                return;
            }
        }
        // Servers with custom tab entries (fake profiles) usually still show your name
        // inside the entry's display name, possibly with a rank prefix.
        String needle = selfName.toLowerCase(Locale.ROOT);
        for (int i = 0; i < list.size(); i++) {
            Text displayName = list.get(i).getDisplayName();
            if (displayName != null && displayName.getString().toLowerCase(Locale.ROOT).contains(needle)) {
                tweaks$moveToFront(cir, list, i);
                return;
            }
        }
        // Own entry was cut off by the 80-entry limit: pull it from the full list.
        for (PlayerListEntry entry : client.player.networkHandler.getListedPlayerListEntries()) {
            if (tweaks$isSelf(entry, selfId, selfName)) {
                List<PlayerListEntry> reordered = new ArrayList<>(list);
                reordered.add(0, entry);
                if (reordered.size() > 80) {
                    reordered.remove(reordered.size() - 1);
                }
                cir.setReturnValue(reordered);
                return;
            }
        }
    }

    @Unique
    private static void tweaks$moveToFront(CallbackInfoReturnable<List<PlayerListEntry>> cir, List<PlayerListEntry> list, int index) {
        if (index > 0) {
            List<PlayerListEntry> reordered = new ArrayList<>(list);
            reordered.add(0, reordered.remove(index));
            cir.setReturnValue(reordered);
        }
    }

    @Unique
    private static boolean tweaks$isSelf(PlayerListEntry entry, UUID selfId, String selfName) {
        return selfId.equals(entry.getProfile().id()) || entry.getProfile().name().equalsIgnoreCase(selfName);
    }

    @Redirect(
        method = "render",
        at = @At(value = "FIELD", target = "Lnet/minecraft/client/gui/hud/PlayerListHud;header:Lnet/minecraft/text/Text;", opcode = Opcodes.GETFIELD)
    )
    private @Nullable Text tweaks$hideHeader(PlayerListHud hud) {
        TweaksConfig.TabListSettings cfg = TweaksConfig.get().tabList;
        return cfg.enabled && cfg.hideHeader ? null : this.header;
    }

    @Redirect(
        method = "render",
        at = @At(value = "FIELD", target = "Lnet/minecraft/client/gui/hud/PlayerListHud;footer:Lnet/minecraft/text/Text;", opcode = Opcodes.GETFIELD)
    )
    private @Nullable Text tweaks$hideFooter(PlayerListHud hud) {
        TweaksConfig.TabListSettings cfg = TweaksConfig.get().tabList;
        return cfg.enabled && cfg.hideFooter ? null : this.footer;
    }

    @ModifyConstant(method = "render", constant = @Constant(intValue = Integer.MIN_VALUE))
    private int tweaks$backgroundColor(int original) {
        TweaksConfig.TabListSettings cfg = TweaksConfig.get().tabList;
        return cfg.enabled ? TweaksConfig.parseRgba(cfg.background, original) : original;
    }

    @Redirect(
        method = "render",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/client/option/GameOptions;getTextBackgroundColor(I)I")
    )
    private int tweaks$rowBackgroundColor(GameOptions options, int fallbackColor) {
        TweaksConfig.TabListSettings cfg = TweaksConfig.get().tabList;
        return cfg.enabled
            ? TweaksConfig.parseRgba(cfg.rowBackground, options.getTextBackgroundColor(fallbackColor))
            : options.getTextBackgroundColor(fallbackColor);
    }

    @Redirect(
        method = {"render", "renderScoreboardObjective", "renderHearts"},
        at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/DrawContext;drawTextWithShadow(Lnet/minecraft/client/font/TextRenderer;Lnet/minecraft/text/Text;III)V")
    )
    private void tweaks$drawText(DrawContext context, TextRenderer textRenderer, Text text, int x, int y, int color) {
        context.drawText(textRenderer, text, x, y, color, this.tweaks$shadow());
    }

    @Redirect(
        method = "render",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/DrawContext;drawTextWithShadow(Lnet/minecraft/client/font/TextRenderer;Lnet/minecraft/text/OrderedText;III)V")
    )
    private void tweaks$drawOrderedText(DrawContext context, TextRenderer textRenderer, OrderedText text, int x, int y, int color) {
        context.drawText(textRenderer, text, x, y, color, this.tweaks$shadow());
    }

    @Inject(method = "renderLatencyIcon", at = @At("HEAD"), cancellable = true)
    private void tweaks$renderLatencyIcon(DrawContext context, int width, int x, int y, PlayerListEntry entry, CallbackInfo ci) {
        TweaksConfig.TabListSettings cfg = TweaksConfig.get().tabList;
        if (!cfg.enabled) {
            return;
        }
        if (cfg.hidePing) {
            ci.cancel();
            return;
        }
        if (cfg.numericPing) {
            ci.cancel();
            MinecraftClient client = MinecraftClient.getInstance();
            long latency = entry.getLatency();
            // The local player's ping is measured directly (see PingMeter); the
            // server-reported value is kept for everyone else.
            if (client.player != null && tweaks$isSelf(entry, client.player.getUuid(), client.player.getGameProfile().name())) {
                long measured = PingMeter.getRoundTrip();
                if (measured >= 0) {
                    latency = measured;
                }
            }
            String text = latency < 0 ? "?" : latency + "ms";
            int textWidth = client.textRenderer.getWidth(text);
            context.drawText(client.textRenderer, text, x + width - textWidth - 1, y, tweaks$latencyColor(latency), cfg.textShadow);
        }
    }

    @Unique
    private static int tweaks$latencyColor(long latency) {
        if (latency < 0) {
            return 0xFF808080;
        }
        if (latency < 150) {
            return 0xFF00E676;
        }
        if (latency < 300) {
            return 0xFFFFEB3B;
        }
        if (latency < 600) {
            return 0xFFFF9800;
        }
        return 0xFFFF5252;
    }

    private boolean tweaks$shadow() {
        TweaksConfig.TabListSettings cfg = TweaksConfig.get().tabList;
        return !cfg.enabled || cfg.textShadow;
    }
}
