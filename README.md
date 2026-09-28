# Minecart Speedway

Server-side Fabric mod for **Minecraft 26.1 and newer**, including 26.1.1, 26.1.2, 26.2, and 26.3. Eligible minecarts get a higher speed cap on powered rails placed on redstone blocks.

Connecting clients do **not** need this mod.

## Speedway rails

Place a **redstone block**, then a **powered rail** on top. The redstone block powers the rail and marks it as a speedway.

Only these minecarts get the higher cap:

- Minecarts with a **player** riding
- Minecarts with a **boat** or **chest boat** (including nested passengers) riding

Empty carts and mob-only carts stay at vanilla speed. Regular powered rails (any other power source) are unchanged.

## What this mod changes

The max speed on speedway rails, and the powered-rail push that builds up to it. The push scales with the cap, so raising the number makes the same rails faster. On any other rail, speed drops back to the vanilla cap immediately. Classic minecart physics also hard-caps carts at 40 blocks/s; on a speedway that cap is lifted to the configured speed. Slopes, friction, and coasting stay vanilla.

## Config

On first run the server creates `config/minecart_speedway.json`:

```json
{
  "maxSpeedBlocksPerSecond": 32.0
}
```

Vanilla max is `8.0`. Edits to this file apply within about a second; a restart is not required. On the new minecart physics, the speedway also follows `max_minecart_speed` when that gamerule is higher than the config.

## Build

Requires **JDK 25**.

```bash
./gradlew build
```

The jar is written to `build/libs/`.

## Install

Put the jar in the dedicated server’s `mods/` folder (with Fabric Loader and Fabric API). For singleplayer / LAN, install it on the host; guests do not need it.
