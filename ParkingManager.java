import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Stack;

public class ParkingManager {
    // 1. DATA STRUCTURE: 2D Array representing the 5x6 parking lot (30 spaces)
    // Stores "EMPTY" if space is available, or the Vehicle ID if occupied
    private String[][] parkingLot;
    private final int ROWS = 5;
    private final int COLS = 6;

    // Entrance Gate coordinates (0, 0) used as reference for Manhattan Distance
    private final int ENTRY_ROW = 0;
    private final int ENTRY_COL = 0;

    // Track the distance of the most recently allocated vehicle for display
    private int lastAllocatedDistance = -1;

    // 2. DATA STRUCTURE: QUEUE (FIFO) to manage waiting vehicles when a zone is full
    private Queue<WaitingVehicle> waitingQueue;

    // 3. DATA STRUCTURE: STACK (LIFO) to maintain recent parking activity history
    private Stack<String> parkingHistory;

    // Helper class representing an occupied parking record for sorting and searching
    public static class ParkingRecord {
        private String vehicleId;
        private String category;
        private String spotId;
        private int row;
        private int col;

        public ParkingRecord(String vehicleId, String category, String spotId, int row, int col) {
            this.vehicleId = vehicleId;
            this.category = category;
            this.spotId = spotId;
            this.row = row;
            this.col = col;
        }

        public String getVehicleId() {
            return vehicleId;
        }

        public String getCategory() {
            return category;
        }

        public String getSpotId() {
            return spotId;
        }

        public int getRow() {
            return row;
        }

        public int getCol() {
            return col;
        }

        @Override
        public String toString() {
            return vehicleId + " (" + category + ") at space " + spotId;
        }
    }

    // Helper class representing a vehicle waiting in the FIFO Queue
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

    // Helper class representing the result of an exit operation
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

    // 4. ALGORITHM: Manhattan Distance calculation: |r1 - r2| + |c1 - c2|
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
     * 5. ALGORITHM: Linear Search & Nearest Allocation
     * - Uses LINEAR SEARCH to scan compatible zone spaces.
     * - Uses MANHATTAN DISTANCE to select the nearest available space to Entrance (0, 0).
     * - If space found, pushes action to STACK (LIFO).
     * - If full, adds to WAITING QUEUE (FIFO).
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
     * Vehicle Exit Feature:
     * - Finds the vehicle in the 2D array and releases its space.
     * - Checks the WAITING QUEUE for the first compatible vehicle (FIFO).
     * - If found, re-allocates newly freed spot and pushes to STACK.
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

    /**
     * 6. ALGORITHM: SELECTION SORT
     * Collects all occupied parking spaces and sorts them alphabetically by Vehicle ID.
     * Time Complexity: O(N^2)
     */
    public List<ParkingRecord> getSortedOccupiedRecords() {
        List<ParkingRecord> records = new ArrayList<>();

        // Gather all occupied spots
        for (int r = 0; r < ROWS; r++) {
            for (int c = 0; c < COLS; c++) {
                if (isSpotOccupied(r, c)) {
                    String spotId = getSpotId(r, c);
                    String category = getZone(r, c).replace(" Zone", "");
                    records.add(new ParkingRecord(parkingLot[r][c], category, spotId, r, c));
                }
            }
        }

        // SELECTION SORT Algorithm
        int n = records.size();
        for (int i = 0; i < n - 1; i++) {
            int minIdx = i;
            for (int j = i + 1; j < n; j++) {
                if (records.get(j).getVehicleId().compareToIgnoreCase(records.get(minIdx).getVehicleId()) < 0) {
                    minIdx = j;
                }
            }
            if (minIdx != i) {
                ParkingRecord temp = records.get(i);
                records.set(i, records.get(minIdx));
                records.set(minIdx, temp);
            }
        }

        return records;
    }

    /**
     * 7. ALGORITHM: BINARY SEARCH
     * Searches for a vehicle in the sorted records by Vehicle ID.
     * Time Complexity: O(log N)
     */
    public ParkingRecord searchVehicle(String vehicleId) {
        List<ParkingRecord> sortedRecords = getSortedOccupiedRecords();
        int low = 0;
        int high = sortedRecords.size() - 1;

        while (low <= high) {
            int mid = low + (high - low) / 2;
            int cmp = sortedRecords.get(mid).getVehicleId().compareToIgnoreCase(vehicleId.trim());

            if (cmp == 0) {
                return sortedRecords.get(mid); // Found via Binary Search!
            } else if (cmp < 0) {
                low = mid + 1;
            } else {
                high = mid - 1;
            }
        }

        return null; // Not found in parking spaces
    }

    // Get position of vehicle in waiting queue (1-based), or -1 if not in queue
    public int getWaitingPosition(String vehicleId) {
        int pos = 1;
        for (WaitingVehicle v : waitingQueue) {
            if (v.getVehicleId().equalsIgnoreCase(vehicleId)) {
                return pos;
            }
            pos++;
        }
        return -1;
    }

    // Helper: Polls first compatible waiting vehicle from queue (FIFO order)
    private WaitingVehicle pollFirstCompatibleWaitingVehicle(String zone) {
        int size = waitingQueue.size();
        WaitingVehicle matched = null;

        for (int i = 0; i < size; i++) {
            WaitingVehicle current = waitingQueue.poll();
            if (matched == null && isCategoryCompatibleWithZone(current.getCategory(), zone)) {
                matched = current;
            } else {
                waitingQueue.offer(current);
            }
        }
        return matched;
    }

    // Helper: Removes vehicle directly from queue if requested
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

    public boolean isVehicleInQueue(String vehicleId) {
        return getWaitingPosition(vehicleId) != -1;
    }

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
