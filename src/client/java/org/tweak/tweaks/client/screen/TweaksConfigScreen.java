package org.tweak.tweaks.client.screen;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.CyclingButtonWidget;
import net.minecraft.client.gui.widget.SliderWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;
import org.jspecify.annotations.Nullable;
import org.tweak.tweaks.client.config.TweaksConfig;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.IntConsumer;
import java.util.function.Supplier;

public class TweaksConfigScreen extends Screen {
    private enum Tab {
        SCOREBOARD("tweaks.config.tab.scoreboard"),
        TAB_LIST("tweaks.config.tab.tablist"),
        SUBTITLES("tweaks.config.tab.subtitles");

        final Text title;

        Tab(String key) {
            this.title = Text.translatable(key);
        }
    }

    private record Label(int x, int y, Text text) {
    }

    private record ColorPreview(int x, int y, Supplier<String> color) {
    }

    private final @Nullable Screen parent;
    private Tab tab = Tab.SCOREBOARD;
    private final List<Label> labels = new ArrayList<>();
    private final List<ColorPreview> previews = new ArrayList<>();
    private int slot;

    public TweaksConfigScreen(@Nullable Screen parent) {
        super(Text.translatable("tweaks.config.title"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        this.labels.clear();
        this.previews.clear();
        this.slot = 0;

        int tabX = this.width / 2 - 156;
        for (Tab t : Tab.values()) {
            ButtonWidget button = ButtonWidget.builder(t.title, b -> this.setTab(t)).dimensions(tabX, 10, 100, 20).build();
            button.active = t != this.tab;
            this.addDrawableChild(button);
            tabX += 104;
        }

        TweaksConfig config = TweaksConfig.get();
        switch (this.tab) {
            case SCOREBOARD -> this.initScoreboard(config.scoreboard);
            case TAB_LIST -> this.initTabList(config.tabList);
            case SUBTITLES -> this.initSubtitles(config.subtitles);
        }

        this.addDrawableChild(
            ButtonWidget.builder(Text.translatable("tweaks.config.reset"), b -> this.resetTab())
                .dimensions(this.width / 2 - 155, this.height - 27, 150, 20)
                .build()
        );
        this.addDrawableChild(
            ButtonWidget.builder(ScreenTexts.DONE, b -> this.close())
                .dimensions(this.width / 2 + 5, this.height - 27, 150, 20)
                .build()
        );
    }

    private void initScoreboard(TweaksConfig.ScoreboardSettings cfg) {
        this.addToggle(Text.translatable("tweaks.config.enabled"), cfg.enabled, v -> cfg.enabled = v);
        this.addEnum(Text.translatable("tweaks.config.horizontal_anchor"), cfg.horizontalAnchor, TweaksConfig.HorizontalAnchor.values(), v -> cfg.horizontalAnchor = v);
        this.addEnum(Text.translatable("tweaks.config.vertical_anchor"), cfg.verticalAnchor, TweaksConfig.VerticalAnchor.values(), v -> cfg.verticalAnchor = v);
        this.addIntSlider(Text.translatable("tweaks.config.offset_x"), -300, 300, cfg.offsetX, v -> cfg.offsetX = v);
        this.addIntSlider(Text.translatable("tweaks.config.offset_y"), -300, 300, cfg.offsetY, v -> cfg.offsetY = v);
        this.addScaleSlider(cfg.scale, v -> cfg.scale = v);
        this.addToggle(Text.translatable("tweaks.config.scoreboard.hide"), cfg.hide, v -> cfg.hide = v);
        this.addToggle(Text.translatable("tweaks.config.scoreboard.hide_numbers"), cfg.hideNumbers, v -> cfg.hideNumbers = v);
        this.addToggle(Text.translatable("tweaks.config.text_shadow"), cfg.textShadow, v -> cfg.textShadow = v);
        this.addRgba(Text.translatable("tweaks.config.background"), () -> cfg.background, v -> cfg.background = v);
    }

    private void initTabList(TweaksConfig.TabListSettings cfg) {
        this.addToggle(Text.translatable("tweaks.config.enabled"), cfg.enabled, v -> cfg.enabled = v);
        this.addEnum(Text.translatable("tweaks.config.horizontal_anchor"), cfg.horizontalAnchor, TweaksConfig.HorizontalAnchor.values(), v -> cfg.horizontalAnchor = v);
        this.addEnum(Text.translatable("tweaks.config.vertical_anchor"), cfg.verticalAnchor, TweaksConfig.VerticalAnchor.values(), v -> cfg.verticalAnchor = v);
        this.addIntSlider(Text.translatable("tweaks.config.offset_x"), -300, 300, cfg.offsetX, v -> cfg.offsetX = v);
        this.addIntSlider(Text.translatable("tweaks.config.offset_y"), -300, 300, cfg.offsetY, v -> cfg.offsetY = v);
        this.addScaleSlider(cfg.scale, v -> cfg.scale = v);
        this.addToggle(Text.translatable("tweaks.config.tablist.toggle"), cfg.toggle, v -> cfg.toggle = v);
        this.addToggle(Text.translatable("tweaks.config.tablist.you_on_top"), cfg.youOnTop, v -> cfg.youOnTop = v);
        this.addToggle(Text.translatable("tweaks.config.tablist.hide_header"), cfg.hideHeader, v -> cfg.hideHeader = v);
        this.addToggle(Text.translatable("tweaks.config.tablist.hide_footer"), cfg.hideFooter, v -> cfg.hideFooter = v);
        this.addToggle(Text.translatable("tweaks.config.tablist.hide_ping"), cfg.hidePing, v -> cfg.hidePing = v);
        this.addToggle(Text.translatable("tweaks.config.tablist.numeric_ping"), cfg.numericPing, v -> cfg.numericPing = v);
        this.addToggle(Text.translatable("tweaks.config.text_shadow"), cfg.textShadow, v -> cfg.textShadow = v);
        this.addRgba(Text.translatable("tweaks.config.background"), () -> cfg.background, v -> cfg.background = v);
        this.addRgba(Text.translatable("tweaks.config.tablist.row_background"), () -> cfg.rowBackground, v -> cfg.rowBackground = v);
    }

    private void initSubtitles(TweaksConfig.SubtitleSettings cfg) {
        this.addToggle(Text.translatable("tweaks.config.enabled"), cfg.enabled, v -> cfg.enabled = v);
        this.addEnum(Text.translatable("tweaks.config.horizontal_anchor"), cfg.horizontalAnchor, TweaksConfig.HorizontalAnchor.values(), v -> cfg.horizontalAnchor = v);
        this.addEnum(Text.translatable("tweaks.config.vertical_anchor"), cfg.verticalAnchor, TweaksConfig.VerticalAnchor.values(), v -> cfg.verticalAnchor = v);
        this.addIntSlider(Text.translatable("tweaks.config.offset_x"), -300, 300, cfg.offsetX, v -> cfg.offsetX = v);
        this.addIntSlider(Text.translatable("tweaks.config.offset_y"), -300, 300, cfg.offsetY, v -> cfg.offsetY = v);
        this.addScaleSlider(cfg.scale, v -> cfg.scale = v);
        this.addSlider(Text.translatable("tweaks.config.subtitles.display_time"), 0.25F, 4.0F, 0.25F, cfg.displayTimeMultiplier,
            v -> cfg.displayTimeMultiplier = v, v -> String.format(Locale.ROOT, "%.2fx", v));
        this.addSlider(Text.translatable("tweaks.config.subtitles.max"), 0.0F, 10.0F, 1.0F, cfg.maxSubtitles,
            v -> cfg.maxSubtitles = Math.round(v),
            v -> Math.round(v) == 0 ? Text.translatable("tweaks.config.unlimited").getString() : Integer.toString(Math.round(v)));
        this.addToggle(Text.translatable("tweaks.config.subtitles.show_arrows"), cfg.showArrows, v -> cfg.showArrows = v);
        this.addToggle(Text.translatable("tweaks.config.text_shadow"), cfg.textShadow, v -> cfg.textShadow = v);
        this.addRgba(Text.translatable("tweaks.config.background"), () -> cfg.background, v -> cfg.background = v);
    }

    private void setTab(Tab tab) {
        if (this.tab != tab) {
            TweaksConfig.save();
            this.tab = tab;
            this.clearAndInit();
        }
    }

    private void resetTab() {
        TweaksConfig config = TweaksConfig.get();
        switch (this.tab) {
            case SCOREBOARD -> config.scoreboard = new TweaksConfig.ScoreboardSettings();
            case TAB_LIST -> config.tabList = new TweaksConfig.TabListSettings();
            case SUBTITLES -> config.subtitles = new TweaksConfig.SubtitleSettings();
        }
        this.clearAndInit();
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
        super.render(context, mouseX, mouseY, deltaTicks);
        for (Label label : this.labels) {
            context.drawTextWithShadow(this.textRenderer, label.text(), label.x(), label.y(), 0xFFFFFFFF);
        }
        for (ColorPreview preview : this.previews) {
            // Light/dark halves underneath so the alpha component is visible.
            context.fill(preview.x() - 1, preview.y() - 1, preview.x() + 13, preview.y() + 13, 0xFF888888);
            context.fill(preview.x(), preview.y(), preview.x() + 6, preview.y() + 12, 0xFFFFFFFF);
            context.fill(preview.x() + 6, preview.y(), preview.x() + 12, preview.y() + 12, 0xFF000000);
            context.fill(preview.x(), preview.y(), preview.x() + 12, preview.y() + 12, TweaksConfig.parseRgba(preview.color().get(), 0xFF000000));
        }
    }

    @Override
    public void close() {
        if (this.client != null) {
            this.client.setScreen(this.parent);
        }
    }

    @Override
    public void removed() {
        TweaksConfig.save();
    }

    private int slotX() {
        return this.width / 2 - 155 + (this.slot % 2) * 160;
    }

    private int slotY() {
        return 36 + (this.slot / 2) * 22;
    }

    private void addToggle(Text label, boolean value, Consumer<Boolean> setter) {
        this.addDrawableChild(
            CyclingButtonWidget.onOffBuilder(value).build(this.slotX(), this.slotY(), 150, 20, label, (button, v) -> setter.accept(v))
        );
        this.slot++;
    }

    private <T extends Enum<T>> void addEnum(Text label, T value, T[] values, Consumer<T> setter) {
        this.addDrawableChild(
            CyclingButtonWidget.<T>builder(v -> Text.literal(prettify(v.name())), value)
                .values(Arrays.asList(values))
                .build(this.slotX(), this.slotY(), 150, 20, label, (button, v) -> setter.accept(v))
        );
        this.slot++;
    }

    private void addIntSlider(Text label, int min, int max, int value, IntConsumer setter) {
        this.addSlider(label, min, max, 1.0F, value, v -> setter.accept(Math.round(v)), v -> Integer.toString(Math.round(v)));
    }

    private void addScaleSlider(float value, Consumer<Float> setter) {
        this.addSlider(Text.translatable("tweaks.config.scale"), 0.5F, 3.0F, 0.05F, value, setter,
            v -> String.format(Locale.ROOT, "%.2fx", v));
    }

    private void addSlider(Text label, float min, float max, float step, float value, Consumer<Float> setter, Function<Float, String> format) {
        this.addDrawableChild(new ConfigSlider(this.slotX(), this.slotY(), min, max, step, value, setter,
            v -> Text.empty().append(label).append(": " + format.apply(v))));
        this.slot++;
    }

    private void addRgba(Text label, Supplier<String> value, Consumer<String> setter) {
        int x = this.slotX();
        int y = this.slotY();
        this.labels.add(new Label(x, y + 6, label));
        this.previews.add(new ColorPreview(x + 66, y + 4, value));
        TextFieldWidget field = new TextFieldWidget(this.textRenderer, x + 82, y, 68, 20, label);
        field.setMaxLength(9);
        field.setText(value.get());
        field.setChangedListener(s -> {
            if (TweaksConfig.isValidRgba(s)) {
                setter.accept(s.startsWith("#") ? s.toUpperCase(Locale.ROOT) : "#" + s.toUpperCase(Locale.ROOT));
            }
        });
        this.addDrawableChild(field);
        this.slot++;
    }

    private static String prettify(String name) {
        return name.charAt(0) + name.substring(1).toLowerCase(Locale.ROOT);
    }

    private static class ConfigSlider extends SliderWidget {
        private final float min;
        private final float max;
        private final float step;
        private final Consumer<Float> setter;
        private final Function<Float, Text> messageFactory;

        ConfigSlider(int x, int y, float min, float max, float step, float value, Consumer<Float> setter, Function<Float, Text> messageFactory) {
            super(x, y, 150, 20, Text.empty(), (value - min) / (max - min));
            this.min = min;
            this.max = max;
            this.step = step;
            this.setter = setter;
            this.messageFactory = messageFactory;
            this.updateMessage();
        }

        private float snappedValue() {
            float raw = (float) (this.min + (this.max - this.min) * this.value);
            return this.step > 0.0F ? Math.round(raw / this.step) * this.step : raw;
        }

        @Override
        protected void updateMessage() {
            this.setMessage(this.messageFactory.apply(this.snappedValue()));
        }

        @Override
        protected void applyValue() {
            this.setter.accept(this.snappedValue());
        }
    }
}
