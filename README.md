# Minecart Speedway

Server-side Fabric mod for **Minecraft 26.2**. Eligible minecarts go faster on powered rails placed on redstone blocks.

Connecting clients do **not** need this mod.

## Speedway rails

Place a **redstone block**, then a **powered rail** on top. The redstone block powers the rail and marks it as a speedway.

Only these minecarts get the boost:

- Minecarts with a **player** riding
- Minecarts with a **boat** or **chest boat** (including nested passengers) riding

Empty carts and mob-only carts stay at vanilla speed. Regular powered rails (any other power source) are unchanged.

## Config

On first run the server creates `config/minecart_speedway.json`:

```json
{
  "maxSpeedBlocksPerSecond": 32.0
}
```

Vanilla max is `8.0`. Acceleration on speedway rails is scaled so carts can reach the configured cap.

Off the speedway, the mod does **not** rewrite velocity (that breaks slopes). Like other speed mods, it only raises `getMaxSpeed` on speedway rails; vanilla clamping and rail friction handle slowing down afterward.

## Build

Requires **JDK 25**.

```bash
./gradlew build
```

The jar is written to `build/libs/`.

## Install

Put the jar in the dedicated server’s `mods/` folder (with Fabric Loader and Fabric API). For singleplayer / LAN, install it on the host; guests do not need it.
