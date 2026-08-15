package dev.minecartspeedway;

import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MinecartSpeedway implements ModInitializer {
	public static final String MOD_ID = "minecart_speedway";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		SpeedwayConfig.load();
		LOGGER.info("Minecart Speedway loaded (max {} blocks/s)", SpeedwayConfig.getMaxSpeedBlocksPerSecond());
	}
}
