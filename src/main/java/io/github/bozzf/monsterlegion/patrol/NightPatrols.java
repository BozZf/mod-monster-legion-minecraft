package io.github.bozzf.monsterlegion.patrol;

import io.github.bozzf.monsterlegion.legion.LegionManager;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.mob.AbstractSkeletonEntity;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.entity.mob.ZombieEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.Difficulty;
import net.minecraft.world.GameRules;
import net.minecraft.world.Heightmap;
import net.minecraft.entity.SpawnReason;
import net.minecraft.world.World;

public final class NightPatrols {
    private static PatrolConfig config = new PatrolConfig();
    private static final List<EntityType<? extends HostileEntity>> COMMANDERS =
            List.of(EntityType.ZOMBIE, EntityType.HUSK, EntityType.SKELETON, EntityType.STRAY);
    private static final List<EntityType<? extends HostileEntity>> MEMBERS =
            List.of(EntityType.ZOMBIE, EntityType.SKELETON, EntityType.HUSK, EntityType.STRAY, EntityType.SPIDER);

    public static void initialize() {
        config = PatrolConfig.load();
        CommandRegistrationCallback.EVENT.register((dispatcher, registry, environment) ->
                dispatcher.register(CommandManager.literal("monsterlegion").requires(s -> s.hasPermissionLevel(2))
                        .then(CommandManager.literal("patrol").executes(context -> {
                            var player = context.getSource().getPlayerOrThrow();
                            BlockPos pos = spawnNear(context.getSource().getWorld(), player);
                            if (pos == null) {
                                context.getSource().sendError(Text.literal(
                                        "Nenhuma posição válida: precisa ser noite, terreno escuro e área carregada no Overworld, abaixo dos limites de patrulhas."));
                                return 0;
                            }
                            context.getSource().sendFeedback(() -> Text.literal("Patrulha em "
                                    + pos.getX() + " " + pos.getY() + " " + pos.getZ()), false);
                            return 1;
                        }))));
    }
    private static boolean allowed(ServerWorld world) {
        return config.enabled && world.getRegistryKey().equals(World.OVERWORLD)
                && world.getDifficulty() != Difficulty.PEACEFUL
                && world.getGameRules().getBoolean(GameRules.DO_MOB_SPAWNING)
                && PatrolRules.isNight(world.getTimeOfDay());
    }
    public static void tick(ServerWorld world) {
        if (!allowed(world) || world.getTime() % config.attemptIntervalTicks != 0) return;
        List<ServerPlayerEntity> players = world.getPlayers().stream()
                .filter(p -> p.isAlive() && !p.isSpectator() && !p.isCreative()).toList();
        if (players.isEmpty() || world.random.nextDouble() >= config.spawnChance) return;
        spawnNear(world, players.get(world.random.nextInt(players.size())));
    }

    /** Bounded search in already loaded terrain. Also used by the operator test command. */
    public static BlockPos spawnNear(ServerWorld world, ServerPlayerEntity player) {
        if (!allowed(world)) return null;
        int loadedGroups = 0, nearbyGroups = 0, nearbyMonsters = 0;
        for (Entity entity : world.iterateEntities()) {
            if (!entity.isAlive()) continue;
            if (PatrolIdentity.isCommander(entity)) {
                loadedGroups++;
                if (entity.squaredDistanceTo(player) < 192 * 192) nearbyGroups++;
            }
            if (entity instanceof HostileEntity && entity.squaredDistanceTo(player) < 128 * 128) nearbyMonsters++;
        }
        if (loadedGroups >= config.maxLoadedGroups || nearbyGroups >= config.maxNearbyGroups || nearbyMonsters >= 64) return null;
        for (int attempt = 0; attempt < 24; attempt++) {
            double angle = world.random.nextDouble() * Math.PI * 2;
            double distance = config.minDistance + world.random.nextDouble() * (config.maxDistance - config.minDistance);
            int x = (int) Math.floor(player.getX() + Math.cos(angle) * distance);
            int z = (int) Math.floor(player.getZ() + Math.sin(angle) * distance);
            BlockPos center = surface(world, x, z);
            if (center == null || !farFromPlayers(world, center)) continue;
            BlockPos spawned = spawnGroup(world, center);
            if (spawned != null) return spawned;
        }
        return null;
    }
    private static BlockPos surface(ServerWorld world, int x, int z) {
        if (!world.isChunkLoaded(x >> 4, z >> 4)) return null;
        int y = world.getTopY(Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, x, z);
        BlockPos pos = new BlockPos(x, y, z);
        if (!world.getWorldBorder().contains(pos) || !world.isInBuildLimit(pos.up(2))) return null;
        if (!world.getBlockState(pos.down()).isSideSolidFullSquare(world, pos.down(), Direction.UP)
                || !world.getFluidState(pos).isEmpty()) return null;
        return pos;
    }
    private static boolean farFromPlayers(ServerWorld world, BlockPos pos) {
        for (ServerPlayerEntity player : world.getPlayers()) {
            if (!player.isSpectator() && player.squaredDistanceTo(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5) < 32 * 32) return false;
        }
        return true;
    }
    private static HostileEntity create(ServerWorld world, EntityType<? extends HostileEntity> type, BlockPos pos) {
        // Collision checks may touch adjacent chunks (notably for spiders). Never load them.
        for (int cx = (pos.getX() - 2) >> 4; cx <= (pos.getX() + 2) >> 4; cx++) {
            for (int cz = (pos.getZ() - 2) >> 4; cz <= (pos.getZ() + 2) >> 4; cz++) {
                if (!world.isChunkLoaded(cx, cz)) return null;
            }
        }
        if (!farFromPlayers(world, pos)
                || !HostileEntity.canSpawnInDark(type, world, SpawnReason.NATURAL, pos, world.random)) return null;
        HostileEntity mob = type.create(world);
        if (mob == null) return null;
        mob.refreshPositionAndAngles(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, world.random.nextFloat() * 360, 0);
        if (!world.isSpaceEmpty(mob) || !world.getOtherEntities(mob, mob.getBoundingBox()).isEmpty()) return null;
        // Deliberately avoid initialize(): no baby/chicken/spider jockey side spawns outside our quotas.
        if (mob instanceof ZombieEntity zombie) zombie.setBaby(false);
        if (mob instanceof AbstractSkeletonEntity) mob.equipStack(EquipmentSlot.MAINHAND, new ItemStack(Items.BOW));
        mob.setCanPickUpLoot(false);
        return mob;
    }
    private static BlockPos spawnGroup(ServerWorld world, BlockPos center) {
        var commanderType = COMMANDERS.get(world.random.nextInt(COMMANDERS.size()));
        HostileEntity commander = create(world, commanderType, center);
        if (commander == null) return null;
        String group = PatrolIdentity.GROUP_PREFIX + UUID.randomUUID();
        Commanders.equip(commander, group);
        if (!world.spawnEntity(commander)) return null;
        List<HostileEntity> spawned = new ArrayList<>();
        spawned.add(commander);
        int size = config.minGroupSize + world.random.nextInt(config.maxGroupSize - config.minGroupSize + 1);
        List<EntityType<? extends HostileEntity>> otherTypes = MEMBERS.stream().filter(t -> t != commanderType).toList();
        for (int attempt = 0; spawned.size() < size && attempt < 64; attempt++) {
            BlockPos pos = surface(world, center.getX() + world.random.nextInt(13) - 6,
                    center.getZ() + world.random.nextInt(13) - 6);
            if (pos == null || Math.abs(pos.getY() - center.getY()) > 3) continue;
            // Ensure a mixed group even when some terrain positions fail.
            var types = spawned.size() == 1 ? otherTypes : MEMBERS;
            HostileEntity member = create(world, types.get(world.random.nextInt(types.size())), pos);
            if (member == null) continue;
            PatrolIdentity.assign(member, group);
            if (!world.spawnEntity(member)) continue;
            if (!LegionManager.tryRecruit(commander, member)) {
                member.discard();
                continue;
            }
            spawned.add(member);
        }
        // No lone commanders or undersized groups left behind after failed placements.
        if (spawned.size() < config.minGroupSize) {
            spawned.forEach(Entity::discard);
            return null;
        }
        return center;
    }
    private NightPatrols() { }
}
