package com.kendrome.kendrome_vh_tweaks;

import com.mojang.logging.LogUtils;
import iskallia.vault.config.gear.VaultGearTierConfig;
import iskallia.vault.gear.attribute.config.FloatAttributeGenerator;
import iskallia.vault.item.InscriptionItem;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.InterModComms;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.InterModEnqueueEvent;
import net.minecraftforge.fml.event.lifecycle.InterModProcessEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.slf4j.Logger;
import iskallia.vault.gear.VaultGearState;
import iskallia.vault.gear.attribute.VaultGearModifier;
import iskallia.vault.gear.data.VaultGearData;
import iskallia.vault.item.tool.JewelItem;

import java.util.stream.Collectors;
import java.util.Formatter;

// The value here should match an entry in the META-INF/mods.toml file
@Mod("kendrome_vh_tweaks")
public class main {

    // Directly reference a slf4j logger
    private static final Logger LOGGER = LogUtils.getLogger();

    public main() {
        // Register the setup method for modloading
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setup);

        // Register ourselves for server and other game events we are interested in
        MinecraftForge.EVENT_BUS.register(this);
    }

    private void setup(final FMLCommonSetupEvent event) {
        // Some preinit code
        LOGGER.info("Kendrome Vault Hunters Tweaks");
    }

    //https://minecraft.fandom.com/wiki/Formatting_codes#Color_codes
    @SubscribeEvent
    public void onItemTooltip(ItemTooltipEvent toolTipEvent) throws IllegalAccessException {
        var toolTip = toolTipEvent.getToolTip();
        var itemStack = toolTipEvent.getItemStack();
        if (itemStack == null)
            return;
        var item = itemStack.getItem();
        if (item == null)
            return;
        if(item instanceof InscriptionItem) {
            var data = itemStack.getTag().getCompound("data");
            var completion =  data.getFloat("completion");
            var instability = data.getFloat("instability");
            var time = data.getInt("time") / 20;

            toolTip.add(new TextComponent("§7" + FormatText((completion * 100) / time) + " §fcompletion/time"));
            toolTip.add(new TextComponent("§7" + FormatText(completion / instability) + " §fcompletion/instability"));
            toolTip.add(new TextComponent("§7" + FormatText(time / (instability * 100)) + " §ftime/instability"));
        }
        if (item instanceof JewelItem) {
            VaultGearData data = VaultGearData.read(itemStack);
            var state = data.getState();
            if (state == VaultGearState.IDENTIFIED) {
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
        }
    }



    public float GetRelative(Object value, int size) {
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
    public TextComponent GetJewelRelativeDisplay(VaultGearModifier suffix, float relative, Object min, Object max, boolean showDetails) {
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
        if(showDetails) {
            var minRelative = FormatText(GetRelative(min, 90) * multiplier);
            var maxRelative =  FormatText(GetRelative(max, 10) * multiplier);
            return new TextComponent("§7" + FormatText(relative) + " §f" + name + "/size §7(" + minRelative  + "-" + maxRelative + ")");
        } else {
            return new TextComponent("§7" + FormatText(relative) + " §f" + name + "/size");
        }

    }

    public String FormatText(float value) {
        Formatter fmt = new Formatter();
        return fmt.format("%.2g", value).toString();
    }
}
