package com.kendrome.kendrome_vh_tweaks.tooltips;

import com.kendrome.kendrome_vh_tweaks.Utils;
import com.kendrome.kendrome_vh_tweaks.config.ClientConfig;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public class InscriptionTooltips {
    public static void appendTooltips(ItemStack itemStack, List<Component> toolTip) {
        if (!ClientConfig.INSCRIPTION_RELATIVE_TOOLTIPS_ENABLED.get() || !Utils.shouldShow(ClientConfig.INSCRIPTION_RELATIVE_TOOLTIPS_KEY.get())) {
            return;
        }

        if (itemStack == null || itemStack.getTag() == null)
            return;
        var data = itemStack.getTag().getCompound("data");
        var completion = data.getFloat("completion");
        var instability = data.getFloat("instability");
        var time = data.getInt("time") / 20;


        var options = ClientConfig.INSCRIPTIONS_RELATIVE_OPTIONS.get();

        for (var opt : options) {

        }
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
