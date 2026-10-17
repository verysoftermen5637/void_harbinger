package net.voidharbinger.registry;

import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroups;
import net.minecraft.item.SpawnEggItem;
import net.minecraft.item.ToolMaterials;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import net.minecraft.util.Rarity;
import net.voidharbinger.VoidHarbingerMod;
import net.voidharbinger.item.HeartOfTheVoidItem;
import net.voidharbinger.item.VoidScytheItem;

public class ModItems {
    public static final Item VOID_SCYTHE = Registry.register(
            Registries.ITEM,
            new Identifier(VoidHarbingerMod.MOD_ID, "void_scythe"),
            new VoidScytheItem(ToolMaterials.NETHERITE, 14, -2.4f,
                    new FabricItemSettings().maxCount(1).fireproof().rarity(Rarity.EPIC))
    );

    public static final Item HEART_OF_THE_VOID = Registry.register(
            Registries.ITEM,
            new Identifier(VoidHarbingerMod.MOD_ID, "heart_of_the_void"),
            new HeartOfTheVoidItem(new FabricItemSettings().maxCount(1).fireproof().rarity(Rarity.EPIC))
    );

    public static final Item VOID_HARBINGER_SPAWN_EGG = Registry.register(
            Registries.ITEM,
            new Identifier(VoidHarbingerMod.MOD_ID, "void_harbinger_spawn_egg"),
            new SpawnEggItem(ModEntities.VOID_HARBINGER, 0x110926, 0x9933ff, new FabricItemSettings().maxCount(64).rarity(Rarity.RARE))
    );

    public static void registerModItems() {
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.COMBAT).register(entries -> {
            entries.add(VOID_SCYTHE);
            entries.add(HEART_OF_THE_VOID);
        });
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.SPAWN_EGGS).register(entries -> {
            entries.add(VOID_HARBINGER_SPAWN_EGG);
        });
        VoidHarbingerMod.LOGGER.info("[VoidHarbinger] Registered Mod Items.");
    }
}
