package io.github.bozzf.monsterlegion.combat;

import io.github.bozzf.monsterlegion.entity.LichKingEntity;
import java.util.List;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.AbstractSkeletonEntity;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.ZombieEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

public final class Summoning {
    private static final List<EntityType<? extends HostileEntity>> TYPES = List.of(
            EntityType.ZOMBIE, EntityType.HUSK, EntityType.SKELETON, EntityType.STRAY, EntityType.WITHER_SKELETON);
    private static final List<List<Item>> ARMOR = List.of(
            List.of(Items.LEATHER_HELMET, Items.LEATHER_CHESTPLATE, Items.LEATHER_LEGGINGS, Items.LEATHER_BOOTS),
            List.of(Items.CHAINMAIL_HELMET, Items.CHAINMAIL_CHESTPLATE, Items.CHAINMAIL_LEGGINGS, Items.CHAINMAIL_BOOTS),
            List.of(Items.IRON_HELMET, Items.IRON_CHESTPLATE, Items.IRON_LEGGINGS, Items.IRON_BOOTS),
            List.of(Items.GOLDEN_HELMET, Items.GOLDEN_CHESTPLATE, Items.GOLDEN_LEGGINGS, Items.GOLDEN_BOOTS),
            List.of(Items.DIAMOND_HELMET, Items.DIAMOND_CHESTPLATE, Items.DIAMOND_LEGGINGS, Items.DIAMOND_BOOTS),
            List.of(Items.NETHERITE_HELMET, Items.NETHERITE_CHESTPLATE, Items.NETHERITE_LEGGINGS, Items.NETHERITE_BOOTS));
    private static final EquipmentSlot[] SLOTS =
            {EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET};

    public static long now(MinecraftServer server) { return server.getOverworld().getTime(); }

    private static MobEntity create(ServerWorld world, LivingEntity summoner) {
        var type = TYPES.get(world.random.nextInt(TYPES.size()));
        for (int attempt = 0; attempt < 32; attempt++) {
            int x = summoner.getBlockX() + world.random.nextInt(9) - 4;
            int z = summoner.getBlockZ() + world.random.nextInt(9) - 4;
            for (int offset = 3; offset >= -8; offset--) {
                BlockPos pos = new BlockPos(x, summoner.getBlockY() + offset, z);
                if (!world.isInBuildLimit(pos.up(2)) || !world.getWorldBorder().contains(pos)) continue;
                boolean loaded = true;
                for (int cx = (x - 2) >> 4; cx <= (x + 2) >> 4; cx++)
                    for (int cz = (z - 2) >> 4; cz <= (z + 2) >> 4; cz++)
                        loaded &= world.isChunkLoaded(cx, cz);
                if (!loaded) continue;
                if (!world.getBlockState(pos.down()).isSideSolidFullSquare(world, pos.down(), Direction.UP)
                        || !world.getFluidState(pos).isEmpty()) continue;
                HostileEntity mob = type.create(world);
                if (mob == null) return null;
                mob.refreshPositionAndAngles(x + 0.5, pos.getY(), z + 0.5, summoner.getYaw(), 0);
                if (!world.isSpaceEmpty(mob) || !world.getOtherEntities(mob, mob.getBoundingBox()).isEmpty()) continue;
                if (mob instanceof ZombieEntity zombie) zombie.setBaby(false);
                List<Item> armor = ARMOR.get(world.random.nextInt(ARMOR.size()));
                for (int i = 0; i < SLOTS.length; i++) mob.equipStack(SLOTS[i], new ItemStack(armor.get(i)));
                mob.equipStack(EquipmentSlot.MAINHAND, new ItemStack(
                        mob instanceof AbstractSkeletonEntity ? Items.BOW : Items.IRON_SWORD));
                for (EquipmentSlot slot : SLOTS) mob.setEquipmentDropChance(slot, 0);
                mob.setEquipmentDropChance(EquipmentSlot.MAINHAND, 0);
                mob.setEquipmentDropChance(EquipmentSlot.OFFHAND, 0);
                mob.setCanPickUpLoot(false);
                ((SummonData) mob).monsterLegion$conjured(true);
                return mob;
            }
        }
        return null;
    }
    public static boolean byLich(LichKingEntity lich) {
        if (!(lich.getWorld() instanceof ServerWorld world)) return false;
        int count = 0;
        for (ServerWorld dimension : world.getServer().getWorlds()) {
            for (Entity entity : dimension.iterateEntities()) {
                if (entity instanceof MobEntity mob && mob.isAlive()
                        && ((SummonData) mob).monsterLegion$conjured()
                        && lich.getUuid().equals(((SummonData) mob).monsterLegion$lichOwner())) count++;
            }
        }
        int wanted = Math.min(12 - count, 2 + world.random.nextInt(3));
        int spawned = 0;
        for (int i = 0; i < wanted; i++) {
            MobEntity mob = create(world, lich);
            if (mob == null) break;
            ((SummonData) mob).monsterLegion$lichOwner(lich.getUuid());
            mob.setPersistent();
            mob.setTarget(lich.getTarget());
            if (world.spawnEntity(mob)) spawned++;
        }
        return spawned > 0;
    }
    public static String byPlayer(ServerPlayerEntity player) {
        MinecraftServer server = player.getServer();
        StaffSummonState state = StaffSummonState.get(server);
        SummonSlots.Slot old = state.slots.get(player.getUuid());
        if (old != null) {
            if (old.expiresAt() > now(server)) return "Você já possui uma invocação ativa (máximo de uma por jogador).";
            expire(server, old.entity());
            state.slots.remove(player.getUuid(), old.entity());
            state.markDirty();
        }
        ServerWorld world = player.getServerWorld();
        MobEntity mob = create(world, player);
        if (mob == null) return "Não há espaço seguro próximo para invocar um monstro.";
        long expiresAt = now(server) + LichRules.SUMMON_TICKS;
        SummonData data = (SummonData) mob;
        data.monsterLegion$playerOwner(player.getUuid());
        data.monsterLegion$expiresAt(expiresAt);
        mob.setPersistent();
        if (!state.slots.reserve(player.getUuid(), mob.getUuid(), expiresAt)) return "Você já possui uma invocação ativa.";
        state.markDirty();
        if (!world.spawnEntity(mob)) {
            state.slots.remove(player.getUuid(), mob.getUuid());
            state.markDirty();
            return "Não foi possível concluir a invocação.";
        }
        world.spawnParticles(ParticleTypes.ENCHANT, mob.getX(), mob.getBodyY(0.5), mob.getZ(), 30, 0.5, 0.8, 0.5, 0.1);
        return null;
    }
    private static void expire(MinecraftServer server, java.util.UUID uuid) {
        for (ServerWorld world : server.getWorlds()) {
            Entity entity = world.getEntity(uuid);
            if (entity instanceof MobEntity mob) expire(mob);
        }
    }
    private static void expire(MobEntity mob) {
        if (mob.isAlive()) {
            // Use the normal death path, preserving death sounds/animation and the loot suppression.
            mob.damage(mob.getDamageSources().genericKill(), Float.MAX_VALUE);
        }
    }
    public static void onLoad(MobEntity mob, ServerWorld world) {
        SummonData data = (SummonData) mob;
        if (!mob.isAlive()) return;
        if (data.monsterLegion$conjured() && data.monsterLegion$lichOwner() != null) {
            mob.setPersistent();
            if (StaffSummonState.get(world.getServer()).isLichDead(data.monsterLegion$lichOwner())) expire(mob);
            return;
        }
        if (data.monsterLegion$playerOwner() == null) return;
        if (data.monsterLegion$expiresAt() <= now(world.getServer())) {
            expire(mob);
            return;
        }
        var slot = StaffSummonState.get(world.getServer()).slots.get(data.monsterLegion$playerOwner());
        // Only invalid/duplicated reservations are discarded, never an expired legitimate summon.
        if (slot == null || !slot.entity().equals(mob.getUuid())) mob.discard();
    }
    public static void onLichDeath(LichKingEntity lich) {
        if (!(lich.getWorld() instanceof ServerWorld world)) return;
        StaffSummonState.get(world.getServer()).markLichDead(lich.getUuid());
        for (ServerWorld dimension : world.getServer().getWorlds()) {
            java.util.List<MobEntity> minions = new java.util.ArrayList<>();
            for (Entity entity : dimension.iterateEntities()) {
                if (entity instanceof MobEntity mob && mob.isAlive()
                        && ((SummonData) mob).monsterLegion$conjured()
                        && lich.getUuid().equals(((SummonData) mob).monsterLegion$lichOwner())) minions.add(mob);
            }
            minions.forEach(Summoning::expire);
        }
    }
    public static void onDeath(MobEntity mob) {
        SummonData data = (SummonData) mob;
        if (data.monsterLegion$playerOwner() == null || !(mob.getWorld() instanceof ServerWorld world)) return;
        StaffSummonState state = StaffSummonState.get(world.getServer());
        state.slots.remove(data.monsterLegion$playerOwner(), mob.getUuid());
        state.markDirty();
    }
    public static void onConversion(MobEntity old, MobEntity converted) {
        if (converted == null) return;
        SummonData from = (SummonData) old, to = (SummonData) converted;
        to.monsterLegion$lichOwner(from.monsterLegion$lichOwner());
        to.monsterLegion$playerOwner(from.monsterLegion$playerOwner());
        to.monsterLegion$expiresAt(from.monsterLegion$expiresAt());
        to.monsterLegion$conjured(from.monsterLegion$conjured());
        if (from.monsterLegion$conjured() && from.monsterLegion$lichOwner() != null) converted.setPersistent();
        if (from.monsterLegion$playerOwner() != null && old.getWorld() instanceof ServerWorld world) {
            StaffSummonState state = StaffSummonState.get(world.getServer());
            state.slots.transfer(from.monsterLegion$playerOwner(), old.getUuid(), converted.getUuid());
            state.markDirty();
        }
    }
    public static void tick(MinecraftServer server) {
        long time = now(server);
        StaffSummonState state = StaffSummonState.get(server);
        state.slots.snapshot().forEach((owner, slot) -> {
            if (slot.expiresAt() <= time) {
                expire(server, slot.entity());
                state.slots.remove(owner, slot.entity());
                state.markDirty();
            }
        });
        if (time % 20 != 0) return;
        for (ServerWorld world : server.getWorlds()) {
            java.util.List<MobEntity> loaded = new java.util.ArrayList<>();
            for (Entity entity : world.iterateEntities()) {
                if (entity instanceof MobEntity mob && mob.isAlive()) loaded.add(mob);
            }
            for (MobEntity mob : loaded) {
                SummonData data = (SummonData) mob;
                if (data.monsterLegion$playerOwner() != null || data.monsterLegion$conjured()) onLoad(mob, world);
                if (mob.isAlive()) SummonTeleport.followPlayer(mob);
                if (!data.monsterLegion$conjured() && data.monsterLegion$lichOwner() != null
                        && LichAllies.leader(mob) == null) {
                    data.monsterLegion$lichOwner(null);
                    mob.setTarget(null);
                }
            }
        }
    }
    private Summoning() { }
}
