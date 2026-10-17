package net.voidharbinger.entity;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.projectile.ExplosiveProjectileEntity;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.world.World;
import net.voidharbinger.registry.ModEntities;

public class VoidProjectileEntity extends ExplosiveProjectileEntity {
    public VoidProjectileEntity(EntityType<? extends ExplosiveProjectileEntity> entityType, World world) {
        super(entityType, world);
    }

    public VoidProjectileEntity(World world, LivingEntity owner, double directionX, double directionY, double directionZ) {
        super(ModEntities.VOID_PROJECTILE, owner, directionX, directionY, directionZ, world);
    }

    @Override
    protected void onEntityHit(EntityHitResult entityHitResult) {
        super.onEntityHit(entityHitResult);
        if (!this.getWorld().isClient()) {
            Entity entity = entityHitResult.getEntity();
            Entity owner = this.getOwner();
            if (entity instanceof LivingEntity living && entity != owner) {
                living.damage(this.getDamageSources().indirectMagic(this, owner), 16.0f);
                living.addStatusEffect(new StatusEffectInstance(StatusEffects.WITHER, 120, 2));
                living.addStatusEffect(new StatusEffectInstance(StatusEffects.DARKNESS, 100, 0));
            }
        }
    }

    @Override
    protected void onCollision(HitResult hitResult) {
        super.onCollision(hitResult);
        if (!this.getWorld().isClient()) {
            this.getWorld().createExplosion(this, this.getX(), this.getY(), this.getZ(), 2.0f, World.ExplosionSourceType.NONE);
            this.discard();
        }
    }

    @Override
    protected ParticleEffect getParticleType() {
        return ParticleTypes.DRAGON_BREATH;
    }

    @Override
    public boolean isOnFire() { return false; }
}
