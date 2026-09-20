<div align="center">

# 🌻 Plants vs. Zombies 2 — Java Edition

### A full faithful recreation of PvZ2 built from scratch with [libGDX](https://libgdx.com/)

[![Java](https://img.shields.io/badge/Java-21-007396?logo=openjdk&logoColor=white)](https://www.oracle.com/java/technologies/)
[![libGDX](https://img.shields.io/badge/libGDX-1.14.2-e84d0e?logo=java&logoColor=white)](https://libgdx.com/)
[![Gradle](https://img.shields.io/badge/Gradle-9.6.1-02303A?logo=gradle&logoColor=white)](https://gradle.org/)
[![LWJGL](https://img.shields.io/badge/LWJGL-3.4.1-3b3b5a?logo=java&logoColor=white)](https://www.lwjgl.org/)
[![Gson](https://img.shields.io/badge/Gson-2.11.0-5f259f?logo=java&logoColor=white)](https://github.com/google/gson)
[![GraalVM](https://img.shields.io/badge/GraalVM%20Native-optional-0979C2?logo=openjdk&logoColor=white)](https://www.graalvm.org/)
[![Platform](https://img.shields.io/badge/platform-Windows%20%7C%20macOS%20%7C%20Linux-lightgrey)]()
[![Build](https://img.shields.io/badge/build-passing-brightgreen)]()

> The zombies are coming… the lawn is doomed… and only the plants can save the day.
> **69 plants. 56 zombies. 4 chapters. 11 game modes. And full online PvP.**

</div>

---

## 📖 Table of Contents

- [✨ Highlights](#-highlights)
- [🎬 Screenshots](#-screenshots)
- [🎥 Gameplay Videos](#-gameplay-videos)
- [🎮 Features](#-features)
- [🌍 Online Multiplayer](#-online-multiplayer)
- [🗺️ Chapters & Game Modes](#-chapters--game-modes)
- [🏗️ Architecture](#-architecture)
- [🚀 Getting Started](#-getting-started)
- [🎹 Controls](#-controls)
- [🛠️ Build for Production](#-build-for-production)
- [📁 Project Structure](#-project-structure)
- [👥 Contributors](#-contributors)

---

## ✨ Highlights

- 🧠 **A real brain inside the game** — every plant and zombie is a state machine with config-driven stats, armor systems, and skill mechanics (Gargantuar slams, Octopus binding, Wizard zaps, Tomb Raiser gravestones…).
- 🎬 **Original PvZ2 animations** — 780+ PAM atlas sheets and the libPVZ animation engine bring the original game's sprites to life.
- 🌊 **Living chapters** — Ancient Egypt, Frostbite Caves, Big Wave Beach and Dark Ages each with their own stage maps, hazards (sandstorms ❄️ cold winds 🌊 waves 🔥 hot potatoes) and boss battles against the mighty **Zomboss**.
- 💥 **11 game modes** — Standard, I, Zombie, Conveyor Belt, Vasebreaker, Wall-nut Bowling, Beghouled, Timed War, Save Our Seeds, Deadline, Scored and more.
- 🎫 **Full game economies** — Coins, diamonds, plant food, a daily-offer shop, a season system and a quest log with unlockable rewards.
- 🕹️ **Real-time online PvP** — a standalone TCP server handles accounts, friend invites and random matchmaking, then pits you against another human in I, Zombie.

---

## 🎬 Screenshots

> Real captures straight from the running game — no mockups, no concept art.
> All shots were taken in-game from the `PvZ_Footages/` folder.

<div align="center">

### 🏠 Menus & Meta

| The Main Menu 🏡 | Login & Registration 🔐 |
|---|---|
| <img src="PvZ_Footages/main_menu.png" width="380" alt="The main menu"> | <img src="PvZ_Footages/login_menu.png" width="380" alt="Login & registration screen"> |

| Your Profile 👤 | In-Game News 📰 |
|---|---|
| <img src="PvZ_Footages/profile_menu.png" width="380" alt="Player profile screen"> | <img src="PvZ_Footages/news_menu.png" width="380" alt="In-game news screen"> |

| Daily Offers Shop 🛒 | Travel Log — Quests 🗺️ |
|---|---|
| <img src="PvZ_Footages/shop_menu.png" width="380" alt="Shop with daily offers"> | <img src="PvZ_Footages/travellog_menu.png" width="380" alt="Travel Log quest screen"> |

| The Collection — Plants 🌱 | The Collection — Zombies 🧟 |
|---|---|
| <img src="PvZ_Footages/collection_menu.png" width="380" alt="Plant collection screen"> | <img src="PvZ_Footages/collection_menu_2.png" width="380" alt="Zombie collection screen"> |

| The Zen Garden (Greenhouse) 🌷 | Ancient Egypt — Level Select 🏺 |
|---|---|
| <img src="PvZ_Footages/greenhouse_menu.png" width="380" alt="Zen Garden greenhouse screen"> | <img src="PvZ_Footages/ancient_egypt_level_menu.png" width="380" alt="Ancient Egypt level select"> |

| Ancient Egypt — Meet the NPC 🧙 |
|---|
| <img src="PvZ_Footages/npc_ancient_egypt.png" width="380" alt="Ancient Egypt story NPC"> |

</div>

<div align="center">

### ⚔️ Gameplay — The 4 Chapters

| Ancient Egypt — Holding the Line 🏺 | Ancient Egypt — Battle Action 🏺 |
|---|---|
| <img src="PvZ_Footages/gameplay_ancient_egypt_1.png" width="380" alt="Gameplay in Ancient Egypt"> | <img src="PvZ_Footages/gameplay_ancient_egypt_2.png" width="380" alt="More gameplay in Ancient Egypt"> |

| Frostbite Caves — Chilly Defense ❄️ | Frostbite Caves — Freezing Front ❄️ |
|---|---|
| <img src="PvZ_Footages/gameplay_frostbite_caves_1.png" width="380" alt="Gameplay in Frostbite Caves"> | <img src="PvZ_Footages/gameplay_frostbite_caves_2.png" width="380" alt="More gameplay in Frostbite Caves"> |

| Frostbite Caves — Close Call ❄️ | Big Wave Beach — Riding the Surge 🌊 |
|---|---|
| <img src="PvZ_Footages/gameplay_frostbite_caves_3.png" width="380" alt="Third gameplay shot in Frostbite Caves"> | <img src="PvZ_Footages/gameplay_big_wave_beach.png" width="380" alt="Gameplay in Big Wave Beach"> |

| Big Wave Beach — High Tide 🌊 | Big Wave Beach — Deeper Waters 🌊 |
|---|---|
| <img src="PvZ_Footages/gameplay_big_wave_beach_2.png" width="380" alt="More gameplay in Big Wave Beach"> | <img src="PvZ_Footages/gameplay_big_wave_beach_3.png" width="380" alt="Third gameplay shot in Big Wave Beach"> |

| Dark Ages — Medieval Madness 🏰 | Dark Ages — Knight's Charge 🏰 |
|---|---|
| <img src="PvZ_Footages/gameplay_dark_ages_1.png" width="380" alt="Gameplay in Dark Ages"> | <img src="PvZ_Footages/gameplay_dark_ages_2.png" width="380" alt="More gameplay in Dark Ages"> |

</div>

<div align="center">

### 🎭 Game Modes

| Vasebreaker — Break & Sweat 🎭 |
|---|
| <img src="PvZ_Footages/gameplay_vase_breaker_1.png" width="380" alt="Vasebreaker game mode gameplay"> |

</div>

## 🎥 Gameplay Videos

> 🎞️ Real gameplay recordings — now streaming from YouTube.

<div align="center">

### 🌊 Big Wave Beach — Gameplay

<iframe width="560" height="315" src="https://www.youtube.com/embed/UovvqkaO-XM" frameborder="0" allow="accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture" allowfullscreen></iframe>

*More chapter gameplay coming soon…*

</div>

---

## 🎮 Features

### 🌱 The Plant Roster (69 types)
Sunflowers, Twin Sunflowers, Sunshrooms, Repeaters, Cabbepersons… and of course the classics: Cherry Bomb 💣, Jalapeno 🔥, Doom-shroom ☠️, Wall-nut, Torchwood, Chomper, Potato Mine and the mighty Mints.

Each with its own **actions** (shooters, lobbers, melee, explosives, supports) powered by **23 behavior classes** and **30 JSON config loaders**.

### 🧟 The Zombie Horde (56 types)
From the humble Conehead to the terrifying **Gargantuar**, through Mummy, Explorer, Troglobite, Snorkel, Wizard, Juggler, King… and the four **Zomboss** bosses — each chapter has a final showdown.

Zombies come with:
- 🛡️ A layered **armor system** (cone → bucket → brick, and everything in between)
- 🌀 **Zombie skills** — torch-lighting Explorers, ice-tossing Hunters, sun-stealing Ra, grave-raising Tomb Raisers
- **13 FSM states** — walking, eating, freezing, ice-blocked, sandstorm-carried, underwater-dragged…

### 🏟️ The Board & Environment
- 5-row lawns with water tiles 🌊, graves ⚰️, ice ❄️ and slippery tiles
- Tile behaviors: Lily Pads, Grave Busters, hot potato delivery, octopus traps
- Chapter effects: **sandstorms**, **cold wind**, **big-wave surges**, **dark-ages fog**
- Lawn Mowers 🚜 as the last line of defense

### 🏡 The Zen Garden (Greenhouse)
Buy pots, plant seeds, water them, and harvest coins from **275+ plant icons** and a full grow/harvest cycle.

### 🏆 Meta Progression
- **Leaderboards** — global rankings sorted by progress, minigames, daily & score
- **Quests** (`Travel Log`) — categories, priorities and currency/inventory/unlock rewards
- **Shop** — daily offers, currency exchange, plant food, seed packets, extra pot slots
- **Settings** — difficulty, game speed, grid toggle, brightness, music & SFX volumes

---

## 🌍 Online Multiplayer

`PvZ2` comes with its own lightweight TCP server (port `5454`) in a `java -cp`-runnable class — **no database, no cloud, just pure Java + Gson**.

| Capability | What it does |
|---|---|
| ✍️ **Account system** | Register, login, session persistence, password recovery via security questions, profile editing |
| 👋 **Friend invites** | Send an invite by username, pick your role (Plant or Zombie) |
| 🎲 **Random matchmaking** | Join a FIFO queue and get paired with whoever's waiting |
| ⚔️ **Online I, Zombie PvP** | Play as the zombies against a real human opponent, with quick-chat and sticker reactions |

The protocol is a clean **JSON-line** protocol over TCP, mirrored by `MessageType` (18 message types) and `MatchDTOs`.

---

## 🗺️ Chapters & Game Modes

### The 4 Adventure Chapters

| 🌴 Chapter | Enemies | Boss |
|---|---|---|
| **Ancient Egypt** | Mummies, Explorers, Tomb Raisers, Sun-stealing Ra | 🐘 Zomboss Egypt |
| **Frostbite Caves** | Ice Ages, Hunters, Dodo riders, Troglobites | ❄️ Zomboss (Caves) |
| **Big Wave Beach** | Snorkels, Fishermen, Octopus throwers | 🌊 Zomboss (Beach) |
| **Dark Ages** | Knights, Wizards, Juggling zombies, the King | 🏰 Zomboss Dark |

### The 11 Game Modes

| Mode | Blurb |
|---|---|
| **Normal** | The classic tower-defense lawn |
| **I, Zombie** 🧟 | *You* play the zombies and eat brains |
| **Conveyor Belt** | The shovel radar hands you random seeds |
| **Vasebreaker** | Break vases — hope you found a shooter 🎭 |
| **Wall-nut Bowling** | Bowl endless wall-nuts down the lane 🎳 |
| **Beghouled** | Match-3 plant swapping |
| **Timed War** | Survive on the clock ⏱️ |
| **Save Our Seeds** | Protect the endangered plants |
| **Deadline** | Race against death (literally) |
| **Scored** | Chase the high score |
| **Split I, Zombie** | Local vs. split-screen zombie play |

---

## 🏗️ Architecture

```
┌───────────────────────────────────────────────┐
│            libGDX Client (core + lwjgl3)      │
│                                               │
│  view/        scene2d screens & menus         │
│  controller/  game loop, FSM game states      │
│  models/      entities, engine, modes, levels │
│  network/     JSON-line TCP client, DTOs      │
│                                               │
└──────────────────┬────────────────────────────┘
                   │ TCP :5454
┌──────────────────▼────────────────────────────┐
│  PvzServer (standalone, no libGDX needed)     │
│  │  accounts • username index • leaderboard   │
│  │  matchmaking registry • invites • queue    │
│  └── JSON save files (Gson) ──► users/*.json  │
└───────────────────────────────────────────────┘
```

- 💾 **Data-driven** — plant/zombie/config maps, animations and levels are all JSON.
- 🎭 **FSM everywhere** — plants, zombies, projectiles and screens all run on finite state machines.
- 🧵 **Thread-safe server** — `ACCOUNTS_LOCK` guards the shared username index; per-client daemon threads.
- 🧪 **Game stats** — every match tracks sun collected, kills by plant, timings and more.

---

## 🚀 Getting Started

### Prerequisites
- **Java 21** (the Gradle toolchain can auto-download it via Foojay)
- **Gradle 9.6+** (or just use the bundled wrapper)

### 1️⃣ Start the server
```bash
gradlew :core:runServer
```
The server listens on port `5454` and stores accounts under `core/src/main/java/com/pvz/saves/`.

### 2️⃣ Run the game
```bash
gradlew lwjgl3:run
```

Or build a runnable JAR:
```bash
gradlew lwjgl3:jar
# → lwjgl3/build/libs/
```

---

## 🎹 Controls

| Action | Input |
|---|---|
| Click / tap | Select, plant, collect sun ☀️ |
| Drag | Place plants on the lawn, scroll the conveyor belt |
| Back / close | Screen corner buttons |
| Esc-in-game | Pause menu |

---

## 🛠️ Build for Production

Cross-platform fat-JARs are wired up for **Windows, macOS and Linux**:

| Task | Output |
|---|---|
| `gradlew lwjgl3:jar` | Cross-platform runnable JAR |
| `gradlew lwjgl3:jarWin` / `jarMac` / `jarLinux` | Platform-native JARs |
| `gradlew lwjgl3:package` *(Construo)* | Native jlink images + installers |
| `gradlew lwjgl3:run` | Launch straight from source |

> Want native images? Flip `enableGraalNative=true` in `gradle.properties` (GraalVM + SVM scaffolding included).

---

## 📁 Project Structure

```
plantsvszombies2/
 ├─ core/          # platform-independent game logic (the whole game)
 │  └─ src/main/java/com/pvz/
 │     ├─ view/          # 20+ scene2d screens & modals
 │     ├─ controller/    # game loop, chapters, greenhouse, shop…
 │     ├─ models/        # entities, engine, modes, levels, user, quests…
 │     ├─ network/       # TCP client, DTOs, message types
 │     ├─ server/        # standalone PvzServer + matchmaking
 │     └─ utils/         # SaveManager, PasswordUtils, avatars
 ├─ lwjgl3/        # desktop launcher (LWJGL3 backend)
 ├─ assets/        # textures, audio, skins, PAM animation catalogs
 ├─ PvZ_Footages/  # screenshots & gameplay recordings
 └─ app/           # (placeholder)
```

---

## 👥 Contributors

A big thank-you to the team behind this garden 🌻:

| Contributor | Student ID |
|---|---|
| **Hossein Hosseini Nezhad** | 404105756 |
| **Yousof Rahimzadeh** | 404105853 |
| **Mahdi Shakeri** | 404105959 |

---

<div align="center">

*Made with 💚, ☕, and a lot of sunflower seeds.*
*No zombies were harmed in the making of this game… except the ones on your lawn.*

</div>