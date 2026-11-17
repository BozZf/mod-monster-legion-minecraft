package io.github.bozzf.monsterlegion.combat;

import java.util.UUID;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class LichCombatTest {
    @Test void dragonBodyReductionAndSkullNumbersAreFixed() {
        assertEquals(3, LichRules.dragonBodyDamage(8));
        assertEquals(6, LichRules.dragonBodyDamage(20));
        assertEquals(0.625F, LichRules.dragonBodyDamage(0.5F));
        assertEquals(0, LichRules.dragonBodyDamage(0));
        assertEquals(200, LichRules.HEALTH);
        assertEquals(12000, LichRules.XP);
        assertEquals(8, LichRules.SKULL_DAMAGE);
        assertEquals(200, LichRules.EFFECT_TICKS);
        assertEquals(6000, LichRules.SUMMON_TICKS);
        assertEquals(0.25F, LichRules.STAFF_CHANCE);
    }
    @Test void healingLastsTenSecondsAndNeverCreatesExtraHealthBudget() {
        HealingBudget budget = new HealingBudget();
        budget.add(8);
        float healed = 0;
        for (int i = 0; i < 100; i++) healed += budget.tick();
        assertEquals(4, healed, 0.001);
        for (int i = 0; i < 100; i++) healed += budget.tick();
        assertEquals(8, healed, 0.001);
        assertEquals(0, budget.tick());
    }
    @Test void hitsHaveIndependentWindowsAndSaveReloadKeepsRemainingBudget() {
        HealingBudget budget = new HealingBudget();
        budget.add(8);
        float healed = 0;
        for (int i = 0; i < 100; i++) healed += budget.tick();
        budget.add(4);
        HealingBudget restored = new HealingBudget();
        for (var entry : budget.snapshot()) restored.restore(entry.remaining(), entry.ticks());
        for (int i = 0; i < 200; i++) healed += restored.tick();
        assertEquals(12, healed, 0.001);
        assertEquals(0, restored.tick());
    }
    @Test void invalidOrBlockedHitsDoNotHeal() {
        HealingBudget budget = new HealingBudget();
        budget.add(0);
        budget.add(-1);
        budget.add(Float.NaN);
        assertTrue(budget.snapshot().isEmpty());
    }
    @Test void onePlayerCannotCreateSecondSummonAndOtherPlayersRemainIndependent() {
        SummonSlots slots = new SummonSlots();
        UUID a = UUID.randomUUID(), b = UUID.randomUUID(), first = UUID.randomUUID();
        assertTrue(slots.reserve(a, first, 6000));
        assertFalse(slots.reserve(a, UUID.randomUUID(), 7000));
        assertTrue(slots.reserve(b, UUID.randomUUID(), 6000));
        slots.remove(a, UUID.randomUUID());
        assertNotNull(slots.get(a));
        slots.remove(a, first);
        assertNull(slots.get(a));
        assertNotNull(slots.get(b));
    }
    @Test void conversionTransfersTheSlotWithoutResettingExpiry() {
        SummonSlots slots = new SummonSlots();
        UUID owner = UUID.randomUUID(), old = UUID.randomUUID(), converted = UUID.randomUUID();
        slots.reserve(owner, old, 6100);
        slots.transfer(owner, old, converted);
        assertEquals(converted, slots.get(owner).entity());
        assertEquals(6100, slots.get(owner).expiresAt());
        slots.remove(owner, old);
        assertNotNull(slots.get(owner));
    }
}
