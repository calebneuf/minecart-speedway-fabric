package dev.minecartspeedway.mixin;

import dev.minecartspeedway.SpeedwayConfig;
import dev.minecartspeedway.SpeedwayRules;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.vehicle.minecart.AbstractMinecart;
import net.minecraft.world.entity.vehicle.minecart.MinecartBehavior;
import net.minecraft.world.entity.vehicle.minecart.OldMinecartBehavior;
import net.minecraft.world.level.block.BaseRailBlock;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Raises the speed cap on speedway rails and scales the vanilla powered-rail push
 * with that cap. Slopes, friction, and coasting stay vanilla.
 */
@Mixin(OldMinecartBehavior.class)
public abstract class OldMinecartBehaviorMixin extends MinecartBehavior {
	protected OldMinecartBehaviorMixin(AbstractMinecart minecart) {
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

	@Inject(method = "moveAlongTrack", at = @At("RETURN"))
	private void minecartSpeedway$clampOffSpeedway(ServerLevel level, CallbackInfo ci) {
		if (!SpeedwayRules.isEligible(this.minecart)) {
			return;
		}
		BlockPos railPos = this.minecart.getCurrentBlockPosOrRailBelow();
		if (!BaseRailBlock.isRail(level.getBlockState(railPos)) || SpeedwayRules.isSpeedwayRail(level, railPos)) {
			return;
		}
		Vec3 movement = this.getDeltaMovement();
		double horizontal = movement.horizontalDistance();
		double max = SpeedwayRules.vanillaMaxBlocksPerTick(level, this.minecart);
		if (horizontal <= max || horizontal < 1.0E-5) {
			return;
		}
		double scale = max / horizontal;
		this.setDeltaMovement(movement.x * scale, movement.y, movement.z * scale);
	}

	/**
	 * Vanilla clamps horizontal speed with {@code Math.min(2.0, speed)} before the
	 * normal max-speed check. 2 blocks/tick is 40 blocks/s, so a higher config never
	 * showed up. The other 2.0 in this method is the rail progress factor and must stay.
	 */
	@ModifyConstant(method = "moveAlongTrack", constant = @Constant(doubleValue = 2.0, ordinal = 0))
	private double minecartSpeedway$raiseLegacySpeedCap(double original) {
		if (!SpeedwayRules.shouldUseBoostedMaxSpeed(this.minecart)) {
			return original;
		}
		double max = SpeedwayConfig.getMaxSpeedBlocksPerTick();
		if (this.minecart.isInWater()) {
			max *= 0.5;
		}
		return max;
	}

	@ModifyConstant(method = "moveAlongTrack", constant = @Constant(doubleValue = 0.06))
	private double minecartSpeedway$scaleBoost(double original) {
		if (!SpeedwayRules.shouldUseBoostedMaxSpeed(this.minecart)) {
			return original;
		}
		return original * SpeedwayConfig.getAccelerationScale(SpeedwayConfig.getMaxSpeedBlocksPerSecond());
	}

	@ModifyConstant(method = "getKnownMovement", constant = @Constant(doubleValue = 0.4))
	private double minecartSpeedway$raiseAbsoluteMax(double original) {
		if (!SpeedwayRules.shouldUseBoostedMaxSpeed(this.minecart)) {
			return original;
		}
		return SpeedwayConfig.getMaxSpeedBlocksPerTick();
	}

	@ModifyConstant(method = "getKnownMovement", constant = @Constant(doubleValue = -0.4))
	private double minecartSpeedway$raiseAbsoluteMin(double original) {
		if (!SpeedwayRules.shouldUseBoostedMaxSpeed(this.minecart)) {
			return original;
		}
		return -SpeedwayConfig.getMaxSpeedBlocksPerTick();
	}
}
