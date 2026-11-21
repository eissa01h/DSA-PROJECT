# Smart Parking Finder – 3D Parking Lot Simulation System

An advanced **JavaFX 3D Parking Simulation System** built on top of robust **Data Structures and Algorithms (DSA)** in Java.

---

## 🚀 What's New in 3D Upgrade

1. **Full 3D Environment (JavaFX 3D):**
   - Angled overhead isometric/perspective camera with interactive orbit, pan, and smooth mouse-wheel zoom.
   - Asphalt lot with realistic road markings, yellow dashed centerlines, and driving directional arrows.
   - 30 parking stalls organized into 3 color-coded zones with concrete wheel-stops and real-time status LED bollards (Green = Empty, Red = Occupied, Yellow = Search Highlight).
   - Architectural entrance and exit gantries with overhead signage and animated boom barrier arms.
   - Street lighting with ambient and point light sources.

2. **Recognizable 3D Vehicles (Built from JavaFX 3D Primitives):**
   - **Cars:** Colored chassis, cabin roof, tinted windshield & windows, headlights, taillights, and 4 rubber wheels with silver hubcaps.
   - **Motorcycles (Bikes):** Slim chassis frame, colored fuel tank, leather seat, handlebars, headlight, chrome exhaust, and dual slim wheels.
   - **Trucks:** Heavy dual chassis, tall driver cab, large cargo container box, front chrome grille, dual smokestacks, and 6 heavy-duty wheels.
   - **Overhead License Badge:** Each vehicle displays its unique ID floating above its roof facing the camera.

3. **Autonomous Navigation & BFS Route Planning:**
   - Grid-based road graph with waypoints along driving lanes.
   - **Breadth-First Search (BFS)** computes valid driving routes so vehicles never cut across parking bays.
   - Vehicles navigate from the Entrance Gate, down the respective zone aisle, smoothly turn 90°, and park in their assigned stall.
   - When exiting, vehicles pull out into the aisle, drive down to the South Arterial, and depart through the Exit Gate.

4. **Preserved Core DSA Backend (`ParkingManager.java`):**
   - **2D Array (`String[5][6]`):** Physical 30-space lot representation ($O(1)$ coordinate access).
   - **Manhattan Distance:** $|r - 0| + |c - 0|$ allocates the spot physically nearest to Entrance `(0, 0)`.
   - **Linear Search:** Scans compatible zone columns to locate available spots.
   - **Queue (FIFO - `LinkedList`):** Manages waiting vehicles when a zone is full; auto-promotes the first vehicle when a spot opens.
   - **Stack (LIFO - `Stack`):** Tracks parking history and peeks recent activity.
   - **Selection Sort ($O(N^2)$):** Sorts occupied records alphabetically by Vehicle ID.
   - **Binary Search ($O(\log N)$):** Locates parked vehicles and illuminates them with an in-scene 3D spotlight.

---

## 📁 Project Architecture

```
DSA PROJECT/
├── Main.java               # Universal launcher (starts 3D by default; supports --2d flag)
├── ParkingManager.java     # Core DSA Engine (2D Array, Queue, Stack, Sort & Search)
├── PathPlanner.java        # Road network graph & BFS route planning
├── Vehicle3D.java          # Composite 3D vehicle models (Car, Bike, Truck) & physics
├── ParkingLot3DScene.java  # 3D world: asphalt, stalls, LEDs, gantries, lighting & camera
├── Parking3DApp.java       # JavaFX 3D Application with HUD Dashboard & Controls
├── ParkingGUI.java         # Legacy 2D Swing GUI (preserved)
├── pom.xml                 # Maven build configuration
├── run3d.bat               # Windows 1-click batch launcher
├── run3d.ps1               # PowerShell 1-click launcher
└── javafx-sdk/             # Self-contained JavaFX 21 SDK (jars & native DLLs)
```

---

## 🎮 Simulation Controls

- **Auto Simulation:**
  - `▶ Start Auto Sim`: Continuously spawns arriving vehicles and triggers dynamic exits.
  - `⏸ Pause` / `Resume`: Freezes/unfreezes vehicle movement and spawning.
  - `↺ Reset`: Clears all 3D vehicles and resets parking data structures.
  - `Speed Slider`: Adjusts vehicle driving speed from 0.5x to 3.0x.
- **Manual Vehicle Controls:**
  - `Park`: Select category (`Bike`, `Car`, `Truck`), enter ID, and click **Park**.
  - `Exit`: Enter ID and click **Exit** to watch the vehicle drive out to the exit gate.
- **DSA Analysis:**
  - `🔍 Binary Search`: Searches for a vehicle by ID ($O(\log N)$) and spotlights it with a glowing yellow aura in 3D.
  - `📋 Selection Sort`: Opens a sorted table of all parked vehicles ($O(N^2)$).
- **Interactive 3D Camera:**
  - **Left-Click + Drag:** Orbit rotate pitch and yaw.
  - **Right-Click + Drag:** Pan camera position.
  - **Mouse Wheel Scroll:** Zoom in and out.
  - **Camera Preset Buttons:** `Isometric`, `Top-Down`, and `Entrance`.

---

## 🛠️ How to Compile and Run

### Option 1: 1-Click Launchers (Easiest)
Double-click `run3d.bat` or run in PowerShell:
```powershell
.\run3d.ps1
```

### Option 2: Command Line (javac & java)
```powershell
# 1. Compile
javac --module-path javafx-sdk\lib --add-modules javafx.controls,javafx.graphics *.java

# 2. Run 3D Simulation
java --module-path javafx-sdk\lib --add-modules javafx.controls,javafx.graphics Main

# Optional: Run classic 2D Swing interface
java --module-path javafx-sdk\lib --add-modules javafx.controls,javafx.graphics Main --2d
```

### Option 3: Maven (if installed)
```bash
mvn clean compile javafx:run
```
