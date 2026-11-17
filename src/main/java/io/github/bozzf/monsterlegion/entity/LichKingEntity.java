package io.github.bozzf.monsterlegion.entity;

import io.github.bozzf.monsterlegion.legion.LegionManager;
import io.github.bozzf.monsterlegion.combat.HealingBudget;
import io.github.bozzf.monsterlegion.combat.LichRules;
import io.github.bozzf.monsterlegion.combat.LichAllies;
import io.github.bozzf.monsterlegion.combat.Summoning;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.damage.DamageTypes;
import net.minecraft.registry.tag.DamageTypeTags;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtElement;
import net.minecraft.util.math.Vec3d;
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
    private final HealingBudget healing = new HealingBudget();
    private int castingTicks;
    private int spellCooldown = 40;
    private final io.github.bozzf.monsterlegion.combat.LichSpellCooldowns abilityCooldowns =
            new io.github.bozzf.monsterlegion.combat.LichSpellCooldowns();
    private int commandCooldown = 160;
    private int previewTicks;
    private String spell = "";

    public LichKingEntity(EntityType<? extends LichKingEntity> type, World world) {
        super(type, world);
        moveControl = new FlightMoveControl(this, 20, true);
        setNoGravity(true);
        setPersistent();
        addCommandTag(LegionManager.LEADER_TAG);
        experiencePoints = LichRules.XP;
    }
    public static DefaultAttributeContainer.Builder attributes() {
        return HostileEntity.createHostileAttributes()
                .add(EntityAttributes.GENERIC_MAX_HEALTH, LichRules.HEALTH)
                .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.23)
                .add(EntityAttributes.GENERIC_FLYING_SPEED, 0.3)
                .add(EntityAttributes.GENERIC_FOLLOW_RANGE, 32)
                .add(EntityAttributes.GENERIC_ARMOR, 0)
                .add(EntityAttributes.GENERIC_ARMOR_TOUGHNESS, 0)
                .add(EntityAttributes.GENERIC_KNOCKBACK_RESISTANCE, 1);
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
        if (isAffectedByDaylight()) setOnFireFor(8);
        heal(healing.tick());
        if (age % 20 == 0) LichAllies.recruitNearby(this);
        if (previewTicks > 0 && --previewTicks == 0) stopTriggeredAnim("main", null);
        abilityCooldowns.tick();
        if (commandCooldown > 0) commandCooldown--;
        if (spellCooldown > 0) spellCooldown--;
        if (castingTicks > 0) {
            castingTicks--;
            if (castingTicks == 24 && spell.equals("attack_magic")) shoot();
            if (castingTicks == 24 && spell.equals("sumon")) summonGuards();
            if (castingTicks == 16 && spell.equals("command")) commandArmy();
        }
    }
    private void beginSpell(String name, int duration) {
        spell = name;
        abilityCooldowns.cast(name);
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
    private void summonGuards() { Summoning.byLich(this); }
    private void commandArmy() {
        if (!(getWorld() instanceof net.minecraft.server.world.ServerWorld world)) return;
        var players = world.getPlayers().stream()
                .filter(player -> player.isAlive() && !player.isCreative() && !player.isSpectator()
                        && squaredDistanceTo(player) <= LichRules.ALLY_RADIUS * LichRules.ALLY_RADIUS).toList();
        if (players.isEmpty()) return;
        PlayerEntity chosen = players.get(random.nextInt(players.size()));
        setTarget(chosen);
        LichAllies.recruitNearby(this);
        for (net.minecraft.entity.mob.MobEntity member : world.getEntitiesByClass(
                net.minecraft.entity.mob.MobEntity.class, getBoundingBox().expand(LichRules.ALLY_RADIUS),
                mob -> LichAllies.leader(mob) == this && squaredDistanceTo(mob) <= LichRules.ALLY_RADIUS * LichRules.ALLY_RADIUS)) {
            member.setTarget(chosen);
        }
    }
    public void healFromHit(float damage) { if (isAlive()) healing.add(damage); }
    @Override protected int getXpToDrop() { return LichRules.XP; }
    @Override protected boolean shouldAlwaysDropXp() { return true; }
    @Override protected void dropLoot(DamageSource source, boolean causedByPlayer) {
        dropStack(new ItemStack(Items.BONE, 10));
        if (random.nextFloat() < LichRules.STAFF_CHANCE) dropStack(new ItemStack(ModContent.LICH_STAFF));
    }
    @Override public boolean canHaveStatusEffect(StatusEffectInstance effect) { return false; }

    @Override public void writeCustomDataToNbt(NbtCompound nbt) {
        super.writeCustomDataToNbt(nbt);
        NbtList windows = new NbtList();
        for (var window : healing.snapshot()) {
            NbtCompound entry = new NbtCompound();
            entry.putFloat("Amount", window.remaining());
            entry.putInt("Ticks", window.ticks());
            windows.add(entry);
        }
        nbt.put("LichHealing", windows);
        nbt.putInt("LichCombatVersion", 1);
        nbt.putInt("LichSummonCooldown", abilityCooldowns.summonRemaining());
        nbt.putInt("LichMagicCooldown", abilityCooldowns.magicRemaining());
    }
    @Override public void readCustomDataFromNbt(NbtCompound nbt) {
        super.readCustomDataFromNbt(nbt);
        setPersistent();
        if (nbt.getInt("LichCombatVersion") < 1) {
            float ratio = getMaxHealth() > 0 ? getHealth() / getMaxHealth() : 1;
            getAttributeInstance(EntityAttributes.GENERIC_MAX_HEALTH).setBaseValue(LichRules.HEALTH);
            getAttributeInstance(EntityAttributes.GENERIC_ARMOR).setBaseValue(0);
            getAttributeInstance(EntityAttributes.GENERIC_ARMOR_TOUGHNESS).setBaseValue(0);
            getAttributeInstance(EntityAttributes.GENERIC_KNOCKBACK_RESISTANCE).setBaseValue(1);
            setHealth(ratio * getMaxHealth());
        }
        abilityCooldowns.restore(nbt.getInt("LichSummonCooldown"), nbt.getInt("LichMagicCooldown"));
        healing.clear();
        NbtList windows = nbt.getList("LichHealing", NbtElement.COMPOUND_TYPE);
        for (int i = 0; i < windows.size(); i++) {
            var entry = windows.getCompound(i);
            healing.restore(entry.getFloat("Amount"), entry.getInt("Ticks"));
        }
    }
    public void previewAnimation(String name) {
        if (getWorld().isClient || !isAlive()) return;
        stopTriggeredAnim("main", null);
        triggerAnim("main", name.equals("summon") ? "sumon" : name);
        previewTicks = 60;
    }
    @Override public boolean damage(DamageSource source, float amount) {
        if (isInvulnerableTo(source)) return false;
        boolean bypass = source.isIn(DamageTypeTags.BYPASSES_INVULNERABILITY);
        boolean fire = source.isOf(DamageTypes.ON_FIRE) || source.isOf(DamageTypes.IN_FIRE)
                || source.isOf(DamageTypes.LAVA);
        if (!bypass && !fire) {
            if (!(source.getAttacker() instanceof PlayerEntity)
                    && !source.isIn(DamageTypeTags.ALWAYS_HURTS_ENDER_DRAGONS)) return false;
            if (!headHit(source)) amount = LichRules.dragonBodyDamage(amount);
        }
        boolean damaged = super.damage(source, amount);
        if (damaged && !getWorld().isClient && isAlive() && castingTicks == 0 && hurtTime == 10) {
            triggerAnim("main", "hurt");
        }
        return damaged;
    }
    private boolean headHit(DamageSource source) {
        Entity direct = source.getSource();
        if (direct instanceof ProjectileEntity) return direct.getY() >= getY() + 2.7;
        if (source.getAttacker() instanceof LivingEntity attacker) {
            Vec3d eye = attacker.getEyePos();
            Vec3d end = eye.add(attacker.getRotationVec(1).multiply(6));
            return getBoundingBox().raycast(eye, end).map(pos -> pos.y >= getY() + 2.7).orElse(false);
        }
        return false;
    }
    @Override public void onDeath(DamageSource source) {
        super.onDeath(source);
        if (!getWorld().isClient) {
            stopTriggeredAnim("main", null);
            Summoning.onLichDeath(this);
            LichAllies.release(this);
        }
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
                if (abilityCooldowns.canSummon()) {
                    beginSpell("sumon", 49);
                } else if (commandCooldown == 0) {
                    beginSpell("command", 32);
                    commandCooldown = 200;
                } else if (abilityCooldowns.canAttackMagic()) beginSpell("attack_magic", 49);
            }
        }
    }
}
