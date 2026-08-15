package dev.minecartspeedway.mixin;

import dev.minecartspeedway.SpeedwayConfig;
import dev.minecartspeedway.SpeedwayRules;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.vehicle.minecart.AbstractMinecart;
import net.minecraft.world.entity.vehicle.minecart.MinecartBehavior;
import net.minecraft.world.entity.vehicle.minecart.OldMinecartBehavior;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(OldMinecartBehavior.class)
public abstract class OldMinecartBehaviorMixin extends MinecartBehavior {
	protected OldMinecartBehaviorMixin(AbstractMinecart minecart) {
		super(minecart);
	}

	@Inject(method = "getMaxSpeed", at = @At("HEAD"), cancellable = true)
	private void minecartSpeedway$getMaxSpeed(ServerLevel level, CallbackInfoReturnable<Double> cir) {
		if (!SpeedwayRules.isSpeedwayActive(this.minecart)) {
			return;
		}
		double max = SpeedwayConfig.getMaxSpeedBlocksPerTick();
		if (this.minecart.isInWater()) {
			max *= 0.5;
		}
		cir.setReturnValue(max);
	}

	@ModifyConstant(method = "moveAlongTrack", constant = @Constant(doubleValue = 0.06))
	private double minecartSpeedway$scaleBoost(double original) {
		if (SpeedwayRules.isSpeedwayActive(this.minecart)) {
			return original * SpeedwayConfig.getAccelerationScale();
		}
		return original;
	}

	@ModifyConstant(method = "getKnownMovement", constant = @Constant(doubleValue = 0.4))
	private double minecartSpeedway$raiseAbsoluteMax(double original) {
		if (SpeedwayRules.isSpeedwayActive(this.minecart)) {
			return SpeedwayConfig.getMaxSpeedBlocksPerTick();
		}
		return original;
	}

	@ModifyConstant(method = "getKnownMovement", constant = @Constant(doubleValue = -0.4))
	private double minecartSpeedway$raiseAbsoluteMin(double original) {
		if (SpeedwayRules.isSpeedwayActive(this.minecart)) {
			return -SpeedwayConfig.getMaxSpeedBlocksPerTick();
		}
		return original;
	}
}
