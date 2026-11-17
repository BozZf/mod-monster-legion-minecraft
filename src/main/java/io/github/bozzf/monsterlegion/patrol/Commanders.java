package io.github.bozzf.monsterlegion.patrol;

import io.github.bozzf.monsterlegion.legion.LegionManager;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.ZombieEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;

public final class Commanders {
    public static void equip(MobEntity mob, String group) {
        PatrolIdentity.assign(mob, group);
        mob.addCommandTag(PatrolIdentity.COMMANDER_TAG);
        mob.addCommandTag(LegionManager.LEADER_TAG);
        if (mob instanceof ZombieEntity zombie) zombie.setBaby(false);
        mob.setCanPickUpLoot(false);
        put(mob, EquipmentSlot.HEAD, Items.NETHERITE_HELMET);
        put(mob, EquipmentSlot.CHEST, Items.NETHERITE_CHESTPLATE);
        put(mob, EquipmentSlot.LEGS, Items.NETHERITE_LEGGINGS);
        put(mob, EquipmentSlot.FEET, Items.NETHERITE_BOOTS);
        put(mob, EquipmentSlot.MAINHAND, Items.NETHERITE_SWORD);
        mob.equipStack(EquipmentSlot.OFFHAND, ItemStack.EMPTY);
        mob.setEquipmentDropChance(EquipmentSlot.OFFHAND, 0);
    }
    private static void put(MobEntity mob, EquipmentSlot slot, Item item) {
        mob.equipStack(slot, new ItemStack(item));
        mob.setEquipmentDropChance(slot, 0);
    }
    public static void inherit(MobEntity previous, MobEntity converted) {
        String group = PatrolIdentity.group(previous);
        if (group == null || converted == null) return;
        PatrolIdentity.assign(converted, group);
        if (PatrolIdentity.isCommander(previous)) equip(converted, group);
    }
    private Commanders() { }
}
