package net.voidharbinger.item;

import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.SwordItem;
import net.minecraft.item.ToolMaterial;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.voidharbinger.registry.ModSounds;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class VoidScytheItem extends SwordItem {
    public VoidScytheItem(ToolMaterial toolMaterial, int attackDamage, float attackSpeed, Settings settings) {
        super(toolMaterial, attackDamage, attackSpeed, settings);
    }

    @Override
    public boolean postHit(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        target.addStatusEffect(new StatusEffectInstance(StatusEffects.WITHER, 100, 2));
        target.addStatusEffect(new StatusEffectInstance(StatusEffects.GLOWING, 60, 0));
        return super.postHit(stack, target, attacker);
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack stack = user.getStackInHand(hand);

        if (!world.isClient()) {
            // Blink forward 12 blocks with devastating radial cleave
            Vec3d look = user.getRotationVector().multiply(12.0);
            double targetX = user.getX() + look.x;
            double targetY = user.getY() + look.y;
            double targetZ = user.getZ() + look.z;

            // Teleport
            user.teleport(targetX, targetY, targetZ, true);
            user.getItemCooldownManager().set(this, 120); // 6 sec cooldown

            world.playSound(null, targetX, targetY, targetZ, ModSounds.SCYTHE_WARP, SoundCategory.PLAYERS, 2.0f, 1.1f);

            if (world instanceof ServerWorld sw) {
                sw.spawnParticles(ParticleTypes.REVERSE_PORTAL, targetX, targetY + 1.0, targetZ, 60, 1.5, 1.5, 1.5, 0.2);
                sw.spawnParticles(ParticleTypes.SWEEP_ATTACK, targetX, targetY + 1.0, targetZ, 8, 2.0, 0.5, 2.0, 0.0);
            }

            // Radial Cleave Damage (40 pure damage to nearby enemies)
            List<LivingEntity> enemies = world.getEntitiesByClass(
                    LivingEntity.class,
                    new Box(targetX - 6, targetY - 3, targetZ - 6, targetX + 6, targetY + 3, targetZ + 6),
                    e -> e != user && e.isAlive()
            );

            for (LivingEntity enemy : enemies) {
                enemy.damage(user.getDamageSources().playerAttack(user), 40.0f);
                enemy.addStatusEffect(new StatusEffectInstance(StatusEffects.DARKNESS, 80, 0));
                enemy.takeKnockback(1.5, user.getX() - enemy.getX(), user.getZ() - enemy.getZ());
            }
        }

        return TypedActionResult.success(stack, world.isClient());
    }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
        tooltip.add(Text.translatable("item.void_harbinger.void_scythe.desc_1").formatted(Formatting.DARK_PURPLE, Formatting.ITALIC));
        tooltip.add(Text.translatable("item.void_harbinger.void_scythe.desc_2").formatted(Formatting.LIGHT_PURPLE));
        tooltip.add(Text.translatable("item.void_harbinger.void_scythe.desc_3").formatted(Formatting.GOLD));
        super.appendTooltip(stack, world, tooltip, context);
    }
}
