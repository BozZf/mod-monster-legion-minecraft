package io.github.bozzf.monsterlegion.combat;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/** One slot per player, shared by every staff and dimension. Unloaded mobs keep their slot. */
public final class SummonSlots {
    public record Slot(UUID entity, long expiresAt) { }
    private final Map<UUID, Slot> slots = new HashMap<>();
    public Slot get(UUID player) { return slots.get(player); }
    public boolean reserve(UUID player, UUID entity, long expiresAt) {
        if (slots.containsKey(player)) return false;
        slots.put(player, new Slot(entity, expiresAt));
        return true;
    }
    public void remove(UUID player, UUID entity) {
        Slot slot = slots.get(player);
        if (slot != null && slot.entity().equals(entity)) slots.remove(player);
    }
    public void transfer(UUID player, UUID previous, UUID converted) {
        Slot slot = slots.get(player);
        if (slot != null && slot.entity().equals(previous))
            slots.put(player, new Slot(converted, slot.expiresAt()));
    }
    public Map<UUID, Slot> snapshot() { return Map.copyOf(slots); }
}
