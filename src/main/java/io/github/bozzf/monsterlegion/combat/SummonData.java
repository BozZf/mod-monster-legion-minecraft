package io.github.bozzf.monsterlegion.combat;

import java.util.UUID;

public interface SummonData {
    UUID monsterLegion$lichOwner();
    void monsterLegion$lichOwner(UUID owner);
    UUID monsterLegion$playerOwner();
    void monsterLegion$playerOwner(UUID owner);
    long monsterLegion$expiresAt();
    void monsterLegion$expiresAt(long time);
    boolean monsterLegion$conjured();
    void monsterLegion$conjured(boolean value);
}
