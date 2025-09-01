package net.subthy.testmod.items.custom;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class IdentificationScroll extends Item {
    public IdentificationScroll(Properties props) {
        super(props);
    }

    @Override
    public boolean overrideStackedOnOther(ItemStack scroll, Slot slot, ClickAction action, Player player) {
        ItemStack target = slot.getItem();

        if (action == ClickAction.SECONDARY && !target.isEmpty()) {
            if (attemptIdentifyItem(target, player)) {
                // Consume one scroll if not in creative
                if (player instanceof ServerPlayer serverPlayer && !serverPlayer.gameMode.isCreative()) {
                    scroll.shrink(1);
                }
                return true; // handled
            }
        }

        return super.overrideStackedOnOther(scroll, slot, action, player);
    }

    private boolean attemptIdentifyItem(ItemStack stack, Player player) {
        if (stack.hasTag() && stack.getTag().getBoolean("hidden")) {
            stack.getTag().remove("hidden");

            if (!player.level().isClientSide) {
                player.displayClientMessage(
                        Component.literal("Item Identified!").withStyle(ChatFormatting.GREEN),
                        true
                );
            }
            return true;
        }
        return false;
    }
}
