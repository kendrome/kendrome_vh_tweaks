package com.kendrome.kendrome_vh_tweaks.tooltips;

import com.kendrome.kendrome_vh_tweaks.KendromeVhTweaks;
import com.kendrome.kendrome_vh_tweaks.Utils;
import com.kendrome.kendrome_vh_tweaks.config.ClientConfig;
import iskallia.vault.config.gear.VaultGearTierConfig;
import iskallia.vault.gear.VaultGearState;
import iskallia.vault.gear.attribute.VaultGearAttribute;
import iskallia.vault.gear.attribute.VaultGearModifier;
import iskallia.vault.gear.attribute.config.FloatAttributeGenerator;
import iskallia.vault.gear.attribute.config.NumberRangeGenerator;
import iskallia.vault.gear.data.VaultGearData;
import iskallia.vault.gear.reader.DecimalModifierReader;
import iskallia.vault.init.ModGearAttributes;
import iskallia.vault.init.ModItems;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.item.ItemStack;
import org.apache.commons.lang3.reflect.FieldUtils;
import java.util.List;
import java.util.Set;

public class JewelTooltips {
    private static final Set<VaultGearAttribute<?>> EXCLUDED_ATTRIBUTES = Set.of(ModGearAttributes.HAMMER_SIZE);
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
                if (!(suffix.getValue() instanceof Number value) || EXCLUDED_ATTRIBUTES.contains(suffix.getAttribute())) {
                    continue;
                }

                float relative = value.floatValue() / size;
                VaultGearTierConfig config = VaultGearTierConfig.getConfig(itemStack).get();

                var range = config.getTierConfig(suffix);
                if (!(range instanceof NumberRangeGenerator.NumberRange)) {
                    toolTip.add(createTooltip(suffix, relative, 0, 0, false));
                    continue;
                }

                var minGeneric = FieldUtils.readField(range, "min", true);
                var maxGeneric = FieldUtils.readField(range, "max", true);

                if (!(minGeneric instanceof Number min) || !(maxGeneric instanceof Number max)) {
                    toolTip.add(createTooltip(suffix, relative, 0, 0, false));
                    continue;
                }

                toolTip.add(createTooltip(suffix, relative, min, max, Screen.hasShiftDown()));
            }
        } catch (Exception e) {
            KendromeVhTweaks.LOGGER.error(e.getMessage());
        }
    }

    public static Component createTooltip(VaultGearModifier<?> suffix, float relative, Number min, Number max, boolean showDetails) {
        boolean percentage = suffix.getAttribute().getReader() instanceof DecimalModifierReader.Percentage;
        int multiplier = percentage ? 100 : 1;

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

        if (ClientConfig.JEWEL_RELATIVE_RATING_TOOLTIPS_ENABLED.get() && !(min.doubleValue() == 0.0 && max.doubleValue() == 0.0)) {
            var maxRelative = max.floatValue() / 10 * multiplier;
            var rating = relative / maxRelative * 100;
            String raw = Utils.formatText(rating) + "% perfect " + name + " rating";
            return new TextComponent(raw).withStyle(displayTextComponent.getStyle());
        }

        String raw = "+" + Utils.formatText(relative) + (percentage ? "% " : " ") + name + " / size";
        MutableComponent line = new TextComponent(raw).withStyle(displayTextComponent.getStyle());
        if (showDetails) {
            var minRelative = Utils.formatText(min.floatValue() / 35 * multiplier);
            var maxRelative = Utils.formatText(max.floatValue() / 10 * multiplier);
            line.append("§7 (" + minRelative + "-" + maxRelative + ")");
        }
        return line;
    }
}
