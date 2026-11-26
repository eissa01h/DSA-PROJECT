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

    // Spawning Queue: ensures vehicles spawn one at a time and never overlap at the entrance
    private static class SpawnRequest {
        final String category;
        final String id;
        final Color color;
        final String preallocatedSpotId; // null if needs Manhattan search, or spotId if promoted from Queue

        SpawnRequest(String category, String id, Color color, String preallocatedSpotId) {
            this.category = category;
            this.id = id;
            this.color = color;
            this.preallocatedSpotId = preallocatedSpotId;
        }
    }
    private final Queue<SpawnRequest> pendingSpawnQueue = new LinkedList<>();
    private final Queue<String> pendingExitQueue = new LinkedList<>();

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
    private Button topStartSimBtn;
    private Button pauseSimBtn;
    private Slider speedSlider;

    // Simulation Engine State
    private boolean autoSimRunning = false;
    private boolean autoSimPaused = false;
    private double simTimer = 0.0;
    private double spawnInterval = 3.6; // seconds between auto arrivals (ample headway)
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
        SubScene subScene3D = new SubScene(lot3DScene.getRoot3D(), 1050, 560, true, SceneAntialiasing.BALANCED);
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

        // 6. Main Scene & Window setup (Auto-sized to fit screen visual bounds)
        javafx.geometry.Rectangle2D screenBounds = javafx.stage.Screen.getPrimary().getVisualBounds();
        double targetW = Math.min(1240, screenBounds.getWidth() * 0.95);
        double targetH = Math.min(710, screenBounds.getHeight() * 0.92);

        Scene scene = new Scene(root, targetW, targetH);

        // Keyboard Shortcuts
        scene.setOnKeyPressed(ke -> {
            if (ke.getCode() == javafx.scene.input.KeyCode.SPACE) {
                if (!autoSimRunning) startAutoSimulation();
                else pauseAutoSimulation();
            } else if (ke.getCode() == javafx.scene.input.KeyCode.R) {
                resetSimulation();
            } else if (ke.getCode() == javafx.scene.input.KeyCode.DIGIT1) {
                lot3DScene.setCameraIsometric();
            } else if (ke.getCode() == javafx.scene.input.KeyCode.DIGIT2) {
                lot3DScene.setCameraTopDown();
            } else if (ke.getCode() == javafx.scene.input.KeyCode.DIGIT3) {
                lot3DScene.setCameraEntrance();
            }
        });

        primaryStage.setTitle("Smart Parking Finder – 3D Simulation System (JavaFX 3D)");
        primaryStage.setScene(scene);
        primaryStage.show();

        // 7. Start 60 FPS Animation & Physics Loop
        startAnimationLoop();

        // 8. Auto-start simulation after 0.6s so vehicles immediately arrive and park at nearest spots
        javafx.animation.PauseTransition autoStart = new javafx.animation.PauseTransition(javafx.util.Duration.millis(600));
        autoStart.setOnFinished(e -> startAutoSimulation());
        autoStart.play();

        // Initial GUI sync
        refreshHUD();
    }

    /**
     * Spawns an initial parked vehicle at application startup so spaces are occupied immediately.
     */
    private void spawnParkedInitialVehicle(String category, String id, Color color) {
        String spotId = manager.parkVehicle(category, id);
        if (!spotId.equals("DUPLICATE") && !spotId.equals("QUEUED") && !spotId.equals("INVALID")) {
            int[] coords = manager.getSpotCoordinates(spotId);
            if (coords != null) {
                Vehicle3D vehicle = new Vehicle3D(category, id, color);
                vehicle.setAssignedSpot(coords[0], coords[1], spotId);
                double stallX = PathPlanner.COL_X[coords[1]];
                double stallZ = PathPlanner.ROW_Z[coords[0]];
                vehicle.setPosition(stallX, 0, stallZ);
                vehicle.setState(Vehicle3D.State.PARKED);
                if (coords[1] % 2 == 0) {
                    vehicle.setYaw(-90);
                } else {
                    vehicle.setYaw(90);
                }
                activeVehicles.put(id, vehicle);
                lot3DScene.getRoot3D().getChildren().add(vehicle);
                lot3DScene.setSpotStatus(coords[0], coords[1], true, false);
            }
        }
    }

    /**
     * Creates a compact, streamlined 2-row HUD Dashboard pinned at the top:
     * - Row 1: App Title, Quick Start button, Live status message, Zone Legend pills
     * - Row 2: Compact KPI counters (Total, Occupied, Available, Waiting Queue) + Recent Activity
     */
    private VBox createTopDashboard() {
        VBox container = new VBox(4);
        container.setPadding(new Insets(5, 12, 5, 12));
        container.setStyle("-fx-background-color: #1E293B; -fx-border-color: #334155; -fx-border-width: 0 0 1 0;");

        // Row 1: Title, Quick Start, Status, Legends
        HBox topRow = new HBox(10);
        topRow.setAlignment(Pos.CENTER_LEFT);

        Label title = new Label("SMART PARKING 3D");
        title.setStyle("-fx-font-family: 'SansSerif'; -fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: #F8FAFC;");

        Label badge = new Label("JAVA DSA");
        badge.setStyle("-fx-background-color: #2563EB; -fx-text-fill: white; -fx-font-size: 9px; -fx-font-weight: bold; -fx-padding: 2 6 2 6; -fx-background-radius: 4;");

        topStartSimBtn = createStyledButton("▶ Start Auto Sim", "#2563EB");
        topStartSimBtn.setOnAction(e -> {
            if (!autoSimRunning) startAutoSimulation();
            else pauseAutoSimulation();
        });

        statusMsgLbl = new Label("Ready. Auto-simulation starting...");
        statusMsgLbl.setStyle("-fx-font-family: 'SansSerif'; -fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: #38BDF8;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox legendBox = new HBox(6);
        legendBox.setAlignment(Pos.CENTER_RIGHT);
        legendBox.getChildren().addAll(
                createLegendPill("Bikes (Zone 1)", "#0284C7"),
                createLegendPill("Cars (Zone 2)", "#16A34A"),
                createLegendPill("Trucks (Zone 3)", "#D97706")
        );

        topRow.getChildren().addAll(title, badge, topStartSimBtn, statusMsgLbl, spacer, legendBox);

        // Row 2: Compact KPI Cards + Activity Ticker
        HBox cardsRow = new HBox(8);
        cardsRow.setAlignment(Pos.CENTER_LEFT);

        totalSpacesVal = new Label("30");
        occupiedSpacesVal = new Label("0");
        availableSpacesVal = new Label("30");
        waitingQueueVal = new Label("0");

        cardsRow.getChildren().addAll(
                createKpiCard("TOTAL", totalSpacesVal, "#3B82F6"),
                createKpiCard("OCCUPIED", occupiedSpacesVal, "#EF4444"),
                createKpiCard("AVAILABLE", availableSpacesVal, "#10B981"),
                createKpiCard("WAITING (FIFO)", waitingQueueVal, "#A855F7")
        );

        Region sp2 = new Region();
        HBox.setHgrow(sp2, Priority.ALWAYS);

        stackActivityLbl = new Label("Activity: " + manager.getRecentActivity());
        stackActivityLbl.setStyle("-fx-font-family: 'Monospaced'; -fx-font-size: 11px; -fx-text-fill: #94A3B8;");

        cardsRow.getChildren().addAll(sp2, stackActivityLbl);

        container.getChildren().addAll(topRow, cardsRow);
        return container;
    }

    private Node createKpiCard(String title, Label valLbl, String colorHex) {
        HBox card = new HBox(5);
        card.setPadding(new Insets(2, 8, 2, 8));
        card.setAlignment(Pos.CENTER_LEFT);
        card.setStyle("-fx-background-color: #0F172A; -fx-background-radius: 4; -fx-border-color: #334155; -fx-border-radius: 4;");

        Label titleLbl = new Label(title + ":");
        titleLbl.setStyle("-fx-font-size: 10px; -fx-text-fill: #94A3B8; -fx-font-weight: bold;");

        valLbl.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: " + colorHex + ";");

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
        VBox container = new VBox(4);
        container.setPadding(new Insets(4, 10, 5, 10));
        container.setStyle("-fx-background-color: #1E293B; -fx-border-color: #334155; -fx-border-width: 1 0 0 0;");

        HBox toolbar = new HBox(10);
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
        VBox drawer = new VBox(6);
        drawer.setPrefWidth(200);
        drawer.setPadding(new Insets(6));
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

                if (!autoSimPaused) {
                    // 1. Update vehicle physics, navigation, and collision avoidance
                    updateVehicleMovements(dt);

                    // 2. Process orderly single-vehicle exits from queue
                    processPendingExits();

                    // 3. Process arrivals one at a time when entrance runway is clear
                    processPendingSpawns();

                    // 4. Auto Simulation Spawner
                    if (autoSimRunning) {
                        simTimer += dt * simSpeed;
                        if (simTimer >= spawnInterval) {
                            simTimer = 0.0;
                            handleAutoSpawn();
                        }
                    }
                }
            }
        };
        timer.start();
    }

    /**
     * Checks if the entrance roadway and approach are clear so arriving vehicles never overlap.
     */
    private boolean isEntranceClear() {
        for (Vehicle3D v : movingVehicles) {
            if (v.getState() == Vehicle3D.State.DRIVING_TO_SPOT) {
                // Must be well clear of entrance approach & initial merge before next spawn
                if (v.getX() < 120.0 || (v.getZ() < -50.0 && v.getX() < 180.0)) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Checks if a specific aisle (0 for Bikes, 1 for Cars, 2 for Trucks) is clear of moving traffic.
     */
    private boolean isAisleClear(int aisleIndex, Vehicle3D ignoreVehicle) {
        if (aisleIndex < 0 || aisleIndex >= PathPlanner.AISLE_X.length) return true;
        double aisleX = PathPlanner.AISLE_X[aisleIndex];
        for (Vehicle3D v : movingVehicles) {
            if (v == ignoreVehicle) continue;
            // Any vehicle driving or maneuvering in this aisle corridor
            if (Math.abs(v.getX() - aisleX) < 70.0 && v.getZ() >= -70.0 && v.getZ() <= 570.0) {
                return false;
            }
        }
        return true;
    }

    /**
     * Processes pending exits one by one so only one vehicle navigates the exit corridor at a time.
     */
    private void processPendingExits() {
        if (pendingExitQueue.isEmpty() || isAnyVehicleExiting()) {
            return;
        }
        String exitId = pendingExitQueue.poll();
        if (exitId != null) {
            executeVehicleExit(exitId);
        }
    }

    /**
     * Processes pending arrivals from the spawn queue one by one when the entrance is clear.
     */
    private void processPendingSpawns() {
        if (pendingSpawnQueue.isEmpty() || !isEntranceClear()) {
            return;
        }

        SpawnRequest req = pendingSpawnQueue.poll();
        if (req == null) return;

        if (req.preallocatedSpotId != null) {
            // Already allocated in manager via Waiting Queue promotion!
            executeVehicleArrival(req.category, req.id, req.color, req.preallocatedSpotId, true);
        } else {
            // Check & Allocate space with existing ParkingManager DSA logic
            String result = manager.parkVehicle(req.category, req.id);

            if (result.equals("DUPLICATE")) {
                statusMsgLbl.setText("⚠ Vehicle ID '" + req.id + "' is already in the lot or queue!");
                statusMsgLbl.setStyle("-fx-text-fill: #EF4444; -fx-font-weight: bold;");
                return;
            }

            if (result.equals("QUEUED")) {
                statusMsgLbl.setText("ⓘ " + req.category + " Zone is full! Vehicle [" + req.id + "] added to Waiting Queue.");
                statusMsgLbl.setStyle("-fx-text-fill: #A855F7; -fx-font-weight: bold;");
                refreshHUD();
                return;
            }

            if (result.equals("INVALID")) {
                statusMsgLbl.setText("⚠ Invalid category selected.");
                statusMsgLbl.setStyle("-fx-text-fill: #EF4444; -fx-font-weight: bold;");
                return;
            }

            executeVehicleArrival(req.category, req.id, req.color, result, false);
        }
    }

    /**
     * Spawns 3D vehicle at the entrance and begins smooth animated navigation along road lanes.
     */
    private void executeVehicleArrival(String category, String id, Color color, String spotId, boolean isPromoted) {
        int[] coords = manager.getSpotCoordinates(spotId);
        if (coords == null) return;
        int row = coords[0];
        int col = coords[1];

        // 1. Calculate BFS Waypoint Path from Entrance to Spot
        List<PathPlanner.Waypoint> path = pathPlanner.getParkingPath(row, col);

        // 2. Instantiate 3D vehicle
        Vehicle3D vehicle = new Vehicle3D(category, id, color);
        vehicle.setAssignedSpot(row, col, spotId);
        vehicle.setPath(path);
        vehicle.setSpeed(120.0 * simSpeed);
        vehicle.setState(Vehicle3D.State.DRIVING_TO_SPOT);

        // 3. Add to 3D scene and tracking collections
        activeVehicles.put(id, vehicle);
        movingVehicles.add(vehicle);
        lot3DScene.getRoot3D().getChildren().add(vehicle);

        if (isPromoted) {
            statusMsgLbl.setText("✓ Waiting Queue Promotion: " + category + " [" + id + "] -> Space " + spotId);
            statusMsgLbl.setStyle("-fx-text-fill: #A855F7; -fx-font-weight: bold;");
        } else {
            int dist = manager.getLastAllocatedDistance();
            statusMsgLbl.setText("✓ " + category + " [" + id + "] -> Allocated Space " + spotId + " (Manhattan Dist: " + dist + ")");
            statusMsgLbl.setStyle("-fx-text-fill: #10B981; -fx-font-weight: bold;");
        }

        refreshHUD();
    }

    /**
     * Checks if any vehicle is currently exiting to prevent multi-vehicle exit pileups.
     */
    private boolean isAnyVehicleExiting() {
        for (Vehicle3D v : movingVehicles) {
            if (v.getState() == Vehicle3D.State.EXITING) {
                return true;
            }
        }
        return false;
    }

    /**
     * Updates physics, path navigation, multi-stage collision avoidance,
     * and boom barrier automation for all moving vehicles at 60 FPS.
     */
    private void updateVehicleMovements(double dt) {
        boolean needEntranceOpen = false;
        boolean needExitOpen = false;

        for (int i = 0; i < movingVehicles.size(); i++) {
            Vehicle3D v = movingVehicles.get(i);

            // Barrier proximity detection
            if (v.getState() == Vehicle3D.State.DRIVING_TO_SPOT && v.getX() < 20.0 && v.getZ() < 0.0) {
                needEntranceOpen = true;
            }
            if (v.getState() == Vehicle3D.State.EXITING && v.getX() > 840.0 && v.getZ() > 500.0) {
                needExitOpen = true;
            }

            boolean yield = false;

            // 1. Aisle Hold Gate for Entering Vehicles on North Road:
            // If v is traveling on North Road (Z < -50) approaching its assigned aisle,
            // hold right before the aisle entrance if another vehicle is maneuvering in that aisle!
            if (v.getState() == Vehicle3D.State.DRIVING_TO_SPOT && v.getZ() < -50.0) {
                int targetAisle = v.getAssignedCol() / 2;
                double targetAisleX = PathPlanner.AISLE_X[targetAisle];
                if (v.getX() >= (targetAisleX - 70.0) && v.getX() <= targetAisleX) {
                    if (!isAisleClear(targetAisle, v)) {
                        yield = true; // Hold on North Road before entering aisle
                    }
                }
            }

            // 2. Dynamic Inter-Vehicle Collision Avoidance Check
            if (!yield) {
                for (int j = 0; j < movingVehicles.size(); j++) {
                    if (i == j) continue;
                    Vehicle3D other = movingVehicles.get(j);

                    double dx = other.getX() - v.getX();
                    double dz = other.getZ() - v.getZ();
                    double dist = Math.hypot(dx, dz);

                    // Category-based true physical length
                    double lenV = v.getCategory().equalsIgnoreCase("Truck") ? 88.0 : (v.getCategory().equalsIgnoreCase("Car") ? 54.0 : 38.0);
                    double lenOther = other.getCategory().equalsIgnoreCase("Truck") ? 88.0 : (other.getCategory().equalsIgnoreCase("Car") ? 54.0 : 38.0);
                    double minSafetyDist = (lenV + lenOther) * 0.5 + 24.0; // Bumper-to-bumper physical buffer

                    // A. Hard Proximity Stop (Any angle)
                    if (dist < minSafetyDist) {
                        if (v.getRemainingDistance() >= other.getRemainingDistance()) {
                            yield = true;
                            break;
                        }
                    }

                    // B. Safe Following Distance & Driving Corridor Check
                    double yawRad = Math.toRadians(v.getYaw());
                    double dirX = Math.sin(yawRad);
                    double dirZ = Math.cos(yawRad);

                    double forwardDist = dx * dirX + dz * dirZ;
                    double lateralDist = Math.abs(-dx * dirZ + dz * dirX);

                    // If other is ahead of v within v's driving lane (lateral < 36 units)
                    // and within following distance
                    if (forwardDist > 0.0 && forwardDist < (minSafetyDist + 40.0) && lateralDist < 36.0) {
                        yield = true; // Stop behind vehicle ahead
                        break;
                    }

                    // C. South Perimeter Road Intersection Yield:
                    // If v is at an aisle mouth approaching South Road (Z in [470, 575])
                    // and other is already traveling along South Road (Z around 580)
                    if (v.getZ() > 470.0 && v.getZ() < 575.0 && Math.abs(other.getZ() - 580.0) < 35.0) {
                        if (Math.abs(other.getX() - v.getX()) < 140.0 && other.getX() <= v.getX() + 40.0) {
                            yield = true; // Hold at aisle exit mouth until South Road is clear
                            break;
                        }
                    }
                }
            }

            double effectiveDt = yield ? 0.0 : (dt * simSpeed);
            boolean finished = v.updateMovement(effectiveDt);

            if (finished) {
                if (v.getState() == Vehicle3D.State.PARKED) {
                    lot3DScene.setSpotStatus(v.getAssignedRow(), v.getAssignedCol(), true, false);
                    refreshHUD();
                }
            }
        }

        // Clean up vehicles that reached final destination
        Iterator<Vehicle3D> it = movingVehicles.iterator();
        while (it.hasNext()) {
            Vehicle3D v = it.next();
            if (v.getState() == Vehicle3D.State.PARKED) {
                it.remove();
            } else if (v.getState() == Vehicle3D.State.FINISHED) {
                it.remove();
                activeVehicles.remove(v.getVehicleId());
                lot3DScene.getRoot3D().getChildren().remove(v);
                refreshHUD();
            }
        }

        lot3DScene.setEntranceBarrierOpen(needEntranceOpen);
        lot3DScene.setExitBarrierOpen(needExitOpen);
    }

    /**
     * Auto Simulation Spawner Logic:
     * Generates arriving vehicles and triggers orderly, non-colliding single exits.
     */
    private void handleAutoSpawn() {
        // Trigger exit only if enough cars parked AND no other vehicle is currently exiting and exit queue is empty
        if (activeVehicles.size() > 4 && !isAnyVehicleExiting() && pendingExitQueue.isEmpty() && random.nextDouble() < 0.28) {
            triggerRandomExit();
            return;
        }

        // Queue new arrival if queue isn't backed up
        if (pendingSpawnQueue.size() < 2) {
            String[] categories = { "Bike", "Car", "Truck" };
            String cat = categories[random.nextInt(categories.length)];
            String idPrefix = cat.equals("Bike") ? "BK" : (cat.equals("Car") ? "CR" : "TR");
            String id = idPrefix + "-" + (autoIdCounter++);

            pendingSpawnQueue.offer(new SpawnRequest(cat, id, getRandomColorForCategory(cat), null));
        }
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
     * Core Vehicle Exit Pipeline:
     * Enqueues or starts orderly single-vehicle exits so no vehicles ever pile up.
     */
    private void requestVehicleExit(String id) {
        Vehicle3D vehicle = activeVehicles.get(id);

        if (vehicle != null && vehicle.getState() != Vehicle3D.State.PARKED) {
            statusMsgLbl.setText("⚠ Vehicle [" + id + "] is currently in transit and cannot exit until parked.");
            statusMsgLbl.setStyle("-fx-text-fill: #EF4444; -fx-font-weight: bold;");
            return;
        }

        // If another vehicle is currently exiting or the exit queue is active, queue this exit
        if (isAnyVehicleExiting() || !pendingExitQueue.isEmpty()) {
            if (!pendingExitQueue.contains(id)) {
                pendingExitQueue.offer(id);
                statusMsgLbl.setText("ⓘ Vehicle [" + id + "] queued for exit (waiting for runway to clear).");
                statusMsgLbl.setStyle("-fx-text-fill: #3B82F6; -fx-font-weight: bold;");
            }
            return;
        }

        executeVehicleExit(id);
    }

    /**
     * Executes the actual physical exit when the road and aisle are clear.
     */
    private void executeVehicleExit(String id) {
        Vehicle3D vehicle = activeVehicles.get(id);
        if (vehicle == null || vehicle.getState() != Vehicle3D.State.PARKED) return;

        int row = vehicle.getAssignedRow();
        int col = vehicle.getAssignedCol();
        int aisle = col / 2;

        // If the aisle is not clear of moving vehicles, re-queue exit
        if (!isAisleClear(aisle, vehicle)) {
            if (!pendingExitQueue.contains(id)) {
                pendingExitQueue.offer(id);
            }
            return;
        }

        ParkingManager.ExitResult result = manager.exitVehicle(id);

        if (!result.isSuccess()) {
            statusMsgLbl.setText("⚠ " + result.getMessage());
            statusMsgLbl.setStyle("-fx-text-fill: #EF4444; -fx-font-weight: bold;");
            return;
        }

        // 1. Compute BFS Exit Path to Exit Gate & Runway
        List<PathPlanner.Waypoint> exitPath = pathPlanner.getExitPath(row, col);
        vehicle.setPath(exitPath);
        vehicle.setSpeed(115.0 * simSpeed);
        vehicle.setState(Vehicle3D.State.EXITING);
        movingVehicles.add(vehicle);

        // Turn spot LED back to green
        lot3DScene.setSpotStatus(row, col, false, false);

        // Check if a waiting vehicle was auto-promoted from Queue
        String waitingId = result.getAllocatedWaitingVehicleId();
        if (waitingId != null) {
            String cat = "Car";
            if (result.getSpotId().startsWith("B")) cat = "Bike";
            else if (result.getSpotId().startsWith("T")) cat = "Truck";

            // Enqueue promoted vehicle with its pre-allocated spotId
            pendingSpawnQueue.offer(new SpawnRequest(cat, waitingId, getRandomColorForCategory(cat), result.getSpotId()));
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

        pendingSpawnQueue.offer(new SpawnRequest(cat, id, getRandomColorForCategory(cat), null));
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

        // Clear previous highlights across all vehicles and spots
        for (Vehicle3D v : activeVehicles.values()) {
            v.setHighlighted(false);
        }
        for (int r = 0; r < 5; r++) {
            for (int c = 0; c < 6; c++) {
                boolean occ = false;
                for (Vehicle3D v : activeVehicles.values()) {
                    if (v.getState() == Vehicle3D.State.PARKED && v.getAssignedRow() == r && v.getAssignedCol() == c) {
                        occ = true;
                        break;
                    }
                }
                lot3DScene.setSpotStatus(r, c, occ, false);
            }
        }

        // ALGORITHM: Binary Search O(log N)
        ParkingManager.ParkingRecord rec = manager.searchVehicle(searchId);

        if (rec != null) {
            Vehicle3D target = activeVehicles.get(rec.getVehicleId());
            if (target != null) {
                target.setHighlighted(true);
            }
            lot3DScene.setSpotStatus(rec.getRow(), rec.getCol(), true, true);
            statusMsgLbl.setText("✓ Binary Search: Found " + rec.getCategory() + " [" + rec.getVehicleId() + "] at Space " + rec.getSpotId() + " (Spot Highlighted in Grey)!");
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
        if (topStartSimBtn != null) {
            topStartSimBtn.setText("⏸ Pause");
            topStartSimBtn.setStyle("-fx-background-color: #D97706; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 11px; -fx-cursor: hand; -fx-background-radius: 4;");
        }
        pauseSimBtn.setDisable(false);
        pauseSimBtn.setText("⏸ Pause");
        statusMsgLbl.setText("▶ Auto simulation running. Spawning arriving vehicles...");
        statusMsgLbl.setStyle("-fx-text-fill: #38BDF8; -fx-font-weight: bold;");
    }

    private void pauseAutoSimulation() {
        if (!autoSimPaused) {
            autoSimPaused = true;
            if (topStartSimBtn != null) {
                topStartSimBtn.setText("▶ Resume");
                topStartSimBtn.setStyle("-fx-background-color: #2563EB; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 11px; -fx-cursor: hand; -fx-background-radius: 4;");
            }
            pauseSimBtn.setText("▶ Resume");
            statusMsgLbl.setText("⏸ Simulation paused.");
        } else {
            autoSimPaused = false;
            if (topStartSimBtn != null) {
                topStartSimBtn.setText("⏸ Pause");
                topStartSimBtn.setStyle("-fx-background-color: #D97706; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 11px; -fx-cursor: hand; -fx-background-radius: 4;");
            }
            pauseSimBtn.setText("⏸ Pause");
            statusMsgLbl.setText("▶ Simulation resumed.");
        }
    }

    private void resetSimulation() {
        autoSimRunning = false;
        autoSimPaused = false;
        startSimBtn.setDisable(false);
        if (topStartSimBtn != null) {
            topStartSimBtn.setText("▶ Start Auto Sim");
            topStartSimBtn.setStyle("-fx-background-color: #2563EB; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 11px; -fx-cursor: hand; -fx-background-radius: 4;");
        }
        pauseSimBtn.setDisable(true);
        pauseSimBtn.setText("⏸ Pause");

        pendingSpawnQueue.clear();

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
