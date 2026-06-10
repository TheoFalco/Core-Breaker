# CORE BREAKER

> *Destroy the grid. Collect the energy.*

A futuristic brick-breaker game built in Java with a cyberpunk aesthetic — neon visuals, chain explosions, power-ups, and progressive difficulty across multiple levels.

---

## Table of Contents

- [Overview](#overview)
- [Features](#features)
- [Screenshots](#screenshots)
- [Project Structure](#project-structure)
- [Getting Started](#getting-started)
- [Gameplay](#gameplay)
- [Level Design](#level-design)
- [Architecture](#architecture)
- [Configuration](#configuration)
- [Roadmap](#roadmap)

---

## Overview

CORE BREAKER reimagines the classic brick-breaker formula with an energy-core universe. Every brick destroyed releases energy, chain reactions cascade across the grid, and power-ups alter the flow of play. The game scales in difficulty across levels — faster balls, harder bricks, and more complex layouts.

Built entirely in Java using `Swing` for rendering and `javax.sound.sampled` for audio. No external game engine or framework.

---

## Features

### Gameplay
- Smooth 60fps game loop via `javax.swing.Timer`
- Angle-based paddle bounce — hit position determines ball direction
- Multi-ball support (up to 20 simultaneous balls)
- Persistent high score saved between sessions

### Brick Types
| Brick | Symbol | Behavior |
|---|---|---|
| `NormalBrick` | `N` | Standard brick — may drop a power-up on destruction |
| `FireBrick` | `F` | Explodes on hit, destroying all 8 adjacent neighbors (chain reaction capable) |
| `GhostBrick` | `G` | Alternates between visible and invisible — untouchable when faded out |

### Power-ups
| Power-up | Label | Effect | Duration |
|---|---|---|---|
| Multi-Ball | `M` | Splits all active balls in two | Permanent until ball lost |
| Wide Paddle | `W` | Expands paddle width by 40px | 5 seconds |
| Piercing | `P` | Balls pass through bricks without bouncing | 5 seconds |

### Progression
- Level files loaded from plain text (`levels/levelN.txt`)
- Ball and paddle speed increase by `+0.3 px/frame` per level
- Brick HP increases by `+1` every 5 levels
- Automatic transition to the next level on grid completion

### Visual Identity
- Dark `#0D1117` background with scan-lines and vignette
- Electric blue `#00BFFF` and neon purple `#8A2BE2` color palette
- Glow effects on ball, paddle, and bricks
- Particle system on brick destruction (capped at 150 for performance)
- Animated pulsing title on the main menu

### Audio
- Sound effects for: brick hit, paddle hit, explosion, power-up, life lost, game over, victory
- Looping synthwave background music
- Toggle mute at any time with `M`

---

## Project Structure

```
CORE_BREAKER/
│
├── src/
│   └── application/
│       ├── core/               # Engine
│       │   ├── Main.java
│       │   ├── GamePanel.java
│       │   └── Theme.java
│       │
│       ├── scene/              # Game screens
│       │   ├── PlayScene.java
│       │   └── MenuScene.java
│       │
│       ├── entity/             # Game objects
│       │   ├── Ball.java
│       │   ├── Paddle.java
│       │   └── Particle.java
│       │
│       ├── brick/              # Brick hierarchy
│       │   ├── Brick.java
│       │   ├── NormalBrick.java
│       │   ├── FireBrick.java
│       │   └── GhostBrick.java
│       │
│       ├── system/             # Game systems
│       │   ├── CollisionSystem.java
│       │   ├── ScoreManager.java
│       │   ├── SoundManager.java
│       │   ├── LevelLoader.java
│       │   └── DifficultyConfig.java
│       │
│       └── powerup/            # Power-ups
│           └── PowerUp.java
│
├── levels/                     # Level layout files
│   ├── level1.txt
│   ├── level2.txt
│   └── level3.txt
│
├── sounds/                     # Audio files (.wav)
│   ├── hit_brick.wav
│   ├── hit_paddle.wav
│   ├── explode.wav
│   ├── powerup.wav
│   ├── life_lost.wav
│   ├── game_over.wav
│   ├── victory.wav
│   └── music.wav
│
├── highscore.txt               # Auto-generated on first game over
└── README.md
```

---

## Getting Started

### Prerequisites

- Java 17 or higher
- Any IDE with Java support (Eclipse, IntelliJ IDEA, VS Code)

### Run in Eclipse

1. Clone or download the repository
2. Open Eclipse → `File` → `Import` → `Existing Projects into Workspace`
3. Select the project root folder
4. Run `Main.java` as a Java Application

### Run from terminal

```bash
# Compile
javac -d bin -sourcepath src src/application/core/Main.java

# Run (from project root — required for levels/ and sounds/ paths)
java -cp bin application.core.Main
```

> **Important:** always run from the project root directory so that relative paths to `levels/` and `sounds/` resolve correctly.

---

## Gameplay

### Controls

| Key | Action |
|---|---|
| `←` / `→` | Move paddle left / right |
| `SPACE` | Charge special shot |
| `ESC` | Pause / Resume |
| `R` | Restart *(only available on Game Over or Victory)* |
| `M` | Mute / Unmute audio |
| `ENTER` | Start game *(from main menu)* |

### Scoring

Points are awarded for each brick destroyed, multiplied by the current combo multiplier.

```
Score += brick.points × multiplier
```

The multiplier increases by 1 for every 5 consecutive bricks destroyed without losing a ball. Losing a ball resets the combo to 0.

| Combo | Multiplier |
|---|---|
| 0 – 4 | ×1 |
| 5 – 9 | ×2 |
| 10 – 14 | ×3 |
| … | … |

---

## Level Design

Levels are plain text files in the `levels/` folder. Each file represents one grid:

```
N N F N N N F N N N
N G N N F N N N G N
F N N N N N N N N F
N N G N N F N G N N
N N N N N N N N N N
```

### Symbol reference

| Symbol | Brick |
|---|---|
| `N` | NormalBrick |
| `F` | FireBrick |
| `G` | GhostBrick |
| `.` | Empty cell |

To add a new level, create `levels/levelN.txt` where `N` is the next number in sequence. `LevelLoader.getLevelCount()` detects it automatically.

---

## Architecture

### Game loop

```
Swing Timer (16ms) → update() → repaint() → paintComponent() → render()
```

### State machine

```
MENU ──[ ENTER ]──► PLAYING ──[ ESC ]──► PAUSED
  ▲                    │                    │
  │                    │[ Game Over ]        │[ ESC ]
  └────────────────────▼                    │
                    GAME OVER ◄─────────────┘
                    VICTORY
                    [ R ] → new PlayScene
```

### Key design patterns

| Pattern | Where |
|---|---|
| Singleton | `ScoreManager`, `SoundManager` |
| Template Method | `Brick` (abstract `onDestroy()`) |
| Strategy | `CollisionSystem` (static utility) |
| Observer (implicit) | `keys[]` shared between `GamePanel` and `Paddle` |

---

## Configuration

All difficulty values are centralized in `DifficultyConfig.java`:

```java
BASE_BALL_SPEED   = 4.5   // px/frame at level 1
BASE_PADDLE_SPEED = 6     // px/frame at level 1
SPEED_INCREMENT   = 0.3   // added per level
```

All colors, fonts, and visual constants are in `Theme.java`.

---

## Roadmap

- [ ] Sound files integration
- [ ] Additional brick types (HardBrick, ShieldBrick)
- [ ] Additional power-ups (Slow Motion, Ball Shield)
- [ ] Level transition animation
- [ ] High score leaderboard (top 5)
- [ ] Procedurally generated bonus levels