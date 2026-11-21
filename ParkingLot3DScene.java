import javafx.geometry.VPos;
import javafx.scene.AmbientLight;
import javafx.scene.Group;
import javafx.scene.PerspectiveCamera;
import javafx.scene.PointLight;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;
import javafx.scene.shape.Cylinder;
import javafx.scene.shape.Sphere;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Translate;

import java.util.HashMap;
import java.util.Map;

/**
 * ParkingLot3DScene builds the realistic 3D parking lot environment:
 * - Asphalt road surfaces with lane markings and directional arrows.
 * - 30 parking stalls in 3 color-coded zones (Bikes: Cyan, Cars: Green, Trucks: Amber).
 * - Real-time spot status indicator LED bollards (Green = Empty, Red = Occupied).
 * - Entrance and Exit gantries with animated boom barriers.
 * - Modern 3D lampposts with warm point lights.
 * - 3D PerspectiveCamera with interactive orbit/zoom controls.
 */
public class ParkingLot3DScene {

    private final Group root3D = new Group();
    private final PerspectiveCamera camera = new PerspectiveCamera(true);

    // Camera Rig Transforms for Orbit and Zoom
    private final Rotate cameraRotateX = new Rotate(-46, Rotate.X_AXIS);
    private final Rotate cameraRotateY = new Rotate(-26, Rotate.Y_AXIS);
    private final Translate cameraTranslate = new Translate(430, 0, 240); // Pivot around lot center
    private final Translate cameraDistance = new Translate(0, 0, -1250);

    // Entrance and Exit boom barrier arms
    private Cylinder entranceBarrierArm;
    private Cylinder exitBarrierArm;
    private final Rotate entranceBarrierRotate = new Rotate(0, 0, 0, 0, Rotate.Z_AXIS);
    private final Rotate exitBarrierRotate = new Rotate(0, 0, 0, 0, Rotate.Z_AXIS);

    // Spot status LED lights: key = "r_c"
    private final Map<String, Sphere> spotLedMap = new HashMap<>();
    private final Map<String, PhongMaterial> spotLedMatMap = new HashMap<>();

    // Materials
    private final PhongMaterial matLedGreen = new PhongMaterial(Color.rgb(34, 197, 94));
    private final PhongMaterial matLedRed = new PhongMaterial(Color.rgb(239, 68, 68));
    private final PhongMaterial matLedYellow = new PhongMaterial(Color.rgb(250, 204, 21));

    public ParkingLot3DScene() {
        setupMaterials();
        setupCamera();
        setupLighting();
        buildGroundAndRoads();
        buildParkingBays();
        buildGantries();
        buildStreetLamps();
    }

    public Group getRoot3D() {
        return root3D;
    }

    public PerspectiveCamera getCamera() {
        return camera;
    }

    private void setupMaterials() {
        matLedGreen.setSpecularColor(Color.WHITE);
        matLedRed.setSpecularColor(Color.WHITE);
        matLedYellow.setSpecularColor(Color.WHITE);
    }

    private void setupCamera() {
        camera.setNearClip(1.0);
        camera.setFarClip(10000.0);
        camera.setFieldOfView(42.0);

        Group cameraRig = new Group();
        cameraRig.getTransforms().addAll(
                cameraTranslate,
                cameraRotateY,
                cameraRotateX,
                cameraDistance
        );
        cameraRig.getChildren().add(camera);
        root3D.getChildren().add(cameraRig);
    }

    private void setupLighting() {
        // Soft ambient fill light
        AmbientLight ambient = new AmbientLight(Color.rgb(180, 190, 205));
        root3D.getChildren().add(ambient);

        // Angled overhead sunlight
        PointLight sunLight = new PointLight(Color.rgb(255, 250, 240));
        sunLight.setTranslateX(400);
        sunLight.setTranslateY(-900);
        sunLight.setTranslateZ(-400);
        root3D.getChildren().add(sunLight);
    }

    /**
     * Builds asphalt lot, perimeter grass, roads, curbs, and road markings.
     */
    private void buildGroundAndRoads() {
        // 1. Lush Green Grass Lawn (W: 1900, D: 1400)
        Box grass = new Box(1900, 2, 1400);
        PhongMaterial grassMat = new PhongMaterial(Color.rgb(46, 125, 50));
        grass.setMaterial(grassMat);
        grass.setTranslateX(430);
        grass.setTranslateY(2);
        grass.setTranslateZ(250);
        root3D.getChildren().add(grass);

        // 2. Dark Asphalt Parking Surface (W: 1450, D: 950)
        Box asphalt = new Box(1450, 2, 950);
        PhongMaterial asphaltMat = new PhongMaterial(Color.rgb(38, 44, 52));
        asphaltMat.setSpecularColor(Color.rgb(70, 75, 85));
        asphalt.setMaterial(asphaltMat);
        asphalt.setTranslateX(430);
        asphalt.setTranslateY(0);
        asphalt.setTranslateZ(250);
        root3D.getChildren().add(asphalt);

        // 3. Concrete Curbs around boundary
        buildCurbs();

        // 4. Yellow Centerlines on North Road (Z = -80) & South Road (Z = 580)
        buildDashedLine(-100, 920, -80, true);
        buildDashedLine(80, 920, 580, true);

        // 5. White Directional Driving Arrows down the aisles
        for (int a = 0; a < 3; a++) {
            double aisleX = PathPlanner.AISLE_X[a];
            buildAisleMarkings(aisleX);
        }
    }

    private void buildCurbs() {
        PhongMaterial curbMat = new PhongMaterial(Color.rgb(180, 185, 195));
        // North & South curbs
        Box curbN = new Box(1460, 4, 8);
        curbN.setMaterial(curbMat);
        curbN.setTranslateX(430);
        curbN.setTranslateY(-2);
        curbN.setTranslateZ(-225);

        Box curbS = new Box(1460, 4, 8);
        curbS.setMaterial(curbMat);
        curbS.setTranslateX(430);
        curbS.setTranslateY(-2);
        curbS.setTranslateZ(725);

        // West & East curbs
        Box curbW = new Box(8, 4, 958);
        curbW.setMaterial(curbMat);
        curbW.setTranslateX(-295);
        curbW.setTranslateY(-2);
        curbW.setTranslateZ(250);

        Box curbE = new Box(8, 4, 958);
        curbE.setMaterial(curbMat);
        curbE.setTranslateX(1155);
        curbE.setTranslateY(-2);
        curbE.setTranslateZ(250);

        root3D.getChildren().addAll(curbN, curbS, curbW, curbE);
    }

    private void buildDashedLine(double startX, double endX, double z, boolean horizontal) {
        PhongMaterial yellowMat = new PhongMaterial(Color.rgb(245, 197, 24));
        double dashLen = 22;
        double gapLen = 14;
        double currX = startX;

        while (currX < endX) {
            Box dash = new Box(dashLen, 0.4, 2.5);
            dash.setMaterial(yellowMat);
            dash.setTranslateX(currX + dashLen / 2);
            dash.setTranslateY(-1.2);
            dash.setTranslateZ(z);
            root3D.getChildren().add(dash);
            currX += (dashLen + gapLen);
        }
    }

    private void buildAisleMarkings(double x) {
        PhongMaterial whiteMat = new PhongMaterial(Color.rgb(230, 235, 245));

        // Dashed lines between aisle and rows
        for (double z = 20; z <= 500; z += 35) {
            Box arrowStem = new Box(2.5, 0.3, 16);
            arrowStem.setMaterial(whiteMat);
            arrowStem.setTranslateX(x);
            arrowStem.setTranslateY(-1.2);
            arrowStem.setTranslateZ(z);
            root3D.getChildren().add(arrowStem);
        }
    }

    /**
     * Builds the 30 parking bays organized in 3 color-coded zones:
     * - Zone 1 (Bikes, Cols 0-1): Cyan
     * - Zone 2 (Cars, Cols 2-3): Green
     * - Zone 3 (Trucks, Cols 4-5): Amber
     */
    private void buildParkingBays() {
        for (int r = 0; r < 5; r++) {
            double z = PathPlanner.ROW_Z[r];

            for (int c = 0; c < 6; c++) {
                double x = PathPlanner.COL_X[c];

                int zoneIndex = c / 2;
                Color zoneColor;
                String spotId;
                double stallW;
                double stallD;

                if (zoneIndex == 0) {
                    zoneColor = Color.rgb(14, 165, 233); // Cyan
                    int spotNum = (r * 2) + c + 1;
                    spotId = "B" + spotNum;
                    stallW = 54;
                    stallD = 76;
                } else if (zoneIndex == 1) {
                    zoneColor = Color.rgb(34, 197, 94); // Green
                    int spotNum = (r * 2) + (c - 2) + 1;
                    spotId = "C" + spotNum;
                    stallW = 72;
                    stallD = 86;
                } else {
                    zoneColor = Color.rgb(245, 158, 11); // Amber
                    int spotNum = (r * 2) + (c - 4) + 1;
                    spotId = "T" + spotNum;
                    stallW = 86;
                    stallD = 94;
                }

                buildSingleParkingBay(r, c, x, z, stallW, stallD, zoneColor, spotId);
            }
        }
    }

    private void buildSingleParkingBay(int row, int col, double x, double z,
                                      double w, double d, Color zoneColor, String spotId) {
        // 1. Bay ground tint
        Box pad = new Box(w, 0.3, d);
        PhongMaterial padMat = new PhongMaterial(Color.color(zoneColor.getRed(), zoneColor.getGreen(), zoneColor.getBlue(), 0.15));
        pad.setMaterial(padMat);
        pad.setTranslateX(x);
        pad.setTranslateY(-1.1);
        pad.setTranslateZ(z);

        // 2. White Stall Boundary Lines (North, South, and back wall)
        PhongMaterial lineMat = new PhongMaterial(Color.rgb(240, 245, 255));
        lineMat.setSpecularColor(Color.WHITE);

        // Top line
        Box lineN = new Box(w, 0.5, 2.5);
        lineN.setMaterial(lineMat);
        lineN.setTranslateX(x);
        lineN.setTranslateY(-1.3);
        lineN.setTranslateZ(z - d / 2);

        // Bottom line
        Box lineS = new Box(w, 0.5, 2.5);
        lineS.setMaterial(lineMat);
        lineS.setTranslateX(x);
        lineS.setTranslateY(-1.3);
        lineS.setTranslateZ(z + d / 2);

        // Back wall line (outer side)
        double backX = (col % 2 == 0) ? (x - w / 2) : (x + w / 2);
        Box lineBack = new Box(2.5, 0.5, d);
        lineBack.setMaterial(lineMat);
        lineBack.setTranslateX(backX);
        lineBack.setTranslateY(-1.3);
        lineBack.setTranslateZ(z);

        // 3. Concrete Wheel Stop
        PhongMaterial wheelStopMat = new PhongMaterial(Color.rgb(200, 205, 215));
        Box wheelStop = new Box(w * 0.7, 3.5, 5);
        wheelStop.setMaterial(wheelStopMat);
        double stopX = (col % 2 == 0) ? (x - w * 0.35) : (x + w * 0.35);
        wheelStop.setTranslateX(stopX);
        wheelStop.setTranslateY(-2.5);
        wheelStop.setTranslateZ(z);

        // 4. Spot ID Ground Text / Badge
        Group idBadge = createSpotTextBadge(spotId, zoneColor);
        idBadge.setTranslateX(x);
        idBadge.setTranslateY(-1.4);
        idBadge.setTranslateZ(z);

        // 5. Real-Time Status LED Bollard
        double bollardX = (col % 2 == 0) ? (x - w / 2 - 3) : (x + w / 2 + 3);
        Cylinder bollardPost = new Cylinder(1.5, 12);
        bollardPost.setMaterial(new PhongMaterial(Color.rgb(60, 65, 75)));
        bollardPost.setTranslateX(bollardX);
        bollardPost.setTranslateY(-6);
        bollardPost.setTranslateZ(z);

        Sphere led = new Sphere(3.0);
        PhongMaterial ledMat = new PhongMaterial(Color.rgb(34, 197, 94)); // default green (empty)
        ledMat.setSpecularColor(Color.WHITE);
        led.setMaterial(ledMat);
        led.setTranslateX(bollardX);
        led.setTranslateY(-14);
        led.setTranslateZ(z);

        String key = row + "_" + col;
        spotLedMap.put(key, led);
        spotLedMatMap.put(key, ledMat);

        root3D.getChildren().addAll(pad, lineN, lineS, lineBack, wheelStop, idBadge, bollardPost, led);
    }

    private Group createSpotTextBadge(String spotId, Color zoneColor) {
        Group badge = new Group();

        Box plate = new Box(28, 0.2, 16);
        PhongMaterial plateMat = new PhongMaterial(Color.color(zoneColor.getRed() * 0.2, zoneColor.getGreen() * 0.2, zoneColor.getBlue() * 0.2, 0.9));
        plate.setMaterial(plateMat);

        Text text = new Text(spotId);
        text.setFont(Font.font("SansSerif", FontWeight.BOLD, 10));
        text.setFill(zoneColor);
        text.setTextAlignment(TextAlignment.CENTER);
        text.setTextOrigin(VPos.CENTER);
        text.setTranslateX(-9);

        badge.getChildren().addAll(plate, text);
        badge.getTransforms().add(new Rotate(-90, Rotate.X_AXIS)); // lay flat on ground
        return badge;
    }

    /**
     * Builds Entrance and Exit gantries with signs and boom barriers.
     */
    private void buildGantries() {
        PhongMaterial steelMat = new PhongMaterial(Color.rgb(90, 100, 115));
        PhongMaterial signGreenMat = new PhongMaterial(Color.rgb(16, 185, 129));
        PhongMaterial signRedMat = new PhongMaterial(Color.rgb(239, 68, 68));
        PhongMaterial barrierMat = new PhongMaterial(Color.rgb(255, 235, 59));

        // 1. Entrance Gantry (at X = -80, Z = -80)
        buildSingleGantry(-80, -80, "ENTRANCE GATE (0, 0)", steelMat, signGreenMat);

        // Entrance Boom Barrier
        Cylinder postIn = new Cylinder(2.5, 14);
        postIn.setMaterial(steelMat);
        postIn.setTranslateX(-80);
        postIn.setTranslateY(-7);
        postIn.setTranslateZ(-65);

        entranceBarrierArm = new Cylinder(1.4, 38);
        entranceBarrierArm.setMaterial(barrierMat);
        entranceBarrierArm.getTransforms().addAll(
                new Rotate(90, Rotate.X_AXIS),
                entranceBarrierRotate
        );
        entranceBarrierArm.setTranslateX(-80);
        entranceBarrierArm.setTranslateY(-12);
        entranceBarrierArm.setTranslateZ(-80);

        // 2. Exit Gantry (at X = 980, Z = 580)
        buildSingleGantry(980, 580, "EXIT GATE", steelMat, signRedMat);

        // Exit Boom Barrier
        Cylinder postOut = new Cylinder(2.5, 14);
        postOut.setMaterial(steelMat);
        postOut.setTranslateX(980);
        postOut.setTranslateY(-7);
        postOut.setTranslateZ(565);

        exitBarrierArm = new Cylinder(1.4, 38);
        exitBarrierArm.setMaterial(barrierMat);
        exitBarrierArm.getTransforms().addAll(
                new Rotate(90, Rotate.X_AXIS),
                exitBarrierRotate
        );
        exitBarrierArm.setTranslateX(980);
        exitBarrierArm.setTranslateY(-12);
        exitBarrierArm.setTranslateZ(580);

        root3D.getChildren().addAll(postIn, entranceBarrierArm, postOut, exitBarrierArm);
    }

    private void buildSingleGantry(double x, double z, String label, PhongMaterial postMat, PhongMaterial signMat) {
        // Vertical Posts
        Cylinder p1 = new Cylinder(3.5, 48);
        p1.setMaterial(postMat);
        p1.setTranslateX(x);
        p1.setTranslateY(-24);
        p1.setTranslateZ(z - 28);

        Cylinder p2 = new Cylinder(3.5, 48);
        p2.setMaterial(postMat);
        p2.setTranslateX(x);
        p2.setTranslateY(-24);
        p2.setTranslateZ(z + 28);

        // Crossbeam
        Box beam = new Box(6, 6, 62);
        beam.setMaterial(postMat);
        beam.setTranslateX(x);
        beam.setTranslateY(-48);
        beam.setTranslateZ(z);

        // Overhead Glowing Signboard
        Box sign = new Box(4, 14, 52);
        sign.setMaterial(signMat);
        sign.setTranslateX(x);
        sign.setTranslateY(-48);
        sign.setTranslateZ(z);

        Text signText = new Text(label);
        signText.setFont(Font.font("SansSerif", FontWeight.BOLD, 7));
        signText.setFill(Color.WHITE);
        signText.setTextOrigin(VPos.CENTER);
        signText.setTranslateX(x - 2.5);
        signText.setTranslateY(-48);
        signText.setTranslateZ(z - 20);
        signText.getTransforms().add(new Rotate(90, Rotate.Y_AXIS));

        root3D.getChildren().addAll(p1, p2, beam, sign, signText);
    }

    private void buildStreetLamps() {
        PhongMaterial poleMat = new PhongMaterial(Color.rgb(80, 85, 95));
        PhongMaterial bulbMat = new PhongMaterial(Color.rgb(255, 255, 210));
        bulbMat.setSpecularColor(Color.WHITE);

        // Place lamps along aisles
        for (int a = 0; a < 3; a++) {
            double ax = PathPlanner.AISLE_X[a] + 45;
            for (double az : new double[]{ 100, 320 }) {
                Cylinder pole = new Cylinder(2, 60);
                pole.setMaterial(poleMat);
                pole.setTranslateX(ax);
                pole.setTranslateY(-30);
                pole.setTranslateZ(az);

                Sphere bulb = new Sphere(4);
                bulb.setMaterial(bulbMat);
                bulb.setTranslateX(ax);
                bulb.setTranslateY(-62);
                bulb.setTranslateZ(az);

                PointLight lampLight = new PointLight(Color.rgb(255, 240, 200));
                lampLight.setTranslateX(ax);
                lampLight.setTranslateY(-58);
                lampLight.setTranslateZ(az);

                root3D.getChildren().addAll(pole, bulb, lampLight);
            }
        }
    }

    /**
     * Updates the status LED for a spot:
     * - "EMPTY": Green
     * - Occupied: Red
     * - Highlighted: Glowing Yellow
     */
    public void setSpotStatus(int row, int col, boolean occupied, boolean highlighted) {
        String key = row + "_" + col;
        PhongMaterial mat = spotLedMatMap.get(key);
        if (mat == null) return;

        if (highlighted) {
            mat.setDiffuseColor(Color.rgb(250, 204, 21));
        } else if (occupied) {
            mat.setDiffuseColor(Color.rgb(239, 68, 68));
        } else {
            mat.setDiffuseColor(Color.rgb(34, 197, 94));
        }
    }

    /**
     * Barrier animations
     */
    public void setEntranceBarrierOpen(boolean open) {
        entranceBarrierRotate.setAngle(open ? -70 : 0);
    }

    public void setExitBarrierOpen(boolean open) {
        exitBarrierRotate.setAngle(open ? 70 : 0);
    }

    // Camera preset methods
    public void setCameraIsometric() {
        cameraRotateX.setAngle(-46);
        cameraRotateY.setAngle(-26);
        cameraTranslate.setX(430);
        cameraTranslate.setY(0);
        cameraTranslate.setZ(240);
        cameraDistance.setZ(-1250);
    }

    public void setCameraTopDown() {
        cameraRotateX.setAngle(-88);
        cameraRotateY.setAngle(0);
        cameraTranslate.setX(430);
        cameraTranslate.setY(0);
        cameraTranslate.setZ(240);
        cameraDistance.setZ(-1100);
    }

    public void setCameraEntrance() {
        cameraRotateX.setAngle(-22);
        cameraRotateY.setAngle(-45);
        cameraTranslate.setX(80);
        cameraTranslate.setY(0);
        cameraTranslate.setZ(20);
        cameraDistance.setZ(-600);
    }

    // Camera Orbit & Pan helpers
    public void rotateCamera(double deltaX, double deltaY) {
        cameraRotateY.setAngle(cameraRotateY.getAngle() + deltaX * 0.4);
        double newPitch = cameraRotateX.getAngle() - deltaY * 0.4;
        // Clamp pitch to prevent flipping upside down
        newPitch = Math.max(-88, Math.min(-10, newPitch));
        cameraRotateX.setAngle(newPitch);
    }

    public void panCamera(double deltaX, double deltaY) {
        cameraTranslate.setX(cameraTranslate.getX() - deltaX * 0.8);
        cameraTranslate.setZ(cameraTranslate.getZ() - deltaY * 0.8);
    }

    public void zoomCamera(double deltaZoom) {
        double currentZ = cameraDistance.getZ();
        double newZ = currentZ + deltaZoom * 1.5;
        // Clamp zoom distance between -350 and -2500
        newZ = Math.max(-2500, Math.min(-350, newZ));
        cameraDistance.setZ(newZ);
    }
}
