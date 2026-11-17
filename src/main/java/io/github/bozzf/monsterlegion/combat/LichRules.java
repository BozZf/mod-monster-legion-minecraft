package io.github.bozzf.monsterlegion.combat;

public final class LichRules {
    public static final float HEALTH = 200;
    public static final float SKULL_DAMAGE = 8;
    public static final int EFFECT_TICKS = 200;
    public static final int SUMMON_TICKS = 6000;
    public static final int XP = 12000;
    public static final float STAFF_CHANCE = 0.25F;
    public static final double ALLY_RADIUS = 32;
    public static float dragonBodyDamage(float damage) {
        return damage <= 0 ? 0 : damage / 4F + Math.min(damage, 1F);
    }
    private LichRules() { }
}
