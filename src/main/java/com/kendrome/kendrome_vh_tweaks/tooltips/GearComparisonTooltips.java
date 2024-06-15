package com.kendrome.kendrome_vh_tweaks.tooltips;

import com.kendrome.kendrome_vh_tweaks.Utils;
import com.kendrome.kendrome_vh_tweaks.KendromeVhTweaks;
import com.kendrome.kendrome_vh_tweaks.config.ClientConfig;
import com.mojang.authlib.GameProfile;
import iskallia.vault.gear.VaultGearState;
import iskallia.vault.gear.attribute.VaultGearAttribute;
import iskallia.vault.gear.attribute.VaultGearAttributeInstance;
import iskallia.vault.gear.attribute.VaultGearAttributeRegistry;
import iskallia.vault.gear.attribute.VaultGearModifier;
import iskallia.vault.gear.attribute.type.VaultGearAttributeType;
import iskallia.vault.gear.data.AttributeGearData;
import iskallia.vault.gear.data.VaultGearData;
import iskallia.vault.gear.item.CuriosGearItem;
import iskallia.vault.gear.item.VaultGearItem;
import iskallia.vault.gear.trinket.GearAttributeTrinket;
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
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;

import java.util.*;
import java.util.function.Function;

public class GearComparisonTooltips {
    private static final Set<VaultGearAttribute<?>> EXCLUDED_ATTRIBUTES = Set.of(ModGearAttributes.DURABILITY, ModGearAttributes.SOULBOUND);
    private static final List<VaultGearAttribute<?>> ATTRIBUTES_ORDER = new ArrayList<>(VaultGearAttributeRegistry.getRegistry().getValues());

    public static void showComparison(ItemStack itemStack, List<Component> toolTip) {
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
            var baseManaSelected = baseMana(eSlot -> eSlot == slot ? itemStack : player.getItemBySlot(eSlot), curiosItemStacks, player);

            List<VaultGearAttributeInstance<Number>> addingAttributeInstances = new ArrayList<>();
            List<VaultGearAttributeInstance<Number>> removingAttributeInstances = new ArrayList<>();
            Map<VaultGearAttribute<?>, VaultGearAttributeInstance<?>> mergeableAttributes = new HashMap<>();


            /*addEquipmentSlotsAttributes(mergeableAttributes, removingAttributeInstances, (equipmentSlot) -> {
                return equipmentSlot != finalSlot ? ItemStack.EMPTY : simulateVaultGear(equipmentSlot, player.getItemBySlot(equipmentSlot));
            }, true, baseManaEquipped);*/
            /*VaultGearData equippedGearData = VaultGearData.read(equippedStack);
            VaultGearData.Type.ALL_MODIFIERS.getAttributeSource(equippedGearData).forEach((instance) -> {
                addAttribute(mergeableAttributes, removingAttributeInstances, true, VaultGearAttributeInstance.cast(instance.getAttribute(), instance.getValue()), baseManaSelected);
            });*/
            AddAttributes(equippedStack, mergeableAttributes, removingAttributeInstances, true, baseManaEquipped);

            addCuriosAttributes(player, mergeableAttributes, removingAttributeInstances, curiosItemStacks, true, baseManaEquipped);
            //WardrobeContainer.Gear var10003 = (WardrobeContainer.Gear)this.getMenu();
            //Objects.requireNonNull(var10003);
            /*addEquipmentSlotsAttributes(mergeableAttributes, addingAttributeInstances, (equipmentSlot) -> {
                return equipmentSlot != finalSlot ? ItemStack.EMPTY : simulateVaultGear(equipmentSlot, currentItemStack);
            }, false, baseManaSelected);*/

            /*VaultGearData.Type.ALL_MODIFIERS.getAttributeSource(selectedData).forEach((instance) -> {
                addAttribute(mergeableAttributes, addingAttributeInstances, false, VaultGearAttributeInstance.cast(instance.getAttribute(), instance.getValue()), baseManaSelected);
            });*/
            AddAttributes(currentItemStack, mergeableAttributes, addingAttributeInstances, false, baseManaSelected);

            //this.addCuriosAttributes(player, mergeableAttributes, addingAttributeInstances, ((WardrobeContainer.Gear)this.getMenu()).getStoredCurios(), false);
            mergeableAttributes.values().forEach((instance) -> {
                addMergeableAttribute(addingAttributeInstances, removingAttributeInstances, instance);
            });
            addingAttributeInstances.sort(GearAttributeInstanceRegistryOrderComparator.INSTANCE);
            removingAttributeInstances.sort(GearAttributeInstanceRegistryOrderComparator.INSTANCE);
            toolTip.add(new net.minecraft.network.chat.TextComponent(""));
            if (removingAttributeInstances.isEmpty() && addingAttributeInstances.isEmpty()) {
                toolTip.add((new TranslatableComponent("screen.the_vault.wardrobe.tooltip.no_difference")).withStyle(ChatFormatting.YELLOW));
            } else {
                toolTip.add((new TranslatableComponent("screen.the_vault.wardrobe.tooltip.difference")).withStyle(ChatFormatting.GRAY));
                ATTRIBUTES_ORDER.keySet().forEach((attribute) -> {
                    addingAttributeInstances.stream().filter((instance) -> {
                        return instance.getAttribute().equals(attribute);
                    }).forEach((inst) -> {
                        addTooltipDisplay(inst, "+", ChatFormatting.GREEN, toolTip);
                    });
                });
                ATTRIBUTES_ORDER.keySet().forEach((attribute) -> {
                    removingAttributeInstances.stream().filter((instance) -> {
                        return instance.getAttribute().equals(attribute);
                    }).forEach((inst) -> {
                        addTooltipDisplay(inst, "-", ChatFormatting.RED, toolTip);
                    });
                });
            }
        } catch (Exception e) {
            KendromeVhTweaks.LOGGER.error(e.getMessage());
        }
    }




        private static class GearAttributeInstanceRegistryOrderComparator implements Comparator<VaultGearAttributeInstance<?>> {
            public static final GearAttributeInstanceRegistryOrderComparator INSTANCE = new GearAttributeInstanceRegistryOrderComparator();

            private GearAttributeInstanceRegistryOrderComparator() {
            }

            public int compare(VaultGearAttributeInstance<?> o1, VaultGearAttributeInstance<?> o2) {
                VaultGearAttribute<?> attribute = o1.getAttribute();
                int result = (Integer) ATTRIBUTES_ORDER.getOrDefault(attribute, -1) - (Integer) ATTRIBUTES_ORDER.getOrDefault(o2.getAttribute(), -1);
                return result != 0 ? result : compareAttributeValues(attribute, o1.getValue(), o2.getValue());
            }
        }


        private static <T > void addTooltipDisplay (VaultGearAttributeInstance < T > vaultGearAttributeInstance, String
        prefix, ChatFormatting formatting, List < Component > toolTip){
            vaultGearAttributeInstance.getDisplay(VaultGearData.read(new ItemStack(ModItems.BOOTS)), VaultGearModifier.AffixType.IMPLICIT, ItemStack.EMPTY, true).ifPresent((displayText) -> {
                //Temporary Fix for getDisplay returning wrong values for attack_speed
                if (vaultGearAttributeInstance.getAttribute().toString().equals("the_vault:attack_speed")) {
                    displayText = new TextComponent(Utils.formatText((Double) vaultGearAttributeInstance.getValue())).append(new TextComponent(" Attack Speed"));
                }
                if (prefix != null && !prefix.isEmpty()) {
                    displayText = (new TextComponent(prefix)).withStyle(formatting).append(displayText);
                }

                toolTip.add(displayText);
            });
        }

        private static <T > int compareAttributeValues (VaultGearAttribute < T > attribute, Object o1Value, Object
        o2Value){
            VaultGearAttributeType<T> type = attribute.getType();
            return attribute.getAttributeComparator() == null ? 0 : attribute.getAttributeComparator().compare(type.cast(o1Value), type.cast(o2Value));
        }

        private static Component getTooltipWithValue ( double value, String translationKey, String format, Style style){
            return value > 0.0 ? (new TextComponent("+")).append((new TranslatableComponent(translationKey, new Object[]{String.format(format, value)})).withStyle(style)).withStyle(ChatFormatting.GREEN) : (new TextComponent("-")).append((new TranslatableComponent(translationKey, new Object[]{String.format(format, Math.abs(value))})).withStyle(style)).withStyle(ChatFormatting.RED);
        }

        private static Player getPlayerWithWardrobeItemsSwapped (LocalPlayer player, ItemStack itemStack){
            Player gearPlayer = new Player(player.level, player.blockPosition(), 0.0F, new GameProfile((UUID) null, "dummyGearCompareTooltip")) {
                public boolean isSpectator() {
                    return false;
                }

                public boolean isCreative() {
                    return false;
                }
            };
            var curiosItemStacks = IntegrationCurios.getCuriosItemStacks(player);
//        curiosItemStacks.forEach((slotKey, tuples) -> {
//            tuples.forEach((t) -> {
//                ItemStack storedStack = ((WardrobeContainer.Gear)this.getMenu()).getStoredCurio(slotKey, (Integer)t.getB());
//                if (!storedStack.isEmpty()) {
//                    t.setA(storedStack);
//                }
//            });
//        });

            gearPlayer.removeAllEffects();
            var effects = player.getActiveEffects();
            Objects.requireNonNull(gearPlayer);
            effects.forEach(gearPlayer::addEffect);
            EquipmentSlot[] var4 = EquipmentSlot.values();
            int var5 = var4.length;

            for (int var6 = 0; var6 < var5; ++var6) {
                var itemSlot = Mob.getEquipmentSlotForItem(itemStack);
                EquipmentSlot equipmentSlot = var4[var6];
                if (equipmentSlot == itemSlot) {
                    //EquipmentSlot equipmentSlot = var4[var6];
                    //ItemStack storedEquipment = equipmentSlot != EquipmentSlot.MAINHAND ? ((WardrobeContainer.Gear) this.getMenu()).getStoredEquipmentBySlot(equipmentSlot) : ((WardrobeContainer.Gear) this.getMenu()).getHotbarItems().getStackInSlot(0);
                    if (!itemStack.isEmpty()) {
                        updateItemSlot(gearPlayer, equipmentSlot, itemStack);
                    } else {
                        updateItemSlot(gearPlayer, equipmentSlot, player.getItemBySlot(equipmentSlot));
                    }
                }
            }

            curiosItemStacks.forEach((slotKey, stacks) -> {
                stacks.forEach((t) -> {
                    IntegrationCurios.setCurioItemStack(gearPlayer, (ItemStack) t.getA(), slotKey, (Integer) t.getB());
                });
            });
            return gearPlayer;
        }
        private static void updateItemSlot (Player player, EquipmentSlot equipmentSlot, ItemStack stack){
            player.setItemSlot(equipmentSlot, stack);
            stack.getAttributeModifiers(equipmentSlot).forEach((attribute, modifier) -> {
                AttributeInstance attributeInstance = player.getAttribute(attribute);
                if (attributeInstance != null) {
                    attributeInstance.addTransientModifier(modifier);
                }

            });
        }
        private static <T > void addMergeableAttribute
        (List < VaultGearAttributeInstance < ? >> addingAttributeInstances, List < VaultGearAttributeInstance < ?>>
        removingAttributeInstances, VaultGearAttributeInstance < T > instance){
            AttributeTypeHandler<T> ath = getAttributeTypeHandler(instance.getValue());
            if (!ath.isZero(instance.getValue())) {
                if (ath.isGreaterThanZero(instance.getValue())) {
                    addingAttributeInstances.add(instance);
                } else {
                    instance.setValue(ath.invert(instance.getValue()));
                    removingAttributeInstances.add(instance);
                }

            }
        }
        private static <T > AttributeTypeHandler < T > getAttributeTypeHandler(T value) {
            return (AttributeTypeHandler) ATTRIBUTE_TYPE_HANDLERS.getOrDefault(value.getClass(), DEFAULT_ATTRIBUTE_TYPE_HANDLER);
        }

        private static void AddAttributes (ItemStack stack, Map < VaultGearAttribute < ?>,VaultGearAttributeInstance<?>>
        mergeableAttributes, List < VaultGearAttributeInstance < ?>>attributeInstances,boolean inverted, int mana){
            AttributeGearData data = AttributeGearData.read(stack);
            if (data instanceof VaultGearData) {
                VaultGearData gearData = (VaultGearData) data;
                VaultGearData.Type.ALL_MODIFIERS.getAttributeSource(gearData).forEach((instance) -> {
                    addAttribute(mergeableAttributes, attributeInstances, VaultGearAttributeInstance.cast(instance.getAttribute(), instance.getValue()), inverted, mana);
                });
            } else {
                data.getAttributes().forEach((instance) -> {
                    addAttribute(mergeableAttributes, attributeInstances, VaultGearAttributeInstance.cast(instance.getAttribute(), instance.getValue()), inverted, mana);
                });
            }
        }

        private static int baseMana(Function<EquipmentSlot, ItemStack> getItemBySlot,
                                    Map<String, List<Tuple<ItemStack, Integer>>> curiosItemStacks,
                                    Player player) {
            var wrapper = new Object() {
                int mana = 100;
            };
            var equipmentSlots = EquipmentSlot.values();
            for (int i = 0; i < equipmentSlots.length; ++i) {
                var equipmentSlot = equipmentSlots[i];
                var stack = getItemBySlot.apply(equipmentSlot);
                if (!stack.isEmpty()) {
                    stack = simulateVaultGear(equipmentSlot, stack);
                    var item = stack.getItem();
                    if (item instanceof VaultGearItem) {
                        VaultGearItem gearItem = (VaultGearItem) item;
                        if (!gearItem.isIntendedForSlot(stack, equipmentSlot)) {
                            continue;
                        }
                    }

                    if (item instanceof CuriosGearItem) {
                        CuriosGearItem gearItem = (CuriosGearItem) item;
                        if (!gearItem.isIntendedSlot(stack, equipmentSlot)) {
                            continue;
                        }
                    }

                    AttributeGearData data = AttributeGearData.read(stack);
                    if (data instanceof VaultGearData) {
                        VaultGearData gearData = (VaultGearData) data;
                        VaultGearData.Type.ALL_MODIFIERS.getAttributeSource(gearData).forEach((instance) -> {
                            if (instance.getAttribute().toString().equals("the_vault:mana_additive")) {
                                wrapper.mana = wrapper.mana + (int) instance.getValue();
                            }
                        });
                    } else {
                        data.getAttributes().forEach((instance) -> {
                            if (instance.getAttribute().toString().equals("the_vault:mana_additive")) {
                                wrapper.mana = wrapper.mana + (int) instance.getValue();
                            }
                        });
                    }
                }
            }

            TrinketHelper.getTrinkets(curiosItemStacks, GearAttributeTrinket.class).forEach((gearTrinket) -> {
                if (gearTrinket.isUsable(player)) {
                    ((GearAttributeTrinket) gearTrinket.trinket()).getAttributes().forEach((instance) -> {
                        if (instance.getAttribute().toString().equals("the_vault:mana_additive")) {
                            wrapper.mana = wrapper.mana + (int) instance.getValue();
                        }
                    });
                }

            });
            curiosItemStacks.forEach((slot, stacks) -> {
                stacks.forEach((stackTpl) -> {
                    ItemStack stack = stackTpl.getA();
                    if (AttributeGearData.hasData(stack)) {
                        Item patt19717$temp = stack.getItem();
                        if (patt19717$temp instanceof CuriosGearItem) {
                            CuriosGearItem curiosGearItem = (CuriosGearItem) patt19717$temp;
                            if (!curiosGearItem.isIntendedSlot(stack, slot)) {
                                return;
                            }
                        }

                        if (!stack.is(ModItems.MAGNET) || !MagnetItem.isLegacy(stack)) {
                            AttributeGearData.read(stack).getAttributes().forEach((instance) -> {
                                if (instance.getAttribute().toString().equals("the_vault:mana_additive")) {
                                    wrapper.mana = wrapper.mana + (int) instance.getValue();
                                }
                            });
                        }
                    }
                });
            });
            return wrapper.mana;
        }
        private static void addEquipmentSlotsAttributes (Map < VaultGearAttribute < ? >, VaultGearAttributeInstance < ?>>
        mergeableAttributes, List < VaultGearAttributeInstance < ?>>
        attributeInstances, Function < EquipmentSlot, ItemStack > getItemBySlot,boolean inverted, int baseMana){
            EquipmentSlot[] var5 = EquipmentSlot.values();
            int var6 = var5.length;

            for (int var7 = 0; var7 < var6; ++var7) {
                EquipmentSlot equipmentSlot = var5[var7];
                //if (equipmentSlot != EquipmentSlot.MAINHAND || true) {
                ItemStack stack = getItemBySlot.apply(equipmentSlot);
                if (!stack.isEmpty()) {
                    stack = simulateVaultGear(equipmentSlot, stack);
                    Item var11 = stack.getItem();
                    if (var11 instanceof VaultGearItem) {
                        VaultGearItem gearItem = (VaultGearItem) var11;
                        if (!gearItem.isIntendedForSlot(stack, equipmentSlot)) {
                            continue;
                        }
                    }

                    var11 = stack.getItem();
                    if (var11 instanceof CuriosGearItem) {
                        CuriosGearItem gearItem = (CuriosGearItem) var11;
                        if (!gearItem.isIntendedSlot(stack, equipmentSlot)) {
                            continue;
                        }
                    }

                    AttributeGearData data = AttributeGearData.read(stack);
                    if (data instanceof VaultGearData) {
                        VaultGearData gearData = (VaultGearData) data;
                        VaultGearData.Type.ALL_MODIFIERS.getAttributeSource(gearData).forEach((instance) -> {
                            addAttribute(mergeableAttributes, attributeInstances, VaultGearAttributeInstance.cast(instance.getAttribute(), instance.getValue()), inverted, baseMana);
                        });
                    } else {
                        data.getAttributes().forEach((instance) -> {
                            addAttribute(mergeableAttributes, attributeInstances, VaultGearAttributeInstance.cast(instance.getAttribute(), instance.getValue()), inverted, baseMana);
                        });
                    }
                }
                //}
            }
        }


        private static void addAttribute(Map<VaultGearAttribute<?>, VaultGearAttributeInstance<?>> mergeableAttributes,
                                         List<VaultGearAttributeInstance<?>> attributeInstances,
                                         VaultGearAttributeInstance<?> instance, boolean inverted, int baseMana) {
            VaultGearAttribute<?> attribute = instance.getAttribute();
            if (attribute == ModGearAttributes.MANA_ADDITIVE_PERCENTILE) {
                instance = new VaultGearAttributeInstance<>(ModGearAttributes.MANA_ADDITIVE, (int) ((Float) instance.getValue() * baseMana));
            }

            if (!EXCLUDED_ATTRIBUTES.contains(attribute)) {
                if (attribute.getAttributeComparator() == null) {
                    attributeInstances.add(instance);
                } else {
                    mergeAttribute(mergeableAttributes, instance, attribute, inverted);
                }
            }
        }

        private static void mergeAttribute (Map<VaultGearAttribute<?>, VaultGearAttributeInstance<?>> mergeableAttributes,
                                                 VaultGearAttributeInstance<Number> instance, VaultGearAttribute<Number> attribute, boolean inverted) {
            if (mergeableAttributes.containsKey(attribute)) {
                VaultGearAttributeInstance<?> mergeIntoInstance = mergeableAttributes.get(attribute);
                Number value = instance.getValue();
                if (inverted) {
                    value = value.doubleValue() * -1;
                }

                mergeIntoInstance.setValue(mergeIntoInstance.getAttribute().getAttributeComparator().merge(mergeIntoInstance.getValue(), value));
            } else {
                T value = instance.getValue();
                if (inverted) {
                    AttributeTypeHandler<T> ath = getAttributeTypeHandler(value);
                    instance.setValue(ath.invert(value));
                }

                mergeableAttributes.put(attribute, instance);
            }
        }

        private static void addCuriosAttributes(Player player,
                                                Map<VaultGearAttribute<?>, VaultGearAttributeInstance<?>> mergeableAttributes,
                                                List<VaultGearAttributeInstance<?>> attributeInstances,
                                                Map<String, List<Tuple<ItemStack, Integer>>> curiosItemStacks,
                                                boolean inverted, int baseMana) {
            TrinketHelper.getTrinkets(curiosItemStacks, GearAttributeTrinket.class).forEach((gearTrinket) -> {
                if (gearTrinket.isUsable(player)) {
                    gearTrinket.trinket().getAttributes().forEach((instance) -> {
                        addAttribute(mergeableAttributes, attributeInstances, instance, inverted, baseMana);
                    });
                }
            });

            curiosItemStacks.forEach((slot, stacks) -> {
                stacks.forEach((stackTpl) -> {
                    ItemStack stack = stackTpl.getA();
                    if (AttributeGearData.hasData(stack)) {
                        Item item = stack.getItem();
                        if (item instanceof CuriosGearItem curiosGearItem && !curiosGearItem.isIntendedSlot(stack, slot)) {
                           return;
                        }

                        if (!stack.is(ModItems.MAGNET) || !MagnetItem.isLegacy(stack)) {
                            AttributeGearData.read(stack).getAttributes().forEach((instance) -> {
                                addAttribute(mergeableAttributes, attributeInstances, instance, inverted, baseMana);
                            });
                        }
                    }
                });
            });
        }

        private static ItemStack simulateVaultGear(EquipmentSlot slot, ItemStack stack) {
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
