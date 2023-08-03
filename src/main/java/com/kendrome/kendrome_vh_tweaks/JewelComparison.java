package com.kendrome.kendrome_vh_tweaks;

import iskallia.vault.config.gear.VaultGearTierConfig;
import iskallia.vault.gear.VaultGearState;
import iskallia.vault.gear.attribute.VaultGearModifier;
import iskallia.vault.gear.data.VaultGearData;
import iskallia.vault.init.ModItems;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.item.ItemStack;
import org.apache.commons.lang3.reflect.FieldUtils;

import java.util.Formatter;
import java.util.List;

import static com.kendrome.kendrome_vh_tweaks.Helpers.FormatText;

public class JewelComparison {
    public static void ShowComparison(ItemStack itemStack, List<Component> toolTip) throws IllegalAccessException {
        VaultGearData data = VaultGearData.read(itemStack);
        var state = data.getState();

        if(state != VaultGearState.IDENTIFIED)
            return;

        var suffixes = data.getModifiers(VaultGearModifier.AffixType.SUFFIX);
        var implicits = data.getModifiers(VaultGearModifier.AffixType.IMPLICIT);
        int size = 0;
        for (var implicit : implicits) {
            var group = implicit.getModifierGroup();
            if (group.equals("BaseJewelSize")) {
                size = (int) implicit.getValue();
                break;
            }
        }

        for (var suffix : suffixes) {
            if (size > 0) {
                var value = suffix.getValue();
                var relative = GetRelative(value, size);
                if (relative > 0) {
                    var config = VaultGearTierConfig.getConfig(itemStack.getItem()).get();

                    var range = config.getTierConfig(suffix);

                    var min = FieldUtils.readField(range, "min", true);
                    var max = FieldUtils.readField(range, "max", true);
                    //var display2 = suffix.getConfigDisplay(itemStack);
                    //var display = suffix.getDisplay(data, VaultGearModifier.AffixType.SUFFIX, itemStack, true);
                    toolTip.add(GetJewelRelativeDisplay(suffix, relative, min, max, Screen.hasShiftDown()));
                }
            }
        }
    }

    public static float GetRelative(Object value, int size) {
        float relative = 0;
        if (value instanceof Float) {
            relative = (float) value / size;
        } else if (value instanceof Double) {
            relative = (float) ((double) value / size);
        } else if (value instanceof Integer) {
            relative = (float) ((int) value / size);
        }
        return relative;
    }
    public static Component GetJewelRelativeDisplay(VaultGearModifier suffix, float relative, Object min, Object max, boolean showDetails) {
        String name;
        int multiplier = 1;
        switch (suffix.getModifierIdentifier().toString().substring(10)) {
            case "copiously":
                multiplier = 100;
                name = "Copiously";
                break;
            case "item_quantity":
                multiplier = 100;
                name = "Quantity";
                break;
            case "item_rarity":
                multiplier = 100;
                name = "Rarity";
                break;
            case "immortality":
                multiplier = 100;
                name = "Immortality";
                break;
            case "trap_disarming":
                multiplier = 100;
                name = "Disarm";
                break;
            case "mining_speed":
                name = "Mining Speed";
                break;
            case "reach":
                name = "Reach";
                break;
            case "durability":
                name = "Durability";
                break;
            default:
                name = suffix.getModifierGroup().substring(3);
                break;
        }
        relative *= multiplier;

        var display = suffix.getConfigDisplay(new ItemStack(ModItems.JEWEL));
        var displayTextComponent = (TextComponent)display.get();
        //if(display.isPresent())
        //return (Component)display.get();
        if(showDetails) {
            var minRelative = FormatText(GetRelative(min, 90) * multiplier);
            var maxRelative =  FormatText(GetRelative(max, 10) * multiplier);
            return new TextComponent(FormatText(relative) + " " + name + "/size §7(" + minRelative  + "-" + maxRelative + ")").withStyle(displayTextComponent.getStyle());
        } else {
            return new TextComponent(FormatText(relative) + " " + name + "/size").withStyle(displayTextComponent.getStyle());
        }

    }
}
