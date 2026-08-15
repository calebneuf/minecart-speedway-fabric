package dev.minecartspeedway;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;

public final class SpeedwayConfig {
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
	private static final Path CONFIG_PATH = FabricLoader.getInstance().getConfigDir().resolve("minecart_speedway.json");

	/** Vanilla minecart max speed in blocks per second. */
	public static final double VANILLA_MAX_SPEED_BPS = 8.0;

	private static double maxSpeedBlocksPerSecond = 32.0;

	private SpeedwayConfig() {
	}

	public static void load() {
		if (Files.exists(CONFIG_PATH)) {
			try (Reader reader = Files.newBufferedReader(CONFIG_PATH)) {
				Data data = GSON.fromJson(reader, Data.class);
				if (data != null && data.maxSpeedBlocksPerSecond > 0) {
					maxSpeedBlocksPerSecond = data.maxSpeedBlocksPerSecond;
				}
			} catch (IOException e) {
				MinecartSpeedway.LOGGER.error("Failed to read config; using defaults", e);
			}
		} else {
			save();
		}
	}

	public static void save() {
		try {
			Files.createDirectories(CONFIG_PATH.getParent());
			try (Writer writer = Files.newBufferedWriter(CONFIG_PATH)) {
				GSON.toJson(new Data(maxSpeedBlocksPerSecond), writer);
			}
		} catch (IOException e) {
			MinecartSpeedway.LOGGER.error("Failed to write config", e);
		}
	}

	public static double getMaxSpeedBlocksPerSecond() {
		return maxSpeedBlocksPerSecond;
	}

	/** Max speed in blocks per tick (what Minecraft uses internally). */
	public static double getMaxSpeedBlocksPerTick() {
		return maxSpeedBlocksPerSecond / 20.0;
	}

	/**
	 * Scales powered-rail acceleration so carts can reach the configured max
	 * over a similar stretch of rail as vanilla uses for 8 b/s.
	 */
	public static double getAccelerationScale() {
		return maxSpeedBlocksPerSecond / VANILLA_MAX_SPEED_BPS;
	}

	private record Data(double maxSpeedBlocksPerSecond) {
	}
}
