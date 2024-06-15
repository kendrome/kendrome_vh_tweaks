package com.kendrome.kendrome_vh_tweaks;

import com.kendrome.kendrome_vh_tweaks.config.ClientConfig;
import com.kendrome.kendrome_vh_tweaks.tooltips.InscriptionTooltips;
import com.kendrome.kendrome_vh_tweaks.tooltips.JewelTooltips;
import com.mojang.logging.LogUtils;
import iskallia.vault.item.InscriptionItem;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
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

import java.util.List;

@Mod(KendromeVhTweaks.MOD_ID)
public class KendromeVhTweaks {
    public static final String MOD_ID = "kendrome_vh_tweaks";
    public static final Logger LOGGER = LogUtils.getLogger();

    public KendromeVhTweaks() {
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setup);
        MinecraftForge.EVENT_BUS.register(this);
        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, ClientConfig.SPEC, "kendrome_vh_tweaks_client.toml");
    }

    private void setup(FMLCommonSetupEvent event) {
        LOGGER.info("Kendrome Vault Hunters Tweaks");
    }

    //https://minecraft.fandom.com/wiki/Formatting_codes#Color_codes
    @SubscribeEvent
    public void onItemTooltip(ItemTooltipEvent toolTipEvent) throws IllegalAccessException {
        try {
            List<Component> toolTip = toolTipEvent.getToolTip();
            ItemStack itemStack = toolTipEvent.getItemStack();
            Item item = itemStack.getItem();

            if (item instanceof InscriptionItem) {
                InscriptionTooltips.appendTooltips(itemStack, toolTip);
            } else if (item instanceof JewelItem) {
                JewelTooltips.appendTooltips(itemStack, toolTip);
            }
        } catch (Exception e) {
            LOGGER.error(e.getMessage());
        }
    }
}
