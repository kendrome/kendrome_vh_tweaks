package com.kendrome.kendrome_vh_tweaks.tooltips;

import com.kendrome.kendrome_vh_tweaks.KendromeVhTweaks;
import com.kendrome.kendrome_vh_tweaks.Utils;
import com.kendrome.kendrome_vh_tweaks.config.ClientConfig;
import iskallia.vault.config.gear.VaultGearTierConfig;
import iskallia.vault.gear.VaultGearState;
import iskallia.vault.gear.attribute.VaultGearModifier;
import iskallia.vault.gear.data.VaultGearData;
import iskallia.vault.gear.reader.DecimalModifierReader;
import iskallia.vault.init.ModGearAttributes;
import iskallia.vault.init.ModItems;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.item.ItemStack;
import org.apache.commons.lang3.reflect.FieldUtils;

import java.util.List;

public class JewelTooltips {
    private static final ItemStack DUMMY_JEWEL = new ItemStack(ModItems.JEWEL);

    public static void appendTooltip(ItemStack itemStack, List<Component> toolTip) {
        if (!ClientConfig.JEWEL_RELATIVE_TOOLTIPS_ENABLED.get()
                || !Utils.shouldShow(ClientConfig.JEWEL_RELATIVE_TOOLTIPS_KEY.get())) {
            return;
        }

        VaultGearData data = VaultGearData.read(itemStack);
        if (data.getState() != VaultGearState.IDENTIFIED) {
            return;
        }

        int size = data.getFirstValue(ModGearAttributes.JEWEL_SIZE).orElse(0);
        if (size <= 0) {
            return;
        }

        toolTip.add(TextComponent.EMPTY);
        try {
            for (VaultGearModifier<?> suffix : data.getModifiers(VaultGearModifier.AffixType.SUFFIX)) {
                if (!(suffix.getValue() instanceof Number value)) {
                    continue;
                }

                float relative = value.floatValue() / size;
                VaultGearTierConfig config = VaultGearTierConfig.getConfig(itemStack).get();

                var range = config.getTierConfig(suffix);
                var minGeneric = FieldUtils.readField(range, "min", true);
                var maxGeneric = FieldUtils.readField(range, "max", true);

                if (!(minGeneric instanceof Number min) || !(maxGeneric instanceof Number max)) {
                    continue;
                }

                toolTip.add(createTooltip(suffix, relative, min, max, Screen.hasShiftDown()));
            }
        } catch (Exception e) {
            KendromeVhTweaks.LOGGER.error(e.getMessage());
        }
    }

    public static Component createTooltip(VaultGearModifier<?> suffix, float relative, Number min, Number max, boolean showDetails) {
        int multiplier = suffix.getAttribute().getReader() instanceof DecimalModifierReader.Percentage
                ? 100
                : 1;

        String name = switch (suffix.getModifierIdentifier().toString().substring(10)) {
            case "copiously" -> "Copiously";
            case "item_quantity" -> "Quantity";
            case "item_rarity" -> "Rarity";
            case "immortality" -> "Immortality";
            case "trap_disarming" -> "Disarm";
            case "mining_speed" -> "Mining Speed";
            case "reach" -> "Reach";
            case "durability" -> "Durability";
            default -> suffix.getModifierGroup().substring(3);
        };
        relative *= multiplier;

        var display = suffix.getConfigDisplay(DUMMY_JEWEL);
        var displayTextComponent = (TextComponent) display.get();

        if (showDetails) {
            var minRelative = Utils.formatText(min.floatValue() / 90 * multiplier);
            var maxRelative = Utils.formatText(max.floatValue() / 10 * multiplier);
            return new TextComponent(Utils.formatText(relative) + " " + name + "/size §7(" + minRelative + "-" + maxRelative + ")").withStyle(displayTextComponent.getStyle());
        }
        return new TextComponent(Utils.formatText(relative) + " " + name + "/size").withStyle(displayTextComponent.getStyle());
    }
}
