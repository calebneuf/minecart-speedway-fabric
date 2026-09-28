package dev.minecartspeedway.mixin;

import dev.minecartspeedway.SpeedwayConfig;
import dev.minecartspeedway.SpeedwayRules;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.vehicle.minecart.AbstractMinecart;
import net.minecraft.world.level.block.BaseRailBlock;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.entity.vehicle.minecart.MinecartBehavior;
import net.minecraft.world.entity.vehicle.minecart.NewMinecartBehavior;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Raises the speed cap on speedway rails and scales the vanilla powered-rail push
 * with that cap. Slopes, friction, and coasting stay vanilla.
 */
@Mixin(NewMinecartBehavior.class)
public abstract class NewMinecartBehaviorMixin extends MinecartBehavior {
	protected NewMinecartBehaviorMixin(AbstractMinecart minecart) {
		super(minecart);
	}

	@Inject(method = "getMaxSpeed", at = @At("RETURN"), cancellable = true)
	private void minecartSpeedway$getMaxSpeed(ServerLevel level, CallbackInfoReturnable<Double> cir) {
		if (!SpeedwayRules.shouldUseBoostedMaxSpeed(this.minecart)) {
			return;
		}
		double max = SpeedwayConfig.getMaxSpeedBlocksPerTick();
		if (this.minecart.isInWater()) {
			max *= 0.5;
		}
		cir.setReturnValue(Math.max(cir.getReturnValue(), max));
	}

	/**
	 * The vanilla cap is applied once per tick, on the first rail. At speedway
	 * speed a cart can enter a normal rail later in that same tick, so clamp
	 * again at the moment the current rail is not a speedway.
	 */
	@Inject(method = "calculateTrackSpeed", at = @At("RETURN"), cancellable = true)
	private void minecartSpeedway$clampOffSpeedway(CallbackInfoReturnable<Vec3> cir) {
		if (!(this.level() instanceof ServerLevel level) || !SpeedwayRules.isEligible(this.minecart)) {
			return;
		}
		BlockPos railPos = this.minecart.getCurrentBlockPosOrRailBelow();
		if (!BaseRailBlock.isRail(level.getBlockState(railPos)) || SpeedwayRules.isSpeedwayRail(level, railPos)) {
			return;
		}
		Vec3 movement = cir.getReturnValue();
		double max = SpeedwayRules.vanillaMaxBlocksPerTick(level, this.minecart);
		if (movement.length() <= max) {
			return;
		}
		cir.setReturnValue(movement.normalize().scale(max));
	}

	@ModifyConstant(method = "calculateBoostTrackSpeed", constant = @Constant(doubleValue = 0.06))
	private double minecartSpeedway$scaleBoost(double original) {
		if (!SpeedwayRules.shouldUseBoostedMaxSpeed(this.minecart)) {
			return original;
		}
		double blocksPerSecond = SpeedwayConfig.getMaxSpeedBlocksPerSecond();
		if (this.level() instanceof ServerLevel serverLevel && AbstractMinecart.useExperimentalMovement(serverLevel)) {
			blocksPerSecond = Math.max(blocksPerSecond, serverLevel.getGameRules().get(GameRules.MAX_MINECART_SPEED));
		}
		return original * SpeedwayConfig.getAccelerationScale(blocksPerSecond);
	}
}
