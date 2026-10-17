package net.voidharbinger.client.model;

import net.minecraft.util.Identifier;
import net.voidharbinger.VoidHarbingerMod;
import net.voidharbinger.entity.VoidHarbingerEntity;
import software.bernie.geckolib.model.GeoModel;

public class VoidHarbingerModel extends GeoModel<VoidHarbingerEntity> {
    @Override
    public Identifier getModelResource(VoidHarbingerEntity animatable) {
        return new Identifier(VoidHarbingerMod.MOD_ID, "geo/void_harbinger.geo.json");
    }

    @Override
    public Identifier getTextureResource(VoidHarbingerEntity animatable) {
        return new Identifier(VoidHarbingerMod.MOD_ID, "textures/entity/void_harbinger.png");
    }

    @Override
    public Identifier getAnimationResource(VoidHarbingerEntity animatable) {
        return new Identifier(VoidHarbingerMod.MOD_ID, "animations/void_harbinger.animation.json");
    }
}
