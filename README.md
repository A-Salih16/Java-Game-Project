# Food Chain Through Time (COMP132 Project)

A turn-based Java Swing simulation game that models a biological food chain across **Past / Present / Future** eras.  
The player controls the **Predator**, while **Apex** and **Prey** are AI-driven agents. The game is **data-driven**: entities and icons are loaded from external `.txt` files, and the full game state can be **saved/loaded** from disk.

> Course: COMP132 – Advanced Programming (Koç University)  
> Term: Fall 2025  
> Author: Ahmet Salih Çiçek

---

## Features

- **Era-based simulation:** Past / Present / Future eras with different assets and entity sets.
- **Configurable game setup:**
  - Grid sizes: **10×10**, **15×15**, **20×20**
  - Total round count
- **Turn-based gameplay:** Turn cycle **PREY → PREDATOR (Player) → APEX**
- **Interactive UI (Java Swing):**
  - Valid moves highlighted on the grid
  - Mouse click to move the Predator
  - Status panel with round info, scores, cooldowns
- **Special moves:** Era-dependent mechanics (e.g., **Dash** in Present era when alignment conditions are met)
- **Scoring + respawn:**
  - Predator eating Prey: **+3 points**
  - Consumed Prey **respawns** at a random empty cell
- **Save & Load (persistence):**
  - Save game state to `data/save.txt` (human-readable, structured)
  - Load restores positions, scores, turn order, era, etc.
- **Logging:** `data/log.txt` records moves, cooldown updates, and scoring events
- **AI Agents:**
  - **PreyAI:** heuristic safety scoring (avoid predators, seek food)
  - **ApexAI:** greedy chase minimizing **Chebyshev distance**
- **Robust error handling:**
  - File IO exceptions handled safely
  - Custom `InvalidSaveFormatException` for malformed save files

---

## Project Structure (High-level)

The project follows OOP principles (inheritance, encapsulation, polymorphism) and an MVC-style separation between **UI** and **game logic**.

### Core entities
- `Entity` (abstract): base for board objects, includes name and position (`Pos`)
- `Animal` extends `Entity`: role (APEX / PREDATOR / PREY), score, ability cooldown
- `Food` extends `Entity`: static target for Prey
- `Role` enum: strict role definition and turn rules

### GUI (Swing)
- `GameFrame`: main window, manages Start vs Game screen via `CardLayout`
- `BoardPanel`: grid renderer using `JButton[][]` + ActionListeners
- `StatusPanel`: round, scores, cooldowns

### Data + persistence
- `FoodChainLoader`: reads era files like `data/past.txt`, `data/present.txt`, `data/future.txt`
- `GameStateSerializer`: saves/loads state to/from `data/save.txt`

---

## Data-driven configuration

Game content is loaded from external files.  
Changing an era file (e.g., `data/past.txt`) updates entity names/icons on next launch (no code changes required).

Example (conceptually):
- Era: Past
- Food Chain 1: Apex, Predator, Prey, Food
- Food Chain 2: Apex, Predator, Prey, Food

---

## How to Run

### Option 1: Run from IDE (recommended)
1. Open the project in **IntelliJ IDEA** or **Eclipse**
2. Ensure the `data/` folder is present at the project root (contains `past.txt`, `present.txt`, `future.txt`, etc.)
3. Run the main entry point (e.g., `Main.java`)

### Option 2: Run from terminal (if configured)
If your project is set up with a build tool (Maven/Gradle), run using your toolchain.
(If not, use IDE run configuration.)

---

## Gameplay Notes

- The player controls **Predator**. On your turn, valid moves are highlighted.
- Special moves (e.g., **Dash**) appear as distinct highlights when available.
- The game ends after the configured number of rounds; winner is announced.

---

## Save / Load

- **Save:** writes a structured state to `data/save.txt`
- **Load:** reconstructs the exact game state (including positions, scores, cooldowns, and turn)

If `save.txt` is corrupted, the app handles it gracefully using `InvalidSaveFormatException`.

---

## Screenshots / Report

See `acicek24Report.pdf` for a detailed demo and design description.

---

## License

This repository is for educational purposes (COMP132 project).
