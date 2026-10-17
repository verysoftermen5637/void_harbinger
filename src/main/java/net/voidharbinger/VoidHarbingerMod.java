package net.voidharbinger;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.voidharbinger.entity.VoidHarbingerEntity;
import net.voidharbinger.registry.ModEntities;
import net.voidharbinger.registry.ModItems;
import net.voidharbinger.registry.ModSounds;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class VoidHarbingerMod implements ModInitializer {
    public static final String MOD_ID = "void_harbinger";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        LOGGER.info("[VoidHarbinger] Initializing The Void Harbinger Boss Mod...");

        ModSounds.registerModSounds();
        ModEntities.registerModEntities();
        ModItems.registerModItems();

        FabricDefaultAttributeRegistry.register(ModEntities.VOID_HARBINGER, VoidHarbingerEntity.createHarbingerAttributes());

        LOGGER.info("[VoidHarbinger] The Void stirs... Harbinger is ready to conquer!");
    }
}
