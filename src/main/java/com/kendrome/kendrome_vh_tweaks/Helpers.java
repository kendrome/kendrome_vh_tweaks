package com.kendrome.kendrome_vh_tweaks;

import com.kendrome.kendrome_vh_tweaks.config.ClientConfig;
import net.minecraft.client.gui.screens.Screen;

import java.util.Formatter;

public class Helpers {
    public static boolean ShouldShow(ClientConfig.HoldKeys keys) {
        switch(keys) {
            case Ctrl:
                return Screen.hasControlDown();
            case Alt:
                return Screen.hasAltDown();
            case Shift:
                return Screen.hasShiftDown();
            default:
                return true;
        }
    }
    public static <T> String FormatText(T value) {
        if(IsGreaterThanEqual10(value))
            return value.toString();
        Formatter fmt = new Formatter();
        var str = fmt.format("%.2g", value).toString();
        if(str.contains(".") && str.substring(str.length() -1).equals("0")) {
            str = str.substring(0, str.length() - 1);
        }
        if(str.contains(".") && str.substring(str.length() -1).equals(".")) {
            str = str.substring(0, str.length() - 1);
        }
        return str;
    }

    public static boolean IsPositive(Object value) {
        if(value instanceof Integer) {
            return (Integer)value > 0;
        } else if(value instanceof Double) {
            return (Double)value > 0;
        } else if(value instanceof Float) {
            return (Float)value > 0;
        }
        return false;
    }
    public static boolean IsGreaterThanEqual10(Object value) {
        if(value instanceof Integer) {
            return (Integer)value >= 10;
        } else if(value instanceof Double) {
            return (Double)value >= 10;
        } else if(value instanceof Float) {
            return (Float)value >= 10;
        }
        return false;
    }
    /*public static String FormatText(Double value) {
        Formatter fmt = new Formatter();
        var str = fmt.format("%.2g", value).toString();
        if(str.contains(".") && str.substring(str.length() -1).equals("0")) {
            str = str.substring(0, str.length() - 1);
        }
        return str;
    }*/
}
