package io.github.bozzf.monsterlegion;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;
import javax.imageio.ImageIO;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class LichAssetsTest {
    private static final Path ASSETS = Path.of("src/main/resources/assets/monster_legion");
    private static JsonObject json(String path) throws Exception {
        return JsonParser.parseString(Files.readString(ASSETS.resolve(path))).getAsJsonObject();
    }
    @Test void animationBonesExistInTheSuppliedModels() throws Exception {
        for (String name : new String[]{"lich", "staff"}) {
            Set<String> bones = new HashSet<>();
            json("geo/" + name + ".geo.json").getAsJsonArray("minecraft:geometry").get(0)
                    .getAsJsonObject().getAsJsonArray("bones")
                    .forEach(bone -> bones.add(bone.getAsJsonObject().get("name").getAsString()));
            for (var animation : json("animations/" + name + ".animation.json").getAsJsonObject("animations").entrySet()) {
                assertTrue(bones.containsAll(animation.getValue().getAsJsonObject().getAsJsonObject("bones").keySet()),
                        () -> "Unknown bone in " + animation.getKey());
            }
        }
    }
    @Test void keyframesResolveToBundledSoundsAndAllowedVanillaParticles() throws Exception {
        JsonObject sounds = json("sounds.json");
        Set<String> allowed = Set.of("minecraft:sculk_soul", "minecraft:enchant", "minecraft:end_rod", "minecraft:dust_plume", "minecraft:ash");
        for (var entry : json("animations/lich.animation.json").getAsJsonObject("animations").entrySet()) {
            JsonObject animation = entry.getValue().getAsJsonObject();
            double length = animation.get("animation_length").getAsDouble();
            for (String kind : new String[]{"sound_effects", "particle_effects"}) {
                if (!animation.has(kind)) continue;
                for (var keyframe : animation.getAsJsonObject(kind).entrySet()) {
                    assertTrue(Double.parseDouble(keyframe.getKey()) <= length);
                    String effect = keyframe.getValue().getAsJsonObject().get("effect").getAsString();
                    if (kind.equals("particle_effects")) assertTrue(allowed.contains(effect));
                    else {
                        assertTrue(effect.startsWith("monster_legion:"));
                        assertTrue(sounds.has(effect.substring("monster_legion:".length())));
                    }
                }
            }
        }
        for (var entry : sounds.entrySet()) {
            String name = entry.getValue().getAsJsonObject().getAsJsonArray("sounds").get(0).getAsString();
            byte[] bytes = Files.readAllBytes(ASSETS.resolve("sounds/" + name.split(":")[1] + ".ogg"));
            assertEquals("OggS", new String(bytes, 0, 4, java.nio.charset.StandardCharsets.US_ASCII));
        }
    }
    @Test void deathEffectsKeepRequestedOrderAndFinishBeforeDespawn() throws Exception {
        JsonObject death = json("animations/lich.animation.json").getAsJsonObject("animations").getAsJsonObject("animationdeath");
        var frames = death.getAsJsonObject("particle_effects").entrySet().stream()
                .sorted(java.util.Comparator.comparingDouble(e -> Double.parseDouble(e.getKey()))).toList();
        assertEquals(2, frames.size());
        assertEquals("minecraft:dust_plume", frames.get(0).getValue().getAsJsonObject().get("effect").getAsString());
        assertEquals(death.get("animation_length").getAsDouble() / 2, Double.parseDouble(frames.get(0).getKey()), 0.001);
        assertEquals("minecraft:ash", frames.get(1).getValue().getAsJsonObject().get("effect").getAsString());
        assertTrue(Double.parseDouble(frames.get(1).getKey()) < 3);
        assertEquals(1, death.getAsJsonObject("sound_effects").size(), "Death sound must not play twice");
    }
    @Test void emissiveMasksMatchTextureDimensions() throws Exception {
        for (String texture : new String[]{"entity/rei_lich", "item/lich_staff"}) {
            var base = ImageIO.read(ASSETS.resolve("textures/" + texture + ".png").toFile());
            var mask = ImageIO.read(ASSETS.resolve("textures/" + texture + "_glowmask.png").toFile());
            assertNotNull(base);
            assertNotNull(mask);
            assertEquals(base.getWidth(), mask.getWidth());
            assertEquals(base.getHeight(), mask.getHeight());
            assertTrue(mask.getColorModel().hasAlpha());
        }
    }
}
