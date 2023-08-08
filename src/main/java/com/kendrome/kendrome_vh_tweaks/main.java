package com.kendrome.kendrome_vh_tweaks;

import com.kendrome.kendrome_vh_tweaks.config.ClientConfig;
import com.mojang.logging.LogUtils;
import iskallia.vault.gear.item.VaultGearItem;
import iskallia.vault.item.InscriptionItem;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.TextComponent;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;
import iskallia.vault.item.tool.JewelItem;
import net.minecraft.client.Minecraft;

import java.util.List;

import static com.kendrome.kendrome_vh_tweaks.Helpers.FormatText;

@Mod("kendrome_vh_tweaks")
public class main {

    // Directly reference a slf4j logger
    public static final Logger LOGGER = LogUtils.getLogger();

    public main() {
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setup);
        MinecraftForge.EVENT_BUS.register(this);
        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, ClientConfig.SPEC, "kendrome_vh_tweaks_client.toml");
    }

    private void setup(final FMLCommonSetupEvent event) {
        // Some preinit code
        LOGGER.info("Kendrome Vault Hunters Tweaks");
    }

    //https://minecraft.fandom.com/wiki/Formatting_codes#Color_codes
    @SubscribeEvent
    public void onItemTooltip(ItemTooltipEvent toolTipEvent) throws IllegalAccessException {
        try {
            var toolTip = toolTipEvent.getToolTip();
            var itemStack = toolTipEvent.getItemStack();
            if (itemStack == null)
                return;
            var item = itemStack.getItem();
            if (item == null)
                return;
            if (item instanceof InscriptionItem) {
                if (!ClientConfig.INSCRIPTION_RELATIVE_TOOLTIPS_ENABLED.get() || !Helpers.ShouldShow(ClientConfig.INSCRIPTION_RELATIVE_TOOLTIPS_KEY.get())) {
                    return;
                }

                if (itemStack == null || itemStack.getTag() == null)
                    return;
                var data = itemStack.getTag().getCompound("data");
                var completion = data.getFloat("completion");
                var instability = data.getFloat("instability");
                var time = data.getInt("time") / 20;


                var options = ClientConfig.INSCRIPTIONS_RELATIVE_OPTIONS.get();

                for(var opt: options) {

                }
                ClientConfig.INSCRIPTIONS_RELATIVE_OPTIONS.get().forEach(opt -> {
                    if (opt.equals(ClientConfig.InscriptionOptions.CompletionPerTime.toString())) {
                        toolTip.add(new TextComponent("§7" + FormatText((completion * 100) / time) + " §fcompletion/time"));
                    }
                    if (opt.equals(ClientConfig.InscriptionOptions.CompletionPerInstability.toString())) {
                        toolTip.add(new TextComponent("§7" + FormatText(completion / instability) + " §fcompletion/instability"));
                    }
                    if (opt.equals(ClientConfig.InscriptionOptions.TimePerInstability.toString())) {
                        toolTip.add(new TextComponent("§7" + FormatText(time / (instability * 100)) + " §ftime/instability"));
                    }
                });
            }

            if (item instanceof JewelItem) {
                JewelRelativeTooltips.ShowTooltips(itemStack, toolTip);
            }

            if (!itemStack.isEmpty() && item instanceof VaultGearItem && !(item instanceof  JewelItem)) {
                // We now patch directly into the gear item.
//                GearComparison.ShowTooltips(itemStack, toolTip);
            }
        } catch (Exception e) {
            LOGGER.error(e.getMessage());
        }
    }

    @SubscribeEvent
    public void onScreenEvent(ScreenEvent.InitScreenEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        Screen screen = event.getScreen();
        if(screen instanceof AbstractContainerScreen<?> containerScreen) {
            var t = 1;
        }
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
