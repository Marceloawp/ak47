package com.voidsterror.ak47;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.PrimedTnt;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class AK47Item extends Item {

    private static final int FIRE_RATE_TICKS = 4;  // 1 tiro a cada 4 ticks (5 tiros/s)
    private static final double SPEED = 1.6D;      // velocidade da TNT
    private static final int FUSE = 40;            // 40 ticks = 2 segundos

    public AK47Item(Properties properties) {
        super(properties);
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.NONE;
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity entity) {
        return 72000;
    }

    // Segurar o botao direito = tiro automatico
    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        player.startUsingItem(hand);
        return InteractionResultHolder.consume(player.getItemInHand(hand));
    }

    @Override
    public void onUseTick(Level level, LivingEntity entity, ItemStack stack, int remainingUseDuration) {
        if (level.isClientSide() || !(entity instanceof Player player)) return;

        int used = getUseDuration(stack, entity) - remainingUseDuration;
        if (used % FIRE_RATE_TICKS != 0) return;

        // Gasta 1 TNT do inventario (de graca no criativo)
        if (!player.getAbilities().instabuild && !consumeTnt(player)) {
            return;
        }

        Vec3 look = player.getLookAngle();
        double x = player.getX() + look.x * 0.8D;
        double y = player.getEyeY() - 0.2D + look.y * 0.8D;
        double z = player.getZ() + look.z * 0.8D;

        PrimedTnt tnt = new PrimedTnt(level, x, y, z, player);
        tnt.setFuse(FUSE);
        tnt.setDeltaMovement(look.scale(SPEED));
        level.addFreshEntity(tnt);
    }

    private boolean consumeTnt(Player player) {
        for (ItemStack s : player.getInventory().items) {
            if (s.is(Items.TNT)) {
                s.shrink(1);
                return true;
            }
        }
        return false;
    }
}
