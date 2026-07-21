package org.tweak.tweaks.client.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;
import org.tweak.tweaks.Tweaks;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * All mod settings, persisted as JSON in {@code config/tweaks.json}.
 * Backgrounds are stored as RGBA hex strings ({@code #RRGGBBAA}; plain
 * {@code #RRGGBB} is accepted and treated as fully opaque).
 */
public class TweaksConfig {
    public enum HorizontalAnchor {
        LEFT, CENTER, RIGHT
    }

    public enum VerticalAnchor {
        TOP, CENTER, BOTTOM
    }

    public static class ScoreboardSettings {
        public boolean enabled = true;
        public HorizontalAnchor horizontalAnchor = HorizontalAnchor.RIGHT;
        public VerticalAnchor verticalAnchor = VerticalAnchor.CENTER;
        public int offsetX = 0;
        public int offsetY = 0;
        public float scale = 1.0F;
        public boolean textShadow = false;
        public String background = "#0000004D";
        public boolean hide = false;
        public boolean hideNumbers = false;
    }

    public static class TabListSettings {
        public boolean enabled = true;
        public HorizontalAnchor horizontalAnchor = HorizontalAnchor.CENTER;
        public VerticalAnchor verticalAnchor = VerticalAnchor.TOP;
        public int offsetX = 0;
        public int offsetY = 0;
        public float scale = 1.0F;
        public boolean textShadow = true;
        public String background = "#00000080";
        public String rowBackground = "#FFFFFF20";
        public boolean hideHeader = false;
        public boolean hideFooter = false;
        public boolean hidePing = false;
        public boolean numericPing = false;
        public boolean toggle = false;
        public boolean youOnTop = false;
    }

    public static class SubtitleSettings {
        public boolean enabled = true;
        public HorizontalAnchor horizontalAnchor = HorizontalAnchor.RIGHT;
        public VerticalAnchor verticalAnchor = VerticalAnchor.BOTTOM;
        public int offsetX = 0;
        public int offsetY = 0;
        public float scale = 1.0F;
        public boolean textShadow = true;
        public String background = "#000000CC";
        public float displayTimeMultiplier = 1.0F;
        /** 0 means unlimited (vanilla behavior). */
        public int maxSubtitles = 0;
        public boolean showArrows = true;
    }

    public ScoreboardSettings scoreboard = new ScoreboardSettings();
    public TabListSettings tabList = new TabListSettings();
    public SubtitleSettings subtitles = new SubtitleSettings();

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path PATH = FabricLoader.getInstance().getConfigDir().resolve("tweaks.json");
    private static TweaksConfig instance;

    public static TweaksConfig get() {
        if (instance == null) {
            load();
        }
        return instance;
    }

    public static void load() {
        if (Files.exists(PATH)) {
            try {
                instance = GSON.fromJson(Files.readString(PATH), TweaksConfig.class);
            } catch (IOException | RuntimeException e) {
                Tweaks.LOGGER.error("Failed to read {}, using defaults", PATH, e);
            }
        }
        if (instance == null) {
            instance = new TweaksConfig();
            save();
        }
    }

    public static void save() {
        try {
            Files.writeString(PATH, GSON.toJson(get()));
        } catch (IOException e) {
            Tweaks.LOGGER.error("Failed to write {}", PATH, e);
        }
    }

    /**
     * Parses {@code #RRGGBBAA} or {@code #RRGGBB} (alpha defaults to FF) into an ARGB int.
     */
    public static int parseRgba(String hex, int fallbackArgb) {
        if (hex == null) {
            return fallbackArgb;
        }
        String digits = hex.startsWith("#") ? hex.substring(1) : hex;
        try {
            if (digits.length() == 6) {
                return 0xFF000000 | Integer.parseInt(digits, 16);
            }
            if (digits.length() == 8) {
                long rgba = Long.parseLong(digits, 16);
                int alpha = (int) (rgba & 0xFF);
                int rgb = (int) (rgba >> 8);
                return alpha << 24 | rgb;
            }
        } catch (NumberFormatException ignored) {
        }
        return fallbackArgb;
    }

    public static boolean isValidRgba(String hex) {
        if (hex == null) {
            return false;
        }
        String digits = hex.startsWith("#") ? hex.substring(1) : hex;
        if (digits.length() != 6 && digits.length() != 8) {
            return false;
        }
        try {
            Long.parseLong(digits, 16);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}
