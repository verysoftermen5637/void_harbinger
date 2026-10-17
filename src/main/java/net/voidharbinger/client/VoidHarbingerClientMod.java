package net.voidharbinger.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.voidharbinger.client.renderer.VoidHarbingerRenderer;
import net.voidharbinger.registry.ModEntities;

@Environment(EnvType.CLIENT)
public class VoidHarbingerClientMod implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        EntityRendererRegistry.register(ModEntities.VOID_HARBINGER, VoidHarbingerRenderer::new);
    }
}
