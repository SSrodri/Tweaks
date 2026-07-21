package org.tweak.tweaks.client.hud;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.sound.SoundInstance;
import net.minecraft.client.sound.SoundInstanceListener;
import net.minecraft.client.sound.SoundListenerTransform;
import net.minecraft.client.sound.SoundManager;
import net.minecraft.client.sound.WeightedSoundSet;
import net.minecraft.text.Text;
import net.minecraft.util.Util;
import net.minecraft.util.math.ColorHelper;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix3x2fStack;
import org.tweak.tweaks.client.config.TweaksConfig;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

/**
 * Standalone subtitle overlay that replaces {@link net.minecraft.client.gui.hud.SubtitlesHud}.
 * Tracks sounds through its own {@link SoundInstanceListener} so the vanilla HUD's
 * package-private state is never touched.
 */
public final class TweakedSubtitles implements SoundInstanceListener {
    public static final TweakedSubtitles INSTANCE = new TweakedSubtitles();

    private record SoundPing(Vec3d location, long time) {
    }

    private static final class Entry {
        final Text text;
        final float range;
        final List<SoundPing> sounds = new ArrayList<>();

        Entry(Text text, float range, Vec3d pos) {
            this.text = text;
            this.range = range;
            this.sounds.add(new SoundPing(pos, Util.getMeasuringTimeMs()));
        }

        SoundPing getNearestSound(Vec3d pos) {
            if (this.sounds.isEmpty()) {
                return null;
            }
            return this.sounds.size() == 1
                ? this.sounds.getFirst()
                : this.sounds.stream().min(Comparator.comparingDouble(sound -> sound.location().distanceTo(pos))).orElse(null);
        }

        void reset(Vec3d pos) {
            this.sounds.removeIf(sound -> pos.equals(sound.location()));
            this.sounds.add(new SoundPing(pos, Util.getMeasuringTimeMs()));
        }

        boolean canHearFrom(Vec3d pos) {
            if (Float.isInfinite(this.range)) {
                return true;
            }
            if (this.sounds.isEmpty()) {
                return false;
            }
            SoundPing sound = this.getNearestSound(pos);
            return sound != null && pos.isInRange(sound.location(), this.range);
        }

        void removeExpired(double expiry) {
            long now = Util.getMeasuringTimeMs();
            this.sounds.removeIf(sound -> now - sound.time() > expiry);
        }

        boolean hasSounds() {
            return !this.sounds.isEmpty();
        }
    }

    private final List<Entry> entries = new ArrayList<>();
    private boolean registered;

    private TweakedSubtitles() {
    }

    @Override
    public void onSoundPlayed(SoundInstance sound, WeightedSoundSet soundSet, float range) {
        Text subtitle = soundSet.getSubtitle();
        if (subtitle == null) {
            return;
        }
        Vec3d pos = new Vec3d(sound.getX(), sound.getY(), sound.getZ());
        for (Entry entry : this.entries) {
            if (entry.text.equals(subtitle)) {
                entry.reset(pos);
                return;
            }
        }
        this.entries.add(new Entry(subtitle, range, pos));
    }

    public void render(DrawContext context) {
        MinecraftClient client = MinecraftClient.getInstance();
        SoundManager soundManager = client.getSoundManager();
        boolean shouldShow = client.options.getShowSubtitles().getValue();
        if (!this.registered && shouldShow) {
            soundManager.registerListener(this);
            this.registered = true;
        } else if (this.registered && !shouldShow) {
            soundManager.unregisterListener(this);
            this.registered = false;
            this.entries.clear();
        }
        if (!this.registered) {
            return;
        }

        TweaksConfig.SubtitleSettings cfg = TweaksConfig.get().subtitles;
        TextRenderer textRenderer = client.textRenderer;
        SoundListenerTransform transform = soundManager.getListenerTransform();
        Vec3d position = transform.position();
        Vec3d forward = transform.forward();
        Vec3d right = transform.right();
        double displayTime = 3000.0 * client.options.getNotificationDisplayTime().getValue() * cfg.displayTimeMultiplier;

        this.entries.removeIf(entry -> !entry.hasSounds());

        List<Entry> audible = new ArrayList<>();
        for (Entry entry : this.entries) {
            if (entry.canHearFrom(position)) {
                audible.add(entry);
            }
        }
        if (audible.isEmpty()) {
            return;
        }

        int maxWidth = 0;
        Iterator<Entry> iterator = audible.iterator();
        while (iterator.hasNext()) {
            Entry entry = iterator.next();
            entry.removeExpired(displayTime);
            if (!entry.hasSounds()) {
                iterator.remove();
            } else {
                maxWidth = Math.max(maxWidth, textRenderer.getWidth(entry.text));
            }
        }
        if (audible.isEmpty()) {
            return;
        }
        maxWidth += textRenderer.getWidth("<")
            + textRenderer.getWidth(" ")
            + textRenderer.getWidth(">")
            + textRenderer.getWidth(" ");

        context.createNewRootLayer();

        int background = TweaksConfig.parseRgba(cfg.background, 0xCC000000);
        float scale = cfg.scale;
        int halfWidth = maxWidth / 2;
        int lineHeight = 9;
        int halfLine = lineHeight / 2;
        int screenWidth = context.getScaledWindowWidth();
        int screenHeight = context.getScaledWindowHeight();
        Matrix3x2fStack matrices = context.getMatrices();

        int shown = 0;
        for (Entry entry : audible) {
            if (cfg.maxSubtitles > 0 && shown >= cfg.maxSubtitles) {
                break;
            }
            SoundPing sound = entry.getNearestSound(position);
            if (sound == null) {
                continue;
            }

            Vec3d direction = sound.location().subtract(position).normalize();
            double rightDot = right.dotProduct(direction);
            double forwardDot = forward.dotProduct(direction);
            int textWidth = textRenderer.getWidth(entry.text);
            int fade = MathHelper.floor(
                MathHelper.clampedLerp((float) (Util.getMeasuringTimeMs() - sound.time()) / (float) displayTime, 255.0F, 75.0F)
            );

            float stackStep = (lineHeight + 1) * scale;
            float centerX = cfg.offsetX + switch (cfg.horizontalAnchor) {
                case LEFT -> 2.0F + halfWidth * scale;
                case CENTER -> screenWidth / 2.0F;
                case RIGHT -> screenWidth - halfWidth * scale - 2.0F;
            };
            // TOP stacks downwards; CENTER and BOTTOM stack upwards like vanilla.
            float centerY = cfg.offsetY + switch (cfg.verticalAnchor) {
                case TOP -> 10.0F + halfLine * scale + shown * stackStep;
                case CENTER -> screenHeight / 2.0F - shown * stackStep;
                case BOTTOM -> screenHeight - 35.0F - shown * stackStep;
            };

            int color = ColorHelper.getArgb(255, fade, fade, fade);

            matrices.pushMatrix();
            matrices.translate(centerX, centerY);
            matrices.scale(scale, scale);
            context.fill(-halfWidth - 1, -halfLine - 1, halfWidth + 1, halfLine + 1, background);
            if (cfg.showArrows && !(forwardDot > 0.5)) {
                if (rightDot > 0.0) {
                    context.drawText(textRenderer, ">", halfWidth - textRenderer.getWidth(">"), -halfLine, color, cfg.textShadow);
                } else if (rightDot < 0.0) {
                    context.drawText(textRenderer, "<", -halfWidth, -halfLine, color, cfg.textShadow);
                }
            }
            context.drawText(textRenderer, entry.text, -textWidth / 2, -halfLine, color, cfg.textShadow);
            matrices.popMatrix();
            shown++;
        }
    }

}
