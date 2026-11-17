package io.github.bozzf.monsterlegion.patrol;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.github.bozzf.monsterlegion.MonsterLegion;
import java.nio.file.Files;
import java.io.IOException;
import net.fabricmc.loader.api.FabricLoader;

public final class PatrolConfig {
    public boolean enabled = true;
    public int attemptIntervalTicks = 600;
    public double spawnChance = 0.25;
    public int minGroupSize = 5;
    public int maxGroupSize = 8;
    public int minDistance = 48;
    public int maxDistance = 96;
    public int maxLoadedGroups = 8;
    public int maxNearbyGroups = 2;

    public void sanitize() {
        attemptIntervalTicks = Math.max(20, Math.min(72000, attemptIntervalTicks));
        if (!Double.isFinite(spawnChance)) spawnChance = 0.25;
        spawnChance = Math.max(0, Math.min(1, spawnChance));
        minGroupSize = Math.max(3, Math.min(13, minGroupSize));
        maxGroupSize = Math.max(minGroupSize, Math.min(13, maxGroupSize));
        minDistance = Math.max(32, Math.min(96, minDistance));
        maxDistance = Math.max(minDistance, Math.min(112, maxDistance));
        maxLoadedGroups = Math.max(1, Math.min(32, maxLoadedGroups));
        maxNearbyGroups = Math.max(1, Math.min(maxLoadedGroups, maxNearbyGroups));
    }
    public static PatrolConfig load() {
        var path = FabricLoader.getInstance().getConfigDir().resolve("monster_legion-patrols.json");
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        PatrolConfig config = new PatrolConfig();
        try {
            if (Files.exists(path)) {
                PatrolConfig loaded = gson.fromJson(Files.readString(path), PatrolConfig.class);
                if (loaded != null) config = loaded;
            } else {
                Files.createDirectories(path.getParent());
                Files.writeString(path, gson.toJson(config));
            }
        } catch (IOException | RuntimeException error) {
            MonsterLegion.LOGGER.warn("Could not load patrol config; using defaults", error);
        }
        config.sanitize();
        return config;
    }
}
