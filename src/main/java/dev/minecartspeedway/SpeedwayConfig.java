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
	private static final long RELOAD_CHECK_INTERVAL_NANOS = 1_000_000_000L;

	/** Vanilla minecart max speed in blocks per second. */
	public static final double VANILLA_MAX_SPEED_BPS = 8.0;

	private static double maxSpeedBlocksPerSecond = 32.0;
	private static long lastModifiedMillis = Long.MIN_VALUE;
	private static long lastReloadCheckNanos;

	private SpeedwayConfig() {
	}

	public static void load() {
		if (Files.exists(CONFIG_PATH)) {
			readFromDisk();
		} else {
			save();
			noteModifiedTime();
		}
	}

	/**
	 * Picks up edits to {@code config/minecart_speedway.json} without a restart.
	 * Checked at most once a second.
	 */
	public static void reloadIfChanged() {
		long now = System.nanoTime();
		if (now - lastReloadCheckNanos < RELOAD_CHECK_INTERVAL_NANOS) {
			return;
		}
		lastReloadCheckNanos = now;
		try {
			if (!Files.exists(CONFIG_PATH)) {
				return;
			}
			long modified = Files.getLastModifiedTime(CONFIG_PATH).toMillis();
			if (modified == lastModifiedMillis) {
				return;
			}
		} catch (IOException e) {
			MinecartSpeedway.LOGGER.error("Failed to check config timestamp", e);
			return;
		}
		readFromDisk();
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
		reloadIfChanged();
		return maxSpeedBlocksPerSecond;
	}

	/** Max speed in blocks per tick (what Minecraft uses internally). */
	public static double getMaxSpeedBlocksPerTick() {
		return getMaxSpeedBlocksPerSecond() / 20.0;
	}

	/**
	 * Scales powered-rail acceleration so a higher cap is reached on the same
	 * stretch of rail vanilla uses to reach 8 blocks/s.
	 */
	public static double getAccelerationScale(double effectiveBlocksPerSecond) {
		return Math.max(effectiveBlocksPerSecond, VANILLA_MAX_SPEED_BPS) / VANILLA_MAX_SPEED_BPS;
	}

	private static void readFromDisk() {
		double previous = maxSpeedBlocksPerSecond;
		try (Reader reader = Files.newBufferedReader(CONFIG_PATH)) {
			Data data = GSON.fromJson(reader, Data.class);
			if (data != null && data.maxSpeedBlocksPerSecond > 0) {
				maxSpeedBlocksPerSecond = data.maxSpeedBlocksPerSecond;
			}
			noteModifiedTime();
			if (previous != maxSpeedBlocksPerSecond) {
				MinecartSpeedway.LOGGER.info(
						"Minecart Speedway max speed is {} blocks/s ({})",
						maxSpeedBlocksPerSecond,
						CONFIG_PATH.toAbsolutePath()
				);
			}
		} catch (Exception e) {
			MinecartSpeedway.LOGGER.error("Failed to read config; keeping {} blocks/s", maxSpeedBlocksPerSecond, e);
			noteModifiedTime();
		}
	}

	private static void noteModifiedTime() {
		try {
			if (Files.exists(CONFIG_PATH)) {
				lastModifiedMillis = Files.getLastModifiedTime(CONFIG_PATH).toMillis();
			}
		} catch (IOException e) {
			MinecartSpeedway.LOGGER.error("Failed to read config timestamp", e);
		}
	}

	private record Data(double maxSpeedBlocksPerSecond) {
	}
}
