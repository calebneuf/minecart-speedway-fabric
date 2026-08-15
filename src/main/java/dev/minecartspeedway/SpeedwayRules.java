package dev.minecartspeedway;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.boat.AbstractBoat;
import net.minecraft.world.entity.vehicle.boat.AbstractChestBoat;
import net.minecraft.world.entity.vehicle.minecart.AbstractMinecart;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.PoweredRailBlock;
import net.minecraft.world.level.block.state.BlockState;

public final class SpeedwayRules {
	private SpeedwayRules() {
	}

	public static boolean isSpeedwayActive(AbstractMinecart minecart) {
		Level level = minecart.level();
		if (level.isClientSide()) {
			return false;
		}
		return isEligible(minecart) && findSpeedwayRail(level, minecart) != null;
	}

	public static boolean isSpeedwayActive(AbstractMinecart minecart, Level level, BlockPos railPos) {
		if (level.isClientSide()) {
			return false;
		}
		return isEligible(minecart) && isSpeedwayRail(level, railPos);
	}

	/**
	 * Resolves the rail under the cart, including common slope / interpolation edge cases.
	 */
	public static BlockPos findSpeedwayRail(Level level, AbstractMinecart minecart) {
		BlockPos railOrBelow = minecart.getCurrentBlockPosOrRailBelow();
		if (isSpeedwayRail(level, railOrBelow)) {
			return railOrBelow;
		}
		if (isSpeedwayRail(level, railOrBelow.below())) {
			return railOrBelow.below();
		}

		BlockPos at = BlockPos.containing(minecart.position());
		if (isSpeedwayRail(level, at)) {
			return at;
		}
		if (isSpeedwayRail(level, at.below())) {
			return at.below();
		}
		return null;
	}

	public static boolean isSpeedwayRail(Level level, BlockPos railPos) {
		BlockState railState = level.getBlockState(railPos);
		if (!(railState.getBlock() instanceof PoweredRailBlock)) {
			return false;
		}
		return level.getBlockState(railPos.below()).is(Blocks.REDSTONE_BLOCK);
	}

	public static boolean isEligible(AbstractMinecart minecart) {
		for (Entity passenger : minecart.getPassengers()) {
			if (isEligiblePassenger(passenger)) {
				return true;
			}
		}
		return false;
	}

	private static boolean isEligiblePassenger(Entity entity) {
		if (entity instanceof Player) {
			return true;
		}
		if (entity instanceof AbstractChestBoat || entity instanceof AbstractBoat) {
			return true;
		}
		for (Entity nested : entity.getPassengers()) {
			if (isEligiblePassenger(nested)) {
				return true;
			}
		}
		return false;
	}
}
