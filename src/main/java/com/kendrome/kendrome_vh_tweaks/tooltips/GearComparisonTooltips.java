package com.kendrome.kendrome_vh_tweaks.tooltips;

import com.kendrome.kendrome_vh_tweaks.Utils;
import com.kendrome.kendrome_vh_tweaks.KendromeVhTweaks;
import com.kendrome.kendrome_vh_tweaks.config.ClientConfig;
import iskallia.vault.gear.VaultGearState;
import iskallia.vault.gear.attribute.VaultGearAttribute;
import iskallia.vault.gear.attribute.VaultGearAttributeInstance;
import iskallia.vault.gear.attribute.VaultGearAttributeRegistry;
import iskallia.vault.gear.attribute.VaultGearModifier;
import iskallia.vault.gear.attribute.custom.EffectAvoidanceGearAttribute;
import iskallia.vault.gear.data.AttributeGearData;
import iskallia.vault.gear.data.VaultGearData;
import iskallia.vault.gear.item.CuriosGearItem;
import iskallia.vault.gear.item.VaultGearItem;
import iskallia.vault.gear.trinket.GearAttributeTrinket;
import iskallia.vault.gear.trinket.TrinketEffect;
import iskallia.vault.gear.trinket.TrinketHelper;
import iskallia.vault.init.ModGearAttributes;
import iskallia.vault.init.ModItems;
import iskallia.vault.integration.IntegrationCurios;
import iskallia.vault.item.MagnetItem;
import iskallia.vault.item.gear.IdolItem;
import iskallia.vault.item.gear.VaultArmorItem;
import iskallia.vault.item.gear.WandItem;
import iskallia.vault.item.tool.JewelItem;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.util.Tuple;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ShieldItem;
import net.minecraft.world.item.SwordItem;

import java.util.*;
import java.util.function.Function;

public class GearComparisonTooltips {
    private static final Set<VaultGearAttribute<?>> EXCLUDED_ATTRIBUTES = Set.of(ModGearAttributes.DURABILITY, ModGearAttributes.SOULBOUND);
    private static final List<VaultGearAttribute<?>> ATTRIBUTES_ORDER = new ArrayList<>(VaultGearAttributeRegistry.getRegistry().getValues());
    private static final VaultGearData DUMMY_DATA = VaultGearData.read(new ItemStack(ModItems.BOOTS));

    public static void appendTooltip(ItemStack itemStack, List<Component> toolTip) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (!ClientConfig.GEAR_COMPARISON_TOOLTIPS_ENABLED.get()
                || !Utils.shouldShow(ClientConfig.GEAR_COMPARISON_TOOLTIPS_KEY.get())
                || player == null) {
            return;
        }

        try {
            Item item = itemStack.getItem();
            EquipmentSlot slot = item instanceof IdolItem || item instanceof WandItem || item instanceof ShieldItem
                    ? EquipmentSlot.OFFHAND
                    : Mob.getEquipmentSlotForItem(itemStack);

            ItemStack equippedStack = player.getItemBySlot(slot);
            Map<String, List<Tuple<ItemStack, Integer>>> curiosItemStacks = IntegrationCurios.getCuriosItemStacks(player);

            if (item instanceof MagnetItem) {
                for (List<Tuple<ItemStack, Integer>> stack : curiosItemStacks.values()) {
                    for (Tuple<ItemStack, Integer> tpl : stack) {
                        ItemStack stackTpl = tpl.getA();
                        if (stackTpl.getItem() instanceof MagnetItem && AttributeGearData.hasData(stackTpl)) {
                            equippedStack = stackTpl;
                        }
                    }
                }
            }

            if (equippedStack == itemStack) {
                return;
            }

            Item equippedItem = equippedStack.getItem();
            if (slot == EquipmentSlot.MAINHAND) {
                boolean bothWeapons = (equippedItem instanceof SwordItem || equippedItem instanceof AxeItem)
                        || (item instanceof SwordItem || item instanceof AxeItem);

                if (!bothWeapons && !(item instanceof MagnetItem) || item instanceof JewelItem) {
                    return;
                }
            }

            VaultGearData selectedData = VaultGearData.read(itemStack);
            if (selectedData.getState() != VaultGearState.IDENTIFIED) {
                return;
            }

            for (List<Tuple<ItemStack, Integer>> stack : curiosItemStacks.values()) {
                for (Tuple<ItemStack, Integer> tpl : stack) {
                    tpl.setA(ItemStack.EMPTY);
                }
            }

            int baseManaEquipped = baseMana(player::getItemBySlot, curiosItemStacks, player);
            int baseManaSelected = baseMana(eSlot -> eSlot == slot ? itemStack : player.getItemBySlot(eSlot), curiosItemStacks, player);

            List<VaultGearAttributeInstance<?>> addingAttributeInstances = new ArrayList<>();
            List<VaultGearAttributeInstance<?>> removingAttributeInstances = new ArrayList<>();
            Map<VaultGearAttribute<?>, VaultGearAttributeInstance<?>> mergeableAttributes = new HashMap<>();

            addAttributes(equippedStack, mergeableAttributes, removingAttributeInstances, true, baseManaEquipped);
            addCuriosAttributes(player, mergeableAttributes, removingAttributeInstances, curiosItemStacks, true, baseManaEquipped);
            addAttributes(itemStack, mergeableAttributes, addingAttributeInstances, false, baseManaSelected);

            mergeableAttributes.values().forEach((instance) -> addMergeableAttribute(addingAttributeInstances, removingAttributeInstances, instance));
            addingAttributeInstances.sort(Comparator.comparingInt(instance -> ATTRIBUTES_ORDER.indexOf(instance.getAttribute())));
            removingAttributeInstances.sort(Comparator.comparingInt(instance -> ATTRIBUTES_ORDER.indexOf(instance.getAttribute())));

            toolTip.add(TextComponent.EMPTY);
            if (removingAttributeInstances.isEmpty() && addingAttributeInstances.isEmpty()) {
                toolTip.add((new TranslatableComponent("screen.the_vault.wardrobe.tooltip.no_difference")).withStyle(ChatFormatting.YELLOW));
                return;
            }

            toolTip.add((new TranslatableComponent("screen.the_vault.wardrobe.tooltip.difference")).withStyle(ChatFormatting.GRAY));

            for (VaultGearAttributeInstance<?> adding : addingAttributeInstances) {
                addTooltipDisplay(adding, "+", ChatFormatting.GREEN, toolTip);
            }

            for (VaultGearAttributeInstance<?> removing : removingAttributeInstances) {
                addTooltipDisplay(removing, "-", ChatFormatting.RED, toolTip);
            }
        } catch (Exception e) {
            KendromeVhTweaks.LOGGER.error(e.getMessage());
        }
    }

    private static <T> void addTooltipDisplay(VaultGearAttributeInstance<T> vaultGearAttributeInstance,
                                              String prefix, ChatFormatting formatting, List<Component> toolTip) {
        vaultGearAttributeInstance.getDisplay(DUMMY_DATA, VaultGearModifier.AffixType.IMPLICIT, ItemStack.EMPTY, true).ifPresent((displayText) -> {
            //Temporary Fix for getDisplay returning wrong values for attack_speed
            if (vaultGearAttributeInstance.getAttribute() == ModGearAttributes.ATTACK_SPEED) {
                displayText = new TextComponent(Utils.formatText((Double) vaultGearAttributeInstance.getValue()))
                        .append(" Attack Speed").withStyle(Style.EMPTY.withColor(16767592));
            }

            toolTip.add(new TextComponent(prefix).withStyle(formatting).append(displayText));
        });
    }

    private static <T> void addMergeableAttribute(List<VaultGearAttributeInstance<?>> addingAttributeInstances,
                                                  List<VaultGearAttributeInstance<?>> removingAttributeInstances,
                                                  VaultGearAttributeInstance<T> instance) {
        Number number;

        if(instance.getValue() instanceof Number) {
            number = (Number)instance.getValue();
        } else if(instance.getValue() instanceof  EffectAvoidanceGearAttribute) {
            number = ((EffectAvoidanceGearAttribute) instance.getValue()).getChance();
        } else {
            return;
        }

        if(Math.abs(number.doubleValue()) < 1.0E-4F) {
            return;
        }

        if (number.doubleValue() > 0.0) {
            addingAttributeInstances.add(instance);
        } else {
            Utils.invertValue(instance);
            removingAttributeInstances.add(instance);
        }
    }

    private static void addAttributes(ItemStack stack,
                                      Map<VaultGearAttribute<?>, VaultGearAttributeInstance<?>> mergeableAttributes,
                                      List<VaultGearAttributeInstance<?>> attributeInstances, boolean inverted, int mana) {
        AttributeGearData data = AttributeGearData.read(stack);

        Iterable<? extends VaultGearAttributeInstance<?>> instances = (data instanceof VaultGearData gearData)
                ? VaultGearData.Type.ALL_MODIFIERS.getAttributeSource(gearData).toList()
                : data.getAttributes();

        for (VaultGearAttributeInstance<?> instance : instances) {
            addAttribute(mergeableAttributes, attributeInstances, instance, inverted, mana);
        }
    }

    private static int baseMana(Function<EquipmentSlot, ItemStack> getItemBySlot,
                                Map<String, List<Tuple<ItemStack, Integer>>> curiosItemStacks,
                                Player player) {
        int baseMana = 100;
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            ItemStack stack = simulateVaultGear(slot, getItemBySlot.apply(slot));
            if (stack.isEmpty()) {
                continue;
            }

            Item item = stack.getItem();
            if (item instanceof VaultGearItem gearItem) {
                if (!gearItem.isIntendedForSlot(stack, slot)) {
                    continue;
                }
            }

            if (item instanceof CuriosGearItem gearItem) {
                if (!gearItem.isIntendedSlot(stack, slot)) {
                    continue;
                }
            }

            AttributeGearData data = AttributeGearData.read(stack);
            Iterable<? extends VaultGearAttributeInstance<?>> instances = (data instanceof VaultGearData gearData)
                    ? VaultGearData.Type.ALL_MODIFIERS.getAttributeSource(gearData).toList()
                    : data.getAttributes();

            for (VaultGearAttributeInstance<?> instance : instances) {
                if (instance.getAttribute() == ModGearAttributes.MANA_ADDITIVE) {
                    baseMana += (int) instance.getValue();
                }
            }
        }

        for (TrinketHelper.TrinketStack<? extends TrinketEffect<?>> gearTrinket : TrinketHelper.getTrinkets(curiosItemStacks, GearAttributeTrinket.class)) {
            if (gearTrinket.isUsable(player)) {
                for (VaultGearAttributeInstance<?> instance : ((GearAttributeTrinket) gearTrinket.trinket()).getAttributes()) {
                    if (instance.getAttribute() == ModGearAttributes.MANA_ADDITIVE) {
                        baseMana += (int) instance.getValue();
                    }
                }
            }
        }

        for (String slot : curiosItemStacks.keySet()) {
            List<Tuple<ItemStack, Integer>> stacks = curiosItemStacks.get(slot);
            for (Tuple<ItemStack, Integer> stackTpl : stacks) {
                ItemStack stack = stackTpl.getA();
                if (!AttributeGearData.hasData(stack)) {
                    continue;
                }

                Item item = stack.getItem();
                if (item instanceof CuriosGearItem curiosItem && !curiosItem.isIntendedSlot(stack, slot)) {
                    continue;
                }

                if (!stack.is(ModItems.MAGNET) || !MagnetItem.isLegacy(stack)) {
                    for (VaultGearAttributeInstance<?> instance : AttributeGearData.read(stack).getAttributes()) {
                        if (instance.getAttribute() == ModGearAttributes.MANA_ADDITIVE) {
                            baseMana += (int) instance.getValue();
                        }
                    }
                }
            }
        }

        return baseMana;
    }

    private static <T> void addAttribute(Map<VaultGearAttribute<?>, VaultGearAttributeInstance<?>> mergeableAttributes,
                                         List<VaultGearAttributeInstance<?>> attributeInstances,
                                         VaultGearAttributeInstance<T> instance, boolean inverted, int baseMana) {
        if (!(instance.getValue() instanceof Number || instance.getValue() instanceof EffectAvoidanceGearAttribute)) {
            return;
        }

        VaultGearAttribute<T> attribute = instance.getAttribute();
        if (attribute == ModGearAttributes.MANA_ADDITIVE_PERCENTILE) {
            addAttribute(mergeableAttributes, attributeInstances,
                    new VaultGearAttributeInstance<>(ModGearAttributes.MANA_ADDITIVE, (int) ((Float) instance.getValue() * baseMana)),
                    inverted, baseMana);
            return;
        }

        if (!EXCLUDED_ATTRIBUTES.contains(attribute)) {
            if (attribute.getAttributeComparator() == null) {
                attributeInstances.add(instance);
            } else {
                mergeAttribute(mergeableAttributes, instance, attribute, inverted);
            }
        }
    }

    private static <T> void mergeAttribute(Map<VaultGearAttribute<?>, VaultGearAttributeInstance<?>> mergeableAttributes,
                                           VaultGearAttributeInstance<T> instance, VaultGearAttribute<T> attribute, boolean inverted) {
        if (inverted) {
            Utils.invertValue(instance);
        }

        if (mergeableAttributes.containsKey(attribute)) {
            VaultGearAttributeInstance<T> mergeIntoInstance = (VaultGearAttributeInstance<T>) mergeableAttributes.get(attribute);
            Optional<T> merged = attribute.getAttributeComparator().merge(mergeIntoInstance.getValue(), instance.getValue());
            merged.ifPresent(mergeIntoInstance::setValue);
            return;
        }

        mergeableAttributes.put(attribute, instance);
    }

    private static void addCuriosAttributes(Player player,
                                            Map<VaultGearAttribute<?>, VaultGearAttributeInstance<?>> mergeableAttributes,
                                            List<VaultGearAttributeInstance<?>> attributeInstances,
                                            Map<String, List<Tuple<ItemStack, Integer>>> curiosItemStacks,
                                            boolean inverted, int baseMana) {
        for (TrinketHelper.TrinketStack<? extends TrinketEffect<?>> gearTrinket : TrinketHelper.getTrinkets(curiosItemStacks, GearAttributeTrinket.class)) {
            if (gearTrinket.isUsable(player)) {
                for (VaultGearAttributeInstance<?> instance : ((GearAttributeTrinket) gearTrinket.trinket()).getAttributes()) {
                    addAttribute(mergeableAttributes, attributeInstances, instance, inverted, baseMana);
                }
            }
        }

        for (String slot : curiosItemStacks.keySet()) {
            List<Tuple<ItemStack, Integer>> stacks = curiosItemStacks.get(slot);
            for (Tuple<ItemStack, Integer> stackTpl : stacks) {
                ItemStack stack = stackTpl.getA();
                if (!AttributeGearData.hasData(stack)) {
                    continue;
                }

                Item item = stack.getItem();
                if (item instanceof CuriosGearItem curiosItem && !curiosItem.isIntendedSlot(stack, slot)) {
                    continue;
                }

                if (!stack.is(ModItems.MAGNET) || !MagnetItem.isLegacy(stack)) {
                    for (VaultGearAttributeInstance<?> instance : AttributeGearData.read(stack).getAttributes()) {
                        addAttribute(mergeableAttributes, attributeInstances, instance, inverted, baseMana);
                    }
                }
            }
        }
    }

    private static ItemStack simulateVaultGear(EquipmentSlot slot, ItemStack stack) {
        if (stack.isEmpty()) {
            return stack;
        }

        Item item = stack.getItem();
        if (stack.getItem() instanceof VaultGearItem || !(item instanceof ArmorItem armorItem)) {
            return stack;
        }

        ItemStack gearStack = new ItemStack(VaultArmorItem.forSlot(slot));
        VaultGearData data = VaultGearData.read(gearStack);
        data.setState(VaultGearState.IDENTIFIED);
        data.addModifier(VaultGearModifier.AffixType.IMPLICIT, new VaultGearModifier<>(ModGearAttributes.ARMOR, armorItem.getDefense()));
        data.addModifier(VaultGearModifier.AffixType.IMPLICIT, new VaultGearModifier<>(ModGearAttributes.ARMOR_TOUGHNESS, (int) armorItem.getToughness()));
        data.write(gearStack);
        return gearStack;
    }
}
