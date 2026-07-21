package org.tweak.tweaks.client.hud;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.ScoreboardEntry;
import net.minecraft.scoreboard.ScoreboardObjective;
import net.minecraft.scoreboard.Team;
import net.minecraft.scoreboard.number.NumberFormat;
import net.minecraft.scoreboard.number.StyledNumberFormat;
import net.minecraft.text.Text;
import net.minecraft.util.Colors;
import org.joml.Matrix3x2fStack;
import org.tweak.tweaks.client.config.TweaksConfig;

import java.util.Comparator;

/**
 * Replacement for {@link net.minecraft.client.gui.hud.InGameHud}'s sidebar renderer.
 * Layout mirrors vanilla exactly at default settings, then applies the configured
 * anchor, offsets, scale and colors.
 */
public final class ScoreboardRenderer {
    private static final Comparator<ScoreboardEntry> ENTRY_COMPARATOR = Comparator.comparing(ScoreboardEntry::value)
        .reversed()
        .thenComparing(ScoreboardEntry::owner, String.CASE_INSENSITIVE_ORDER);

    private record SidebarEntry(Text name, Text score, int scoreWidth) {
    }

    private ScoreboardRenderer() {
    }

    public static void render(DrawContext context, ScoreboardObjective objective) {
        TweaksConfig.ScoreboardSettings cfg = TweaksConfig.get().scoreboard;
        if (cfg.hide) {
            return;
        }

        MinecraftClient client = MinecraftClient.getInstance();
        TextRenderer textRenderer = client.textRenderer;
        Scoreboard scoreboard = objective.getScoreboard();
        NumberFormat numberFormat = objective.getNumberFormatOr(StyledNumberFormat.RED);

        SidebarEntry[] entries = scoreboard.getScoreboardEntries(objective)
            .stream()
            .filter(score -> !score.hidden())
            .sorted(ENTRY_COMPARATOR)
            .limit(15L)
            .map(entry -> {
                Team team = scoreboard.getScoreHolderTeam(entry.owner());
                Text name = Team.decorateName(team, entry.name());
                if (cfg.hideNumbers) {
                    return new SidebarEntry(name, null, 0);
                }
                Text score = entry.formatted(numberFormat);
                return new SidebarEntry(name, score, textRenderer.getWidth(score));
            })
            .toArray(SidebarEntry[]::new);

        Text title = objective.getDisplayName();
        int titleWidth = textRenderer.getWidth(title);
        int width = titleWidth;
        int separatorWidth = textRenderer.getWidth(": ");
        for (SidebarEntry entry : entries) {
            width = Math.max(width, textRenderer.getWidth(entry.name) + (entry.scoreWidth > 0 ? separatorWidth + entry.scoreWidth : 0));
        }

        int rows = entries.length;
        int bodyHeight = rows * 9;
        int totalWidth = width + 4;
        int totalHeight = bodyHeight + 10;
        float scale = cfg.scale;
        int screenWidth = context.getScaledWindowWidth();
        int screenHeight = context.getScaledWindowHeight();

        float x = cfg.offsetX + switch (cfg.horizontalAnchor) {
            case LEFT -> 1.0F;
            case CENTER -> (screenWidth - totalWidth * scale) / 2.0F;
            case RIGHT -> screenWidth - totalWidth * scale - 1.0F;
        };
        // CENTER reproduces vanilla's slightly-below-center placement.
        float y = cfg.offsetY + switch (cfg.verticalAnchor) {
            case TOP -> 1.0F;
            case CENTER -> screenHeight / 2.0F + (bodyHeight / 3 - bodyHeight - 10) * scale;
            case BOTTOM -> screenHeight - totalHeight * scale - 1.0F;
        };

        int background = TweaksConfig.parseRgba(cfg.background, 0x4D000000);
        // The title band is slightly more opaque than the body, matching vanilla's 0.4 vs 0.3.
        int titleBackground = Math.min(255, (background >>> 24) + 26) << 24 | background & 0xFFFFFF;

        Matrix3x2fStack matrices = context.getMatrices();
        matrices.pushMatrix();
        matrices.translate(x, y);
        matrices.scale(scale, scale);

        context.fill(0, 0, totalWidth, 9, titleBackground);
        context.fill(0, 9, totalWidth, totalHeight, background);
        context.drawText(textRenderer, title, 2 + width / 2 - titleWidth / 2, 1, Colors.WHITE, cfg.textShadow);

        for (int i = 0; i < rows; i++) {
            SidebarEntry entry = entries[i];
            int rowY = 10 + i * 9;
            context.drawText(textRenderer, entry.name, 2, rowY, Colors.WHITE, cfg.textShadow);
            if (entry.score != null) {
                context.drawText(textRenderer, entry.score, totalWidth - entry.scoreWidth, rowY, Colors.WHITE, cfg.textShadow);
            }
        }

        matrices.popMatrix();
    }
}
