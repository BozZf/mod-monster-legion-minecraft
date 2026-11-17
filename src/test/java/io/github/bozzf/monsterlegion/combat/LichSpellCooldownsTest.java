package io.github.bozzf.monsterlegion.combat;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class LichSpellCooldownsTest {
    @Test void summonRequiresAFullMinuteWithoutBlockingMagic() {
        var timers = new LichSpellCooldowns();
        timers.cast("sumon");
        assertFalse(timers.canSummon());
        assertTrue(timers.canAttackMagic());
        for (int i = 0; i < 1199; i++) timers.tick();
        assertFalse(timers.canSummon());
        timers.tick();
        assertTrue(timers.canSummon());
    }
    @Test void magicRequiresTenSecondsAndOtherAnimationsDoNotResetIt() {
        var timers = new LichSpellCooldowns();
        timers.cast("attack_magic");
        for (int i = 0; i < 199; i++) timers.tick();
        timers.cast("command");
        timers.cast("hurt");
        assertFalse(timers.canAttackMagic());
        assertEquals(1, timers.magicRemaining());
        timers.tick();
        assertTrue(timers.canAttackMagic());
    }
    @Test void reloadKeepsBothIndependentDeadlines() {
        var timers = new LichSpellCooldowns();
        timers.cast("sumon");
        for (int i = 0; i < 123; i++) timers.tick();
        timers.cast("attack_magic");
        var restored = new LichSpellCooldowns();
        restored.restore(timers.summonRemaining(), timers.magicRemaining());
        for (int i = 0; i < 200; i++) restored.tick();
        assertTrue(restored.canAttackMagic());
        assertFalse(restored.canSummon());
        assertEquals(877, restored.summonRemaining());
    }
}
