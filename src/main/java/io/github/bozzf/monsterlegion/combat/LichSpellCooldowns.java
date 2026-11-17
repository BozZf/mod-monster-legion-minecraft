package io.github.bozzf.monsterlegion.combat;

/** Server-tick cooldowns; ability timers advance independently. */
public final class LichSpellCooldowns {
    public static final int SUMMON_TICKS = 60 * 20;
    public static final int MAGIC_TICKS = 10 * 20;
    private int summon;
    private int magic;
    public boolean canSummon() { return summon == 0; }
    public boolean canAttackMagic() { return magic == 0; }
    public int summonRemaining() { return summon; }
    public int magicRemaining() { return magic; }
    public void cast(String spell) {
        if (spell.equals("sumon")) summon = SUMMON_TICKS;
        else if (spell.equals("attack_magic")) magic = MAGIC_TICKS;
    }
    public void tick() {
        if (summon > 0) summon--;
        if (magic > 0) magic--;
    }
    public void restore(int summon, int magic) {
        this.summon = Math.max(0, Math.min(SUMMON_TICKS, summon));
        this.magic = Math.max(0, Math.min(MAGIC_TICKS, magic));
    }
}
