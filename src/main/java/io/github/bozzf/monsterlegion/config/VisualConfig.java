package io.github.bozzf.monsterlegion.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.github.bozzf.monsterlegion.MonsterLegion;
import java.io.IOException;
import java.nio.file.Files;
import net.fabricmc.loader.api.FabricLoader;

/** Local cosmetic options, read on client startup. */
public final class VisualConfig {
    public boolean enableAnimationParticles = true;
    public boolean enableAnimationSounds = true;
    public boolean enableLichGlow = true;
    public boolean enableStaffGlow = true;

    public static VisualConfig load() {
        var path = FabricLoader.getInstance().getConfigDir().resolve("monster_legion-client.json");
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        try {
            if (Files.exists(path)) {
                VisualConfig config = gson.fromJson(Files.readString(path), VisualConfig.class);
                if (config != null) return config;
            }
            Files.createDirectories(path.getParent());
            VisualConfig config = new VisualConfig();
            Files.writeString(path, gson.toJson(config));
            return config;
        } catch (IOException | RuntimeException error) {
            MonsterLegion.LOGGER.warn("Could not load visual config; using defaults", error);
            return new VisualConfig();
        }
    }
}
