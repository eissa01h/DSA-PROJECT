import java.util.LinkedList;
import java.util.Queue;
import java.util.Stack;

public class ParkingManager {
    // 2D Array representing the 5x6 parking lot layout (5 rows, 6 columns = 30 spaces)
    // Stores "EMPTY" if space is available, or the Vehicle ID if occupied
    private String[][] parkingLot;
    private final int ROWS = 5;
    private final int COLS = 6;

    // Entrance Gate coordinates (0, 0) used as reference for Manhattan Distance
    private final int ENTRY_ROW = 0;
    private final int ENTRY_COL = 0;

    // Track the distance of the most recently allocated vehicle for display
    private int lastAllocatedDistance = -1;

    // QUEUE (FIFO) to manage waiting vehicles when their compatible zone is full
    private Queue<WaitingVehicle> waitingQueue;

    // STACK (LIFO) to maintain recent parking activity history
    private Stack<String> parkingHistory;

    // Inner class representing a vehicle waiting in the queue
    public static class WaitingVehicle {
        private String category;
        private String vehicleId;

        public WaitingVehicle(String category, String vehicleId) {
            this.category = category;
            this.vehicleId = vehicleId;
        }

        public String getCategory() {
            return category;
        }

        public String getVehicleId() {
            return vehicleId;
        }

        @Override
        public String toString() {
            return category + " [" + vehicleId + "]";
        }
    }

    // Inner class representing the result of an exit operation
    public static class ExitResult {
        private boolean success;
        private String spotId;
        private String allocatedWaitingVehicleId;
        private String message;

        public ExitResult(boolean success, String spotId, String allocatedWaitingVehicleId, String message) {
            this.success = success;
            this.spotId = spotId;
            this.allocatedWaitingVehicleId = allocatedWaitingVehicleId;
            this.message = message;
        }

        public boolean isSuccess() {
            return success;
        }

        public String getSpotId() {
            return spotId;
        }

        public String getAllocatedWaitingVehicleId() {
            return allocatedWaitingVehicleId;
        }

        public String getMessage() {
            return message;
        }
    }

    public ParkingManager() {
        parkingLot = new String[ROWS][COLS];
        waitingQueue = new LinkedList<>();
        parkingHistory = new Stack<>();
        initializeParkingLot();
    }

    // Initialize all 30 spaces as EMPTY
    private void initializeParkingLot() {
        for (int row = 0; row < ROWS; row++) {
            for (int col = 0; col < COLS; col++) {
                parkingLot[row][col] = "EMPTY";
            }
        }
    }

    // Zone column boundaries:
    // Columns 0-1: Bike Zone (10 spaces)
    // Columns 2-3: Car Zone (10 spaces)
    // Columns 4-5: Truck Zone (10 spaces)
    public String getZone(int row, int col) {
        if (col < 2) {
            return "Bike Zone";
        } else if (col < 4) {
            return "Car Zone";
        } else {
            return "Truck Zone";
        }
    }

    // Generate readable spot identifier (B1-B10, C1-C10, T1-T10)
    public String getSpotId(int row, int col) {
        if (col < 2) {
            int spotNum = (row * 2) + col + 1;
            return "B" + spotNum;
        } else if (col < 4) {
            int spotNum = (row * 2) + (col - 2) + 1;
            return "C" + spotNum;
        } else {
            int spotNum = (row * 2) + (col - 4) + 1;
            return "T" + spotNum;
        }
    }

    // Returns current status: "EMPTY" or the parked Vehicle ID
    public String getSpotStatus(int row, int col) {
        return parkingLot[row][col];
    }

    // Checks if a specific spot is occupied
    public boolean isSpotOccupied(int row, int col) {
        return !parkingLot[row][col].equals("EMPTY");
    }

    // Manhattan Distance calculation: |r1 - r2| + |c1 - c2|
    public int calculateManhattanDistance(int row, int col) {
        return Math.abs(row - ENTRY_ROW) + Math.abs(col - ENTRY_COL);
    }

    // Check category compatibility with a zone name
    public boolean isCategoryCompatibleWithZone(String category, String zone) {
        if (category.equalsIgnoreCase("Bike") && zone.contains("Bike")) return true;
        if (category.equalsIgnoreCase("Car") && zone.contains("Car")) return true;
        if (category.equalsIgnoreCase("Truck") && zone.contains("Truck")) return true;
        return false;
    }

    /**
     * Vehicle Allocation (Day 2 & Day 3):
     * 1. If space is available, allocates nearest space via Manhattan Distance & pushes record to STACK.
     * 2. If zone is full, enqueues the vehicle into the WAITING QUEUE.
     *
     * @return Spot ID if parked, "QUEUED" if added to queue, "DUPLICATE" if already exists.
     */
    public String parkVehicle(String category, String vehicleId) {
        if (isVehicleParked(vehicleId) || isVehicleInQueue(vehicleId)) {
            return "DUPLICATE";
        }

        int startCol;
        int endCol;

        if (category.equalsIgnoreCase("Bike")) {
            startCol = 0;
            endCol = 1;
        } else if (category.equalsIgnoreCase("Car")) {
            startCol = 2;
            endCol = 3;
        } else if (category.equalsIgnoreCase("Truck")) {
            startCol = 4;
            endCol = 5;
        } else {
            return "INVALID";
        }

        int bestRow = -1;
        int bestCol = -1;
        int minDistance = Integer.MAX_VALUE;

        // LINEAR SEARCH through the compatible zone
        for (int r = 0; r < ROWS; r++) {
            for (int c = startCol; c <= endCol; c++) {
                if (parkingLot[r][c].equals("EMPTY")) {
                    int distance = calculateManhattanDistance(r, c);
                    if (distance < minDistance) {
                        minDistance = distance;
                        bestRow = r;
                        bestCol = c;
                    }
                }
            }
        }

        // Available space found
        if (bestRow != -1 && bestCol != -1) {
            parkingLot[bestRow][bestCol] = vehicleId;
            lastAllocatedDistance = minDistance;
            String spotId = getSpotId(bestRow, bestCol);

            // STACK: Push recent parking activity record (LIFO)
            String record = "Parked " + category + " [" + vehicleId + "] at " + spotId;
            parkingHistory.push(record);

            return spotId;
        }

        // Compatible zone is full -> Add to WAITING QUEUE (FIFO)
        waitingQueue.offer(new WaitingVehicle(category, vehicleId));
        lastAllocatedDistance = -1;
        return "QUEUED";
    }

    /**
     * Vehicle Exit Feature (Day 3):
     * 1. Finds the vehicle in the 2D array and releases its space.
     * 2. Checks the WAITING QUEUE for the first compatible vehicle (FIFO).
     * 3. If compatible waiting vehicle exists, allocates the freed spot to it and pushes to STACK.
     */
    public ExitResult exitVehicle(String vehicleId) {
        int foundRow = -1;
        int foundCol = -1;

        // Linear Search to find vehicle in the parking lot
        for (int r = 0; r < ROWS; r++) {
            for (int c = 0; c < COLS; c++) {
                if (parkingLot[r][c].equalsIgnoreCase(vehicleId)) {
                    foundRow = r;
                    foundCol = c;
                    break;
                }
            }
            if (foundRow != -1) break;
        }

        // If not parked in lot, check if vehicle is waiting in queue
        if (foundRow == -1) {
            boolean removedFromQueue = removeVehicleFromQueue(vehicleId);
            if (removedFromQueue) {
                return new ExitResult(true, null, null, "Vehicle [" + vehicleId + "] was removed from the Waiting Queue.");
            }
            return new ExitResult(false, null, null, "Vehicle ID '" + vehicleId + "' is not parked in the lot or waiting queue.");
        }

        // Vacate the parking spot
        String spotId = getSpotId(foundRow, foundCol);
        String zone = getZone(foundRow, foundCol);
        parkingLot[foundRow][foundCol] = "EMPTY";

        // Check if a compatible vehicle is in the Waiting Queue (FIFO)
        WaitingVehicle waitingVehicle = pollFirstCompatibleWaitingVehicle(zone);

        if (waitingVehicle != null) {
            // Assign newly freed space to waiting vehicle
            parkingLot[foundRow][foundCol] = waitingVehicle.getVehicleId();

            // STACK: Push queue allocation to history (LIFO)
            String record = "Queued " + waitingVehicle.getCategory() + " [" + waitingVehicle.getVehicleId() + "] parked at " + spotId;
            parkingHistory.push(record);

            String msg = "Vehicle [" + vehicleId + "] exited " + spotId + ". Waiting " + waitingVehicle.getCategory()
                    + " [" + waitingVehicle.getVehicleId() + "] was allocated to space " + spotId + "!";
            return new ExitResult(true, spotId, waitingVehicle.getVehicleId(), msg);
        } else {
            String msg = "Vehicle [" + vehicleId + "] successfully exited from space " + spotId + ".";
            return new ExitResult(true, spotId, null, msg);
        }
    }

    // Helper: Polls the first compatible waiting vehicle from the queue (FIFO order)
    private WaitingVehicle pollFirstCompatibleWaitingVehicle(String zone) {
        int size = waitingQueue.size();
        WaitingVehicle matched = null;

        for (int i = 0; i < size; i++) {
            WaitingVehicle current = waitingQueue.poll();
            if (matched == null && isCategoryCompatibleWithZone(current.getCategory(), zone)) {
                matched = current;
            } else {
                waitingQueue.offer(current); // Re-enqueue remaining vehicles in order
            }
        }
        return matched;
    }

    // Helper: Removes a vehicle directly from the waiting queue if requested
    private boolean removeVehicleFromQueue(String vehicleId) {
        int size = waitingQueue.size();
        boolean removed = false;
        for (int i = 0; i < size; i++) {
            WaitingVehicle current = waitingQueue.poll();
            if (!removed && current.getVehicleId().equalsIgnoreCase(vehicleId)) {
                removed = true;
            } else {
                waitingQueue.offer(current);
            }
        }
        return removed;
    }

    // Check if vehicle is currently in the waiting queue
    public boolean isVehicleInQueue(String vehicleId) {
        for (WaitingVehicle v : waitingQueue) {
            if (v.getVehicleId().equalsIgnoreCase(vehicleId)) {
                return true;
            }
        }
        return false;
    }

    // Check if vehicle is parked in any space
    public boolean isVehicleParked(String vehicleId) {
        for (int r = 0; r < ROWS; r++) {
            for (int c = 0; c < COLS; c++) {
                if (parkingLot[r][c].equalsIgnoreCase(vehicleId)) {
                    return true;
                }
            }
        }
        return false;
    }

    // STACK: Peek at recent activity without removing it
    public String getRecentActivity() {
        if (parkingHistory.isEmpty()) {
            return "None (No vehicles parked yet)";
        }
        return parkingHistory.peek();
    }

    public Stack<String> getParkingHistory() {
        return parkingHistory;
    }

    // QUEUE: Get current count of waiting vehicles
    public int getWaitingQueueCount() {
        return waitingQueue.size();
    }

    public Queue<WaitingVehicle> getWaitingQueue() {
        return waitingQueue;
    }

    public int getLastAllocatedDistance() {
        return lastAllocatedDistance;
    }

    public int getRows() {
        return ROWS;
    }

    public int getCols() {
        return COLS;
    }

    public int getTotalSpaces() {
        return ROWS * COLS;
    }

    public int getOccupiedSpaces() {
        int count = 0;
        for (int row = 0; row < ROWS; row++) {
            for (int col = 0; col < COLS; col++) {
                if (!parkingLot[row][col].equals("EMPTY")) {
                    count++;
                }
            }
        }
        return count;
    }

    public int getAvailableSpaces() {
        return getTotalSpaces() - getOccupiedSpaces();
    }
}
