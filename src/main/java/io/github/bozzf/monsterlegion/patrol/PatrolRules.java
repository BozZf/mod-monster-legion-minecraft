package io.github.bozzf.monsterlegion.patrol;

/** Deterministic rules shared by spawning, rewards and tests. */
public final class PatrolRules {
    public static boolean isNight(long timeOfDay) {
        long time = Math.floorMod(timeOfDay, 24000L);
        return time >= 13000 && time < 23000;
    }
    public static int commanderXp(int baseXp) {
        return (int) Math.min(Integer.MAX_VALUE, Math.max(0L, (long) baseXp) * 3L);
    }
    private PatrolRules() { }
}
