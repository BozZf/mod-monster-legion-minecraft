package io.github.bozzf.monsterlegion.patrol;

import java.util.Set;
import java.util.UUID;
import net.minecraft.entity.Entity;

/** Scoreboard tags are saved by vanilla with each entity. */
public final class PatrolIdentity {
    public static final String MEMBER_TAG = "monster_legion.patrol";
    public static final String COMMANDER_TAG = "monster_legion.commander";
    public static final String GROUP_PREFIX = "monster_legion.group.";

    public static String groupId(Set<String> tags) {
        return tags.stream().filter(tag -> tag.startsWith(GROUP_PREFIX))
                .filter(tag -> {
                    try { UUID.fromString(tag.substring(GROUP_PREFIX.length())); return true; }
                    catch (IllegalArgumentException ignored) { return false; }
                }).sorted().findFirst().orElse(null);
    }
    public static String group(Entity entity) { return groupId(entity.getCommandTags()); }
    public static boolean sameGroup(Entity first, Entity second) {
        String group = group(first);
        return group != null && group.equals(group(second));
    }
    public static boolean canRecruit(Entity leader, Entity member) {
        String a = group(leader), b = group(member);
        return a == null && b == null || a != null && a.equals(b);
    }
    public static void assign(Entity entity, String groupTag) {
        if (groupId(Set.of(groupTag)) == null) throw new IllegalArgumentException("Invalid patrol group");
        for (String old : Set.copyOf(entity.getCommandTags())) {
            if (old.startsWith(GROUP_PREFIX)) entity.removeCommandTag(old);
        }
        entity.addCommandTag(MEMBER_TAG);
        entity.addCommandTag(groupTag);
    }
    public static boolean isCommander(Entity entity) {
        return entity.getCommandTags().contains(COMMANDER_TAG);
    }
    private PatrolIdentity() { }
}
