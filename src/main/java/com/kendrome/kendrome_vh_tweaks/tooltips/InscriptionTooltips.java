package com.kendrome.kendrome_vh_tweaks.tooltips;

import com.kendrome.kendrome_vh_tweaks.Utils;
import com.kendrome.kendrome_vh_tweaks.config.ClientConfig;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public class InscriptionTooltips {
    public static void appendTooltips(ItemStack itemStack, List<Component> toolTip) {
        if (!ClientConfig.INSCRIPTION_RELATIVE_TOOLTIPS_ENABLED.get()
                || !Utils.shouldShow(ClientConfig.INSCRIPTION_RELATIVE_TOOLTIPS_KEY.get())
                || itemStack.getTag() == null) {
            return;
        }

        CompoundTag data = itemStack.getTag().getCompound("data");
        float completion = data.getFloat("completion");
        float instability = data.getFloat("instability");
        int time = data.getInt("time") / 20;

        ClientConfig.INSCRIPTIONS_RELATIVE_OPTIONS.get().forEach(opt -> {
            if (opt.equals(ClientConfig.InscriptionOptions.CompletionPerTime.toString())) {
                toolTip.add(new TextComponent("§7" + Utils.formatText((completion * 100) / time) + " §fcompletion/time"));
            }
            if (opt.equals(ClientConfig.InscriptionOptions.CompletionPerInstability.toString())) {
                toolTip.add(new TextComponent("§7" + Utils.formatText(completion / instability) + " §fcompletion/instability"));
            }
            if (opt.equals(ClientConfig.InscriptionOptions.TimePerInstability.toString())) {
                toolTip.add(new TextComponent("§7" + Utils.formatText(time / (instability * 100)) + " §ftime/instability"));
            }
        });
    }
}
