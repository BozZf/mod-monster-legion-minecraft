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
    public static StaffSummonState get(MinecraftServer server) {
        return server.getOverworld().getPersistentStateManager().getOrCreate(TYPE, "monster_legion_staff_summons");
    }
    private static StaffSummonState read(NbtCompound nbt, RegistryWrapper.WrapperLookup lookup) {
        StaffSummonState state = new StaffSummonState();
        NbtList list = nbt.getList("Summons", NbtElement.COMPOUND_TYPE);
        for (int i = 0; i < list.size(); i++) {
            NbtCompound entry = list.getCompound(i);
            if (entry.containsUuid("Owner") && entry.containsUuid("Entity"))
                state.slots.reserve(entry.getUuid("Owner"), entry.getUuid("Entity"), entry.getLong("ExpiresAt"));
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
        return nbt;
    }
}
