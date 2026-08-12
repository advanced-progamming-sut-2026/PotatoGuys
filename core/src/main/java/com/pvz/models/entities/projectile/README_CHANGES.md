# Projectile State Pattern — what changed

Drop this folder in as `core/src/main/java/com/pvz/models/entities/projectile/`
(overwrite the old one). It replaces the old empty `fsm/ProjectileState.java`.

## Two independent state hierarchies

- **`fsm/ProjectileMotionState`** — how the projectile moves each tick.
  - `StraightMotionState` — the old default behavior (constant velocity), used by
    every projectile type that has no special motion.
  - `LobbedMotionState` — parabolic arc from launch point to a fixed target point,
    used by lobbers (Pepper-pult, Melon-pult, Winter Melon-pult). Calls
    `Projectile.land()` when the arc completes.

- **`effects/ProjectileEffectState`** — what happens on impact.
  - `NormalEffectState` — plain damage, no status effect.
  - `FireEffectState` — single-target burn (Fire Peashooter).
  - `IceEffectState` — single-target chill (Snow Pea).
  - `AreaFireEffectState` — 3x3 area burn (Pepper-pult).
  - `MelonEffectState` — 3x3 area damage, optional chill via constructor flag
    (Melon-pult / Winter Melon-pult).
  - `PiercingEffectState` — damages every zombie touched but is never consumed
    (Smoke Cloud / Cactus Spike). `Projectile` already deduplicates hits per
    zombie via its `hitZombies` set.

## `Projectile.java`

- No longer takes `poison`/`ice`/`fire`/`pierceCount`/`bouncing` booleans/ints in
  its constructor. Instead it holds a `motionState` + `effectState` pair, set via
  `setMotionState(...)` / `setEffectState(...)` (defaults to Straight + Normal).
- Old bounce-related code (`setBouncing`, `handleBounce`) was removed since it
  wasn't wired to anything in Phase 1 and isn't part of the required grouping —
  add a `BouncingMotionState` later the same way if Bowling Bulb needs it.
- Added `land()`, called by `LobbedMotionState` when an arc finishes; resolves
  the effect at the landing spot (area effects pick their own targets from
  `GameContext`, single-target effects fall back to the nearest zombie).

## `ProjectileFactory.java`

- Single place mapping `ProjectileType` → (motion state, effect state) pair.
- Old `create(type, ctx, startPos, vel, damage)` signature still works (kept as
  an overload) so existing callers like `ShooterAction` don't need to change.
- New overload `create(type, ctx, startPos, vel, damage, target)` lets callers
  pass the zombie a lobbed projectile should arc toward.

## `ProjectileType.java`

- Added `PEPPER_BALL`, `MELON`, `WINTER_MELON` — Phase 1 lists Pepper-pult and
  the two melon variants as distinct projectile behaviors, but the old enum had
  no entries for them.

## Wiring a new plant's shot (example)

```java
// Straight, no special effect (e.g. Peashooter) — unchanged, uses the default overload:
ProjectileFactory.create(ProjectileType.PEA, ctx, startPos, vel, damage);

// Pepper-pult (lobbed + area fire) — needs the target zombie for the arc:
ProjectileFactory.create(ProjectileType.PEPPER_BALL, ctx, startPos, vel, damage, targetZombie);
```
