import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.SceneAntialiasing;
import javafx.scene.SubScene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.util.*;

/**
 * Parking3DApp is the main JavaFX 3D Application.
 *
 * It connects the 3D visual simulation environment (ParkingLot3DScene, Vehicle3D, PathPlanner)
 * directly with the completed DSA backend (ParkingManager: 2D Array, Manhattan Distance,
 * FIFO Queue, LIFO Stack, Selection Sort, and Binary Search).
 */
public class Parking3DApp extends Application {

    private final ParkingManager manager = new ParkingManager();
    private final PathPlanner pathPlanner = new PathPlanner();
    private ParkingLot3DScene lot3DScene;

    // Active vehicles on screen: key = vehicleId
    private final Map<String, Vehicle3D> activeVehicles = new HashMap<>();
    private final List<Vehicle3D> movingVehicles = new ArrayList<>();

    // UI Dashboard Labels
    private Label totalSpacesVal;
    private Label occupiedSpacesVal;
    private Label availableSpacesVal;
    private Label waitingQueueVal;
    private Label stackActivityLbl;
    private Label statusMsgLbl;
    private VBox waitingCardsBox;
    private Label waitingSummaryLbl;

    // UI Input Controls
    private ComboBox<String> categoryCombo;
    private TextField parkIdField;
    private TextField exitIdField;
    private TextField searchIdField;
    private Button startSimBtn;
    private Button pauseSimBtn;
    private Slider speedSlider;

    // Simulation Engine State
    private boolean autoSimRunning = false;
    private boolean autoSimPaused = false;
    private double simTimer = 0.0;
    private double spawnInterval = 2.4; // seconds between auto arrivals
    private double simSpeed = 1.0;
    private int autoIdCounter = 101;

    // Mouse drag tracking for 3D Camera Orbit & Pan
    private double mouseAnchorX;
    private double mouseAnchorY;

    // Color palette for randomizing vehicle paint
    private final Color[] carColors = {
            Color.rgb(220, 38, 38),   // Crimson Red
            Color.rgb(37, 99, 235),   // Royal Blue
            Color.rgb(245, 158, 11),  // Amber Gold
            Color.rgb(16, 185, 129),  // Emerald
            Color.rgb(245, 245, 245), // Pearl White
            Color.rgb(75, 85, 99)     // Metallic Grey
    };
    private final Color[] bikeColors = {
            Color.rgb(14, 165, 233),  // Sky Blue
            Color.rgb(234, 88, 12),   // Neon Orange
            Color.rgb(168, 85, 247),  // Purple
            Color.rgb(220, 38, 38)    // Red
    };
    private final Color[] truckColors = {
            Color.rgb(30, 64, 175),   // Navy Blue
            Color.rgb(202, 138, 4),   // Heavy Yellow
            Color.rgb(180, 83, 9),    // Rust
            Color.rgb(71, 85, 105)    // Industrial Slate
    };
    private final Random random = new Random();

    @Override
    public void start(Stage primaryStage) {
        lot3DScene = new ParkingLot3DScene();

        // 1. Root Layout
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: #0F172A;");

        // 2. 3D SubScene (Interactive 3D Viewport)
        SubScene subScene3D = new SubScene(lot3DScene.getRoot3D(), 1100, 640, true, SceneAntialiasing.BALANCED);
        subScene3D.setCamera(lot3DScene.getCamera());
        setupSubSceneMouseControls(subScene3D);

        // Bind SubScene size dynamically with window resizing
        StackPane subSceneContainer = new StackPane(subScene3D);
        subSceneContainer.setStyle("-fx-background-color: #090D16;");
        subScene3D.widthProperty().bind(subSceneContainer.widthProperty());
        subScene3D.heightProperty().bind(subSceneContainer.heightProperty());

        root.setCenter(subSceneContainer);

        // 3. Top HUD Dashboard
        VBox topDashboard = createTopDashboard();
        root.setTop(topDashboard);

        // 4. Bottom Control Toolbar
        VBox bottomControls = createBottomControls();
        root.setBottom(bottomControls);

        // 5. Right Drawer: Waiting Vehicles Panel
        VBox rightWaitingPanel = createRightWaitingDrawer();
        root.setRight(rightWaitingPanel);

        // 6. Main Scene & Window setup
        Scene scene = new Scene(root, 1380, 890);
        primaryStage.setTitle("Smart Parking Finder – 3D Simulation System (JavaFX 3D)");
        primaryStage.setScene(scene);
        primaryStage.show();

        // 7. Start 60 FPS Animation & Physics Loop
        startAnimationLoop();

        // Initial GUI sync
        refreshHUD();
    }

    /**
     * Creates the HUD Dashboard pinned at the top:
     * - App Title & Tagline
     * - 4 KPI Stat Cards (Total, Occupied, Available, Waiting Queue)
     * - Recent Activity Stack LIFO Banner
     */
    private VBox createTopDashboard() {
        VBox container = new VBox(6);
        container.setPadding(new Insets(10, 16, 10, 16));
        container.setStyle("-fx-background-color: #1E293B; -fx-border-color: #334155; -fx-border-width: 0 0 1 0;");

        // Title row
        HBox titleRow = new HBox(12);
        titleRow.setAlignment(Pos.CENTER_LEFT);

        Label title = new Label("SMART PARKING FINDER 3D");
        title.setStyle("-fx-font-family: 'SansSerif'; -fx-font-size: 17px; -fx-font-weight: bold; -fx-text-fill: #F8FAFC;");

        Label badge = new Label("3D SIMULATION • JAVA DSA");
        badge.setStyle("-fx-background-color: #2563EB; -fx-text-fill: white; -fx-font-size: 10px; -fx-font-weight: bold; -fx-padding: 2 7 2 7; -fx-background-radius: 4;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // Zone Legend pills
        HBox legendBox = new HBox(10);
        legendBox.setAlignment(Pos.CENTER_RIGHT);
        legendBox.getChildren().addAll(
                createLegendPill("Bike Zone (B1-B10)", "#0284C7"),
                createLegendPill("Car Zone (C1-C10)", "#16A34A"),
                createLegendPill("Truck Zone (T1-T10)", "#D97706")
        );

        titleRow.getChildren().addAll(title, badge, spacer, legendBox);

        // 4 KPI Stat Cards
        HBox cardsRow = new HBox(12);
        cardsRow.setAlignment(Pos.CENTER);

        totalSpacesVal = new Label("30");
        occupiedSpacesVal = new Label("0");
        availableSpacesVal = new Label("30");
        waitingQueueVal = new Label("0");

        cardsRow.getChildren().addAll(
                createKpiCard("TOTAL SPACES", totalSpacesVal, "#3B82F6"),
                createKpiCard("OCCUPIED", occupiedSpacesVal, "#EF4444"),
                createKpiCard("AVAILABLE", availableSpacesVal, "#10B981"),
                createKpiCard("WAITING QUEUE (FIFO)", waitingQueueVal, "#A855F7")
        );
        HBox.setHgrow(cardsRow, Priority.ALWAYS);

        // Activity and Status Bar
        HBox statusRow = new HBox(15);
        statusRow.setAlignment(Pos.CENTER_LEFT);

        stackActivityLbl = new Label("Recent Activity (Stack LIFO): " + manager.getRecentActivity());
        stackActivityLbl.setStyle("-fx-font-family: 'Monospaced'; -fx-font-size: 11px; -fx-text-fill: #94A3B8;");

        Region sp2 = new Region();
        HBox.setHgrow(sp2, Priority.ALWAYS);

        statusMsgLbl = new Label("Ready. Start auto simulation or use manual controls.");
        statusMsgLbl.setStyle("-fx-font-family: 'SansSerif'; -fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: #38BDF8;");

        statusRow.getChildren().addAll(stackActivityLbl, sp2, statusMsgLbl);

        container.getChildren().addAll(titleRow, cardsRow, statusRow);
        return container;
    }

    private Node createKpiCard(String title, Label valLbl, String colorHex) {
        VBox card = new VBox(2);
        card.setPadding(new Insets(6, 12, 6, 12));
        card.setAlignment(Pos.CENTER_LEFT);
        card.setStyle("-fx-background-color: #0F172A; -fx-background-radius: 6; -fx-border-color: #334155; -fx-border-radius: 6;");
        HBox.setHgrow(card, Priority.ALWAYS);

        Label titleLbl = new Label(title);
        titleLbl.setStyle("-fx-font-size: 10px; -fx-text-fill: #94A3B8; -fx-font-weight: bold;");

        valLbl.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: " + colorHex + ";");

        card.getChildren().addAll(titleLbl, valLbl);
        return card;
    }

    private Node createLegendPill(String text, String colorHex) {
        HBox pill = new HBox(6);
        pill.setAlignment(Pos.CENTER);
        pill.setPadding(new Insets(3, 8, 3, 8));
        pill.setStyle("-fx-background-color: #0F172A; -fx-background-radius: 12; -fx-border-color: " + colorHex + "; -fx-border-radius: 12;");

        Region dot = new Region();
        dot.setPrefSize(7, 7);
        dot.setStyle("-fx-background-color: " + colorHex + "; -fx-background-radius: 4;");

        Label label = new Label(text);
        label.setStyle("-fx-font-size: 11px; -fx-text-fill: #E2E8F0;");

        pill.getChildren().addAll(dot, label);
        return pill;
    }

    /**
     * Creates Bottom Control Toolbar:
     * - Auto Simulation Controls (Start, Pause, Resume, Reset, Speed)
     * - Manual Park (Category, ID, Park button)
     * - Manual Exit (Exit ID, Exit button)
     * - Search & Sort (Binary Search, Selection Sort)
     * - Camera Presets (Isometric, Top-Down, Entrance)
     */
    private VBox createBottomControls() {
        VBox container = new VBox(8);
        container.setPadding(new Insets(10, 16, 12, 16));
        container.setStyle("-fx-background-color: #1E293B; -fx-border-color: #334155; -fx-border-width: 1 0 0 0;");

        HBox toolbar = new HBox(16);
        toolbar.setAlignment(Pos.CENTER_LEFT);

        // 1. Auto Simulation Section
        HBox simBox = new HBox(8);
        simBox.setAlignment(Pos.CENTER_LEFT);

        startSimBtn = createStyledButton("▶ Start Auto Sim", "#2563EB");
        startSimBtn.setOnAction(e -> startAutoSimulation());

        pauseSimBtn = createStyledButton("⏸ Pause", "#D97706");
        pauseSimBtn.setDisable(true);
        pauseSimBtn.setOnAction(e -> pauseAutoSimulation());

        Button resetBtn = createStyledButton("↺ Reset", "#DC2626");
        resetBtn.setOnAction(e -> resetSimulation());

        Label speedLbl = new Label("Speed:");
        speedLbl.setStyle("-fx-text-fill: #94A3B8; -fx-font-size: 11px;");

        speedSlider = new Slider(0.5, 3.0, 1.0);
        speedSlider.setPrefWidth(90);
        speedSlider.valueProperty().addListener((obs, oldV, newV) -> {
            simSpeed = newV.doubleValue();
            for (Vehicle3D v : movingVehicles) {
                v.setSpeed(120.0 * simSpeed);
            }
        });

        simBox.getChildren().addAll(startSimBtn, pauseSimBtn, resetBtn, speedLbl, speedSlider);

        // Divider
        Separator sep1 = new Separator(javafx.geometry.Orientation.VERTICAL);

        // 2. Manual Park Section
        HBox parkBox = new HBox(6);
        parkBox.setAlignment(Pos.CENTER_LEFT);

        Label parkLbl = new Label("Park:");
        parkLbl.setStyle("-fx-text-fill: #F1F5F9; -fx-font-weight: bold; -fx-font-size: 12px;");

        categoryCombo = new ComboBox<>();
        categoryCombo.getItems().addAll("Bike", "Car", "Truck");
        categoryCombo.setValue("Car");
        categoryCombo.setStyle("-fx-background-color: #0F172A; -fx-text-fill: white; -fx-border-color: #475569;");

        parkIdField = new JTextFieldFx(7);
        parkIdField.setText("CR-101");
        parkIdField.setStyle("-fx-background-color: #0F172A; -fx-text-fill: #F8FAFC; -fx-border-color: #475569; -fx-pref-width: 75px;");

        Button parkBtn = createStyledButton("Park", "#10B981");
        parkBtn.setOnAction(e -> handleManualPark());

        parkBox.getChildren().addAll(parkLbl, categoryCombo, parkIdField, parkBtn);

        // Divider
        Separator sep2 = new Separator(javafx.geometry.Orientation.VERTICAL);

        // 3. Manual Exit Section
        HBox exitBox = new HBox(6);
        exitBox.setAlignment(Pos.CENTER_LEFT);

        Label exitLbl = new Label("Exit ID:");
        exitLbl.setStyle("-fx-text-fill: #F1F5F9; -fx-font-weight: bold; -fx-font-size: 12px;");

        exitIdField = new JTextFieldFx(7);
        exitIdField.setPromptText("e.g. CR-101");
        exitIdField.setStyle("-fx-background-color: #0F172A; -fx-text-fill: #F8FAFC; -fx-border-color: #475569; -fx-pref-width: 80px;");

        Button exitBtn = createStyledButton("Exit", "#EF4444");
        exitBtn.setOnAction(e -> handleManualExit());

        exitBox.getChildren().addAll(exitLbl, exitIdField, exitBtn);

        // Divider
        Separator sep3 = new Separator(javafx.geometry.Orientation.VERTICAL);

        // 4. DSA Search & Sort Section
        HBox dsaBox = new HBox(6);
        dsaBox.setAlignment(Pos.CENTER_LEFT);

        searchIdField = new JTextFieldFx(7);
        searchIdField.setPromptText("Find ID");
        searchIdField.setStyle("-fx-background-color: #0F172A; -fx-text-fill: #F8FAFC; -fx-border-color: #475569; -fx-pref-width: 75px;");

        Button searchBtn = createStyledButton("🔍 Binary Search", "#0D9488");
        searchBtn.setOnAction(e -> handleBinarySearch());

        Button sortBtn = createStyledButton("📋 Selection Sort", "#6366F1");
        sortBtn.setOnAction(e -> handleShowSortedRecords());

        dsaBox.getChildren().addAll(searchIdField, searchBtn, sortBtn);

        // Divider
        Separator sep4 = new Separator(javafx.geometry.Orientation.VERTICAL);

        // 5. Camera Presets
        HBox camBox = new HBox(5);
        camBox.setAlignment(Pos.CENTER_LEFT);

        Button camIso = createSmallButton("Isometric");
        camIso.setOnAction(e -> lot3DScene.setCameraIsometric());

        Button camTop = createSmallButton("Top-Down");
        camTop.setOnAction(e -> lot3DScene.setCameraTopDown());

        Button camEnt = createSmallButton("Entrance");
        camEnt.setOnAction(e -> lot3DScene.setCameraEntrance());

        camBox.getChildren().addAll(new Label("Cam:"), camIso, camTop, camEnt);

        toolbar.getChildren().addAll(simBox, sep1, parkBox, sep2, exitBox, sep3, dsaBox, sep4, camBox);
        container.getChildren().add(toolbar);
        return container;
    }

    /**
     * Creates Right Waiting Queue Drawer:
     * - Real-time FIFO Waiting Queue list
     */
    private VBox createRightWaitingDrawer() {
        VBox drawer = new VBox(8);
        drawer.setPrefWidth(240);
        drawer.setPadding(new Insets(10));
        drawer.setStyle("-fx-background-color: #1E293B; -fx-border-color: #334155; -fx-border-width: 0 0 0 1;");

        Label title = new Label("Waiting Vehicles (FIFO)");
        title.setStyle("-fx-text-fill: #F8FAFC; -fx-font-weight: bold; -fx-font-size: 13px;");

        waitingSummaryLbl = new Label("0 vehicles waiting");
        waitingSummaryLbl.setStyle("-fx-text-fill: #C084FC; -fx-font-size: 11px;");

        waitingCardsBox = new VBox(6);
        waitingCardsBox.setPadding(new Insets(4));

        ScrollPane sp = new ScrollPane(waitingCardsBox);
        sp.setFitToWidth(true);
        sp.setStyle("-fx-background: #0F172A; -fx-background-color: transparent; -fx-border-color: #334155;");
        VBox.setVgrow(sp, Priority.ALWAYS);

        drawer.getChildren().addAll(title, waitingSummaryLbl, sp);
        return drawer;
    }

    private Button createStyledButton(String text, String bgHex) {
        Button btn = new Button(text);
        btn.setStyle("-fx-background-color: " + bgHex + "; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 11px; -fx-cursor: hand; -fx-background-radius: 4;");
        return btn;
    }

    private Button createSmallButton(String text) {
        Button btn = new Button(text);
        btn.setStyle("-fx-background-color: #334155; -fx-text-fill: #E2E8F0; -fx-font-size: 10px; -fx-cursor: hand; -fx-background-radius: 3;");
        return btn;
    }

    // Custom TextField wrapper for JavaFX
    private static class JTextFieldFx extends TextField {
        public JTextFieldFx(int cols) {
            setPrefColumnCount(cols);
        }
    }

    /**
     * Interactive 3D SubScene Mouse Controls (Orbit, Pan, Zoom)
     */
    private void setupSubSceneMouseControls(SubScene subScene) {
        subScene.setOnMousePressed(me -> {
            mouseAnchorX = me.getSceneX();
            mouseAnchorY = me.getSceneY();
        });

        subScene.setOnMouseDragged(me -> {
            double dx = me.getSceneX() - mouseAnchorX;
            double dy = me.getSceneY() - mouseAnchorY;

            if (me.isPrimaryButtonDown()) {
                // Left click drag: Orbit Rotate
                lot3DScene.rotateCamera(dx, dy);
            } else if (me.isSecondaryButtonDown() || me.isMiddleButtonDown()) {
                // Right click drag: Pan
                lot3DScene.panCamera(dx, dy);
            }

            mouseAnchorX = me.getSceneX();
            mouseAnchorY = me.getSceneY();
        });

        subScene.setOnScroll(se -> {
            lot3DScene.zoomCamera(se.getDeltaY());
        });
    }

    /**
     * Main 60 FPS Animation Loop
     */
    private void startAnimationLoop() {
        AnimationTimer timer = new AnimationTimer() {
            private long lastNano = 0;

            @Override
            public void handle(long now) {
                if (lastNano == 0) {
                    lastNano = now;
                    return;
                }

                double dt = (now - lastNano) / 1e9;
                lastNano = now;

                // Clamp delta time to avoid jumps
                if (dt > 0.1) dt = 0.1;

                // Update vehicle physics and waypoint navigation
                updateVehicleMovements(dt);

                // Auto Simulation Spawner
                if (autoSimRunning && !autoSimPaused) {
                    simTimer += dt * simSpeed;
                    if (simTimer >= spawnInterval) {
                        simTimer = 0.0;
                        handleAutoSpawn();
                    }
                }
            }
        };
        timer.start();
    }

    /**
     * Updates physics and path navigation for all moving vehicles
     */
    private void updateVehicleMovements(double dt) {
        Iterator<Vehicle3D> it = movingVehicles.iterator();
        while (it.hasNext()) {
            Vehicle3D v = it.next();
            boolean finished = v.updateMovement(dt);

            if (finished) {
                if (v.getState() == Vehicle3D.State.PARKED) {
                    // Spot reached and parked
                    it.remove();
                    lot3DScene.setSpotStatus(v.getAssignedRow(), v.getAssignedCol(), true, false);
                    lot3DScene.setEntranceBarrierOpen(false);
                    refreshHUD();
                } else if (v.getState() == Vehicle3D.State.FINISHED) {
                    // Exit reached and left the lot
                    it.remove();
                    activeVehicles.remove(v.getVehicleId());
                    lot3DScene.getRoot3D().getChildren().remove(v);
                    lot3DScene.setExitBarrierOpen(false);
                    refreshHUD();
                }
            }
        }
    }

    /**
     * Auto Simulation Spawner Logic:
     * Spawns random incoming vehicles, and occasionally requests an exit
     */
    private void handleAutoSpawn() {
        // Occasionally trigger a vehicle exit if lot is moderately occupied
        if (activeVehicles.size() > 5 && random.nextDouble() < 0.35) {
            triggerRandomExit();
            return;
        }

        String[] categories = { "Bike", "Car", "Truck" };
        String cat = categories[random.nextInt(categories.length)];
        String idPrefix = cat.equals("Bike") ? "BK" : (cat.equals("Car") ? "CR" : "TR");
        String id = idPrefix + "-" + (autoIdCounter++);

        spawnAndParkVehicle(cat, id, getRandomColorForCategory(cat));
    }

    private void triggerRandomExit() {
        List<String> parkedIds = new ArrayList<>();
        for (Vehicle3D v : activeVehicles.values()) {
            if (v.getState() == Vehicle3D.State.PARKED) {
                parkedIds.add(v.getVehicleId());
            }
        }

        if (!parkedIds.isEmpty()) {
            String toExit = parkedIds.get(random.nextInt(parkedIds.size()));
            requestVehicleExit(toExit);
        }
    }

    /**
     * Core Vehicle Parking Pipeline:
     * 1. Consults ParkingManager (Linear Search & Manhattan Distance).
     * 2. If available, computes BFS route from Entrance to Bay.
     * 3. Spawns 3D vehicle and animates smooth road navigation.
     * 4. If full, adds to FIFO Queue.
     */
    private void spawnAndParkVehicle(String category, String id, Color color) {
        String result = manager.parkVehicle(category, id);

        if (result.equals("DUPLICATE")) {
            statusMsgLbl.setText("⚠ Vehicle ID '" + id + "' is already in the lot or queue!");
            statusMsgLbl.setStyle("-fx-text-fill: #EF4444; -fx-font-weight: bold;");
            return;
        }

        if (result.equals("QUEUED")) {
            statusMsgLbl.setText("ⓘ " + category + " Zone is full! Vehicle [" + id + "] added to Waiting Queue.");
            statusMsgLbl.setStyle("-fx-text-fill: #A855F7; -fx-font-weight: bold;");
            refreshHUD();
            return;
        }

        if (result.equals("INVALID")) {
            statusMsgLbl.setText("⚠ Invalid category selected.");
            statusMsgLbl.setStyle("-fx-text-fill: #EF4444; -fx-font-weight: bold;");
            return;
        }

        // Spot found! e.g. "B1", "C3", "T10"
        int[] coords = manager.getSpotCoordinates(result);
        if (coords == null) return;
        int row = coords[0];
        int col = coords[1];

        // 1. Calculate BFS Waypoint Path from Entrance to Spot
        List<PathPlanner.Waypoint> path = pathPlanner.getParkingPath(row, col);

        // 2. Instantiate 3D vehicle
        Vehicle3D vehicle = new Vehicle3D(category, id, color);
        vehicle.setAssignedSpot(row, col, result);
        vehicle.setPath(path);
        vehicle.setSpeed(120.0 * simSpeed);
        vehicle.setState(Vehicle3D.State.DRIVING_TO_SPOT);

        // 3. Add to 3D scene and lists
        activeVehicles.put(id, vehicle);
        movingVehicles.add(vehicle);
        lot3DScene.getRoot3D().getChildren().add(vehicle);

        // Open entrance barrier
        lot3DScene.setEntranceBarrierOpen(true);

        int dist = manager.getLastAllocatedDistance();
        statusMsgLbl.setText("✓ " + category + " [" + id + "] -> Allocated Space " + result + " (Manhattan Dist: " + dist + ")");
        statusMsgLbl.setStyle("-fx-text-fill: #10B981; -fx-font-weight: bold;");

        refreshHUD();
    }

    /**
     * Core Vehicle Exit Pipeline:
     * 1. Finds vehicle in ParkingManager.
     * 2. Computes BFS route from Bay to Exit Gate.
     * 3. Animate vehicle driving out.
     * 4. Re-allocates freed spot if compatible vehicle is waiting in FIFO Queue!
     */
    private void requestVehicleExit(String id) {
        Vehicle3D vehicle = activeVehicles.get(id);

        ParkingManager.ExitResult result = manager.exitVehicle(id);

        if (!result.isSuccess()) {
            statusMsgLbl.setText("⚠ " + result.getMessage());
            statusMsgLbl.setStyle("-fx-text-fill: #EF4444; -fx-font-weight: bold;");
            return;
        }

        if (vehicle != null && vehicle.getState() == Vehicle3D.State.PARKED) {
            int row = vehicle.getAssignedRow();
            int col = vehicle.getAssignedCol();

            // 1. Compute BFS Exit Path to Exit Gate
            List<PathPlanner.Waypoint> exitPath = pathPlanner.getExitPath(row, col);
            vehicle.setPath(exitPath);
            vehicle.setState(Vehicle3D.State.EXITING);
            movingVehicles.add(vehicle);

            // Open exit barrier
            lot3DScene.setExitBarrierOpen(true);

            // Turn spot LED back to green (or keep for waiting car)
            lot3DScene.setSpotStatus(row, col, false, false);
        }

        // Check if a waiting vehicle was auto-promoted from Queue
        String waitingId = result.getAllocatedWaitingVehicleId();
        if (waitingId != null) {
            // Find category
            String cat = "Car";
            if (result.getSpotId().startsWith("B")) cat = "Bike";
            else if (result.getSpotId().startsWith("T")) cat = "Truck";

            // Spawn the promoted waiting vehicle
            spawnAndParkVehicle(cat, waitingId, getRandomColorForCategory(cat));
        }

        statusMsgLbl.setText("✓ " + result.getMessage());
        statusMsgLbl.setStyle("-fx-text-fill: #10B981; -fx-font-weight: bold;");

        refreshHUD();
    }

    // Manual Park Handler
    private void handleManualPark() {
        String cat = categoryCombo.getValue();
        String id = parkIdField.getText().trim();
        if (id.isEmpty()) {
            statusMsgLbl.setText("⚠ Please enter a vehicle ID to park.");
            statusMsgLbl.setStyle("-fx-text-fill: #EF4444; -fx-font-weight: bold;");
            return;
        }

        spawnAndParkVehicle(cat, id, getRandomColorForCategory(cat));
        parkIdField.setText(cat.substring(0, 2).toUpperCase() + "-" + (autoIdCounter++));
    }

    // Manual Exit Handler
    private void handleManualExit() {
        String id = exitIdField.getText().trim();
        if (id.isEmpty()) {
            statusMsgLbl.setText("⚠ Please enter a vehicle ID to exit.");
            statusMsgLbl.setStyle("-fx-text-fill: #EF4444; -fx-font-weight: bold;");
            return;
        }

        requestVehicleExit(id);
        exitIdField.clear();
    }

    // DSA: Binary Search Handler
    private void handleBinarySearch() {
        String searchId = searchIdField.getText().trim();
        if (searchId.isEmpty()) {
            statusMsgLbl.setText("⚠ Please enter a vehicle ID to search.");
            statusMsgLbl.setStyle("-fx-text-fill: #EF4444; -fx-font-weight: bold;");
            return;
        }

        // Clear previous highlights
        for (Vehicle3D v : activeVehicles.values()) {
            v.setHighlighted(false);
            if (v.getState() == Vehicle3D.State.PARKED) {
                lot3DScene.setSpotStatus(v.getAssignedRow(), v.getAssignedCol(), true, false);
            }
        }

        // ALGORITHM: Binary Search O(log N)
        ParkingManager.ParkingRecord rec = manager.searchVehicle(searchId);

        if (rec != null) {
            Vehicle3D target = activeVehicles.get(rec.getVehicleId());
            if (target != null) {
                target.setHighlighted(true);
                lot3DScene.setSpotStatus(rec.getRow(), rec.getCol(), true, true);
            }
            statusMsgLbl.setText("✓ Binary Search: Found " + rec.getCategory() + " [" + rec.getVehicleId() + "] at Space " + rec.getSpotId() + "!");
            statusMsgLbl.setStyle("-fx-text-fill: #2DD4BF; -fx-font-weight: bold;");
        } else {
            int queuePos = manager.getWaitingPosition(searchId);
            if (queuePos != -1) {
                statusMsgLbl.setText("ⓘ Vehicle [" + searchId + "] is waiting in Queue (Position #" + queuePos + ").");
                statusMsgLbl.setStyle("-fx-text-fill: #C084FC; -fx-font-weight: bold;");
            } else {
                statusMsgLbl.setText("⚠ Vehicle ID '" + searchId + "' not found in parking spaces or waiting queue.");
                statusMsgLbl.setStyle("-fx-text-fill: #EF4444; -fx-font-weight: bold;");
            }
        }
    }

    // DSA: Selection Sort Dialog Handler
    private void handleShowSortedRecords() {
        // ALGORITHM: Selection Sort O(N^2)
        List<ParkingManager.ParkingRecord> sorted = manager.getSortedOccupiedRecords();

        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Sorted Parking Records (Selection Sort O(N^2))");
        dialog.setHeaderText("Alphabetical Order by Vehicle ID (Selection Sort Algorithm)");

        TextArea area = new TextArea();
        area.setEditable(false);
        area.setPrefSize(480, 280);
        area.setStyle("-fx-font-family: 'Monospaced'; -fx-font-size: 12px;");

        StringBuilder sb = new StringBuilder();
        sb.append(String.format("%-4s | %-12s | %-8s | %s\n", "No.", "Vehicle ID", "Category", "Parking Space"));
        sb.append("--------------------------------------------------\n");

        if (sorted.isEmpty()) {
            sb.append("No vehicles are currently parked.\n");
        } else {
            int idx = 1;
            for (ParkingManager.ParkingRecord r : sorted) {
                sb.append(String.format("%-4d | %-12s | %-8s | %s\n", idx++, r.getVehicleId(), r.getCategory(), r.getSpotId()));
            }
        }
        sb.append("--------------------------------------------------\n");
        sb.append("Total Occupied: ").append(sorted.size()).append(" spaces.\n");
        sb.append("Algorithm: In-place Selection Sort O(N^2)");

        area.setText(sb.toString());
        dialog.getDialogPane().setContent(area);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        dialog.showAndWait();
    }

    // Auto Simulation Controls
    private void startAutoSimulation() {
        autoSimRunning = true;
        autoSimPaused = false;
        startSimBtn.setDisable(true);
        pauseSimBtn.setDisable(false);
        statusMsgLbl.setText("▶ Auto simulation running. Spawning arriving vehicles...");
        statusMsgLbl.setStyle("-fx-text-fill: #38BDF8; -fx-font-weight: bold;");
    }

    private void pauseAutoSimulation() {
        if (!autoSimPaused) {
            autoSimPaused = true;
            pauseSimBtn.setText("▶ Resume");
            statusMsgLbl.setText("⏸ Simulation paused.");
        } else {
            autoSimPaused = false;
            pauseSimBtn.setText("⏸ Pause");
            statusMsgLbl.setText("▶ Simulation resumed.");
        }
    }

    private void resetSimulation() {
        autoSimRunning = false;
        autoSimPaused = false;
        startSimBtn.setDisable(false);
        pauseSimBtn.setDisable(true);
        pauseSimBtn.setText("⏸ Pause");

        // Clear 3D vehicles
        for (Vehicle3D v : activeVehicles.values()) {
            lot3DScene.getRoot3D().getChildren().remove(v);
        }
        activeVehicles.clear();
        movingVehicles.clear();

        // Reset DSA backend
        manager.reset();

        // Reset LEDs
        for (int r = 0; r < 5; r++) {
            for (int c = 0; c < 6; c++) {
                lot3DScene.setSpotStatus(r, c, false, false);
            }
        }

        lot3DScene.setEntranceBarrierOpen(false);
        lot3DScene.setExitBarrierOpen(false);
        lot3DScene.setCameraIsometric();

        statusMsgLbl.setText("↺ Simulation reset. All spaces empty.");
        statusMsgLbl.setStyle("-fx-text-fill: #38BDF8; -fx-font-weight: bold;");

        refreshHUD();
    }

    /**
     * Refreshes Top HUD Cards and Right Waiting Drawer
     */
    private void refreshHUD() {
        Platform.runLater(() -> {
            totalSpacesVal.setText(String.valueOf(manager.getTotalSpaces()));
            occupiedSpacesVal.setText(String.valueOf(manager.getOccupiedSpaces()));
            availableSpacesVal.setText(String.valueOf(manager.getAvailableSpaces()));
            waitingQueueVal.setText(String.valueOf(manager.getWaitingQueueCount()));

            stackActivityLbl.setText("Recent Activity (Stack LIFO): " + manager.getRecentActivity());

            // Update Waiting Vehicles Panel
            updateWaitingDrawer();
        });
    }

    private void updateWaitingDrawer() {
        waitingCardsBox.getChildren().clear();
        Queue<ParkingManager.WaitingVehicle> queue = manager.getWaitingQueue();

        int count = manager.getWaitingQueueCount();
        waitingSummaryLbl.setText(count + " vehicle" + (count == 1 ? "" : "s") + " waiting");

        if (queue.isEmpty()) {
            Label empty = new Label("No vehicles waiting");
            empty.setStyle("-fx-text-fill: #64748B; -fx-font-style: italic; -fx-font-size: 11px;");
            waitingCardsBox.getChildren().add(empty);
        } else {
            int pos = 1;
            for (ParkingManager.WaitingVehicle v : queue) {
                VBox card = new VBox(2);
                card.setPadding(new Insets(5, 8, 5, 8));

                String color = v.getCategory().equalsIgnoreCase("Bike") ? "#0284C7" : (v.getCategory().equalsIgnoreCase("Car") ? "#16A34A" : "#D97706");
                card.setStyle("-fx-background-color: #0F172A; -fx-border-color: " + color + "; -fx-border-radius: 4; -fx-background-radius: 4;");

                Label posLbl = new Label("Position #" + (pos++));
                posLbl.setStyle("-fx-font-size: 10px; -fx-font-weight: bold; -fx-text-fill: " + color + ";");

                Label infoLbl = new Label(v.getCategory() + " [" + v.getVehicleId() + "]");
                infoLbl.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: #F1F5F9;");

                card.getChildren().addAll(posLbl, infoLbl);
                waitingCardsBox.getChildren().add(card);
            }
        }
    }

    private Color getRandomColorForCategory(String category) {
        if (category.equalsIgnoreCase("Bike")) {
            return bikeColors[random.nextInt(bikeColors.length)];
        } else if (category.equalsIgnoreCase("Truck")) {
            return truckColors[random.nextInt(truckColors.length)];
        } else {
            return carColors[random.nextInt(carColors.length)];
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
