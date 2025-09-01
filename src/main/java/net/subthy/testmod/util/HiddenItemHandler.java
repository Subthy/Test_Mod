package net.subthy.testmod.util;

import com.mojang.datafixers.util.Either;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.event.RenderTooltipEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraft.resources.ResourceLocation;

@Mod.EventBusSubscriber(modid = "testmod", bus = Mod.EventBusSubscriber.Bus.FORGE)
public class HiddenItemHandler {

    // Hide tooltip
    @SubscribeEvent
    public static void onTooltip(ItemTooltipEvent event) {
        ItemStack stack = event.getItemStack();

        if (stack.hasTag() && stack.getTag().getBoolean("hidden")) {
            event.getToolTip().clear(); // remove normal tooltip
            event.getToolTip().add(Component.literal("Unidentified Item")
                    .withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC));
        }
    }

    // Hide custom display name with enchantment font
    @SubscribeEvent
    public static void onItemName(RenderTooltipEvent.GatherComponents event) {
        ItemStack stack = event.getItemStack();

        if (stack.hasTag() && stack.getTag().getBoolean("hidden")) {
            // Replace the display name with the original name in enchantment font
            event.getTooltipElements().set(0,
                    Either.left(Component.literal(stack.getItem().getDescriptionId())
                            .withStyle(style -> style
                                    .withFont(new ResourceLocation("minecraft", "alt"))
                                    .withObfuscated(true)
                                    .withColor(ChatFormatting.GRAY))));
        }
    }
}
