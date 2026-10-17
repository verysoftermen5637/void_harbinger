package net.voidharbinger.entity;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.*;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.boss.BossBar;
import net.minecraft.entity.boss.ServerBossBar;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.DamageTypes;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.voidharbinger.registry.ModSounds;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.EnumSet;
import java.util.List;

public class VoidHarbingerEntity extends HostileEntity implements GeoEntity {
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    private final ServerBossBar bossBar;

    public static final TrackedData<Integer> PHASE = DataTracker.registerData(VoidHarbingerEntity.class, TrackedDataHandlerRegistry.INTEGER);
    public static final TrackedData<Integer> ATTACK_STATE = DataTracker.registerData(VoidHarbingerEntity.class, TrackedDataHandlerRegistry.INTEGER);
    public static final TrackedData<Boolean> SHIELD_ACTIVE = DataTracker.registerData(VoidHarbingerEntity.class, TrackedDataHandlerRegistry.BOOLEAN);

    private int attackAnimationTicks = 0;
    private int teleportCooldown = 0;
    private int singularityCooldown = 100;
    private int barrageCooldown = 60;

    public VoidHarbingerEntity(EntityType<? extends HostileEntity> entityType, World world) {
        super(entityType, world);
        this.bossBar = (ServerBossBar) new ServerBossBar(
                Text.translatable("entity.void_harbinger.void_harbinger"),
                BossBar.Color.PURPLE,
                BossBar.Style.NOTCHED_10
        ).setDarkenSky(true).setThickenFog(true);
        this.setNoGravity(true);
        this.experiencePoints = 2500;
    }

    public static DefaultAttributeContainer.Builder createHarbingerAttributes() {
        return HostileEntity.createHostileAttributes()
                .add(EntityAttributes.GENERIC_MAX_HEALTH, 800.0D)
                .add(EntityAttributes.GENERIC_ARMOR, 25.0D)
                .add(EntityAttributes.GENERIC_ARMOR_TOUGHNESS, 12.0D)
                .add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 24.0D)
                .add(EntityAttributes.GENERIC_KNOCKBACK_RESISTANCE, 1.0D)
                .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.35D)
                .add(EntityAttributes.GENERIC_FLYING_SPEED, 0.5D)
                .add(EntityAttributes.GENERIC_FOLLOW_RANGE, 64.0D);
    }

    @Override
    protected void initDataTracker() {
        super.initDataTracker();
        this.dataTracker.startTracking(PHASE, 1);
        this.dataTracker.startTracking(ATTACK_STATE, 0);
        this.dataTracker.startTracking(SHIELD_ACTIVE, false);
    }

    @Override
    protected void initGoals() {
        this.goalSelector.add(0, new SwimGoal(this));
        this.goalSelector.add(1, new HarbingerSingularityGoal(this));
        this.goalSelector.add(2, new HarbingerBarrageGoal(this));
        this.goalSelector.add(3, new HarbingerMeleeAttackGoal(this));
        this.goalSelector.add(5, new LookAtEntityGoal(this, PlayerEntity.class, 32.0f));
        this.goalSelector.add(6, new LookAroundGoal(this));

        this.targetSelector.add(1, new ActiveTargetGoal<>(this, PlayerEntity.class, true));
    }

    @Override
    public void tick() {
        super.tick();

        // Hover movement physics
        if (!this.isOnGround()) {
            this.setVelocity(this.getVelocity().multiply(0.92, 0.6, 0.92));
        }

        // Particle halo aura
        if (this.getWorld().isClient()) {
            spawnAuraParticles();
        } else {
            // Boss bar update
            this.bossBar.setPercent(this.getHealth() / this.getMaxHealth());
            updatePhases();

            // Periodic boss abilities
            if (this.teleportCooldown > 0) this.teleportCooldown--;
            if (this.singularityCooldown > 0) this.singularityCooldown--;
            if (this.barrageCooldown > 0) this.barrageCooldown--;

            if (this.attackAnimationTicks > 0) {
                this.attackAnimationTicks--;
                if (this.attackAnimationTicks == 0) {
                    this.setAttackState(0);
                }
            }

            // Phase 3 Void Decay Aura
            if (getPhase() == 3 && this.age % 20 == 0) {
                applyRageAura();
            }
        }
    }

    private void updatePhases() {
        float healthPct = this.getHealth() / this.getMaxHealth();
        if (healthPct <= 0.25f && getPhase() != 3) {
            setPhase(3);
            this.getWorld().playSound(null, getX(), getY(), getZ(), ModSounds.HARBINGER_ROAR, SoundCategory.HOSTILE, 3.0f, 0.8f);
            this.addStatusEffect(new StatusEffectInstance(StatusEffects.STRENGTH, 999999, 1, false, false));
            this.addStatusEffect(new StatusEffectInstance(StatusEffects.SPEED, 999999, 1, false, false));
        } else if (healthPct <= 0.60f && healthPct > 0.25f && getPhase() != 2) {
            setPhase(2);
            setShieldActive(true);
            this.getWorld().playSound(null, getX(), getY(), getZ(), ModSounds.HARBINGER_ROAR, SoundCategory.HOSTILE, 2.5f, 1.0f);
        }
    }

    private void spawnAuraParticles() {
        for (int i = 0; i < 2; i++) {
            double ox = (this.random.nextDouble() - 0.5) * 2.5;
            double oy = this.random.nextDouble() * 3.5;
            double oz = (this.random.nextDouble() - 0.5) * 2.5;
            this.getWorld().addParticle(ParticleTypes.PORTAL, this.getX() + ox, this.getY() + oy, this.getZ() + oz,
                    (this.random.nextDouble() - 0.5) * 0.5, -0.2, (this.random.nextDouble() - 0.5) * 0.5);
            if (getPhase() >= 2) {
                this.getWorld().addParticle(ParticleTypes.DRAGON_BREATH, this.getX() + ox * 0.5, this.getY() + oy, this.getZ() + oz * 0.5, 0, 0.05, 0);
            }
        }
    }

    private void applyRageAura() {
        List<LivingEntity> nearby = this.getWorld().getEntitiesByClass(
                LivingEntity.class,
                new Box(this.getBlockPos()).expand(12.0),
                e -> e != this && !(e instanceof VoidHarbingerEntity)
        );
        for (LivingEntity target : nearby) {
            target.damage(this.getDamageSources().magic(), 6.0f);
            target.addStatusEffect(new StatusEffectInstance(StatusEffects.WITHER, 80, 1));
            target.addStatusEffect(new StatusEffectInstance(StatusEffects.DARKNESS, 60, 0));
        }
    }

    @Override
    public boolean damage(DamageSource source, float amount) {
        // Complete immunities
        if (source.isOf(DamageTypes.FALL) || source.isOf(DamageTypes.DROWN) ||
            source.isOf(DamageTypes.IN_FIRE) || source.isOf(DamageTypes.ON_FIRE) ||
            source.isOf(DamageTypes.LAVA) || source.isOf(DamageTypes.WITHER) ||
            source.isOf(DamageTypes.OUT_OF_WORLD)) {
            return false;
        }

        // Deflect projectiles during Phase 2 & 3
        if (source.getSource() instanceof ProjectileEntity && getPhase() >= 2) {
            this.getWorld().playSound(null, getX(), getY(), getZ(), SoundEvents.ITEM_SHIELD_BLOCK, SoundCategory.HOSTILE, 1.5f, 0.8f);
            teleportBehindAttacker(source.getAttacker());
            return false;
        }

        // Phase 2 Void Aegis shield
        if (isShieldActive()) {
            amount *= 0.35f; // 65% damage reduction
        }

        boolean damaged = super.damage(source, amount);

        // Reactive teleport
        if (damaged && this.teleportCooldown <= 0 && source.getAttacker() instanceof LivingEntity attacker) {
            if (this.random.nextFloat() < 0.45f) {
                teleportBehindAttacker(attacker);
                this.teleportCooldown = (getPhase() == 3) ? 40 : 80;
            }
        }

        return damaged;
    }

    public void teleportBehindAttacker(Entity attacker) {
        if (attacker == null || this.getWorld().isClient()) return;
        Vec3d lookVec = attacker.getRotationVector().multiply(-2.5);
        double targetX = attacker.getX() + lookVec.x;
        double targetY = attacker.getY() + 0.5;
        double targetZ = attacker.getZ() + lookVec.z;

        if (this.teleport(targetX, targetY, targetZ, true)) {
            this.getWorld().playSound(null, targetX, targetY, targetZ, ModSounds.HARBINGER_TELEPORT, SoundCategory.HOSTILE, 2.0f, 1.0f);
            if (this.getWorld() instanceof ServerWorld serverWorld) {
                serverWorld.spawnParticles(ParticleTypes.REVERSE_PORTAL, targetX, targetY + 1.5, targetZ, 40, 0.5, 1.0, 0.5, 0.1);
            }
        }
    }

    // Bossbar tracking
    @Override
    public void onStartedTrackingBy(ServerPlayerEntity player) {
        super.onStartedTrackingBy(player);
        this.bossBar.addPlayer(player);
    }

    @Override
    public void onStoppedTrackingBy(ServerPlayerEntity player) {
        super.onStoppedTrackingBy(player);
        this.bossBar.removePlayer(player);
    }

    // Getters and Setters for Tracked Data
    public int getPhase() { return this.dataTracker.get(PHASE); }
    public void setPhase(int phase) { this.dataTracker.set(PHASE, phase); }

    public int getAttackState() { return this.dataTracker.get(ATTACK_STATE); }
    public void setAttackState(int state) {
        this.dataTracker.set(ATTACK_STATE, state);
        this.attackAnimationTicks = 25;
    }

    public boolean isShieldActive() { return this.dataTracker.get(SHIELD_ACTIVE); }
    public void setShieldActive(boolean active) { this.dataTracker.set(SHIELD_ACTIVE, active); }

    // GeckoLib Controller Registration
    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "main_controller", 4, this::predicateMain));
        controllers.add(new AnimationController<>(this, "wings_controller", 4, this::predicateWings));
    }

    private PlayState predicateMain(AnimationState<VoidHarbingerEntity> state) {
        if (this.isDead()) {
            return state.setAndContinue(RawAnimation.begin().thenPlay("animation.void_harbinger.death"));
        }
        int attack = getAttackState();
        if (attack == 1) {
            return state.setAndContinue(RawAnimation.begin().thenPlay("animation.void_harbinger.attack_scythe"));
        } else if (attack == 2) {
            return state.setAndContinue(RawAnimation.begin().thenPlay("animation.void_harbinger.attack_barrage"));
        } else if (attack == 3) {
            return state.setAndContinue(RawAnimation.begin().thenPlay("animation.void_harbinger.singularity"));
        }

        if (state.isMoving()) {
            return state.setAndContinue(RawAnimation.begin().thenLoop("animation.void_harbinger.walk"));
        }
        return state.setAndContinue(RawAnimation.begin().thenLoop("animation.void_harbinger.idle"));
    }

    private PlayState predicateWings(AnimationState<VoidHarbingerEntity> state) {
        return state.setAndContinue(RawAnimation.begin().thenLoop("animation.void_harbinger.idle"));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }

    @Override
    protected SoundEvent getAmbientSound() { return ModSounds.HARBINGER_AMBIENT; }
    @Override
    protected SoundEvent getHurtSound(DamageSource source) { return SoundEvents.ENTITY_WITHER_HURT; }
    @Override
    protected SoundEvent getDeathSound() { return ModSounds.HARBINGER_ROAR; }

    // ==================== Custom Goals ====================
    static class HarbingerMeleeAttackGoal extends Goal {
        private final VoidHarbingerEntity boss;
        private int cooldown = 0;

        public HarbingerMeleeAttackGoal(VoidHarbingerEntity boss) {
            this.boss = boss;
            this.setControls(EnumSet.of(Control.MOVE, Control.LOOK));
        }

        @Override
        public boolean canStart() {
            LivingEntity target = this.boss.getTarget();
            return target != null && target.isAlive() && this.boss.squaredDistanceTo(target) < 16.0;
        }

        @Override
        public void tick() {
            LivingEntity target = this.boss.getTarget();
            if (target == null) return;
            this.boss.getLookControl().lookAt(target, 30.0f, 30.0f);
            if (--this.cooldown <= 0) {
                this.boss.setAttackState(1);
                this.boss.tryAttack(target);
                target.addStatusEffect(new StatusEffectInstance(StatusEffects.WITHER, 100, 2));
                this.boss.getWorld().playSound(null, this.boss.getX(), this.boss.getY(), this.boss.getZ(),
                        ModSounds.HARBINGER_ATTACK, SoundCategory.HOSTILE, 1.8f, 1.0f);
                this.cooldown = 30;
            }
        }
    }

    static class HarbingerBarrageGoal extends Goal {
        private final VoidHarbingerEntity boss;
        private int timer = 0;

        public HarbingerBarrageGoal(VoidHarbingerEntity boss) {
            this.boss = boss;
        }

        @Override
        public boolean canStart() {
            LivingEntity target = this.boss.getTarget();
            return target != null && target.isAlive() && this.boss.barrageCooldown <= 0;
        }

        @Override
        public void start() {
            this.timer = 0;
            this.boss.setAttackState(2);
            this.boss.barrageCooldown = 120;
        }

        @Override
        public void tick() {
            LivingEntity target = this.boss.getTarget();
            if (target == null) return;
            this.timer++;
            if (this.timer % 6 == 0 && this.timer <= 24) {
                // Launch void projectile
                double dx = target.getX() - this.boss.getX();
                double dy = target.getBodyY(0.5) - this.boss.getBodyY(0.5);
                double dz = target.getZ() - this.boss.getZ();
                VoidProjectileEntity proj = new VoidProjectileEntity(this.boss.getWorld(), this.boss, dx, dy, dz);
                proj.setPos(this.boss.getX(), this.boss.getY() + 2.5, this.boss.getZ());
                this.boss.getWorld().spawnEntity(proj);
                this.boss.getWorld().playSound(null, this.boss.getX(), this.boss.getY(), this.boss.getZ(),
                        SoundEvents.ENTITY_WITHER_SHOOT, SoundCategory.HOSTILE, 1.5f, 1.2f);
            }
        }

        @Override
        public boolean shouldContinue() {
            return this.timer < 30;
        }
    }

    static class HarbingerSingularityGoal extends Goal {
        private final VoidHarbingerEntity boss;
        private int timer = 0;
        private Vec3d center = null;

        public HarbingerSingularityGoal(VoidHarbingerEntity boss) {
            this.boss = boss;
        }

        @Override
        public boolean canStart() {
            LivingEntity target = this.boss.getTarget();
            return target != null && target.isAlive() && this.boss.singularityCooldown <= 0 && this.boss.getPhase() >= 2;
        }

        @Override
        public void start() {
            LivingEntity target = this.boss.getTarget();
            this.center = (target != null) ? target.getPos() : this.boss.getPos();
            this.timer = 0;
            this.boss.setAttackState(3);
            this.boss.singularityCooldown = 220;
            this.boss.getWorld().playSound(null, center.x, center.y, center.z,
                    ModSounds.HARBINGER_SINGULARITY, SoundCategory.HOSTILE, 3.0f, 0.7f);
        }

        @Override
        public void tick() {
            if (this.center == null) return;
            this.timer++;
            // Pull all players towards singularity center
            List<LivingEntity> victims = this.boss.getWorld().getEntitiesByClass(
                    LivingEntity.class,
                    new Box(this.center.x - 14, this.center.y - 4, this.center.z - 14,
                            this.center.x + 14, this.center.y + 8, this.center.z + 14),
                    e -> e != this.boss
            );
            for (LivingEntity victim : victims) {
                Vec3d pull = this.center.subtract(victim.getPos()).normalize().multiply(0.35);
                victim.setVelocity(victim.getVelocity().add(pull));
                victim.velocityModified = true;
            }

            if (this.boss.getWorld() instanceof ServerWorld sw) {
                sw.spawnParticles(ParticleTypes.SQUID_INK, center.x, center.y + 1, center.z, 20, 1.0, 1.0, 1.0, 0.05);
                sw.spawnParticles(ParticleTypes.END_ROD, center.x, center.y + 1, center.z, 10, 0.5, 0.5, 0.5, 0.1);
            }

            // Big Detonation at tick 50
            if (this.timer == 50) {
                for (LivingEntity victim : victims) {
                    victim.damage(this.boss.getDamageSources().magic(), 22.0f);
                    victim.addStatusEffect(new StatusEffectInstance(StatusEffects.LEVITATION, 60, 1));
                    victim.addStatusEffect(new StatusEffectInstance(StatusEffects.BLINDNESS, 80, 0));
                }
                if (this.boss.getWorld() instanceof ServerWorld sw) {
                    sw.spawnParticles(ParticleTypes.EXPLOSION_EMITTER, center.x, center.y + 1, center.z, 2, 0, 0, 0, 0);
                }
                this.boss.getWorld().playSound(null, center.x, center.y, center.z,
                        SoundEvents.ENTITY_GENERIC_EXPLODE, SoundCategory.HOSTILE, 2.5f, 0.6f);
            }
        }

        @Override
        public boolean shouldContinue() {
            return this.timer < 60;
        }
    }
}
