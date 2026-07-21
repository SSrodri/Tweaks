package org.tweak.tweaks.client.hud;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.hud.PlayerListHud;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.scoreboard.ReadableScoreboardScore;
import net.minecraft.scoreboard.ScoreHolder;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.ScoreboardCriterion;
import net.minecraft.scoreboard.ScoreboardObjective;
import net.minecraft.scoreboard.Team;
import net.minecraft.scoreboard.number.NumberFormat;
import net.minecraft.scoreboard.number.StyledNumberFormat;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Text;
import net.minecraft.util.Nullables;
import net.minecraft.world.GameMode;
import org.jspecify.annotations.Nullable;

import java.util.Comparator;
import java.util.List;

/**
 * Recomputes the tab list's on-screen bounds using the same math as
 * {@link PlayerListHud#render}, so the overlay can be anchored before it is drawn.
 * Coordinates are in unscaled GUI space, exactly where vanilla would draw.
 */
public final class TabListLayout {
    /** Mirror of PlayerListHud.ENTRY_ORDERING (private in vanilla). */
    private static final Comparator<PlayerListEntry> ENTRY_ORDERING = Comparator.<PlayerListEntry>comparingInt(entry -> -entry.getListOrder())
        .thenComparingInt(entry -> entry.getGameMode() == GameMode.SPECTATOR ? 1 : 0)
        .thenComparing(entry -> (String) Nullables.mapOrElse(entry.getScoreboardTeam(), Team::getName, ""))
        .thenComparing(entry -> entry.getProfile().name(), String::compareToIgnoreCase);

    public record Bounds(int left, int top, int right, int bottom) {
        public int width() {
            return this.right - this.left;
        }

        public int height() {
            return this.bottom - this.top;
        }
    }

    private TabListLayout() {
    }

    public static @Nullable Bounds measure(
        PlayerListHud hud,
        int scaledWindowWidth,
        Scoreboard scoreboard,
        @Nullable ScoreboardObjective objective,
        @Nullable Text header,
        @Nullable Text footer
    ) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null || client.getNetworkHandler() == null) {
            return null;
        }

        List<PlayerListEntry> list = client.player.networkHandler.getListedPlayerListEntries()
            .stream()
            .sorted(ENTRY_ORDERING)
            .limit(80L)
            .toList();

        int spaceWidth = client.textRenderer.getWidth(" ");
        int nameWidth = 0;
        int scoreWidth = 0;
        for (PlayerListEntry entry : list) {
            nameWidth = Math.max(nameWidth, client.textRenderer.getWidth(hud.getPlayerName(entry)));
            if (objective != null && objective.getRenderType() != ScoreboardCriterion.RenderType.HEARTS) {
                ScoreHolder scoreHolder = ScoreHolder.fromProfile(entry.getProfile());
                ReadableScoreboardScore score = scoreboard.getScore(scoreHolder, objective);
                NumberFormat numberFormat = objective.getNumberFormatOr(StyledNumberFormat.YELLOW);
                int formattedWidth = client.textRenderer.getWidth(ReadableScoreboardScore.getFormattedScore(score, numberFormat));
                scoreWidth = Math.max(scoreWidth, formattedWidth > 0 ? spaceWidth + formattedWidth : 0);
            }
        }

        int count = list.size();
        int rows = count;
        int columns;
        for (columns = 1; rows > 20; rows = (count + columns - 1) / columns) {
            columns++;
        }

        boolean showSkins = client.isInSingleplayer() || client.getNetworkHandler().getConnection().isEncrypted();
        int objectiveWidth;
        if (objective != null) {
            objectiveWidth = objective.getRenderType() == ScoreboardCriterion.RenderType.HEARTS ? 90 : scoreWidth;
        } else {
            objectiveWidth = 0;
        }

        int columnWidth = Math.min(columns * ((showSkins ? 9 : 0) + nameWidth + objectiveWidth + 13), scaledWindowWidth - 50) / columns;
        int contentWidth = columnWidth * columns + (columns - 1) * 5;

        int headerLines = 0;
        if (header != null) {
            for (OrderedText line : client.textRenderer.wrapLines(header, scaledWindowWidth - 50)) {
                contentWidth = Math.max(contentWidth, client.textRenderer.getWidth(line));
                headerLines++;
            }
        }
        int footerLines = 0;
        if (footer != null) {
            for (OrderedText line : client.textRenderer.wrapLines(footer, scaledWindowWidth - 50)) {
                contentWidth = Math.max(contentWidth, client.textRenderer.getWidth(line));
                footerLines++;
            }
        }

        int y = 10;
        if (headerLines > 0) {
            y += headerLines * 9 + 1;
        }
        int bottom = y + rows * 9;
        if (footerLines > 0) {
            y += rows * 9 + 1;
            bottom = y + footerLines * 9;
        }

        int left = scaledWindowWidth / 2 - contentWidth / 2 - 1;
        int right = scaledWindowWidth / 2 + contentWidth / 2 + 1;
        return new Bounds(left, 9, right, bottom);
    }
}
