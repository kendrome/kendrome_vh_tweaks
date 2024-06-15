package com.kendrome.kendrome_vh_tweaks.mixin;
import iskallia.vault.gear.tooltip.GearTooltip;
import iskallia.vault.gear.tooltip.VaultGearTooltipItem;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Intrinsic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.List;
@Mixin(value = VaultGearTooltipItem.class, remap = false)
public interface MixinVaultGearTooltipItem  {
    @Shadow List<Component> createTooltip(ItemStack stack, GearTooltip flag);

    @Intrinsic(displace = true)
    default List<Component> tooltip$createTooltip(ItemStack stack, GearTooltip flag){
        List<Component> toolTip = createTooltip(stack, flag);
        toolTip.add(new TextComponent("Test"));
        return toolTip;
    }
}