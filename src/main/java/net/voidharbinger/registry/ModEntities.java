package net.voidharbinger.registry;

import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import net.voidharbinger.VoidHarbingerMod;
import net.voidharbinger.entity.VoidHarbingerEntity;
import net.voidharbinger.entity.VoidProjectileEntity;

public class ModEntities {
    public static final EntityType<VoidHarbingerEntity> VOID_HARBINGER = Registry.register(
            Registries.ENTITY_TYPE,
            new Identifier(VoidHarbingerMod.MOD_ID, "void_harbinger"),
            FabricEntityTypeBuilder.create(SpawnGroup.MONSTER, VoidHarbingerEntity::new)
                    .dimensions(EntityDimensions.fixed(1.8f, 3.8f))
                    .fireImmune()
                    .trackRangeBlocks(64)
                    .trackedUpdateRate(1)
                    .build()
    );

    public static final EntityType<VoidProjectileEntity> VOID_PROJECTILE = Registry.register(
            Registries.ENTITY_TYPE,
            new Identifier(VoidHarbingerMod.MOD_ID, "void_projectile"),
            FabricEntityTypeBuilder.<VoidProjectileEntity>create(SpawnGroup.MISC, VoidProjectileEntity::new)
                    .dimensions(EntityDimensions.fixed(0.8f, 0.8f))
                    .trackRangeBlocks(48)
                    .trackedUpdateRate(1)
                    .build()
    );

    public static void registerModEntities() {
        VoidHarbingerMod.LOGGER.info("[VoidHarbinger] Registered Mod Entities.");
    }
}
