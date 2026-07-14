#!/usr/bin/env python3
"""One-off generator: plants.csv -> app/src/main/resources/plant_profiles.json

This script is NOT part of the shipped application. It is a build-time
authoring aid used once to transform the human-authored CSV dataset into the
structured, data-driven JSON schema consumed at runtime by
pvz.Models.Entities.Plants.data.PlantRegistry.
"""
import csv
import json
import re
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
CSV_PATH = ROOT / "plants.csv"
OUT_PATH = ROOT / "app" / "src" / "main" / "resources" / "plant_profiles.json"

# Persian / Extended-Arabic-Indic digits -> ASCII
PERSIAN_DIGITS = "۰۱۲۳۴۵۶۷۸۹"
ARABIC_DIGITS = "٠١٢٣٤٥٦٧٨٩"
DIGIT_MAP = {ord(p): str(i) for i, p in enumerate(PERSIAN_DIGITS)}
DIGIT_MAP.update({ord(a): str(i) for i, a in enumerate(ARABIC_DIGITS)})


def normalize_digits(s: str) -> str:
    return s.translate(DIGIT_MAP)


CATEGORY_MAP = {
    "Sun Producer": "SUN_PRODUCER",
    "Shooter": "SHOOTER",
    "Homing": "HOMING",
    "Strike-through": "STRIKE_THROUGH",
    "Lobber": "LOBBER",
    "Explosive": "EXPLOSIVE",
    "Melee": "MELEE",
    "Wall-nut": "WALL_NUT",
    "Modifier": "MODIFIER",
}

TAG_MAP = {
    "day": "DAY",
    "night": "NIGHT",
    "shroom": "SHROOM",
    "wramp-up": "WRAMP_UP",
    "pea": "PEA",
    "ice": "ICE",
    "fire": "FIRE",
    "stack": "STACK",
    "charge": "CHARGE",
    "magic": "MAGIC",
    "poison": "POISON",
    "water": "WATER",
    "aoe": "AOE",
    "trap": "TRAP",
    "movezombies": "MOVE_ZOMBIES",
    "sun": "SUN",
    "explosive": "EXPLOSIVE",
}

# CSV "Name" -> PlantType enum constant (hand-mapped; CSV names use spaces/hyphens)
ID_TO_TYPE = {
    1: "Sunflower", 2: "TwinSunflower", 3: "Sunshroom", 4: "PrimalSunflower", 5: "GoldBloom",
    6: "Peashooter", 7: "Repeater", 8: "Threepeater", 9: "SnowPea", 10: "Rotobaga",
    11: "PeaPod", 12: "SplitPea", 13: "Citron", 14: "Caulipower", 15: "ElectricBlueberry",
    16: "BowlingBulb", 17: "Cactus", 18: "FirePeashooter", 19: "Starfruit", 20: "GooPeashooter",
    21: "MegaGatlingPea", 22: "Seashroom", 23: "Puffshroom", 24: "Fumeshroom", 25: "Cabbagepult",
    26: "Kernelpult", 27: "Melonpult", 28: "WinterMelon", 29: "Pepperpult", 30: "PotatoMine",
    31: "PrimalPotatoMine", 32: "CherryBomb", 33: "Squash", 34: "Grapeshot", 35: "Jalapeno",
    36: "Doomshroom", 37: "TangleKelp", 38: "IcebergLettuce", 39: "BonkChoy", 40: "PhatBeet",
    41: "Chomper", 42: "WasabiWhip", 43: "Kiwibeast", 44: "Wallnut", 45: "Tallnut",
    46: "Endurian", 47: "Garlic", 48: "SweetPotato", 49: "Explodeonut", 50: "Pumpkin",
    51: "SunBean", 52: "Torchwood", 53: "Magnetshroom", 54: "Hypnoshroom", 55: "Cattail",
    56: "Imitater", 57: "Iceshroom", 58: "LilyPad", 59: "HotPotato", 60: "GraveBuster",
    61: "Enlightenmint", 62: "Appeasemint", 63: "Armamint", 64: "Bombardmint", 65: "Enforcemint",
    66: "Reinforcemint", 67: "Enchantmint", 68: "Piercemint", 69: "catTailmint",
}

MINT_IDS = set(range(61, 70))

# Per-ID plant-food profile overrides (kind + magnitude), curated from domain
# knowledge + the source dataset's flavor text. Unlisted plants fall back to a
# sensible category default (see `default_plant_food`).
PLANT_FOOD_OVERRIDES = {
    1: {"kind": "INSTANT_SUN", "amount": 150},
    2: {"kind": "INSTANT_SUN", "amount": 250},
    3: {"kind": "INSTANT_SUN", "amount": 225},
    4: {"kind": "INSTANT_SUN", "amount": 225},
    5: {"kind": "NONE"},
    7: {"kind": "RAPID_FIRE", "durationSeconds": 5, "flags": ["GIANT_PEA_BONUS"]},
    13: {"kind": "AOE_BURST", "count": 0, "flags": ["CLEAR_LANE"]},
    14: {"kind": "HYPNOTIZE", "count": 3},
    15: {"kind": "MULTI_INSTAKILL", "count": 3},
    16: {"kind": "AOE_BURST", "count": 3},
    22: {"kind": "RAPID_FIRE", "durationSeconds": 5, "flags": ["RESET_LIFESPAN_ALL"]},
    23: {"kind": "RAPID_FIRE", "durationSeconds": 5, "flags": ["RESET_LIFESPAN_ALL"]},
    30: {"kind": "CLONE_SELF", "count": 2, "flags": ["INSTANT_ARM"]},
    31: {"kind": "CLONE_SELF", "count": 2, "flags": ["INSTANT_ARM"]},
    32: {"kind": "NONE"},
    33: {"kind": "MULTI_INSTAKILL", "count": 2},
    34: {"kind": "NONE"},
    35: {"kind": "NONE"},
    36: {"kind": "NONE"},
    37: {"kind": "MULTI_INSTAKILL", "count": 3, "flags": ["WATER_ONLY"]},
    38: {"kind": "FREEZE_ALL"},
    41: {"kind": "MULTI_INSTAKILL", "count": 3, "flags": ["LONG_RANGE"]},
    44: {"kind": "PERMANENT_HP_BOOST", "amount": 4000},
    45: {"kind": "PERMANENT_HP_BOOST", "amount": 8000},
    46: {"kind": "PERMANENT_HP_BOOST", "amount": 1000, "flags": ["REFLECT_DAMAGE_BUFF"]},
    47: {"kind": "FORCE_MOVE_ALL_IN_LANE"},
    48: {"kind": "FULL_HEAL_AND_ABSORB"},
    49: {"kind": "PERMANENT_HP_BOOST", "amount": 1000, "flags": ["EXPLODE_ON_DESTROY"]},
    50: {"kind": "PERMANENT_HP_BOOST", "amount": 1000},
    51: {"kind": "PERMANENT_HP_BOOST", "amount": 1000, "flags": ["SUN_ON_HIT"]},
    52: {"kind": "AURA_BUFF", "amount": 3, "flags": ["TRIPLE_FIRE_DAMAGE_AURA"]},
    53: {"kind": "MULTI_DISARM", "count": 3},
    54: {"kind": "CONVERT_EATER_TO_ALLY"},
    55: {"kind": "RAPID_FIRE", "durationSeconds": 5},
    56: {"kind": "NONE"},
    57: {"kind": "NONE"},
    58: {"kind": "CLONE_SELF", "count": 3},
    59: {"kind": "NONE"},
    60: {"kind": "NONE"},
}
for _mint_id in MINT_IDS:
    PLANT_FOOD_OVERRIDES.setdefault(_mint_id, {"kind": "NONE", "flags": ["CONSUMED_AS_FAMILY_BUFF_TRIGGER"]})

DEFAULT_PLANT_FOOD_BY_CATEGORY = {
    "SUN_PRODUCER": {"kind": "INSTANT_SUN", "amount": 150},
    "SHOOTER": {"kind": "RAPID_FIRE", "durationSeconds": 5},
    "STRIKE_THROUGH": {"kind": "RAPID_FIRE", "durationSeconds": 5},
    "HOMING": {"kind": "MULTI_INSTAKILL", "count": 3},
    "LOBBER": {"kind": "AOE_BURST", "count": 3},
    "EXPLOSIVE": {"kind": "NONE"},
    "MELEE": {"kind": "AOE_BURST", "count": 0},
    "WALL_NUT": {"kind": "PERMANENT_HP_BOOST", "amount": 1000},
    "MODIFIER": {"kind": "NONE"},
}

# Sun-production profile for autonomous SUN_PRODUCER plants (mints excluded).
PRODUCTION_OVERRIDES = {
    1: {"kind": "FIXED", "amount": 50},
    2: {"kind": "FIXED", "amount": 100},
    3: {"kind": "STAGED", "stages": [25, 50, 75], "stageSeconds": [24, 72]},
    4: {"kind": "FIXED", "amount": 75},
    5: {"kind": "ONESHOT", "amount": 375},
}

STAT_NAME_MAP = {
    "dmg": "DAMAGE",
    "hp": "MAX_HP",
    "cost": "SUN_COST",
    "cooldown": "RECHARGE_SECONDS",
    "recharge": "RECHARGE_SECONDS",
    "prod. time": "ACTION_INTERVAL_SECONDS",
    "prod time": "ACTION_INTERVAL_SECONDS",
    "charge time": "ACTION_INTERVAL_SECONDS",
    "grow time": "ACTION_INTERVAL_SECONDS",
    "digest": "ACTION_INTERVAL_SECONDS",
    "arm time": "ACTION_INTERVAL_SECONDS",
    "eat time": "ACTION_INTERVAL_SECONDS",
    "range": "RANGE_TILES",
    "pierce": "PIERCE_COUNT",
    "atk speed": "ATK_SPEED_PERCENT",
    "aoe dmg": "DAMAGE",
    "duration": "EFFECT_DURATION_SECONDS",
}

NUMERIC_TOKEN_RE = re.compile(r"^(?P<name>[A-Za-z.\s]+?)\s*(?P<sign>[+-])\s*(?P<num>\d+(?:\.\d+)?)\s*(?P<unit>s|%|Tile)?\.?$")


def parse_upgrade_token(token: str):
    """Returns (stat_dict_or_None, flag_str_or_None)."""
    token = token.strip()
    if token in ("", "-"):
        return None, None
    m = NUMERIC_TOKEN_RE.match(token)
    if m:
        name = m.group("name").strip().lower().rstrip(".")
        if name in STAT_NAME_MAP:
            delta = float(m.group("num"))
            if m.group("sign") == "-":
                delta = -delta
            return {"stat": STAT_NAME_MAP[name], "delta": delta}, None
    # Not a recognized numeric stat -> keep as a descriptive flag for extensibility
    return None, token


def parse_level_upgrades(row):
    upgrades = []
    for level, col in ((2, "Lvl 2"), (3, "Lvl 3"), (4, "Lvl 4")):
        raw = row[col].strip()
        if raw in ("", "-"):
            continue
        stats = []
        flags = []
        for piece in raw.split(","):
            stat, flag = parse_upgrade_token(piece)
            if stat:
                stats.append(stat)
            elif flag:
                flags.append(flag)
        upgrades.append({"level": level, "stats": stats, "flags": flags})
    return upgrades


def parse_tags(raw: str):
    raw = raw.strip()
    if raw in ("", "-"):
        return []
    out = []
    for piece in raw.split(","):
        key = piece.strip().lower()
        if key in TAG_MAP:
            out.append(TAG_MAP[key])
    return out


def parse_damage(raw: str):
    raw = raw.strip()
    if raw.lower() in ("insta-kill", "insta kill", "instakill"):
        return {"kind": "INSTA_KILL", "value": 0, "count": 1, "stages": []}
    if raw in ("", "-", "0"):
        return {"kind": "NONE", "value": 0, "count": 1, "stages": []}
    if "/" in raw:
        stages = [float(x) for x in raw.split("/")]
        return {"kind": "STAGED", "value": stages[0], "count": 1, "stages": stages}
    if "x" in raw:
        base, count = raw.split("x")
        return {"kind": "MULTI_SHOT", "value": float(base), "count": int(count), "stages": []}
    return {"kind": "FIXED", "value": float(raw), "count": 1, "stages": []}


def parse_seconds(raw: str):
    raw = raw.strip()
    if raw in ("", "-"):
        return None
    return float(raw)


def build_plant_food(plant_id: int, category: str, description: str):
    profile = dict(PLANT_FOOD_OVERRIDES.get(
        plant_id, DEFAULT_PLANT_FOOD_BY_CATEGORY.get(category, {"kind": "NONE"})
    ))
    profile.setdefault("amount", 0)
    profile.setdefault("count", 0)
    profile.setdefault("durationSeconds", 0)
    profile.setdefault("flags", [])
    profile["description"] = description
    return profile


GROWTH_STAGE_RE = re.compile(r"To Stg2:\s*(\d+)s\s*\|\s*To Stg3:\s*(\d+)s")


def build_growth(description: str):
    m = GROWTH_STAGE_RE.search(description)
    if not m:
        return None
    return {"stageSeconds": [float(m.group(1)), float(m.group(2))]}


def build_production(plant_id: int, category: str):
    if category != "SUN_PRODUCER" or plant_id in MINT_IDS:
        return None
    if plant_id not in PRODUCTION_OVERRIDES:
        return None
    profile = dict(PRODUCTION_OVERRIDES[plant_id])
    profile.setdefault("stages", [])
    profile.setdefault("stageSeconds", [])
    return profile


def main():
    with CSV_PATH.open(encoding="utf-8-sig") as f:
        reader = csv.DictReader(f)
        rows = list(reader)

    profiles = []
    for row in rows:
        plant_id = int(row["ID"])
        name = row["Name"].strip()
        category = CATEGORY_MAP[row["Category"].strip()]
        tags = parse_tags(row["Tags"])
        sun_cost = int(row["Cost"])
        base_hp = int(row["Base HP"])
        damage = parse_damage(row["Damage"])
        action_interval = parse_seconds(row["Action Interval (s)"])
        recharge = parse_seconds(row["Recharge (s)"])
        level_upgrades = parse_level_upgrades(row)
        description = normalize_digits(row["Base Ability"].strip())
        pf_description = normalize_digits(row["Plant Food Effect"].strip())
        is_mint = plant_id in MINT_IDS

        profile = {
            "id": plant_id,
            "name": name,
            "type": ID_TO_TYPE[plant_id],
            "category": category,
            "tags": tags,
            "isMint": is_mint,
            "sunCost": sun_cost,
            "baseHp": base_hp,
            "damage": damage,
            "actionIntervalSeconds": action_interval,
            "rechargeSeconds": recharge,
            "production": build_production(plant_id, category),
            "plantFood": build_plant_food(plant_id, category, pf_description),
            "levelUpgrades": level_upgrades,
            "growth": build_growth(description),
            "description": description,
        }
        profiles.append(profile)

    OUT_PATH.parent.mkdir(parents=True, exist_ok=True)
    with OUT_PATH.open("w", encoding="utf-8") as f:
        json.dump(profiles, f, ensure_ascii=False, indent=2)

    print(f"Wrote {len(profiles)} plant profiles to {OUT_PATH}")


if __name__ == "__main__":
    sys.exit(main())
