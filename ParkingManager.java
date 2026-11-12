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

    public ParkingManager() {
        parkingLot = new String[ROWS][COLS];
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

    /**
     * Vehicle Allocation (Day 2):
     * 1. Identifies the compatible zone columns for the vehicle category.
     * 2. Uses LINEAR SEARCH to scan compatible spaces.
     * 3. Uses MANHATTAN DISTANCE to pick the nearest available compatible space.
     * 
     * @param category  "Bike", "Car", or "Truck"
     * @param vehicleId Unique identifier for the vehicle
     * @return Allocated Spot ID (e.g. "B1"), or null if no compatible space is available.
     */
    public String allocateVehicle(String category, String vehicleId) {
        int startCol;
        int endCol;

        // Map category to compatible zone columns
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
            return null; // Invalid category
        }

        int bestRow = -1;
        int bestCol = -1;
        int minDistance = Integer.MAX_VALUE;

        // LINEAR SEARCH through the compatible zone
        for (int r = 0; r < ROWS; r++) {
            for (int c = startCol; c <= endCol; c++) {
                if (parkingLot[r][c].equals("EMPTY")) {
                    // Calculate Manhattan Distance from Entrance (0, 0)
                    int distance = calculateManhattanDistance(r, c);
                    if (distance < minDistance) {
                        minDistance = distance;
                        bestRow = r;
                        bestCol = c;
                    }
                }
            }
        }

        // If an available compatible space was found
        if (bestRow != -1 && bestCol != -1) {
            parkingLot[bestRow][bestCol] = vehicleId;
            lastAllocatedDistance = minDistance;
            return getSpotId(bestRow, bestCol);
        }

        // Zone is full
        lastAllocatedDistance = -1;
        return null;
    }

    // Check if a vehicle ID is already parked in the lot using linear search
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
