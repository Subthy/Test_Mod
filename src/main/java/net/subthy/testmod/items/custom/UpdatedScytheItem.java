package net.subthy.testmod.items.custom;

import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.HoeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.ToolAction;
import net.minecraftforge.common.ToolActions;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class UpdatedScytheItem extends HoeItem {
    // Configurable radius (5x5 area by default)
    private static final int RADIUS = 2; // -2 to +2 = 5 blocks wide

    public UpdatedScytheItem(Tier pTier, Properties pProperties) {
        super(pTier, 0, 0, pProperties); // Attack damage and speed set to 0, adjust if needed
    }

    // Radius range of the scythe
    @NotNull
    private static List<BlockPos> getBlockPos(BlockPos pos) {
        List<BlockPos> blockPosList = new ArrayList<>();
        int x = pos.getX();
        int y = pos.getY();
        int z = pos.getZ();
        for (int i = x - RADIUS; i <= x + RADIUS; i++) {
            for (int j = z - RADIUS; j <= z + RADIUS; j++) {
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

        // Tilling when sneaking
        if (player.isShiftKeyDown()) {
            boolean damaged = false;
            for (int i = -RADIUS; i <= RADIUS; i++) {
                for (int j = -RADIUS; j <= RADIUS; j++) {
                    BlockPos targetPos = pos.offset(i, 0, j);
                    BlockState targetState = level.getBlockState(targetPos);
                    if (targetState.is(Blocks.GRASS_BLOCK) || targetState.is(Blocks.DIRT)) {
                        level.setBlockAndUpdate(targetPos, Blocks.FARMLAND.defaultBlockState());
                        if (!damaged) {
                            stack.hurtAndBreak(1, player, user -> user.broadcastBreakEvent(player.getUsedItemHand()));
                            damaged = true;
                        }
                    }
                }
            }
            return damaged ? InteractionResult.SUCCESS : InteractionResult.FAIL;
        }

        // Harvesting mature crops
        if (state.is(BlockTags.CROPS)) {
            CropBlock block = (CropBlock) state.getBlock();
            int age = block.getAge(state);
            int maxAge = block.getMaxAge();
            if (age == maxAge) {
                List<BlockPos> posList = getBlockPos(pos);
                boolean damaged = false;
                for (BlockPos blockPos : posList) {
                    BlockState blockState = level.getBlockState(blockPos);
                    if (blockState.is(BlockTags.CROPS)) {
                        CropBlock cropBlock = (CropBlock) blockState.getBlock();
                        int cropAge = cropBlock.getAge(blockState);
                        int cropMaxAge = cropBlock.getMaxAge();
                        if (cropAge == cropMaxAge) {
                            level.destroyBlock(blockPos, true); // Drops items
                            level.setBlockAndUpdate(blockPos, cropBlock.getStateForAge(0)); // Reseeds
                            if (!damaged) {
                                stack.hurtAndBreak(1, player, user -> user.broadcastBreakEvent(player.getUsedItemHand()));
                                damaged = true;
                            }
                        }
                    }
                }
                return InteractionResult.SUCCESS;
            }
            return InteractionResult.FAIL;
        }

        // Clearing vegetation
        if (state.is(Blocks.GRASS) || state.is(Blocks.TALL_GRASS) || state.is(Blocks.FERN) || state.is(Blocks.LARGE_FERN) || state.is(BlockTags.FLOWERS)) {
            boolean damaged = false;
            for (int i = -RADIUS; i <= RADIUS; i++) {
                for (int j = -RADIUS; j <= RADIUS; j++) {
                    BlockPos targetPos = pos.offset(i, 0, j);
                    BlockState targetState = level.getBlockState(targetPos);
                    if (targetState.is(Blocks.GRASS) || targetState.is(Blocks.TALL_GRASS) || targetState.is(Blocks.FERN) || targetState.is(Blocks.LARGE_FERN) || targetState.is(BlockTags.FLOWERS)) {
                        level.destroyBlock(targetPos, true);
                        if (!damaged) {
                            stack.hurtAndBreak(1, player, user -> user.broadcastBreakEvent(player.getUsedItemHand()));
                            damaged = true;
                        }
                    }
                }
            }
            return InteractionResult.SUCCESS;
        }

        return InteractionResult.FAIL;
    }

    @Override
    public boolean mineBlock(@NotNull ItemStack stack, @NotNull Level level, @NotNull BlockState state, @NotNull BlockPos pos, @NotNull LivingEntity pMiningEntity) {
        stack.hurtAndBreak(1, pMiningEntity, user -> user.broadcastBreakEvent(EquipmentSlot.MAINHAND));
        return true;
    }

    @Override
    public boolean canAttackBlock(@NotNull BlockState pState, @NotNull Level pLevel, @NotNull BlockPos pPos, @NotNull Player pPlayer) {
        return super.canAttackBlock(pState, pLevel, pPos, pPlayer);
    }

    @Override
    public boolean canPerformAction(@NotNull ItemStack stack, @NotNull ToolAction toolAction) {
        return ToolActions.DEFAULT_HOE_ACTIONS.contains(toolAction); // Supports hoe actions like tilling
    }
}