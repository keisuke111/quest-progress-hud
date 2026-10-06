package io.github.keisuke111.questprogresshud;

import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.common.ModConfigSpec;
import net.neoforged.neoforge.common.TranslatableEnum;

import java.util.Locale;

public final class HudConfig {
    public enum Anchor implements TranslatableEnum {
        TOP_LEFT(false, false),
        TOP_RIGHT(true, false),
        BOTTOM_LEFT(false, true),
        BOTTOM_RIGHT(true, true);

        public final boolean right;
        public final boolean bottom;

        Anchor(boolean right, boolean bottom) {
            this.right = right;
            this.bottom = bottom;
        }

        @Override
        public Component getTranslatedName() {
            return Component.translatable("quest_progress_hud.configuration.anchor."
                    + name().toLowerCase(Locale.ROOT));
        }
    }

    public static final ModConfigSpec SPEC;
    public static final ModConfigSpec.BooleanValue ENABLED;
    public static final ModConfigSpec.EnumValue<Anchor> ANCHOR;
    public static final ModConfigSpec.IntValue OFFSET_X;
    public static final ModConfigSpec.IntValue OFFSET_Y;
    public static final ModConfigSpec.DoubleValue SCALE;
    public static final ModConfigSpec.IntValue BACKGROUND_OPACITY;

    static {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();
        ENABLED = builder.comment("Show the quest progress HUD.")
                .translation("quest_progress_hud.configuration.enabled")
                .define("enabled", true);
        ANCHOR = builder.comment("Screen corner used to position the HUD.")
                .translation("quest_progress_hud.configuration.anchor")
                .defineEnum("anchor", Anchor.TOP_LEFT);
        OFFSET_X = builder.comment("Horizontal offset in GUI pixels. Positive moves inward from the selected edge.")
                .translation("quest_progress_hud.configuration.offsetX")
                .defineInRange("offsetX", 0, -10000, 10000);
        OFFSET_Y = builder.comment("Vertical offset in GUI pixels. Positive moves inward from the selected edge.")
                .translation("quest_progress_hud.configuration.offsetY")
                .defineInRange("offsetY", 0, -10000, 10000);
        SCALE = builder.comment("HUD scale relative to Minecraft GUI scale. Automatically reduced to fit small screens.")
                .translation("quest_progress_hud.configuration.scale")
                .defineInRange("scale", 1.0, 0.5, 3.0);
        BACKGROUND_OPACITY = builder.comment("Background opacity in percent: 0 is transparent, 100 is opaque.")
                .translation("quest_progress_hud.configuration.backgroundOpacity")
                .defineInRange("backgroundOpacity", 72, 0, 100);
        SPEC = builder.build();
    }

    private HudConfig() {}
}
