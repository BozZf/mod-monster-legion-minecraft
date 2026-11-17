package io.github.bozzf.monsterlegion.mixin;

import io.github.bozzf.monsterlegion.combat.SummonData;
import io.github.bozzf.monsterlegion.combat.Summoning;
import java.util.UUID;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.nbt.NbtCompound;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MobEntity.class)
public abstract class SummonedMobMixin implements SummonData {
    @Unique private UUID monsterLegion$lich;
    @Unique private UUID monsterLegion$player;
    @Unique private long monsterLegion$expires;
    @Unique private static final TrackedData<Boolean> monsterLegion$CONJURED =
            DataTracker.registerData(MobEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
    public UUID monsterLegion$lichOwner() { return monsterLegion$lich; }
    public void monsterLegion$lichOwner(UUID value) { monsterLegion$lich = value; }
    public UUID monsterLegion$playerOwner() { return monsterLegion$player; }
    public void monsterLegion$playerOwner(UUID value) { monsterLegion$player = value; }
    public long monsterLegion$expiresAt() { return monsterLegion$expires; }
    public void monsterLegion$expiresAt(long value) { monsterLegion$expires = value; }
    public boolean monsterLegion$conjured() {
        MobEntity mob = (MobEntity) (Object) this;
        return mob.getDataTracker() != null && mob.getDataTracker().get(monsterLegion$CONJURED);
    }
    public void monsterLegion$conjured(boolean value) {
        ((MobEntity) (Object) this).getDataTracker().set(monsterLegion$CONJURED, value);
    }
    @Inject(method = "initDataTracker", at = @At("TAIL"))
    private void monsterLegion$trackConjured(DataTracker.Builder builder, CallbackInfo ci) {
        builder.add(monsterLegion$CONJURED, false);
    }
    @Inject(method = "writeCustomDataToNbt", at = @At("TAIL"))
    private void monsterLegion$save(NbtCompound nbt, CallbackInfo ci) {
        NbtCompound data = new NbtCompound();
        if (monsterLegion$lich != null) data.putUuid("Lich", monsterLegion$lich);
        if (monsterLegion$player != null) data.putUuid("Player", monsterLegion$player);
        data.putLong("Expires", monsterLegion$expires);
        data.putBoolean("Conjured", monsterLegion$conjured());
        nbt.put("MonsterLegionSummon", data);
    }
    @Inject(method = "readCustomDataFromNbt", at = @At("TAIL"))
    private void monsterLegion$load(NbtCompound nbt, CallbackInfo ci) {
        NbtCompound data = nbt.getCompound("MonsterLegionSummon");
        monsterLegion$lich = data.containsUuid("Lich") ? data.getUuid("Lich") : null;
        monsterLegion$player = data.containsUuid("Player") ? data.getUuid("Player") : null;
        monsterLegion$expires = data.getLong("Expires");
        monsterLegion$conjured(data.getBoolean("Conjured"));
    }
    @Inject(method = "convertTo", at = @At("RETURN"))
    private <T extends MobEntity> void monsterLegion$convert(EntityType<T> type, boolean keepEquipment,
            CallbackInfoReturnable<T> cir) {
        Summoning.onConversion((MobEntity) (Object) this, cir.getReturnValue());
    }
}
