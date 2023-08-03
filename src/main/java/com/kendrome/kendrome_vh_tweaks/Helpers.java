package com.kendrome.kendrome_vh_tweaks;

import java.util.Formatter;

public class Helpers {
    public static <T> String FormatText(T value) {
        if(IsPositive(value))
            return value.toString();
        Formatter fmt = new Formatter();
        var str = fmt.format("%.2g", value).toString();
        if(str.contains(".") && str.substring(str.length() -1).equals("0")) {
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
    /*public static String FormatText(Double value) {
        Formatter fmt = new Formatter();
        var str = fmt.format("%.2g", value).toString();
        if(str.contains(".") && str.substring(str.length() -1).equals("0")) {
            str = str.substring(0, str.length() - 1);
        }
        return str;
    }*/
}
