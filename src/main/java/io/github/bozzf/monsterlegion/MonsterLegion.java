package io.github.bozzf.monsterlegion;

import io.github.bozzf.monsterlegion.legion.LegionManager;
import io.github.bozzf.monsterlegion.legion.FollowLeaderGoal;
import io.github.bozzf.monsterlegion.mixin.MobEntityAccessor;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.entity.mob.PathAwareEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class MonsterLegion implements ModInitializer {
    public static final String MOD_ID = "monster_legion";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        ServerEntityEvents.ENTITY_LOAD.register((entity, world) -> {
            if (entity instanceof PathAwareEntity mob) {
                ((MobEntityAccessor) mob).monsterLegion$getGoalSelector()
                        .add(2, new FollowLeaderGoal(mob));
            }
        });
        ServerTickEvents.END_WORLD_TICK.register(LegionManager::tick);
        ServerLifecycleEvents.SERVER_STOPPED.register(server -> LegionManager.clear());
        LOGGER.info("Monster Legion initialized");
    }
}
