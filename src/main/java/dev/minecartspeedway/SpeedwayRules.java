package dev.minecartspeedway;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.boat.AbstractBoat;
import net.minecraft.world.entity.vehicle.boat.AbstractChestBoat;
import net.minecraft.world.entity.vehicle.minecart.AbstractMinecart;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseRailBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.PoweredRailBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gamerules.GameRules;

import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Speedway eligibility. Only used to decide whether {@code getMaxSpeed} should return the
 * configured value — no velocity rewriting.
 */
public final class SpeedwayRules {
	/**
	 * Keep the boosted max for a few ticks only when the cart is not on a rail,
	 * so a slope detection gap does not snap the cap down mid-climb. A real
	 * non-speedway rail ends the boost immediately.
	 */
	private static final int DETECTION_GRACE_TICKS = 10;

	private static final Map<UUID, Long> lastSpeedwayGameTime = new ConcurrentHashMap<>();

	private SpeedwayRules() {
	}

	/**
	 * Whether this cart should use the configured max speed (vanilla otherwise).
	 */
	public static boolean shouldUseBoostedMaxSpeed(AbstractMinecart minecart) {
		Level level = minecart.level();
		if (level.isClientSide() || !isEligible(minecart)) {
			lastSpeedwayGameTime.remove(minecart.getUUID());
			return false;
		}

		long gameTime = level.getGameTime();
		BlockPos railPos = minecart.getCurrentBlockPosOrRailBelow();
		if (isSpeedwayRail(level, railPos)) {
			lastSpeedwayGameTime.put(minecart.getUUID(), gameTime);
			prune(gameTime);
			return true;
		}

		if (BaseRailBlock.isRail(level.getBlockState(railPos))) {
			lastSpeedwayGameTime.remove(minecart.getUUID());
			return false;
		}

		Long last = lastSpeedwayGameTime.get(minecart.getUUID());
		if (last != null && gameTime - last <= DETECTION_GRACE_TICKS) {
			return true;
		}

		lastSpeedwayGameTime.remove(minecart.getUUID());
		return false;
	}

	public static boolean isSpeedwayRail(Level level, BlockPos railPos) {
		BlockState railState = level.getBlockState(railPos);
		if (!(railState.getBlock() instanceof PoweredRailBlock)) {
			return false;
		}
		return level.getBlockState(railPos.below()).is(Blocks.REDSTONE_BLOCK);
	}

	/** Vanilla cap for this cart, ignoring the speedway config. */
	public static double vanillaMaxBlocksPerTick(ServerLevel level, AbstractMinecart minecart) {
		double max;
		if (AbstractMinecart.useExperimentalMovement(level)) {
			max = level.getGameRules().get(GameRules.MAX_MINECART_SPEED) / 20.0;
		} else {
			max = 0.4;
		}
		if (minecart.isInWater()) {
			max *= 0.5;
		}
		return max;
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

	private static void prune(long gameTime) {
		if (lastSpeedwayGameTime.size() < 64) {
			return;
		}
		Iterator<Map.Entry<UUID, Long>> it = lastSpeedwayGameTime.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry<UUID, Long> entry = it.next();
			if (gameTime - entry.getValue() > DETECTION_GRACE_TICKS * 4L) {
				it.remove();
			}
		}
	}
}
