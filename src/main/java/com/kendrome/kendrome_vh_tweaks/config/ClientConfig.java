package com.kendrome.kendrome_vh_tweaks.config;

import net.minecraftforge.common.ForgeConfigSpec;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ClientConfig {
    public static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
    public static final ForgeConfigSpec SPEC;

    public static final ForgeConfigSpec.ConfigValue<Boolean> JEWEL_RELATIVE_TOOLTIPS_ENABLED;
    public static final ForgeConfigSpec.EnumValue<HoldKeys> JEWEL_RELATIVE_TOOLTIPS_KEY;
    public static final ForgeConfigSpec.ConfigValue<Boolean> GEAR_COMPARISON_TOOLTIPS_ENABLED;
    public static final ForgeConfigSpec.EnumValue<HoldKeys> GEAR_COMPARISON_TOOLTIPS_KEY;
    public static final ForgeConfigSpec.ConfigValue<Boolean> INSCRIPTION_RELATIVE_TOOLTIPS_ENABLED;
    public static final ForgeConfigSpec.EnumValue<HoldKeys> INSCRIPTION_RELATIVE_TOOLTIPS_KEY;
    public static final ForgeConfigSpec.ConfigValue<List<? extends String>> INSCRIPTIONS_RELATIVE_OPTIONS;

    static {
        BUILDER.push("Configs for Kendrome VH Tweaks");

        JEWEL_RELATIVE_TOOLTIPS_ENABLED = BUILDER.comment("Shows relative values for jewel affixs in tooltips")
                .define("Enable jewel relative tooltips", true);

        JEWEL_RELATIVE_TOOLTIPS_KEY = BUILDER.comment("Require key to show (None for always show)")
                .defineEnum("Jewel relative tooltips key", HoldKeys.None);

        GEAR_COMPARISON_TOOLTIPS_ENABLED = BUILDER.comment("Show gear comparison tooltips")
                .define("Enable gear comparison tooltips", true);

        GEAR_COMPARISON_TOOLTIPS_KEY = BUILDER.comment("Require key to show (None for always show)")
                .defineEnum("Gear Comparison tooltips key", HoldKeys.None);

        INSCRIPTION_RELATIVE_TOOLTIPS_ENABLED = BUILDER.comment("Show inscription relative tooltips")
                .define("Enable inscription relative tooltips", true);

        INSCRIPTION_RELATIVE_TOOLTIPS_KEY = BUILDER.comment("Require key to show (None for always show)")
                .defineEnum("Inscription relative tooltips key", HoldKeys.None);

        List<String> inscriptionOptions = Stream.of(InscriptionOptions.values()).map(Enum::name).collect(Collectors.toList());
        INSCRIPTIONS_RELATIVE_OPTIONS = BUILDER.comment("Which relatives values to show, Valid options [\"CompletionPerTime\", \"CompletionPerInstability\", \"TimePerInstability\"]")
                .defineList("Inscription display options",
                        inscriptionOptions,
                        entry -> true
                );

        BUILDER.pop();
        SPEC = BUILDER.build();
    }

    public enum HoldKeys {
        None, Shift, Alt, Ctrl
    }

    public enum InscriptionOptions {
        CompletionPerTime,
        CompletionPerInstability,
        TimePerInstability
    }
}
