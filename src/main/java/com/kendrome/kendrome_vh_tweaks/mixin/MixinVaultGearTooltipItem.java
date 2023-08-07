package com.kendrome.kendrome_vh_tweaks.mixin;

import com.kendrome.kendrome_vh_tweaks.GearComparison;
import iskallia.vault.gear.item.VaultGearItem;
import iskallia.vault.gear.tooltip.GearTooltip;
import iskallia.vault.gear.tooltip.VaultGearTooltipItem;
import iskallia.vault.init.ModConfigs;
import iskallia.vault.item.gear.VaultArmorItem;
import iskallia.vault.item.tool.JewelItem;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.struct.Target;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/*@Mixin(value = VaultGearTooltipItem.class, remap = false)
public interface MixinVaultGearTooltipItem  {
    @Shadow public abstract List<Component> createTooltip(ItemStack stack, GearTooltip flag);
    @Intrinsic(displace = true)
    default List<Component> soft$createTooltip(ItemStack stack, GearTooltip flag){
        List<Component> toolTip = createTooltip(stack, flag);
        toolTip.add(new TextComponent("Test"));
        return toolTip;
    }
} */

/*@Mixin(value = JewelItem.class)
@Implements(@Interface(iface = VaultGearTooltipItem.class, prefix = "tooltip$"))
public abstract class MixinVaultGearTooltipItem {
    @Inject(method="createTooltip", at = @At("Tail"))
    private void tooltip$createTooltip(ItemStack stack, GearTooltip flag, CallbackInfoReturnable<List<Component>> callback) {
        List<Component> toolTip = callback.getReturnValue();
        toolTip.add(new TextComponent("Inject Worked"));
        callback.setReturnValue(toolTip);
    }
}*/

@Mixin(value = VaultGearTooltipItem.class, remap = false)
//@Implements(@Interface(iface = VaultGearTooltipItem.class, prefix = "tooltip$"))
public interface MixinVaultGearTooltipItem  {
    @Shadow public abstract List<Component> createTooltip(ItemStack stack, GearTooltip flag);
    @Intrinsic(displace = true)
    default List<Component> tooltip$createTooltip(ItemStack stack, GearTooltip flag){
        List<Component> toolTip = createTooltip(stack, flag);
        toolTip.add(new TextComponent("Test"));
        //toolTip.addAll()
        return toolTip;
    }
}