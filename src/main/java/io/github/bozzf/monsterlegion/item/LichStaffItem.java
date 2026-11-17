package io.github.bozzf.monsterlegion.item;

import io.github.bozzf.monsterlegion.combat.Summoning;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
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
        if (player instanceof ServerPlayerEntity serverPlayer) {
            String error = Summoning.byPlayer(serverPlayer);
            if (error != null) {
                player.sendMessage(Text.literal(error), true);
                return TypedActionResult.fail(stack);
            }
            player.getItemCooldownManager().set(this, 20);
        }
        return TypedActionResult.success(stack, world.isClient);
    }
}
