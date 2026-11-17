package io.github.bozzf.monsterlegion.combat;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtElement;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.PersistentState;

public final class StaffSummonState extends PersistentState {
    public static final Type<StaffSummonState> TYPE =
            new Type<>(StaffSummonState::new, StaffSummonState::read, null);
    public final SummonSlots slots = new SummonSlots();
    private final java.util.Set<java.util.UUID> deadLiches = new java.util.HashSet<>();
    private final java.util.Set<java.util.UUID> cancelledSummons = new java.util.HashSet<>();
    public boolean isCancelled(java.util.UUID summon) { return cancelledSummons.contains(summon); }
    public void cancel(java.util.UUID summon) {
        if (cancelledSummons.add(summon)) markDirty();
    }
    public boolean isLichDead(java.util.UUID lich) { return deadLiches.contains(lich); }
    public void markLichDead(java.util.UUID lich) {
        if (deadLiches.add(lich)) markDirty();
    }
    public static StaffSummonState get(MinecraftServer server) {
        return server.getOverworld().getPersistentStateManager().getOrCreate(TYPE, "monster_legion_staff_summons");
    }
    static StaffSummonState read(NbtCompound nbt, RegistryWrapper.WrapperLookup lookup) {
        StaffSummonState state = new StaffSummonState();
        NbtList list = nbt.getList("Summons", NbtElement.COMPOUND_TYPE);
        for (int i = 0; i < list.size(); i++) {
            NbtCompound entry = list.getCompound(i);
            if (entry.containsUuid("Owner") && entry.containsUuid("Entity"))
                state.slots.reserve(entry.getUuid("Owner"), entry.getUuid("Entity"), entry.getLong("ExpiresAt"));
        }
        NbtList deaths = nbt.getList("DeadLiches", NbtElement.COMPOUND_TYPE);
        for (int i = 0; i < deaths.size(); i++) {
            NbtCompound entry = deaths.getCompound(i);
            if (entry.containsUuid("Lich")) state.deadLiches.add(entry.getUuid("Lich"));
        }
        NbtList cancelled = nbt.getList("CancelledSummons", NbtElement.COMPOUND_TYPE);
        for (int i = 0; i < cancelled.size(); i++) {
            NbtCompound entry = cancelled.getCompound(i);
            if (entry.containsUuid("Entity")) state.cancelledSummons.add(entry.getUuid("Entity"));
        }
        return state;
    }
    @Override public NbtCompound writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup lookup) {
        NbtList list = new NbtList();
        slots.snapshot().forEach((owner, slot) -> {
            NbtCompound entry = new NbtCompound();
            entry.putUuid("Owner", owner);
            entry.putUuid("Entity", slot.entity());
            entry.putLong("ExpiresAt", slot.expiresAt());
            list.add(entry);
        });
        nbt.put("Summons", list);
        NbtList deaths = new NbtList();
        for (java.util.UUID lich : deadLiches) {
            NbtCompound entry = new NbtCompound();
            entry.putUuid("Lich", lich);
            deaths.add(entry);
        }
        nbt.put("DeadLiches", deaths);
        NbtList cancelled = new NbtList();
        for (java.util.UUID summon : cancelledSummons) {
            NbtCompound entry = new NbtCompound();
            entry.putUuid("Entity", summon);
            cancelled.add(entry);
        }
        nbt.put("CancelledSummons", cancelled);
        return nbt;
    }
}
