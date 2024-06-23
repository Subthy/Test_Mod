package net.subthy.testmod.items.custom;

import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.ToolAction;
import net.minecraftforge.common.ToolActions;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class ScytheItem extends SwordItem {
    public ScytheItem(Tier pTier, Properties pProperties) {
        super(pTier, 0, 0, pProperties);
    }




    // Radius range of the scythe
    @NotNull
    private static List<BlockPos> getBlockPos(BlockPos pos) {
        List<BlockPos> blockPosList = new ArrayList<>();
        int x = pos.getX();
        int y = pos.getY();
        int z = pos.getZ();
        for (int i = x - 2; i <= x + 2; i++) {
            for (int j = z - 2; j <= z + 2; j++) {
                blockPosList.add(new BlockPos(i, y, j));
            }
        }

        return blockPosList;
    }

    @Override
    public @NotNull InteractionResult useOn(@NotNull UseOnContext pContext) {
        Player player = pContext.getPlayer();
        Level level = pContext.getLevel();
        BlockPos pos = pContext.getClickedPos();
        BlockState state = level.getBlockState(pos);
        ItemStack stack = pContext.getItemInHand();
        assert player != null;

        if (player.isShiftKeyDown()) {
            for (int i = -2; i <= 2; i++) {
                for (int j = -2; j <= 2; j++) {
                    BlockPos targetPos = pos.offset(i, 0, j);
                    BlockState targetState = level.getBlockState(targetPos);
                    if (targetState.is(Blocks.GRASS_BLOCK) || targetState.is(Blocks.DIRT)) {
                        level.setBlockAndUpdate(targetPos, Blocks.FARMLAND.defaultBlockState());
                    }
                }
            }
            stack.hurtAndBreak(1, player, user -> user.broadcastBreakEvent(player.getUsedItemHand()));
            return InteractionResult.SUCCESS;
        }

        if (state.is(BlockTags.CROPS)) {
            CropBlock block = (CropBlock) state.getBlock();
            int age = block.getAge(state);
            int maxAge = block.getMaxAge();
            if (age == maxAge) {
                List<BlockPos> posList = getBlockPos(pos);
                for (BlockPos blockPos : posList) {
                    BlockState blockState = level.getBlockState(blockPos);
                    if (blockState.is(BlockTags.CROPS)) {
                        CropBlock cropBlock = (CropBlock) blockState.getBlock();
                        int cropAge = cropBlock.getAge(blockState);
                        int cropMaxAge = cropBlock.getMaxAge();
                        if (cropAge == cropMaxAge) {
                            level.destroyBlock(blockPos, true);
                            level.setBlockAndUpdate(blockPos, Blocks.AIR.defaultBlockState());
                            level.sendBlockUpdated(blockPos, blockState, Blocks.AIR.defaultBlockState(), 3);
                            stack.hurtAndBreak(1, player, user -> user.broadcastBreakEvent(player.getUsedItemHand()));
                        }
                    }
                }
            }
            return InteractionResult.SUCCESS;
        }

        if (state.is(Blocks.GRASS) || state.is(Blocks.TALL_GRASS) || state.is(Blocks.FERN) || state.is(Blocks.LARGE_FERN) || state.is(BlockTags.FLOWERS)) {
            // Destroy the stone block and neighboring stone blocks in a 5x5 area
            for (int i = -2; i <= 2; i++) {
                for (int j = -2; j <= 2; j++) {
                    BlockPos targetPos = pos.offset(i, 0, j);
                    BlockState targetState = level.getBlockState(targetPos);
                    if (targetState.is(Blocks.GRASS) || targetState.is(Blocks.TALL_GRASS) || targetState.is(Blocks.FERN) || targetState.is(Blocks.LARGE_FERN) || targetState.is(BlockTags.FLOWERS)) {
                        level.destroyBlock(targetPos, true);
                        stack.hurtAndBreak(1, player, user -> user.broadcastBreakEvent(player.getUsedItemHand()));
                    }
                }
            }
            return InteractionResult.SUCCESS;
        }

        return InteractionResult.FAIL;
    }

    @Override
    public boolean mineBlock(@NotNull ItemStack stack, @NotNull Level level, @NotNull BlockState state, @NotNull BlockPos pos, @NotNull LivingEntity pMiningEntity) {
        stack.hurtAndBreak(1, pMiningEntity, (user) -> user.broadcastBreakEvent(EquipmentSlot.MAINHAND));
        return true;
    }


    @Override
    public boolean canAttackBlock(BlockState pState, Level pLevel, BlockPos pPos, Player pPlayer) {
        return super.canAttackBlock(pState, pLevel, pPos, pPlayer);
    }

    @Override
    public boolean isEnchantable(@NotNull ItemStack pStack) {
        return super.isEnchantable(pStack) || EnchantmentHelper.getItemEnchantmentLevel(Enchantments.SWEEPING_EDGE, pStack) > 0;
    }

    @Override
    public boolean isBookEnchantable(ItemStack stack, ItemStack book) {
        return true;
    }
}
