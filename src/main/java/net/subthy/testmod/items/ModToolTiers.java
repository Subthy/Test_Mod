package net.subthy.testmod.items;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;

import net.minecraft.world.item.Items;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.common.ForgeTier;
import net.minecraftforge.common.TierSortingRegistry;
import net.subthy.testmod.TestMod;
import net.subthy.testmod.util.ModTags;

import java.util.List;

        public class ModToolTiers {
            public static final Tier IRON_SCYTHE = TierSortingRegistry.registerTier(
                    new ForgeTier(2, 250, 3f, 7f, 14,
                            ModTags.Blocks.NEEDS_SCYTHE_TOOL, () -> Ingredient.of(Items.IRON_INGOT)),
                    new ResourceLocation(TestMod.MOD_ID, "iron_scythe"), List.of(Tiers.IRON), List.of());
}
