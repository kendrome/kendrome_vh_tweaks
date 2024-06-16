package com.kendrome.kendrome_vh_tweaks.config;

import net.minecraftforge.common.ForgeConfigSpec;

public class ClientConfig {
    public static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
    public static final ForgeConfigSpec SPEC;

    public static final ForgeConfigSpec.ConfigValue<Boolean> JEWEL_RELATIVE_TOOLTIPS_ENABLED;
    public static final ForgeConfigSpec.ConfigValue<Boolean> JEWEL_RELATIVE_RATING_TOOLTIPS_ENABLED;
    public static final ForgeConfigSpec.EnumValue<HoldKeys> JEWEL_RELATIVE_TOOLTIPS_KEY;
    public static final ForgeConfigSpec.ConfigValue<Boolean> GEAR_COMPARISON_TOOLTIPS_ENABLED;
    public static final ForgeConfigSpec.EnumValue<HoldKeys> GEAR_COMPARISON_TOOLTIPS_KEY;

    static {
        BUILDER.push("Configs for Kendrome VH Tweaks");

        JEWEL_RELATIVE_TOOLTIPS_ENABLED = BUILDER.comment("Shows relative values for jewel affixs in tooltips")
                .define("Enable jewel relative tooltips", true);

        JEWEL_RELATIVE_RATING_TOOLTIPS_ENABLED = BUILDER.comment("Show a rating instead of the relative value in jewel tooltips")
                .define("Enable jewel rating tooltips", false);

        JEWEL_RELATIVE_TOOLTIPS_KEY = BUILDER.comment("Require key to show (None for always show)")
                .defineEnum("Jewel relative tooltips key", HoldKeys.None);

        GEAR_COMPARISON_TOOLTIPS_ENABLED = BUILDER.comment("Show gear comparison tooltips")
                .define("Enable gear comparison tooltips", true);

        GEAR_COMPARISON_TOOLTIPS_KEY = BUILDER.comment("Require key to show (None for always show)")
                .defineEnum("Gear Comparison tooltips key", HoldKeys.None);

        BUILDER.pop();
        SPEC = BUILDER.build();
    }

    public enum HoldKeys {
        None, Shift, Alt, Ctrl
    }
}
