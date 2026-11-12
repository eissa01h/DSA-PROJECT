# Smart Parking Finder – Parking Lot Simulation System

A Java Swing-based Parking Lot Simulation System developed for Data Structures and Algorithms (DSA).

## Project Overview (Day 2 - 50% Milestone)
- **GUI:** Minimalist, clean Java Swing interface with real-time dynamic dashboard.
- **Layout:** $5 \times 6$ parking grid representing 30 parking spaces.
- **Data Structure:** 2D Array layout representation (`String[][]`).
- **Zone Segregation:**
  - **Bike Zone:** Columns 1–2 (Spots `B1` to `B10`)
  - **Car Zone:** Columns 3–4 (Spots `C1` to `C10`)
  - **Truck Zone:** Columns 5–6 (Spots `T1` to `T10`)
- **Vehicle Allocation (Day 2):**
  - Categories: **Bike**, **Car**, and **Truck**.
  - **Linear Search:** Scans compatible zones for available parking spaces.
  - **Manhattan Distance:** Finds the nearest available space to the Entrance Gate `(0, 0)`.
  - **Real-Time GUI Updates:** Automatically updates spot cards and statistics (Total, Available, Occupied).
  - **Status Messaging:** Clear feedback for successful parking, full zones, and input validation.

## Project Structure (Only 3 Java Files)
- `Main.java` — Application entry point launching the Swing GUI on the EDT.
- `ParkingGUI.java` — Graphical user interface, grid layout, dashboard display, and vehicle entry form.
- `ParkingManager.java` — 2D array representation, Linear Search, Manhattan Distance calculation, and zone allocation.

## How to Compile and Run

```bash
# Compile
javac Main.java ParkingGUI.java ParkingManager.java

# Run
java Main
```
