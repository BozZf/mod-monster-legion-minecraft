package io.github.bozzf.monsterlegion.item;

import io.github.bozzf.monsterlegion.entity.LichBoltEntity;
import io.github.bozzf.monsterlegion.registry.ModContent;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;

public final class LichStaffItem extends Item implements GeoItem {
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    private static final RawAnimation ROTATE = RawAnimation.begin().thenLoop("rotação");
    public LichStaffItem(Settings settings) { super(settings); }
    @Override public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "rotation", 0, state -> state.setAndContinue(ROTATE)));
    }
    @Override public AnimatableInstanceCache getAnimatableInstanceCache() { return cache; }
    @Override public TypedActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {
        ItemStack stack = player.getStackInHand(hand);
        if (!world.isClient) {
            LichBoltEntity bolt = new LichBoltEntity(world, player);
            bolt.setVelocity(player, player.getPitch(), player.getYaw(), 0, 1.5F, 0.5F);
            world.spawnEntity(bolt);
            world.playSound(null, player.getX(), player.getY(), player.getZ(), ModContent.SOUNDS.get("magic_bolt"),
                    SoundCategory.PLAYERS, 1, 1);
            player.getItemCooldownManager().set(this, 30);
        }
        return TypedActionResult.success(stack, world.isClient);
    }
}
