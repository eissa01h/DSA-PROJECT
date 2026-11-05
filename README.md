# Smart Parking Finder – Parking Lot Simulation System

A Java Swing-based Parking Lot Simulation System developed for Data Structures and Algorithms (DSA).

## Project Overview (Day 1 - 25% Milestone)
- **GUI:** Minimalist, clean Java Swing interface.
- **Layout:** $5 \times 6$ parking grid representing 30 parking spaces.
- **Data Structure:** 2D Array layout representation (`String[][]`).
- **Zone Segregation:**
  - **Bike Zone:** Columns 1–2 (Spots `B1` to `B10`)
  - **Car Zone:** Columns 3–4 (Spots `C1` to `C10`)
  - **Truck Zone:** Columns 5–6 (Spots `T1` to `T10`)
- **Dashboard:** Real-time statistics counters for Total Spaces, Occupied Spaces, and Available Spaces.

## Project Structure
- `Main.java` — Main entry point launching the Swing application.
- `ParkingGUI.java` — Graphical user interface, grid layout, and dashboard display.
- `ParkingManager.java` — 2D array representation of the 30-slot parking grid and zone metrics.

## How to Compile and Run

```bash
# Compile
javac Main.java ParkingGUI.java ParkingManager.java

# Run
java Main
```
