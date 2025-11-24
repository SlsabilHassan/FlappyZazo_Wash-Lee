# 🎮 Flappy Bird @ W&L University Edition

<div align="center">

![Java](https://img.shields.io/badge/Java-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)
![Swing](https://img.shields.io/badge/Swing-GUI-blue?style=for-the-badge)
![Game](https://img.shields.io/badge/Game-Flappy%20Bird-yellow?style=for-the-badge)

*A feature-rich, special version of the classic Flappy Bird game built with Java Swing*

</div>

## 🎮 Gameplay Demo

<div align="center">
  <img src="screenshots/gameplay-demo.gif" width="300">
  
  *Slowed down for the GIF - the real game is way faster and more intense!*
</div>

---

## 📋 Table of Contents
- [Overview](#-overview)
- [Features](#-features)
- [Game Mechanics](#-game-mechanics)
- [Power-Up System](#-power-up-system)
- [Controls](#-controls)
- [Technical Architecture](#-technical-architecture)
- [Installation & Setup](#-installation--setup)
- [Code Structure](#-code-structure)

---

## 🌟 Overview

Flappy Bird+ is an ambitious reimagining of the FlappyBird but at Washington and Lee University famous colonnade with me as the Bird

Built entirely in Java using the Swing framework, this project demonstrates game development concepts including:
- Real-time physics simulation
- Dynamic difficulty scaling
- Particle effect systems
- AI-powered gameplay automation
- Persistent data storage
- Object-oriented game architecture

---

## ✨ Features

#### **Progressive Difficulty System**
The game intelligently adapts to your skill level:
- Pipe velocity increases gradually as your score climbs
- Velocity increment: +0.5 every 10 points
- Maximum speed cap at -7 pixels/frame
- Smooth difficulty curve ensures extended gameplay sessions

#### **Combo System**
- Combo counter activates after passing 2+ pipes without touching the ground
- Bonus scoring: Base 0.5 points + (combo multiplier × 0.5)
- Example: 5-combo grants 3 bonus points per pipe (0.5 + 5×0.5)
- Visual feedback with yellow combo indicator
- Max combo tracking displayed on game over screen

#### **Persistent High Score System**
Your achievements are permanently saved:
- Automatic high score detection and storage
- Loads previous best score on game startup

#### **Particle Effect Engine**
- **Yellow particles**: Successful pipe navigation
- **Cyan particles**: Shield absorption of damage
- **Red particles**: Collision/game over events
- Physics-based particle behavior with gravity simulation
- 30-frame lifespan with alpha fade for smooth disappearance
- Random velocity vectors create organic-looking effects
---

## 🎮 Game Mechanics

### Physics Engine
The game implements a realistic gravity-based physics system:

```
Gravity: 1 pixel/frame²
Jump Velocity: -9 pixels/frame
Horizontal Pipe Speed: -4 to -7 pixels/frame (adaptive)
Bird Dimensions: 51×36 pixels
Pipe Dimensions: 64×512 pixels
Gap Size: boardHeight/4 (160 pixels)
```

### Collision Detection
Axis-Aligned Bounding Box (AABB) collision system:
- Precise hitbox detection for bird-pipe interactions
- Separate collision detection for power-up collection
- Shield system uses per-pipe collision tracking to prevent multi-frame triggering

### Scoring System
Multi-layered scoring rewards skilled play:
1. **Base Score**: 0.5 points per pipe (1 point per pipe pair)
2. **Combo Bonus**: (combo × 0.5) additional points when combo > 2
3. **Power-Up Bonus**: +5 instant points from green power-ups

Example scoring at 10× combo:
- Base: 0.5
- Combo Bonus: 5.0
- **Total per pipe: 5.5 points**

---

## 🔮 Power-Up System

### Shield Power-Up (Blue "S")
**The Guardian Angel**

- **Visual**: Cyan bubble surrounding the bird
- **Duration**: 5 seconds OR until collision
- **Effect**: Absorbs one pipe collision without triggering game over
- **Mechanics**:
  - Per-pipe collision tracking prevents multi-frame bugs
  - Creates spectacular cyan particle burst on absorption
  - Timer displayed in top-left corner
  - Strategic collection can enable risky maneuvers

**Use Case**: Navigate through tight gaps or recover from positioning mistakes

### Points Power-Up (Green "+")
**The Score Booster**

- **Visual**: Green circle with "+" symbol
- **Effect**: Instant +5 points added to score
- **Mechanics**:
  - Immediate score addition upon collection
  - Green particle explosion confirms collection
  - No duration—instant gratification

---

## 🎯 Controls

| Key | Action | Context |
|-----|--------|---------|
| **SPACE** | Jump / Start Game / Restart | Primary action button |
| **A** | Toggle AI Autopilot | Available anytime |
| **P** | Pause/Resume | Only during active gameplay |

### Control Philosophy
Minimal, intuitive controls ensure accessibility while maintaining gameplay depth. Single-button jump mechanic (SPACE) puts emphasis on timing and positioning rather than complex input combinations.

---

## 🏗️ Technical Architecture

### Class Structure

#### **FlappyBird (Main Panel)**
The central orchestrator extending `JPanel`:
- Implements `ActionListener` for game loop
- Implements `KeyListener` for user input
- Manages all game state and rendering
- Coordinates between subsystems

#### **Inner Classes**

**Bird Class**
```java
- Position tracking (x, y)
- Dimensions (width, height)
- Image reference
- Shield status and timer
- Represents player entity
```

**Pipe Class**
```java
- Position and dimensions
- Image reference
- Passed flag (scoring)
- Shield usage tracking (collision system)
- Represents obstacle entities
```

**PowerUp Class**
```java
- Position and size
- Type identifier (shield/points)
- Collection status
- Represents collectible entities
```

**Particle Class**
```java
- Position and velocity vectors
- Size and color
- Lifespan counter
- Physics update method
- Represents visual effect entities
```

### Game Loop Architecture

```
┌─────────────────────────────────┐
│      Game Loop (60 FPS)         │
│   Timer: 1000/60 milliseconds   │
└────────────┬────────────────────┘
             │
             ▼
┌─────────────────────────────────┐
│   actionPerformed() Method      │
└────────────┬────────────────────┘
             │
    ┌────────┴────────┐
    ▼                 ▼
┌─────────┐      ┌──────────┐
│ AutoPlay│      │  Manual  │
│  Logic  │      │  Control │
└────┬────┘      └─────┬────┘
     └──────┬──────────┘
            ▼
    ┌───────────────┐
    │   move()      │
    │   Update      │
    │   Physics     │
    └───────┬───────┘
            ▼
    ┌─────────────── ┐
    │updatePowerUps()│
    │   Collect &    │
    │   Move Items   │
    └───────┬─────── ┘
            ▼
    ┌───────────────┐
    │updateParticles()│
    │   Animate     │
    │   Effects     │
    └───────┬───────┘
            ▼
    ┌───────────────┐
    │   repaint()   │
    │   Render All  │
    └───────────────┘
```
---
## 💻 Installation & Setup

### Prerequisites
- Java Development Kit (JDK) 8 or higher
- Any Java IDE (IntelliJ IDEA, Eclipse, VS Code) or command line

### Required Assets
Place these image files in the same directory as your Java files:
```
├── flappybirdbg.png    (Background: 360×640)
├── flappybird.png      (Bird sprite: 51×36)
├── toppipe.png         (Top pipe: 64×512)
└── bottompipe.png      (Bottom pipe: 64×512)
```

### Compilation & Execution

**Using Command Line:**
```bash
# Compile
javac App.java FlappyBird.java

# Run
java App
```

**Using IDE:**
1. Create new Java project
2. Add `App.java` and `FlappyBird.java` to source folder
3. Add image assets to source folder
4. Run `App.java`

---

## 📁 Code Structure

### File Organization
```
FlappyBird+/
├── App.java                 # Entry point, JFrame setup
├── FlappyBird.java          # Main game logic
├── highscore.txt            # Generated at runtime
├── flappybirdbg.png         # Asset: Background
├── flappybird.png           # Asset: Player sprite
├── toppipe.png              # Asset: Top obstacle
├── bottompipe.png           # Asset: Bottom obstacle
└── README.md                # This file
```

### Key Methods

**Game Loop Methods:**
- `actionPerformed()` - 60 FPS update cycle
- `move()` - Physics and collision processing
- `draw()` - Rendering pipeline
- `paintComponent()` - Swing rendering entry point

**Gameplay Methods:**
- `placePipes()` - Pipe and power-up spawning
- `autoPlayLogic()` - AI decision making
- `collision()` - AABB collision detection (overloaded)
- `updatePowerUps()` - Power-up movement and collection
- `applyPowerUp()` - Power-up effect application

**Utility Methods:**
- `createParticles()` - Particle effect generation
- `updateParticles()` - Particle physics and cleanup
- `saveHighScore()` - Persistent storage
- `loadHighScore()` - Data retrieval
- `getPowerUpColor()` - Visual theming
- `getPowerUpSymbol()` - Icon rendering

---

## 🎬 Acknowledgments

- **Original Flappy Bird** - Dong Nguyen for the iconic game design
- **Java Swing** - For providing robust 2D game development capabilities
- **Community** - For endless inspiration and gameplay feedback
