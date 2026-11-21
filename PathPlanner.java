import java.util.*;

/**
 * PathPlanner implements a grid-based road network graph and uses
 * Breadth-First Search (BFS) to compute driving waypoints for vehicles.
 *
 * This ensures vehicles strictly follow designated driving lanes,
 * make realistic 90-degree turns, approach their assigned stalls,
 * and never cut diagonally across parking bays.
 */
public class PathPlanner {

    public static class Waypoint {
        public final String id;
        public final double x;
        public final double y;
        public final double z;

        public Waypoint(String id, double x, double y, double z) {
            this.id = id;
            this.x = x;
            this.y = y;
            this.z = z;
        }

        @Override
        public String toString() {
            return id + "(" + x + ", " + z + ")";
        }
    }

    // Roadway coordinates
    public static final double ENTRANCE_X = -120.0;
    public static final double ENTRANCE_Z = -80.0;
    public static final double EXIT_X = 980.0;
    public static final double EXIT_Z = 580.0;

    public static final double NORTH_ROAD_Z = -80.0;
    public static final double SOUTH_ROAD_Z = 580.0;

    // Aisle X centers: Aisle 0 (Bikes), Aisle 1 (Cars), Aisle 2 (Trucks)
    public static final double[] AISLE_X = { 140.0, 440.0, 740.0 };

    // Row Z centers (Rows 0 through 4)
    public static final double[] ROW_Z = { 60.0, 160.0, 260.0, 360.0, 460.0 };

    // Stall X centers (Columns 0 through 5)
    // Col 0 (left of Aisle 0), Col 1 (right of Aisle 0)
    // Col 2 (left of Aisle 1), Col 3 (right of Aisle 1)
    // Col 4 (left of Aisle 2), Col 5 (right of Aisle 2)
    public static final double[] COL_X = { 65.0, 215.0, 365.0, 515.0, 650.0, 830.0 };

    // Adjacency graph for BFS
    private final Map<String, Waypoint> waypoints = new HashMap<>();
    private final Map<String, List<String>> adjList = new HashMap<>();

    public PathPlanner() {
        buildRoadGraph();
    }

    private void addWaypoint(String id, double x, double y, double z) {
        waypoints.put(id, new Waypoint(id, x, y, z));
        adjList.putIfAbsent(id, new ArrayList<>());
    }

    private void addEdge(String from, String to) {
        adjList.computeIfAbsent(from, k -> new ArrayList<>()).add(to);
    }

    private void buildRoadGraph() {
        // 1. Entrance Waypoints
        addWaypoint("ENTRANCE", ENTRANCE_X, 0, ENTRANCE_Z);
        addWaypoint("ENTRY_MERGE", 0.0, 0, NORTH_ROAD_Z);
        addEdge("ENTRANCE", "ENTRY_MERGE");

        // 2. North Cross-Road (connects entrance to aisles)
        addWaypoint("NORTH_AISLE_0", AISLE_X[0], 0, NORTH_ROAD_Z);
        addWaypoint("NORTH_AISLE_1", AISLE_X[1], 0, NORTH_ROAD_Z);
        addWaypoint("NORTH_AISLE_2", AISLE_X[2], 0, NORTH_ROAD_Z);

        addEdge("ENTRY_MERGE", "NORTH_AISLE_0");
        addEdge("NORTH_AISLE_0", "NORTH_AISLE_1");
        addEdge("NORTH_AISLE_1", "NORTH_AISLE_2");

        // 3. Aisles and Parking Rows
        for (int a = 0; a < 3; a++) {
            String northNode = "NORTH_AISLE_" + a;
            String prevAisleNode = northNode;

            for (int r = 0; r < 5; r++) {
                String aisleRowNode = "AISLE_" + a + "_ROW_" + r;
                addWaypoint(aisleRowNode, AISLE_X[a], 0, ROW_Z[r]);
                addEdge(prevAisleNode, aisleRowNode);
                prevAisleNode = aisleRowNode;

                // Connect to left spot (col = a*2) and right spot (col = a*2 + 1)
                int leftCol = a * 2;
                int rightCol = a * 2 + 1;

                String leftSpotNode = getSpotNodeId(r, leftCol);
                String rightSpotNode = getSpotNodeId(r, rightCol);

                addWaypoint(leftSpotNode, COL_X[leftCol], 0, ROW_Z[r]);
                addWaypoint(rightSpotNode, COL_X[rightCol], 0, ROW_Z[r]);

                // Aisle connects to spot (for parking)
                addEdge(aisleRowNode, leftSpotNode);
                addEdge(aisleRowNode, rightSpotNode);

                // Spot connects back to aisle (for exiting)
                addEdge(leftSpotNode, aisleRowNode);
                addEdge(rightSpotNode, aisleRowNode);
            }

            // Aisle connects to South Cross-Road
            String southNode = "SOUTH_AISLE_" + a;
            addWaypoint(southNode, AISLE_X[a], 0, SOUTH_ROAD_Z);
            addEdge(prevAisleNode, southNode);
        }

        // 4. South Cross-Road (connects aisles to Exit)
        addEdge("SOUTH_AISLE_0", "SOUTH_AISLE_1");
        addEdge("SOUTH_AISLE_1", "SOUTH_AISLE_2");

        addWaypoint("SOUTH_MERGE", 860.0, 0, SOUTH_ROAD_Z);
        addWaypoint("EXIT_GATE", EXIT_X, 0, SOUTH_ROAD_Z);
        addWaypoint("EXIT_RUNWAY", EXIT_X + 110.0, 0, SOUTH_ROAD_Z);

        addEdge("SOUTH_AISLE_2", "SOUTH_MERGE");
        addEdge("SOUTH_MERGE", "EXIT_GATE");
        addEdge("EXIT_GATE", "EXIT_RUNWAY");
    }

    public static String getSpotNodeId(int row, int col) {
        return "SPOT_" + row + "_" + col;
    }

    /**
     * ALGORITHM: Breadth-First Search (BFS)
     * Finds the shortest path in the road network graph from startNode to goalNode.
     * Time Complexity: O(V + E)
     */
    public List<Waypoint> findPath(String startNode, String goalNode) {
        if (!waypoints.containsKey(startNode) || !waypoints.containsKey(goalNode)) {
            return Collections.emptyList();
        }

        Queue<String> queue = new LinkedList<>();
        Map<String, String> parentMap = new HashMap<>();
        Set<String> visited = new HashSet<>();

        queue.offer(startNode);
        visited.add(startNode);

        boolean found = false;
        while (!queue.isEmpty()) {
            String current = queue.poll();
            if (current.equals(goalNode)) {
                found = true;
                break;
            }

            List<String> neighbors = adjList.getOrDefault(current, Collections.emptyList());
            for (String neighbor : neighbors) {
                if (!visited.contains(neighbor)) {
                    visited.add(neighbor);
                    parentMap.put(neighbor, current);
                    queue.offer(neighbor);
                }
            }
        }

        if (!found) {
            return Collections.emptyList();
        }

        // Reconstruct path backwards from goal to start
        LinkedList<Waypoint> path = new LinkedList<>();
        String step = goalNode;
        while (step != null) {
            path.addFirst(waypoints.get(step));
            step = parentMap.get(step);
        }

        return path;
    }

    /**
     * Helper to get full path for parking a vehicle in spot (row, col)
     */
    public List<Waypoint> getParkingPath(int row, int col) {
        return findPath("ENTRANCE", getSpotNodeId(row, col));
    }

    /**
     * Helper to get full path for exiting a vehicle from spot (row, col)
     */
    public List<Waypoint> getExitPath(int row, int col) {
        return findPath(getSpotNodeId(row, col), "EXIT_RUNWAY");
    }

    public Waypoint getSpotWaypoint(int row, int col) {
        return waypoints.get(getSpotNodeId(row, col));
    }

    public Waypoint getEntranceWaypoint() {
        return waypoints.get("ENTRANCE");
    }

    public Waypoint getExitWaypoint() {
        return waypoints.get("EXIT_GATE");
    }
}
