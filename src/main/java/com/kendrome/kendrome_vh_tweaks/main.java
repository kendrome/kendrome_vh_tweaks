package com.kendrome.kendrome_vh_tweaks;

import com.mojang.authlib.GameProfile;
import com.mojang.logging.LogUtils;
import iskallia.vault.client.gui.screen.block.WardrobeScreen;
import iskallia.vault.container.WardrobeContainer;
import iskallia.vault.gear.attribute.VaultGearAttribute;
import iskallia.vault.gear.attribute.VaultGearAttributeInstance;
import iskallia.vault.gear.attribute.VaultGearAttributeRegistry;
import iskallia.vault.gear.attribute.custom.EffectGearAttribute;
import iskallia.vault.gear.attribute.type.VaultGearAttributeType;
import iskallia.vault.gear.data.AttributeGearData;
import iskallia.vault.gear.item.CuriosGearItem;
import iskallia.vault.gear.item.VaultGearItem;
import iskallia.vault.gear.trinket.GearAttributeTrinket;
import iskallia.vault.gear.trinket.TrinketHelper;
import iskallia.vault.init.ModGearAttributes;
import iskallia.vault.init.ModItems;
import iskallia.vault.integration.IntegrationCurios;
import iskallia.vault.item.InscriptionItem;
import iskallia.vault.item.MagnetItem;
import iskallia.vault.item.gear.IdolItem;
import iskallia.vault.item.gear.VaultArmorItem;
import iskallia.vault.item.gear.VaultAxeItem;
import iskallia.vault.item.gear.VaultSwordItem;
import iskallia.vault.util.StatUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.util.Tuple;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;
import iskallia.vault.gear.VaultGearState;
import iskallia.vault.gear.attribute.VaultGearModifier;
import iskallia.vault.gear.data.VaultGearData;
import iskallia.vault.item.tool.JewelItem;
import net.minecraft.client.Minecraft;
import vazkii.quark.content.tools.item.SeedPouchItem;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;

import static com.kendrome.kendrome_vh_tweaks.Helpers.FormatText;

@Mod("kendrome_vh_tweaks")
public class main {

    // Directly reference a slf4j logger
    private static final Logger LOGGER = LogUtils.getLogger();

    public main() {
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setup);
        MinecraftForge.EVENT_BUS.register(this);
    }

    private void setup(final FMLCommonSetupEvent event) {
        // Some preinit code
        LOGGER.info("Kendrome Vault Hunters Tweaks");
    }

    //https://minecraft.fandom.com/wiki/Formatting_codes#Color_codes
    @SubscribeEvent
    public void onItemTooltip(ItemTooltipEvent toolTipEvent) throws IllegalAccessException {
        //try {
            var toolTip = toolTipEvent.getToolTip();
            var itemStack = toolTipEvent.getItemStack();
            if (itemStack == null)
                return;
            var item = itemStack.getItem();
            if (item == null)
                return;
            if (item instanceof InscriptionItem) {
                if (itemStack == null || itemStack.getTag() == null)
                    return;
                var data = itemStack.getTag().getCompound("data");
                var completion = data.getFloat("completion");
                var instability = data.getFloat("instability");
                var time = data.getInt("time") / 20;

                toolTip.add(new TextComponent("§7" + FormatText((completion * 100) / time) + " §fcompletion/time"));
                toolTip.add(new TextComponent("§7" + FormatText(completion / instability) + " §fcompletion/instability"));
                toolTip.add(new TextComponent("§7" + FormatText(time / (instability * 100)) + " §ftime/instability"));
            }
            if (item instanceof JewelItem) {
                JewelComparison.ShowComparison(itemStack, toolTip);
            }

            if (!itemStack.isEmpty() && item instanceof VaultGearItem && !(item instanceof  JewelItem)) {
                GearComparison.ShowComparison(itemStack, toolTip);
            }
        //} catch (Exception e) {
//            LOGGER.error(e.getMessage());
//        }
    }


    public static Object SetNegative(Object value1) {
        if(value1 instanceof Integer) {
            return 0 - (Integer)value1;
        } else if(value1 instanceof Double) {
            return 0 - (Double)value1;
        } else if(value1 instanceof Float) {
            return 0 - (Float)value1;
        }
        return null;
    }
    public static Object SubtractValues(Object value1, Object value2) {
        if(value1 instanceof Integer) {
            return (Integer)value1 - (Integer)value2;
        } else if(value1 instanceof Double) {
            return (Double)value1 - (Double)value2;
        } else if(value1 instanceof Float) {
            return (Float)value1 - (Float)value2;
        }
        return null;
    }
    public static Object AddValues(Object value1, Object value2) {
        if(value1 instanceof Integer) {
            return (Integer)value1 + (Integer)value2;
        } else if(value1 instanceof Double) {
            return (Double)value1 + (Double)value2;
        } else if(value1 instanceof Float) {
            return (Float)value1 + (Float)value2;
        }
        return null;
    }

}
