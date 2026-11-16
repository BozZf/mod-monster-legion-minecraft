package io.github.bozzf.monsterlegion.legion;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.BiPredicate;

/** Server-side membership; independent of Minecraft so ownership rules can be tested. */
public final class LegionRoster {
    private final Map<UUID, UUID> leadersByMember = new HashMap<>();
    private final int capacity;

    public LegionRoster(int capacity) {
        if (capacity < 1) throw new IllegalArgumentException("capacity must be positive");
        this.capacity = capacity;
    }

    public boolean recruit(UUID leader, UUID member) {
        if (leader.equals(member) || leadersByMember.containsKey(member)) return false;
        long size = leadersByMember.values().stream().filter(leader::equals).count();
        if (size >= capacity) return false;
        leadersByMember.put(member, leader);
        return true;
    }

    public UUID leaderOf(UUID member) { return leadersByMember.get(member); }

    public void retain(BiPredicate<UUID, UUID> valid) {
        leadersByMember.entrySet().removeIf(entry -> !valid.test(entry.getValue(), entry.getKey()));
    }
}
