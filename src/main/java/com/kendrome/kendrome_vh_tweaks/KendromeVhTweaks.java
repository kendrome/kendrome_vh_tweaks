package com.kendrome.kendrome_vh_tweaks;

import com.kendrome.kendrome_vh_tweaks.config.ClientConfig;
import com.kendrome.kendrome_vh_tweaks.tooltips.GearComparisonTooltips;
import com.kendrome.kendrome_vh_tweaks.tooltips.JewelTooltips;
import com.mojang.logging.LogUtils;
import iskallia.vault.gear.tooltip.VaultGearTooltipItem;
import iskallia.vault.item.tool.JewelItem;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

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

    /**
     * Modifies the tooltips of {@link VaultGearTooltipItem} items.
     * This method is called from the core mod: src/main/resources/coremods/patch_vault_item_tooltip.js
     */
    @SuppressWarnings("unused")
    public static void tweakTooltips(ItemStack itemStack, List<Component> tooltip) {
        if (itemStack.getItem() instanceof JewelItem) {
            JewelTooltips.appendTooltip(itemStack, tooltip);
        } else {
            GearComparisonTooltips.appendTooltip(itemStack, tooltip);
        }
    }
}
