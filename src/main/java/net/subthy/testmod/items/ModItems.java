package net.subthy.testmod.items;

import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.Item;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.subthy.testmod.TestMod;
import net.subthy.testmod.items.custom.PlateArmorItem;
import net.subthy.testmod.items.custom.ScytheItem;
import net.subthy.testmod.items.custom.UpdatedScytheItem;

public class ModItems {

    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, TestMod.MOD_ID);

    public static final RegistryObject<Item> PLATE_HELMET = ITEMS.register("plate_helmet",
            () -> new PlateArmorItem(ModArmorMaterials.Plate_Armor, ArmorItem.Type.HELMET, new Item.Properties()));
    public static final RegistryObject<Item> PLATE_CHESTPLATE = ITEMS.register("plate_chestplate",
            () -> new PlateArmorItem(ModArmorMaterials.Plate_Armor, ArmorItem.Type.CHESTPLATE, new Item.Properties()));
    public static final RegistryObject<Item> PLATE_LEGGINGS = ITEMS.register("plate_leggings",
            () -> new PlateArmorItem(ModArmorMaterials.Plate_Armor, ArmorItem.Type.LEGGINGS, new Item.Properties()));
    public static final RegistryObject<Item> PLATE_BOOTS = ITEMS.register("plate_boots",
            () -> new PlateArmorItem(ModArmorMaterials.Plate_Armor, ArmorItem.Type.BOOTS, new Item.Properties()));

    public static RegistryObject<Item> IRON_SCYTHE = ITEMS.register("iron_scythe",
            ()-> new ScytheItem(ModToolTiers.IRON_SCYTHE, new Item.Properties().durability(89)));

    public static RegistryObject<Item> IRONN_SCYTHE = ITEMS.register("ironn_scythe",
            ()-> new UpdatedScytheItem(ModToolTiers.IRON_SCYTHE, new Item.Properties().durability(89)));
    

    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}