package com.kendrome.kendrome_vh_tweaks;

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
import java.util.function.Predicate;
import java.util.function.UnaryOperator;

public class GearComparison {
    public static void ShowComparison(ItemStack itemStack, List<Component> toolTip) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) return;

        var slot = Mob.getEquipmentSlotForItem(itemStack);
        var item = itemStack.getItem();
        if(item instanceof IdolItem || item instanceof WandItem || item instanceof ShieldItem) {
            slot = EquipmentSlot.OFFHAND;
        }
        var equippedStack = player.getItemBySlot(slot);
        var equippedItem = equippedStack.getItem();

        if(slot == EquipmentSlot.MAINHAND && !(equippedItem instanceof SwordItem || equippedItem instanceof AxeItem)) {
            return;
        }

        var data = VaultGearData.read(itemStack);
        if(data.getState() != VaultGearState.IDENTIFIED)
            return;

        final EquipmentSlot finalSlot = slot;
        if (!equippedStack.isEmpty() || true) {
            var swappedPlayer = getPlayerWithWardrobeItemsSwapped(player, itemStack);

            if(ATTRIBUTES_ORDER == null) {
                ATTRIBUTES_ORDER = new LinkedHashMap();
                int i = 0;
                var iterator = VaultGearAttributeRegistry.getRegistry().iterator();

                while(iterator.hasNext()) {
                    VaultGearAttribute<?> attribute = (VaultGearAttribute)iterator.next();
                    ATTRIBUTES_ORDER.put(attribute, i++);
                }
            }
            if(equippedStack == itemStack )
                return;

            Map<String, java.util.List<Tuple<ItemStack, Integer>>> curiosItemStacks = IntegrationCurios.getCuriosItemStacks(player);
            curiosItemStacks.forEach((slotKey, tuples) -> {
                tuples.forEach((t) -> {
                    //if (((WardrobeContainer.Gear)this.getMenu()).getStoredCurio(slotKey, (Integer)t.getB()).isEmpty()) {
                    t.setA(ItemStack.EMPTY);
                    //}

                });
            });

            var baseManaEquiped = BaseMana((equipmentSlot) -> {
                return player.getItemBySlot(equipmentSlot);
            }, curiosItemStacks, player);

            var baseManaSelected = BaseMana((equipmentSlot) -> {
                return equipmentSlot == finalSlot ? itemStack : player.getItemBySlot(equipmentSlot);
            }, curiosItemStacks, player);


            java.util.List<VaultGearAttributeInstance<?>> addingAttributeInstances = new ArrayList();
            java.util.List<VaultGearAttributeInstance<?>> removingAttributeInstances = new ArrayList();
            Map<VaultGearAttribute<?>, VaultGearAttributeInstance<?>> mergeableAttributes = new HashMap();
            addEquipmentSlotsAttributes(mergeableAttributes, removingAttributeInstances, (equipmentSlot) -> {
                return equipmentSlot != finalSlot ? ItemStack.EMPTY : simulateVaultGear(equipmentSlot, player.getItemBySlot(equipmentSlot));
            }, true, baseManaEquiped);

            addCuriosAttributes(player, mergeableAttributes, removingAttributeInstances, curiosItemStacks, true, baseManaEquiped);
            //WardrobeContainer.Gear var10003 = (WardrobeContainer.Gear)this.getMenu();
            //Objects.requireNonNull(var10003);
            addEquipmentSlotsAttributes(mergeableAttributes, addingAttributeInstances, (equipmentSlot) -> {
                return equipmentSlot != finalSlot ? ItemStack.EMPTY : simulateVaultGear(equipmentSlot, itemStack);
            }, false, baseManaSelected);
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

        }
    }


    private static Map<VaultGearAttribute<?>, Integer> ATTRIBUTES_ORDER;

    private static class GearAttributeInstanceRegistryOrderComparator implements Comparator<VaultGearAttributeInstance<?>> {
        public static final GearAttributeInstanceRegistryOrderComparator INSTANCE = new GearAttributeInstanceRegistryOrderComparator();

        private GearAttributeInstanceRegistryOrderComparator() {
        }

        public int compare(VaultGearAttributeInstance<?> o1, VaultGearAttributeInstance<?> o2) {
            VaultGearAttribute<?> attribute = o1.getAttribute();
            int result = (Integer)ATTRIBUTES_ORDER.getOrDefault(attribute, -1) - (Integer)ATTRIBUTES_ORDER.getOrDefault(o2.getAttribute(), -1);
            return result != 0 ? result : compareAttributeValues(attribute, o1.getValue(), o2.getValue());
        }
    }


    private static <T> void addTooltipDisplay(VaultGearAttributeInstance<T> vaultGearAttributeInstance, String prefix, ChatFormatting formatting, List<Component> toolTip) {
        vaultGearAttributeInstance.getDisplay(VaultGearData.read(new ItemStack(ModItems.BOOTS)), VaultGearModifier.AffixType.IMPLICIT, ItemStack.EMPTY, true).ifPresent((displayText) -> {
            //Temporary Fix for getDisplay returning wrong values for attack_speed
            if(vaultGearAttributeInstance.getAttribute().toString().equals("the_vault:attack_speed")) {
                displayText = new TextComponent(Helpers.FormatText((Double)vaultGearAttributeInstance.getValue())).append(new TextComponent(" Attack Speed"));
            }
            if (prefix != null && !prefix.isEmpty()) {
                displayText = (new TextComponent(prefix)).withStyle(formatting).append(displayText);
            }

            toolTip.add(displayText);
        });
    }

    private static <T> int compareAttributeValues(VaultGearAttribute<T> attribute, Object o1Value, Object o2Value) {
        VaultGearAttributeType<T> type = attribute.getType();
        return attribute.getAttributeComparator() == null ? 0 : attribute.getAttributeComparator().compare(type.cast(o1Value), type.cast(o2Value));
    }

    private static Component getTooltipWithValue(double value, String translationKey, String format, Style style) {
        return value > 0.0 ? (new TextComponent("+")).append((new TranslatableComponent(translationKey, new Object[]{String.format(format, value)})).withStyle(style)).withStyle(ChatFormatting.GREEN) : (new TextComponent("-")).append((new TranslatableComponent(translationKey, new Object[]{String.format(format, Math.abs(value))})).withStyle(style)).withStyle(ChatFormatting.RED);
    }

    private static Player getPlayerWithWardrobeItemsSwapped(LocalPlayer player, ItemStack itemStack) {
        Player gearPlayer = new Player(player.level, player.blockPosition(), 0.0F, new GameProfile((UUID)null, "dummyGearCompareTooltip")) {
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

        for(int var6 = 0; var6 < var5; ++var6) {
            var itemSlot = Mob.getEquipmentSlotForItem(itemStack);
            EquipmentSlot equipmentSlot = var4[var6];
            if(equipmentSlot == itemSlot) {
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
                IntegrationCurios.setCurioItemStack(gearPlayer, (ItemStack)t.getA(), slotKey, (Integer)t.getB());
            });
        });
        return gearPlayer;
    }
    private static void updateItemSlot(Player player, EquipmentSlot equipmentSlot, ItemStack stack) {
        player.setItemSlot(equipmentSlot, stack);
        stack.getAttributeModifiers(equipmentSlot).forEach((attribute, modifier) -> {
            AttributeInstance attributeInstance = player.getAttribute(attribute);
            if (attributeInstance != null) {
                attributeInstance.addTransientModifier(modifier);
            }

        });
    }
    private static <T> void addMergeableAttribute(List<VaultGearAttributeInstance<?>> addingAttributeInstances, List<VaultGearAttributeInstance<?>> removingAttributeInstances, VaultGearAttributeInstance<T> instance) {
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
    private static <T> AttributeTypeHandler<T> getAttributeTypeHandler(T value) {
        return (AttributeTypeHandler)ATTRIBUTE_TYPE_HANDLERS.getOrDefault(value.getClass(), DEFAULT_ATTRIBUTE_TYPE_HANDLER);
    }

    private static int BaseMana(Function<EquipmentSlot, ItemStack> getItemBySlot, Map<String, List<Tuple<ItemStack, Integer>>> curiosItemStacks, Player player) {
        var wrapper = new Object() { int mana = 100;};
        var equipmentSlots = EquipmentSlot.values();
        for(int i = 0; i < equipmentSlots.length; ++i) {
            var equipmentSlot = equipmentSlots[i];
            var stack = getItemBySlot.apply(equipmentSlot);
            if(!stack.isEmpty()) {
                stack = simulateVaultGear(equipmentSlot, stack);
                var item = stack.getItem();
                if (item instanceof VaultGearItem) {
                    VaultGearItem gearItem = (VaultGearItem)item;
                    if (!gearItem.isIntendedForSlot(stack, equipmentSlot)) {
                        continue;
                    }
                }

                if (item instanceof CuriosGearItem) {
                    CuriosGearItem gearItem = (CuriosGearItem)item;
                    if (!gearItem.isIntendedSlot(stack, equipmentSlot)) {
                        continue;
                    }
                }

                AttributeGearData data = AttributeGearData.read(stack);
                if (data instanceof VaultGearData) {
                    VaultGearData gearData = (VaultGearData)data;
                    VaultGearData.Type.ALL_MODIFIERS.getAttributeSource(gearData).forEach((instance) -> {
                        if(instance.getAttribute().toString().equals("the_vault:mana_additive")) {
                            wrapper.mana = wrapper.mana + (int) instance.getValue();
                        }
                    });
                } else {
                    data.getAttributes().forEach((instance) -> {
                        if(instance.getAttribute().toString().equals("the_vault:mana_additive")) {
                            wrapper.mana = wrapper.mana + (int) instance.getValue();
                        }
                    });
                }
            }
        }

        TrinketHelper.getTrinkets(curiosItemStacks, GearAttributeTrinket.class).forEach((gearTrinket) -> {
            if (gearTrinket.isUsable(player)) {
                ((GearAttributeTrinket)gearTrinket.trinket()).getAttributes().forEach((instance) -> {
                    if(instance.getAttribute().toString().equals("the_vault:mana_additive")) {
                        wrapper.mana = wrapper.mana + (int) instance.getValue();
                    }
                });
            }

        });
        curiosItemStacks.forEach((slot, stacks) -> {
            stacks.forEach((stackTpl) -> {
                ItemStack stack = (ItemStack)stackTpl.getA();
                if (AttributeGearData.hasData(stack)) {
                    Item patt19717$temp = stack.getItem();
                    if (patt19717$temp instanceof CuriosGearItem) {
                        CuriosGearItem curiosGearItem = (CuriosGearItem)patt19717$temp;
                        if (!curiosGearItem.isIntendedSlot(stack, slot)) {
                            return;
                        }
                    }

                    if (!stack.is(ModItems.MAGNET) || !MagnetItem.isLegacy(stack)) {
                        AttributeGearData.read(stack).getAttributes().forEach((instance) -> {
                            if(instance.getAttribute().toString().equals("the_vault:mana_additive")) {
                                wrapper.mana = wrapper.mana + (int) instance.getValue();
                            }
                        });
                    }
                }
            });
        });
        return wrapper.mana;
    }
    private static void addEquipmentSlotsAttributes(Map<VaultGearAttribute<?>, VaultGearAttributeInstance<?>> mergeableAttributes, List<VaultGearAttributeInstance<?>> attributeInstances, Function<EquipmentSlot, ItemStack> getItemBySlot, boolean inverted, int baseMana) {
        EquipmentSlot[] var5 = EquipmentSlot.values();
        int var6 = var5.length;

        for(int var7 = 0; var7 < var6; ++var7) {
            EquipmentSlot equipmentSlot = var5[var7];
            //if (equipmentSlot != EquipmentSlot.MAINHAND || true) {
            ItemStack stack = getItemBySlot.apply(equipmentSlot);
            if (!stack.isEmpty()) {
                stack = simulateVaultGear(equipmentSlot, stack);
                Item var11 = stack.getItem();
                if (var11 instanceof VaultGearItem) {
                    VaultGearItem gearItem = (VaultGearItem)var11;
                    if (!gearItem.isIntendedForSlot(stack, equipmentSlot)) {
                        continue;
                    }
                }

                var11 = stack.getItem();
                if (var11 instanceof CuriosGearItem) {
                    CuriosGearItem gearItem = (CuriosGearItem)var11;
                    if (!gearItem.isIntendedSlot(stack, equipmentSlot)) {
                        continue;
                    }
                }

                AttributeGearData data = AttributeGearData.read(stack);
                if (data instanceof VaultGearData) {
                    VaultGearData gearData = (VaultGearData)data;
                    VaultGearData.Type.ALL_MODIFIERS.getAttributeSource(gearData).forEach((instance) -> {
                        addAttribute(mergeableAttributes, attributeInstances, inverted, VaultGearAttributeInstance.cast(instance.getAttribute(), instance.getValue()), baseMana);
                    });
                } else {
                    data.getAttributes().forEach((instance) -> {
                        addAttribute(mergeableAttributes, attributeInstances, inverted, VaultGearAttributeInstance.cast(instance.getAttribute(), instance.getValue()), baseMana);
                    });
                }
            }
            //}
        }

    }
    private static Set<VaultGearAttribute<?>> EXCLUDED_ATTRIBUTES = Set.of(ModGearAttributes.DURABILITY, ModGearAttributes.SOULBOUND);

    private static void addAttribute(Map<VaultGearAttribute<?>, VaultGearAttributeInstance<?>> mergeableAttributes, List<VaultGearAttributeInstance<?>> attributeInstances, boolean inverted, VaultGearAttributeInstance<?> instance, int baseMana) {
        if(instance.getAttribute().toString().equals("the_vault:mana_additive_percentile")) {
            //var newAttribute = new VaultGearAttribute<Int>(ModGearAttributes.MANA_ADDITIVE.getRegistryName(), new GearAt)
            var newInstance = new VaultGearAttributeInstance<Integer>(ModGearAttributes.MANA_ADDITIVE, (int)((Float)instance.getValue() * baseMana));
            instance = newInstance;
        }
        VaultGearAttribute<?> attribute = instance.getAttribute();
        if (!EXCLUDED_ATTRIBUTES.contains(attribute)) {
            if (attribute.getAttributeComparator() == null) {
                attributeInstances.add(instance);
            } else {
                mergeAttribute(mergeableAttributes, inverted, instance, attribute);
            }

        }
    }

    private static <T> void mergeAttribute(Map<VaultGearAttribute<?>, VaultGearAttributeInstance<?>> mergeableAttributes, boolean inverted, VaultGearAttributeInstance<T> instance, VaultGearAttribute<?> attribute) {
        if (mergeableAttributes.containsKey(attribute)) {
            VaultGearAttributeInstance<T> mergeIntoInstance = (VaultGearAttributeInstance)mergeableAttributes.get(attribute);
            T value = instance.getValue();
            if (inverted) {
                AttributeTypeHandler<T> ath = getAttributeTypeHandler(value);
                value = ath.invert(value);
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

    private static void addCuriosAttributes(Player player, Map<VaultGearAttribute<?>, VaultGearAttributeInstance<?>> mergeableAttributes, List<VaultGearAttributeInstance<?>> attributeInstances, Map<String, List<Tuple<ItemStack, Integer>>> curiosItemStacks, boolean inverted, int baseMana) {
        TrinketHelper.getTrinkets(curiosItemStacks, GearAttributeTrinket.class).forEach((gearTrinket) -> {
            if (gearTrinket.isUsable(player)) {
                ((GearAttributeTrinket)gearTrinket.trinket()).getAttributes().forEach((instance) -> {
                    addAttribute(mergeableAttributes, attributeInstances, inverted, instance, baseMana);
                });
            }

        });
        curiosItemStacks.forEach((slot, stacks) -> {
            stacks.forEach((stackTpl) -> {
                ItemStack stack = (ItemStack)stackTpl.getA();
                if (AttributeGearData.hasData(stack)) {
                    Item patt19717$temp = stack.getItem();
                    if (patt19717$temp instanceof CuriosGearItem) {
                        CuriosGearItem curiosGearItem = (CuriosGearItem)patt19717$temp;
                        if (!curiosGearItem.isIntendedSlot(stack, slot)) {
                            return;
                        }
                    }

                    if (!stack.is(ModItems.MAGNET) || !MagnetItem.isLegacy(stack)) {
                        AttributeGearData.read(stack).getAttributes().forEach((instance) -> {
                            addAttribute(mergeableAttributes, attributeInstances, inverted, instance, baseMana);
                        });
                    }
                }
            });
        });
    }

    private static ItemStack simulateVaultGear(EquipmentSlot slot, ItemStack stack) {
        if (stack.getItem() instanceof VaultGearItem) {
            return stack;
        } else {
            Item var4 = stack.getItem();
            if (var4 instanceof ArmorItem) {
                ArmorItem armorItem = (ArmorItem)var4;
                ItemStack gearStack = new ItemStack(VaultArmorItem.forSlot(slot));
                VaultGearData data = VaultGearData.read(gearStack);
                data.setState(VaultGearState.IDENTIFIED);
                data.addModifier(VaultGearModifier.AffixType.IMPLICIT, new VaultGearModifier(ModGearAttributes.ARMOR, armorItem.getDefense()));
                data.addModifier(VaultGearModifier.AffixType.IMPLICIT, new VaultGearModifier(ModGearAttributes.ARMOR_TOUGHNESS, (int)armorItem.getToughness()));
                data.write(gearStack);
                return gearStack;
            } else {
                return stack;
            }
        }
    }

    private static final AttributeTypeHandler<?> DEFAULT_ATTRIBUTE_TYPE_HANDLER = new AttributeTypeHandler(0, (v) -> {
        return false;
    }, (v) -> {
        return false;
    }, (v) -> {
        return v;
    });

    private static final Map<Class<?>, AttributeTypeHandler<?>> ATTRIBUTE_TYPE_HANDLERS = Map.of(Integer.class, new AttributeTypeHandler(100, (v) -> {
        return (Integer)v == 0;
    }, (v) -> {
        return (Integer)v > 0;
    }, (v) -> {
        return -(Integer)v;
    }), Float.class, new AttributeTypeHandler(99, (v) -> {
        return Math.abs((Float)v) < 1.0E-4F;
    }, (v) -> {
        return (Float)v > 0.0F;
    }, (v) -> {
        return -(Float)v;
    }), Double.class, new AttributeTypeHandler(98, (v) -> {
        return Math.abs((Double)v) < 1.0E-4;
    }, (v) -> {
        return (Double)v > 0.0;
    }, (v) -> {
        return -(Double)v;
    }));


    private static class AttributeTypeHandler<T> {
        private final Predicate<T> isZero;
        private final Predicate<T> isGreaterThanZero;
        private final int sortOrder;
        private UnaryOperator<T> invert;

        private AttributeTypeHandler(int sortOrder, Predicate<T> isZero, Predicate<T> isGreaterThanZero, UnaryOperator<T> invert) {
            this.isZero = isZero;
            this.isGreaterThanZero = isGreaterThanZero;
            this.sortOrder = sortOrder;
            this.invert = invert;
        }

        public T invert(T value) {
            return this.invert.apply(value);
        }

        public boolean isZero(T value) {
            return this.isZero.test(value);
        }

        public boolean isGreaterThanZero(T value) {
            return this.isGreaterThanZero.test(value);
        }

        public int getSortOrder() {
            return this.sortOrder;
        }
    }

}
