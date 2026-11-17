package io.github.bozzf.monsterlegion.patrol;

import java.util.Set;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class PatrolRulesTest {
    @Test void spawnsOnlyWithinNightWindowAcrossMultipleDays() {
        assertFalse(PatrolRules.isNight(0));
        assertFalse(PatrolRules.isNight(12999));
        assertTrue(PatrolRules.isNight(13000));
        assertTrue(PatrolRules.isNight(22999));
        assertFalse(PatrolRules.isNight(23000));
        assertTrue(PatrolRules.isNight(24000L * 30 + 18000));
        assertFalse(PatrolRules.isNight(24000L * 30 + 6000));
        assertTrue(PatrolRules.isNight(-6000));
    }
    @Test void triplesBaseXpWithoutEquipmentBonusesOrOverflow() {
        assertEquals(15, PatrolRules.commanderXp(5));
        assertEquals(30, PatrolRules.commanderXp(10));
        assertEquals(0, PatrolRules.commanderXp(0));
        assertEquals(0, PatrolRules.commanderXp(-1));
        assertEquals(Integer.MAX_VALUE, PatrolRules.commanderXp(Integer.MAX_VALUE));
    }
    @Test void reloadRecognizesGroupFromSavedTagsAndIgnoresMalformedTags() {
        String group = PatrolIdentity.GROUP_PREFIX + "b0caf004-f489-4d48-b7fe-623f40ebdc3b";
        assertEquals(group, PatrolIdentity.groupId(Set.of(group, PatrolIdentity.COMMANDER_TAG)));
        assertNull(PatrolIdentity.groupId(Set.of(PatrolIdentity.GROUP_PREFIX + "invalid")));
        assertNull(PatrolIdentity.groupId(Set.of(PatrolIdentity.MEMBER_TAG)));
    }
    @Test void clampsInvalidSpawnConfigurationToBoundedWork() {
        var config = new PatrolConfig();
        config.attemptIntervalTicks = -1;
        config.spawnChance = Double.NaN;
        config.minGroupSize = 100;
        config.maxGroupSize = -4;
        config.minDistance = 0;
        config.maxDistance = 999999;
        config.maxLoadedGroups = 0;
        config.maxNearbyGroups = 10000;
        config.sanitize();
        assertEquals(20, config.attemptIntervalTicks);
        assertEquals(0.5, config.spawnChance);
        assertEquals(13, config.minGroupSize);
        assertEquals(13, config.maxGroupSize);
        assertEquals(32, config.minDistance);
        assertEquals(112, config.maxDistance);
        assertEquals(1, config.maxLoadedGroups);
        assertEquals(1, config.maxNearbyGroups);
    }
    @Test void creativeCanSpawnPatrolsButSpectatorsAndDeadPlayersCannot() {
        var config = new PatrolConfig();
        assertTrue(config.eligiblePlayer(true, false, true));
        assertTrue(config.eligiblePlayer(true, false, false));
        assertFalse(config.eligiblePlayer(true, true, false));
        assertFalse(config.eligiblePlayer(false, false, true));
        config.spawnInCreative = false;
        assertFalse(config.eligiblePlayer(true, false, true));
        assertTrue(config.eligiblePlayer(true, false, false));
    }
    @Test void legacyDefaultsUpgradeButCustomFrequencyRemainsUntouched() {
        var legacy = new PatrolConfig();
        legacy.attemptIntervalTicks = 600;
        legacy.spawnChance = 0.25;
        legacy.upgradeLegacyDefaults();
        assertEquals(400, legacy.attemptIntervalTicks);
        assertEquals(0.5, legacy.spawnChance);
        var custom = new PatrolConfig();
        custom.attemptIntervalTicks = 1200;
        custom.spawnChance = 0.1;
        custom.upgradeLegacyDefaults();
        assertEquals(1200, custom.attemptIntervalTicks);
        assertEquals(0.1, custom.spawnChance);
    }

}
