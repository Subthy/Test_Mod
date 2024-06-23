package net.subthy.testmod.items.custom.client;

import net.minecraft.resources.ResourceLocation;
import net.subthy.testmod.TestMod;
import net.subthy.testmod.items.custom.PlateArmorItem;
import software.bernie.geckolib.model.GeoModel;

public class PlateArmorModel extends GeoModel<PlateArmorItem> {
    @Override
    public ResourceLocation getModelResource(PlateArmorItem animatable) {
        return new ResourceLocation(TestMod.MOD_ID, "geo/plate_armor.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(PlateArmorItem animatable) {
        return new ResourceLocation(TestMod.MOD_ID, "textures/armor/plate_armor.png");
    }

    @Override
    public ResourceLocation getAnimationResource(PlateArmorItem animatable) {
        return new ResourceLocation(TestMod.MOD_ID, "animations/plate_armor.animation.json");
    }
}