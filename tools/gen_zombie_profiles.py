#!/usr/bin/env python3
"""One-off generator: writes app/src/main/resources/zombie_profiles.json.

This transcribes the data previously hard-coded in ZombieRegistry.java into a
data-driven JSON resource, loaded at runtime via SaveManager (Gson). Re-run
this script whenever you need to regenerate the file from scratch; day-to-day
balance edits should just be made directly in the JSON.
"""
import json
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
OUT_PATH = ROOT / "app" / "src" / "main" / "resources" / "zombie_profiles.json"

SCALING_PRESETS = {
    "standard": [
        {"key": "Hitpoints", "formula": "standard", "arg1": 1.3, "arg2": 0.05},
        {"key": "EatDPS", "formula": "standard", "arg1": 1.3, "arg2": 0.05},
        {"key": "Speed", "formula": "constant", "arg1": 0, "arg2": 0},
        {"key": "WavePointCost", "formula": "constant", "arg1": 0, "arg2": 0},
    ],
    "constant": [
        {"key": "Hitpoints", "formula": "constant", "arg1": 0, "arg2": 0},
        {"key": "EatDPS", "formula": "constant", "arg1": 0, "arg2": 0},
        {"key": "Speed", "formula": "constant", "arg1": 0, "arg2": 0},
        {"key": "WavePointCost", "formula": "constant", "arg1": 0, "arg2": 0},
    ],
}

ARMORS = [
    {"alias": "ConeDefault", "type": "Cone", "baseHealth": 370,
     "flags": ["damageable", "droppable", "helm"]},
    {"alias": "BucketDefault", "type": "Bucket", "baseHealth": 1100,
     "flags": ["metallic", "damageable", "droppable", "helm"]},
    {"alias": "BrickDefault", "type": "Brick", "baseHealth": 2200,
     "flags": ["damageable", "droppable", "helm"]},
    {"alias": "ShoulderArmorDefault", "type": "ShoulderArmor", "baseHealth": 1600,
     "flags": ["damageable", "passdamage"]},
    {"alias": "CrownDefault", "type": "Crown", "baseHealth": 1600,
     "flags": ["damageable", "droppable", "metallic", "helm"]},
    {"alias": "NewspaperDefault", "type": "Newspaper", "baseHealth": 800,
     "flags": ["damageable"]},
]
for a in ARMORS:
    a["layerThresholds"] = [0.666, 0.333]


def basic(alias, wave_cost, weight, armor=None, obj_class="ZombiePropertySheet",
          hp=190, dps=100, speed=0.185, pf=True):
    z = {
        "alias": alias, "objClass": obj_class, "scaling": "standard",
        "hitPoints": hp, "eatDps": dps, "speed": speed,
        "wavePointCost": wave_cost, "weight": weight, "canSpawnPlantFood": pf,
    }
    if armor:
        z["armorAliases"] = armor
    return z


def imp(alias, wave_cost=100, weight=1000, speed=0.22):
    return {
        "alias": alias, "objClass": "ZombiePropertySheet", "scaling": "standard",
        "hitPoints": 190, "eatDps": 100, "speed": speed,
        "wavePointCost": wave_cost, "weight": weight, "canSpawnPlantFood": False,
        "imp": True,
    }


def gargantuar(alias, imp_type, wave_cost=1500):
    return {
        "alias": alias, "objClass": "ZombieGargantuarProps", "scaling": "standard",
        "hitPoints": 3600, "eatDps": 0, "speed": 0.24,
        "wavePointCost": wave_cost, "weight": 3000, "canSpawnPlantFood": False,
        "impType": imp_type, "healthThresholdToThrowImp": 0.5,
        "smashDamage": 1500, "smashDuration": 2,
    }


def zomboss(alias, obj_class, hp):
    return {
        "alias": alias, "objClass": obj_class, "scaling": "constant",
        "hitPoints": hp, "eatDps": 0, "speed": 0.1,
        "wavePointCost": 0, "weight": 3000, "canSpawnPlantFood": False,
    }


ZOMBIES = [
    # ── Core / all-chapter ──────────────────────────────────────────────
    basic("ZombieTutorialDefault", 100, 1000),
    basic("ZombieTutorialArmor1Default", 200, 3000, armor=["ConeDefault"]),
    basic("ZombieTutorialArmor2Default", 400, 4000, armor=["BucketDefault"]),
    basic("ZombieTutorialArmor4Default", 700, 3000, armor=["BrickDefault"]),
    basic("ZombieTutorialFlagDefault", 100, 1000),
    imp("ZombieTutorialImpDefault"),
    gargantuar("ZombieGargantuarBasic", "ZombieTutorialImpDefault"),
    {
        "alias": "ZombieModernNewspaperDefault", "objClass": "ZombieModernNewspaperProps",
        "scaling": "standard", "hitPoints": 460, "eatDps": 200, "speed": 0.22,
        "wavePointCost": 700, "weight": 4000, "canSpawnPlantFood": True,
        "armorAliases": ["NewspaperDefault"],
    },

    # ── Ancient Egypt ────────────────────────────────────────────────────
    basic("ZombieMummyDefault", 100, 1000),
    basic("ZombieMummyArmor1Default", 200, 3000, armor=["ConeDefault"]),
    basic("ZombieMummyArmor2Default", 400, 4000, armor=["BucketDefault"]),
    basic("ZombieMummyArmor4Default", 700, 3000, armor=["BrickDefault"]),
    {
        "alias": "ZombieRaDefault", "objClass": "ZombieRaProps", "scaling": "standard",
        "hitPoints": 190, "eatDps": 100, "speed": 0.2, "wavePointCost": 100,
        "weight": 700, "canSpawnPlantFood": True, "maxClaimedSunCurrency": 250,
    },
    {
        "alias": "ZombieExplorerDefault", "objClass": "ZombieExplorerProps", "scaling": "standard",
        "hitPoints": 250, "eatDps": 100, "speed": 0.25, "wavePointCost": 250,
        "weight": 3000, "canSpawnPlantFood": True, "maxTorchReach": 37,
    },
    {
        "alias": "ZombieTombRaiserDefault", "objClass": "ZombieTombRaiserProps", "scaling": "standard",
        "hitPoints": 380, "eatDps": 100, "speed": 0.185, "wavePointCost": 300,
        "weight": 2000, "canSpawnPlantFood": True,
        "ammo": 5, "numberOfTombsToSpawn": 2, "timeBetweenRaisings": 6,
    },
    gargantuar("ZombieEgyptGargantuar", "ZombieEgyptImpDefault"),
    imp("ZombieEgyptImpDefault"),

    # ── Frostbite Caves ──────────────────────────────────────────────────
    basic("ZombieIceageDefault", 100, 1000),
    basic("ZombieIceageArmor1Default", 200, 3000, armor=["ConeDefault"]),
    basic("ZombieIceageArmor2Default", 400, 4000, armor=["BucketDefault"]),
    {
        "alias": "ZombieIceAgeHunter", "objClass": "ZombieIceAgeHunterProps", "scaling": "standard",
        "hitPoints": 700, "eatDps": 100, "speed": 0.12, "wavePointCost": 500,
        "weight": 3500, "canSpawnPlantFood": True,
        "snowballsPerBarrage": 3, "farAttackRange": 4, "nearAttackRange": 1,
    },
    {
        "alias": "ZombieIceAgeTroglobite", "objClass": "ZombieIceAgeTroglobiteProps", "scaling": "standard",
        "hitPoints": 470, "eatDps": 100, "speed": 0.185, "wavePointCost": 600,
        "weight": 3500, "canSpawnPlantFood": True,
        "impType": "ZombieIceageImpDefault", "numberOfIceblocksToSpawnWith": 3,
    },
    {
        "alias": "ZombieIceAgeDodo", "objClass": "ZombieIceAgeDodoProps", "scaling": "standard",
        "hitPoints": 490, "eatDps": 100, "speed": 0.3, "wavePointCost": 600,
        "weight": 3500, "canSpawnPlantFood": True,
    },
    gargantuar("ZombieIceAgeGargantuar", "ZombieIceageImpDefault"),
    imp("ZombieIceageImpDefault"),

    # ── Big Wave Beach ───────────────────────────────────────────────────
    basic("ZombieBeachDefault", 100, 1000),
    basic("ZombieBeachArmor1Default", 200, 3000, armor=["ConeDefault"]),
    basic("ZombieBeachArmor2Default", 400, 4000, armor=["BucketDefault"]),
    {
        "alias": "ZombieBeachSnorkel", "objClass": "ZombieBeachSnorkelProps", "scaling": "standard",
        "hitPoints": 350, "eatDps": 100, "speed": 0.185, "wavePointCost": 200,
        "weight": 3000, "canSpawnPlantFood": True,
    },
    {
        "alias": "ZombieBeachFisherman", "objClass": "ZombieBeachFishermanProps", "scaling": "standard",
        "hitPoints": 1000, "eatDps": 100, "speed": 0.185, "wavePointCost": 700,
        "weight": 2500, "canSpawnPlantFood": True,
    },
    {
        "alias": "ZombieBeachOctopus", "objClass": "ZombieBeachOctopusProps", "scaling": "standard",
        "hitPoints": 910, "eatDps": 100, "speed": 0.12, "wavePointCost": 900,
        "weight": 3500, "canSpawnPlantFood": True,
    },
    gargantuar("ZombieBeachGargantuar", "ZombieBeachImpDefault"),
    imp("ZombieBeachImpDefault"),

    # ── Dark Ages ─────────────────────────────────────────────────────────
    basic("ZombieDarkDefault", 100, 1000),
    basic("ZombieDarkArmor1Default", 200, 3000, armor=["ConeDefault"]),
    basic("ZombieDarkArmor2Default", 400, 4000, armor=["BucketDefault"]),
    basic("ZombieDarkArmor3Default", 550, 4500, armor=["ShoulderArmorDefault", "CrownDefault"]),
    basic("ZombieDarkArmor4Default", 700, 3000, armor=["BrickDefault"]),
    {
        "alias": "ZombieWizardDefault", "objClass": "ZombieDarkWizardProps", "scaling": "standard",
        "hitPoints": 490, "eatDps": 100, "speed": 0.12, "wavePointCost": 800,
        "weight": 3500, "canSpawnPlantFood": True,
    },
    {
        "alias": "ZombieDarkJugglerDefault", "objClass": "ZombieDarkJugglerProps", "scaling": "standard",
        "hitPoints": 420, "eatDps": 100, "speed": 0.2, "wavePointCost": 450,
        "weight": 3500, "canSpawnPlantFood": True,
    },
    {
        "alias": "ZombieDarkKing", "objClass": "ZombieDarkKingProps", "scaling": "standard",
        "hitPoints": 1000, "eatDps": 100, "speed": 0.185, "wavePointCost": 750,
        "weight": 2000, "canSpawnPlantFood": True,
    },
    gargantuar("ZombieDarkGargantuar", "ZombieDarkImpDefault"),
    imp("ZombieDarkImpDefault", wave_cost=150, weight=2000),

    # ── Zombosses ─────────────────────────────────────────────────────────
    zomboss("ZombieZombossMechEgypt", "ZombieZombossMechEgyptProps", 18500),
    zomboss("ZombieZombossMechDark", "ZombieZombossMechDarkProps", 27000),
    zomboss("ZombieZombossMechCowboy", "ZombieZombossMechCowboyProps", 25500),
    zomboss("ZombieZombossMechPirate", "ZombieZombossMechPirateProps", 26500),
]


def main():
    document = {
        "scalingPresets": SCALING_PRESETS,
        "armors": ARMORS,
        "zombies": ZOMBIES,
    }
    OUT_PATH.parent.mkdir(parents=True, exist_ok=True)
    with OUT_PATH.open("w", encoding="utf-8") as f:
        json.dump(document, f, ensure_ascii=False, indent=2)
    print(f"Wrote {len(ZOMBIES)} zombies and {len(ARMORS)} armor sheets to {OUT_PATH}")


if __name__ == "__main__":
    sys.exit(main())
