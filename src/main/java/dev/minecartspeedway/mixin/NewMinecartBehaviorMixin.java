package dev.minecartspeedway.mixin;

import dev.minecartspeedway.SpeedwayConfig;
import dev.minecartspeedway.SpeedwayRules;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.vehicle.minecart.AbstractMinecart;
import net.minecraft.world.entity.vehicle.minecart.MinecartBehavior;
import net.minecraft.world.entity.vehicle.minecart.NewMinecartBehavior;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(NewMinecartBehavior.class)
public abstract class NewMinecartBehaviorMixin extends MinecartBehavior {
	protected NewMinecartBehaviorMixin(AbstractMinecart minecart) {
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

	@ModifyConstant(method = "calculateBoostTrackSpeed", constant = @Constant(doubleValue = 0.06))
	private double minecartSpeedway$scaleBoost(double original, Vec3 deltaMovement, BlockPos pos, BlockState state) {
		if (SpeedwayRules.isSpeedwayActive(this.minecart, this.minecart.level(), pos)) {
			return original * SpeedwayConfig.getAccelerationScale();
		}
		return original;
	}
}
