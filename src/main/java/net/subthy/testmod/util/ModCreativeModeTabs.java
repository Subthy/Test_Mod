package net.subthy.testmod.util;

import net.minecraft.world.item.Items;
import net.subthy.testmod.TestMod;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;
import net.subthy.testmod.block.ModBlocks;
import net.subthy.testmod.items.ModItems;

public class ModCreativeModeTabs {

    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, TestMod.MOD_ID);

    public static final RegistryObject<CreativeModeTab> TEST_TAB = CREATIVE_MODE_TABS.register("test_tab",
            () -> CreativeModeTab.builder().icon(() -> new ItemStack(Items.STONE))
                    .title(Component.translatable("creativetab.test_tab"))
                    .displayItems((displayParameters, output) -> {
                        output.accept(new ItemStack(ModItems.PLATE_CHESTPLATE.get()));
                        output.accept(new ItemStack(ModItems.PLATE_HELMET.get()));
                        output.accept(new ItemStack(ModItems.PLATE_LEGGINGS.get()));
                        output.accept(new ItemStack(ModItems.PLATE_BOOTS.get()));
                        output.accept(new ItemStack(ModItems.IRON_SCYTHE.get()));


                    }).build());


    public static void register(IEventBus eventBus) {
        CREATIVE_MODE_TABS.register(eventBus);
    }
}

