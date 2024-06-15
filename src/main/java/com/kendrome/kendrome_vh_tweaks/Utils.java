package com.kendrome.kendrome_vh_tweaks;

import com.kendrome.kendrome_vh_tweaks.config.ClientConfig;
import iskallia.vault.gear.attribute.VaultGearAttributeInstance;
import net.minecraft.client.gui.screens.Screen;

public class Utils {
    public static boolean shouldShow(ClientConfig.HoldKeys keys) {
        return switch (keys) {
            case Ctrl -> Screen.hasControlDown();
            case Alt -> Screen.hasAltDown();
            case Shift -> Screen.hasShiftDown();
            default -> true;
        };
    }

    public static String formatText(Number number) {
        if (number.floatValue() >= 10) {
            return number.toString();
        }

        String str = "%.2g".formatted(number);
        if (str.contains(".") && str.endsWith("0")) {
            str = str.substring(0, str.length() - 1);
        }

        if (str.contains(".") && str.endsWith(".")) {
            str = str.substring(0, str.length() - 1);
        }

        return str;
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
}
