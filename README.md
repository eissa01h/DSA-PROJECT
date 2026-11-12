# Smart Parking Finder – Parking Lot Simulation System

A Java Swing-based Parking Lot Simulation System developed for Data Structures and Algorithms (DSA).

## Project Overview (100% Complete)
- **GUI:** Minimalist, clean Java Swing interface with real-time dynamic dashboard.
- **Layout:** $5 \times 6$ parking grid representing 30 parking spaces across 3 distinct zones.
- **Zone Segregation:**
  - **Bike Zone:** Columns 1–2 (Spots `B1` to `B10`)
  - **Car Zone:** Columns 3–4 (Spots `C1` to `C10`)
  - **Truck Zone:** Columns 5–6 (Spots `T1` to `T10`)

## Implemented DSA Concepts
1. **2D Array (`String[][]`):** Manages the physical 5x6 parking lot layout ($O(1)$ coordinate access).
2. **Linear Search:** Scans compatible parking spots during allocation and vehicle lookup ($O(N)$).
3. **Manhattan Distance:** Evaluates grid distance from Entrance Gate `(0, 0)` to allocate the physically nearest spot.
4. **Queue (`Queue<WaitingVehicle>` via `LinkedList` - FIFO):** Manages waiting vehicles when compatible zones reach capacity. Automatically admits the first compatible vehicle when a space is vacated.
5. **Stack (`Stack<String>` - LIFO):** Maintains recent parking activity history, allowing quick peek at the latest transaction.
6. **Selection Sort:** Sorts occupied parking records alphabetically by Vehicle ID ($O(N^2)$).
7. **Binary Search:** Searches for a parked vehicle by Vehicle ID on the sorted records ($O(\log N)$).

## Project Structure (Strictly 3 Java Files)
- `Main.java` — Application entry point launching the Swing GUI on the Event Dispatch Thread (EDT).
- `ParkingGUI.java` — Graphical user interface, grid layout, 4-card dashboard, operations toolbar, and search highlighting.
- `ParkingManager.java` — Core data structures, algorithms (Manhattan Distance, Linear/Binary Search, Selection Sort, Queue, Stack), and business logic.

## How to Compile and Run

```bash
# Compile
javac Main.java ParkingGUI.java ParkingManager.java

# Run
java Main
```
