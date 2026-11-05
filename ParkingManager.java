public class ParkingManager {
    // 2D Array representing the 5x6 parking lot layout (5 rows, 6 columns = 30 spaces)
    private String[][] parkingLot;
    private final int ROWS = 5;
    private final int COLS = 6;

    public ParkingManager() {
        parkingLot = new String[ROWS][COLS];
        initializeParkingLot();
    }

    // Initialize all 30 spaces as EMPTY for Day 1
    private void initializeParkingLot() {
        for (int row = 0; row < ROWS; row++) {
            for (int col = 0; col < COLS; col++) {
                parkingLot[row][col] = "EMPTY";
            }
        }
    }

    // Divide the lot into 3 zones across the 6 columns:
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

    // Returns the status of the spot ("EMPTY")
    public String getSpotStatus(int row, int col) {
        return parkingLot[row][col];
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

