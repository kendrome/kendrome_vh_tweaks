package com.kendrome.kendrome_vh_tweaks;

import com.kendrome.kendrome_vh_tweaks.config.ClientConfig;
import iskallia.vault.gear.attribute.VaultGearAttributeInstance;
import net.minecraft.client.gui.screens.Screen;

import java.text.DecimalFormat;

public class Utils {
    private static final DecimalFormat GREATER_ONE = new DecimalFormat("#.##");
    private static final DecimalFormat LESS_ONE = new DecimalFormat("#.##############################");

    public static boolean shouldShow(ClientConfig.HoldKeys keys) {
        return switch (keys) {
            case Ctrl -> Screen.hasControlDown();
            case Alt -> Screen.hasAltDown();
            case Shift -> Screen.hasShiftDown();
            default -> true;
        };
    }

    public static String formatText(Number number) {
        if (Math.abs(number.doubleValue()) >= 1) {
            return GREATER_ONE.format(number.doubleValue());
        } else {
            return LESS_ONE.format(roundSigDig(number.doubleValue(), 3));
        }
    }

    public static <T> void invertValue(VaultGearAttributeInstance<T> instance) {
        T value = instance.getValue();
        if (value instanceof Integer i) {
            value = (T) Integer.valueOf(-i);
        } else if (value instanceof Float f) {
            value = (T) Float.valueOf(-f);
        } else if (value instanceof Double d) {
            value = (T) Double.valueOf(-d);
        }
        instance.setValue(value);
    }

    public static double roundSigDig(double value, int sigDig) {
        if (value == 0) {
            return 0;
        }

        final double scale = Math.pow(10, sigDig - 1 - (int) Math.floor(Math.log10(Math.abs(value))));
        return Math.round(value * scale) / scale;
    }
}
