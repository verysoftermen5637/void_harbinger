package net.voidharbinger.client.renderer;

import net.minecraft.client.render.entity.EntityRendererFactory;
import net.voidharbinger.client.model.VoidHarbingerModel;
import net.voidharbinger.entity.VoidHarbingerEntity;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class VoidHarbingerRenderer extends GeoEntityRenderer<VoidHarbingerEntity> {
    public VoidHarbingerRenderer(EntityRendererFactory.Context renderManager) {
        super(renderManager, new VoidHarbingerModel());
        this.shadowRadius = 1.2f;
    }
}
