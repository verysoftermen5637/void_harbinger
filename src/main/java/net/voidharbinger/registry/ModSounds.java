package net.voidharbinger.registry;

import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;
import net.voidharbinger.VoidHarbingerMod;

public class ModSounds {
    public static final SoundEvent HARBINGER_AMBIENT = register("entity.void_harbinger.ambient");
    public static final SoundEvent HARBINGER_ROAR = register("entity.void_harbinger.roar");
    public static final SoundEvent HARBINGER_ATTACK = register("entity.void_harbinger.attack");
    public static final SoundEvent HARBINGER_TELEPORT = register("entity.void_harbinger.teleport");
    public static final SoundEvent HARBINGER_SINGULARITY = register("entity.void_harbinger.singularity");
    public static final SoundEvent SCYTHE_WARP = register("item.void_scythe.warp");

    private static SoundEvent register(String id) {
        Identifier identifier = new Identifier(VoidHarbingerMod.MOD_ID, id);
        return Registry.register(Registries.SOUND_EVENT, identifier, SoundEvent.of(identifier));
    }

    public static void registerModSounds() {
        VoidHarbingerMod.LOGGER.info("[VoidHarbinger] Registered Mod Sounds.");
    }
}
