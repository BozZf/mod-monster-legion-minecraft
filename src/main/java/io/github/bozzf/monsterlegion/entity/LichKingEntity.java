package io.github.bozzf.monsterlegion.entity;

import io.github.bozzf.monsterlegion.legion.LegionManager;
import io.github.bozzf.monsterlegion.registry.ModContent;
import java.util.EnumSet;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.control.FlightMoveControl;
import net.minecraft.entity.ai.goal.ActiveTargetGoal;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.ai.goal.LookAroundGoal;
import net.minecraft.entity.ai.goal.LookAtEntityGoal;
import net.minecraft.entity.ai.goal.RevengeGoal;
import net.minecraft.entity.ai.pathing.BirdNavigation;
import net.minecraft.entity.ai.pathing.EntityNavigation;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.mob.ZombieEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.sound.SoundEvent;
import net.minecraft.world.World;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;

public final class LichKingEntity extends HostileEntity implements GeoEntity {
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    private static final RawAnimation IDLE = RawAnimation.begin().thenLoop("idle_float");
    private static final RawAnimation FLY = RawAnimation.begin().thenLoop("fly_move");
    private static final RawAnimation DEATH = RawAnimation.begin().thenPlayAndHold("animationdeath");
    private int castingTicks;
    private int spellCooldown = 40;
    private int summonCooldown;
    private int commandCooldown = 160;
    private int previewTicks;
    private String spell = "";

    public LichKingEntity(EntityType<? extends LichKingEntity> type, World world) {
        super(type, world);
        moveControl = new FlightMoveControl(this, 20, true);
        setNoGravity(true);
        addCommandTag(LegionManager.LEADER_TAG);
        experiencePoints = 30;
    }
    public static DefaultAttributeContainer.Builder attributes() {
        return HostileEntity.createHostileAttributes()
                .add(EntityAttributes.GENERIC_MAX_HEALTH, 120)
                .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.23)
                .add(EntityAttributes.GENERIC_FLYING_SPEED, 0.3)
                .add(EntityAttributes.GENERIC_FOLLOW_RANGE, 32)
                .add(EntityAttributes.GENERIC_ARMOR, 6);
    }
    @Override protected EntityNavigation createNavigation(World world) {
        return new BirdNavigation(this, world);
    }
    @Override protected void initGoals() {
        goalSelector.add(1, new SpellGoal());
        goalSelector.add(7, new LookAtEntityGoal(this, PlayerEntity.class, 12));
        goalSelector.add(8, new LookAroundGoal(this));
        targetSelector.add(1, new RevengeGoal(this));
        targetSelector.add(2, new ActiveTargetGoal<>(this, PlayerEntity.class, true));
    }
    @Override public boolean canTarget(LivingEntity target) {
        return !LegionManager.isLegionMember(this, target) && super.canTarget(target);
    }
    @Override public void tick() {
        super.tick();
        if (getWorld().isClient || !isAlive()) return;
        if (previewTicks > 0 && --previewTicks == 0) stopTriggeredAnim("main", null);
        if (summonCooldown > 0) summonCooldown--;
        if (commandCooldown > 0) commandCooldown--;
        if (spellCooldown > 0) spellCooldown--;
        if (castingTicks > 0) {
            castingTicks--;
            if (castingTicks == 24 && spell.equals("attack_magic")) shoot();
            if (castingTicks == 24 && spell.equals("sumon")) summonGuards();
        }
    }
    private void beginSpell(String name, int duration) {
        spell = name;
        castingTicks = duration;
        spellCooldown = duration + 20;
        getNavigation().stop();
        setVelocity(getVelocity().multiply(0.2));
        triggerAnim("main", name);
    }
    private void shoot() {
        LivingEntity target = getTarget();
        if (target == null || !target.isAlive() || !canSee(target) || !canTarget(target)) return;
        LichBoltEntity bolt = new LichBoltEntity(getWorld(), this);
        bolt.setPosition(getX(), getEyeY() - 0.3, getZ());
        bolt.setVelocity(target.getX() - bolt.getX(), target.getBodyY(0.5) - bolt.getY(),
                target.getZ() - bolt.getZ(), 1.1F, 0.5F);
        getWorld().spawnEntity(bolt);
    }
    private void summonGuards() {
        if (!(getWorld() instanceof net.minecraft.server.world.ServerWorld world)) return;
        for (int side : new int[]{-1, 1}) {
            ZombieEntity guard = EntityType.ZOMBIE.create(world);
            if (guard == null) continue;
            guard.refreshPositionAndAngles(getX() + side * 2, getY(), getZ() + 2, getYaw(), 0);
            if (!world.isSpaceEmpty(guard) || !LegionManager.tryRecruit(this, guard)) continue;
            guard.setTarget(getTarget());
            world.spawnEntity(guard);
        }
    }
    public void previewAnimation(String name) {
        if (getWorld().isClient || !isAlive()) return;
        stopTriggeredAnim("main", null);
        triggerAnim("main", name.equals("summon") ? "sumon" : name);
        previewTicks = 60;
    }
    @Override public boolean damage(DamageSource source, float amount) {
        boolean damaged = super.damage(source, amount);
        if (damaged && !getWorld().isClient && isAlive() && castingTicks == 0 && hurtTime == 10) {
            triggerAnim("main", "hurt");
        }
        return damaged;
    }
    @Override public void onDeath(DamageSource source) {
        super.onDeath(source);
        if (!getWorld().isClient) stopTriggeredAnim("main", null);
    }
    @Override protected SoundEvent getHurtSound(DamageSource source) { return null; }
    @Override protected SoundEvent getDeathSound() { return null; }
    @Override protected void updatePostDeath() {
        // Keep the entity for the whole 2.7917 s animation, then use vanilla cleanup.
        if (deathTime < 59) deathTime++;
        else {
            deathTime = 19;
            super.updatePostDeath();
        }
    }
    @Override public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        AnimationController<LichKingEntity> controller = new AnimationController<>(this, "main", 2,
                state -> state.setAndContinue(isDead() ? DEATH : state.isMoving() ? FLY : IDLE));
        for (String name : new String[]{"sumon", "command", "attack_magic", "hurt", "animationdeath", "idle_float", "fly_move"}) {
            controller.triggerableAnim(name, RawAnimation.begin().thenPlay(name));
        }
        controller.setParticleKeyframeHandler(event ->
                LichEffects.handler.particle(this, event.getKeyframeData().getEffect()));
        controller.setSoundKeyframeHandler(event ->
                LichEffects.handler.sound(this, event.getKeyframeData().getSound()));
        controllers.add(controller);
    }
    @Override public AnimatableInstanceCache getAnimatableInstanceCache() { return cache; }

    private final class SpellGoal extends Goal {
        SpellGoal() { setControls(EnumSet.of(Control.MOVE, Control.LOOK)); }
        @Override public boolean canStart() { return getTarget() != null && getTarget().isAlive(); }
        @Override public boolean shouldContinue() { return canStart(); }
        @Override public void stop() { getNavigation().stop(); }
        @Override public void tick() {
            LivingEntity target = getTarget();
            if (target == null || !canTarget(target)) { setTarget(null); return; }
            getLookControl().lookAt(target, 20, 20);
            if (castingTicks > 0) return;
            if (squaredDistanceTo(target) > 144 || !canSee(target)) {
                getNavigation().startMovingTo(target.getX(), target.getY() + 1, target.getZ(), 1.0);
            } else {
                getNavigation().stop();
                if (spellCooldown > 0) return;
                if (summonCooldown == 0) {
                    beginSpell("sumon", 49);
                    summonCooldown = 300;
                } else if (commandCooldown == 0) {
                    beginSpell("command", 32);
                    commandCooldown = 200;
                } else beginSpell("attack_magic", 49);
            }
        }
    }
}
