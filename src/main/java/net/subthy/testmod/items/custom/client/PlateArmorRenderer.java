package net.subthy.testmod.items.custom.client;

import net.subthy.testmod.items.custom.PlateArmorItem;
import software.bernie.geckolib.renderer.GeoArmorRenderer;

public class PlateArmorRenderer extends GeoArmorRenderer<PlateArmorItem> {
    public PlateArmorRenderer() {
        super(new PlateArmorModel());
    }
}