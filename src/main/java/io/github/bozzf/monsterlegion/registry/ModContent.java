package io.github.bozzf.monsterlegion.registry;

import io.github.bozzf.monsterlegion.MonsterLegion;
import io.github.bozzf.monsterlegion.entity.LichKingEntity;
import io.github.bozzf.monsterlegion.entity.LichBoltEntity;
import io.github.bozzf.monsterlegion.item.LichStaffItem;
import java.util.Map;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroups;
import net.minecraft.item.SpawnEggItem;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;

public final class ModContent {
    public static Identifier id(String path) { return Identifier.of(MonsterLegion.MOD_ID, path); }
    public static final EntityType<LichKingEntity> LICH_KING = Registry.register(Registries.ENTITY_TYPE,
            id("lich_king"), EntityType.Builder.create(LichKingEntity::new, SpawnGroup.MONSTER)
                    .dimensions(1.0F, 3.5F).maxTrackingRange(10).build("monster_legion:lich_king"));
    public static final EntityType<LichBoltEntity> LICH_BOLT = Registry.register(Registries.ENTITY_TYPE,
            id("lich_bolt"), EntityType.Builder.<LichBoltEntity>create(LichBoltEntity::new, SpawnGroup.MISC)
                    .dimensions(0.25F, 0.25F).maxTrackingRange(6).trackingTickInterval(1)
                    .build("monster_legion:lich_bolt"));
    public static final LichStaffItem LICH_STAFF = Registry.register(Registries.ITEM,
            id("lich_staff"), new LichStaffItem(new Item.Settings().maxCount(1)));
    public static final Item LICH_KING_SPAWN_EGG = Registry.register(Registries.ITEM,
            id("lich_king_spawn_egg"), new SpawnEggItem(LICH_KING, 0x302039, 0x6FFFD4, new Item.Settings()));
    public static final Map<String, SoundEvent> SOUNDS = Map.of(
            "hurt", sound("hurt"), "death", sound("death"), "magic_bolt", sound("magic_bolt"),
            "summon", sound("summon"), "command", sound("command"));

    private static SoundEvent sound(String name) {
        Identifier id = id("lich." + name);
        return Registry.register(Registries.SOUND_EVENT, id, SoundEvent.of(id));
    }
    public static void initialize() {
        FabricDefaultAttributeRegistry.register(LICH_KING, LichKingEntity.attributes());
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.SPAWN_EGGS).register(e -> e.add(LICH_KING_SPAWN_EGG));
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.COMBAT).register(e -> e.add(LICH_STAFF));
    }
    private ModContent() { }
}
