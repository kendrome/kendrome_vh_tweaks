package com.kendrome.kendrome_vh_tweaks.mixin;

import com.kendrome.kendrome_vh_tweaks.tooltips.GearComparisonTooltips;
import com.kendrome.kendrome_vh_tweaks.tooltips.JewelTooltips;
import iskallia.vault.gear.tooltip.GearTooltip;
import iskallia.vault.gear.tooltip.VaultGearTooltipItem;
import iskallia.vault.item.tool.JewelItem;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
@Mixin(value = VaultGearTooltipItem.class, remap = false)
public interface MixinVaultGearTooltipItem  {
    @Inject(method = "createTooltip", at = @At("RETURN"))
    default void createTooltip(ItemStack stack, GearTooltip flag, CallbackInfoReturnable<List<Component>> cir) {
        List<Component> tooltip = cir.getReturnValue();
        if (tooltip.isEmpty()) {
            return;
        }

        if (this instanceof JewelItem) {
            JewelTooltips.appendTooltip(stack, tooltip);
        } else {
            GearComparisonTooltips.appendTooltip(stack, tooltip);
        }
    }
}