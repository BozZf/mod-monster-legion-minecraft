package io.github.bozzf.monsterlegion.legion;

import java.util.UUID;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class LegionRosterTest {
    @Test void enforcesCapacityAndExclusiveOwnership() {
        var roster = new LegionRoster(1);
        UUID leader = UUID.randomUUID(), other = UUID.randomUUID(), member = UUID.randomUUID();
        assertFalse(roster.recruit(leader, leader));
        assertTrue(roster.recruit(leader, member));
        assertFalse(roster.recruit(other, member));
        assertFalse(roster.recruit(leader, UUID.randomUUID()));
        assertEquals(leader, roster.leaderOf(member));
    }
    @Test void releasesInvalidMembersAndFreesCapacity() {
        var roster = new LegionRoster(1);
        UUID leader = UUID.randomUUID(), member = UUID.randomUUID(), next = UUID.randomUUID();
        roster.recruit(leader, member);
        roster.retain((l, m) -> false);
        assertNull(roster.leaderOf(member));
        assertTrue(roster.recruit(leader, next));
    }
    @Test void preservesOtherLegionsWhenOneLeaderDisappears() {
        var roster = new LegionRoster(2);
        UUID a = UUID.randomUUID(), b = UUID.randomUUID(), x = UUID.randomUUID(), y = UUID.randomUUID();
        roster.recruit(a, x);
        roster.recruit(b, y);
        roster.retain((leader, member) -> !leader.equals(a));
        assertNull(roster.leaderOf(x));
        assertEquals(b, roster.leaderOf(y));
    }
}
