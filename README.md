# Smart Parking Finder – Parking Lot Simulation System

A Java Swing-based Parking Lot Simulation System developed for Data Structures and Algorithms (DSA).

## Project Overview 
- **GUI:** Minimalist, clean Java Swing interface with real-time dynamic dashboard.
- **Layout:** $5 \times 6$ parking grid representing 30 parking spaces.
- **Data Structures:**
  - **2D Array (`String[][]`):** Parking space allocation and zone grid.
  - **Queue (`Queue<WaitingVehicle>` via `LinkedList` - FIFO):** Manages waiting vehicles when compatible zones are full.
  - **Stack (`Stack<String>` - LIFO):** Tracks recent parking activity history.
- **Zone Segregation:**
  - **Bike Zone:** Columns 1–2 (Spots `B1` to `B10`)
  - **Car Zone:** Columns 3–4 (Spots `C1` to `C10`)
  - **Truck Zone:** Columns 5–6 (Spots `T1` to `T10`)
- **Key Features:**
  - **Vehicle Allocation:** Uses Linear Search & Manhattan Distance to find the closest spot to Entrance Gate `(0, 0)`.
  - **Waiting Queue:** Automatically queues vehicles when their zone is at capacity.
  - **Vehicle Exit:** Removes parked vehicles by ID, frees the space, and checks the Waiting Queue to re-allocate the space to the first compatible waiting vehicle.
  - **Audit Stack:** Pushes every parking action onto a LIFO stack and displays recent activity.

## Project Structure (Only 3 Java Files)
- `Main.java` — Application entry point launching the Swing GUI on the EDT.
- `ParkingGUI.java` — Graphical user interface, grid layout, 4-card dashboard, entry/exit forms, and live messages.
- `ParkingManager.java` — 2D array, Queue (FIFO), Stack (LIFO), Manhattan Distance, and allocation/exit logic.

## How to Compile and Run

```bash
# Compile
javac Main.java ParkingGUI.java ParkingManager.java

# Run
java Main
```
