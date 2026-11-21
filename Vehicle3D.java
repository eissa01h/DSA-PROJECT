import javafx.geometry.VPos;
import javafx.scene.Group;
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

import java.util.List;

/**
 * Vehicle3D creates realistic 3D representations of Cars, Bikes, and Trucks
 * using JavaFX 3D primitives (Box, Cylinder, PhongMaterial) with composite
 * parts (wheels, cabins, roofs, windows, lights, and floating ID labels).
 */
public class Vehicle3D extends Group {

    public enum State {
        SPAWNING,
        DRIVING_TO_SPOT,
        PARKED,
        EXITING,
        FINISHED
    }

    private final String vehicleId;
    private final String category;
    private final Color primaryColor;

    private State state = State.SPAWNING;
    private List<PathPlanner.Waypoint> path;
    private int currentTargetIndex = 0;

    private double currentX = 0;
    private double currentY = 0;
    private double currentZ = 0;
    private double currentYaw = 0; // degrees around Y axis

    private double speed = 120.0; // units per second

    private final Rotate rotateY = new Rotate(0, Rotate.Y_AXIS);
    private final Translate translate = new Translate();

    private Box highlightAura;
    private boolean highlighted = false;

    // Assigned parking spot
    private int assignedRow = -1;
    private int assignedCol = -1;
    private String spotId = "";

    // Cycles through the 3 custom car models (Batmobile, Green Muscle Car, Dino-Driven Convertible)
    private static int carModelCycle = 0;

    public Vehicle3D(String category, String vehicleId, Color primaryColor) {
        this.category = category;
        this.vehicleId = vehicleId;
        this.primaryColor = primaryColor;

        getTransforms().addAll(translate, rotateY);

        buildModel();
        buildIdBadge();
        buildHighlightAura();
    }

    public String getVehicleId() {
        return vehicleId;
    }

    public String getCategory() {
        return category;
    }

    public State getState() {
        return state;
    }

    public void setState(State state) {
        this.state = state;
    }

    public void setAssignedSpot(int row, int col, String spotId) {
        this.assignedRow = row;
        this.assignedCol = col;
        this.spotId = spotId;
    }

    public int getAssignedRow() {
        return assignedRow;
    }

    public int getAssignedCol() {
        return assignedCol;
    }

    public String getSpotId() {
        return spotId;
    }

    public void setPath(List<PathPlanner.Waypoint> newPath) {
        this.path = newPath;
        if (newPath != null && !newPath.isEmpty()) {
            PathPlanner.Waypoint first = newPath.get(0);
            setPosition(first.x, first.y, first.z);
            this.currentTargetIndex = (newPath.size() > 1) ? 1 : 0;
        } else {
            this.currentTargetIndex = 0;
        }
    }

    public void setSpeed(double speed) {
        this.speed = speed;
    }

    public void setPosition(double x, double y, double z) {
        this.currentX = x;
        this.currentY = y;
        this.currentZ = z;
        translate.setX(x);
        translate.setY(y);
        translate.setZ(z);
    }

    public void setYaw(double degrees) {
        this.currentYaw = normalizeAngle(degrees);
        rotateY.setAngle(this.currentYaw);
    }

    public double getX() { return currentX; }
    public double getY() { return currentY; }
    public double getZ() { return currentZ; }
    public double getYaw() { return currentYaw; }
    public int getCurrentTargetIndex() { return currentTargetIndex; }

    /**
     * Calculates total remaining distance along BFS path to destination.
     * Used for intersection right-of-way arbitration.
     */
    public double getRemainingDistance() {
        if (path == null || currentTargetIndex >= path.size()) return 0;
        double rem = Math.hypot(path.get(currentTargetIndex).x - currentX, path.get(currentTargetIndex).z - currentZ);
        for (int k = currentTargetIndex; k < path.size() - 1; k++) {
            rem += Math.hypot(path.get(k + 1).x - path.get(k).x, path.get(k + 1).z - path.get(k).z);
        }
        return rem;
    }

    public void setHighlighted(boolean h) {
        this.highlighted = h;
        if (highlightAura != null) {
            highlightAura.setVisible(h);
        }
    }

    public boolean isHighlighted() {
        return highlighted;
    }

    /**
     * Builds composite 3D models for Car, Bike, or Truck using JavaFX primitives.
     */
    private void buildModel() {
        if (category.equalsIgnoreCase("Bike")) {
            buildBikeModel();
        } else if (category.equalsIgnoreCase("Truck")) {
            buildTruckModel();
        } else {
            // Cycle through the 3 requested car models:
            // 0 -> 1989 Batmobile
            // 1 -> Green Muscle Car with dual black racing stripes & hood scoop
            // 2 -> Red Open-Roof Convertible driven by Velociraptor Dinosaur
            int modelChoice = (carModelCycle++) % 3;
            if (modelChoice == 0) {
                buildBatmobileModel();
            } else if (modelChoice == 1) {
                buildGreenMuscleCarModel();
            } else {
                buildDinoConvertibleModel();
            }
        }
    }

    /**
     * 1. 1989 Batmobile:
     * - Stealth matte black sculpted fuselage with low-slung stance.
     * - Iconic front turbine jet intake with central nose cone and side cowl wings.
     * - Sculpted front fender wheel arches and elongated hood with center trough.
     * - Rounded wraparound smoked-canopy cockpit with roof air intake scoop.
     * - Sweeping scalloped bat-wing tailfins rising over the rear fenders.
     * - Rear center afterburner jet thruster with orange burner flame core.
     * - Waist flank mechanical actuators & wide low-profile dark wheels.
     */
    private void buildBatmobileModel() {
        PhongMaterial batBlackMat = new PhongMaterial(Color.rgb(18, 18, 22));
        batBlackMat.setSpecularColor(Color.rgb(60, 65, 75));
        batBlackMat.setSpecularPower(15.0);

        PhongMaterial batCharcoalMat = new PhongMaterial(Color.rgb(32, 34, 40));
        batCharcoalMat.setSpecularColor(Color.rgb(80, 85, 95));

        PhongMaterial canopyGlassMat = new PhongMaterial(Color.rgb(30, 36, 44));
        canopyGlassMat.setSpecularColor(Color.rgb(150, 165, 185));

        PhongMaterial jetFlameMat = new PhongMaterial(Color.rgb(255, 120, 20));
        jetFlameMat.setSpecularColor(Color.YELLOW);

        PhongMaterial tireMat = new PhongMaterial(Color.rgb(22, 22, 24));
        PhongMaterial chromeDetailMat = new PhongMaterial(Color.rgb(190, 195, 205));

        // 1. Lower Fuselage
        Box fuselage = new Box(22, 6.5, 48);
        fuselage.setMaterial(batBlackMat);
        fuselage.setTranslateY(-7.5);
        fuselage.setTranslateZ(-1.0);
        getChildren().add(fuselage);

        // 2. Front Hood & Fender Arches
        Box centerTrough = new Box(12, 4.8, 22);
        centerTrough.setMaterial(batBlackMat);
        centerTrough.setTranslateY(-8.0);
        centerTrough.setTranslateZ(13.0);

        Box fenderArchL = new Box(5.2, 6.2, 24);
        fenderArchL.setMaterial(batBlackMat);
        fenderArchL.setTranslateX(-8.8);
        fenderArchL.setTranslateY(-8.6);
        fenderArchL.setTranslateZ(13.0);

        Box fenderArchR = new Box(5.2, 6.2, 24);
        fenderArchR.setMaterial(batBlackMat);
        fenderArchR.setTranslateX(8.8);
        fenderArchR.setTranslateY(-8.6);
        fenderArchR.setTranslateZ(13.0);

        getChildren().addAll(centerTrough, fenderArchL, fenderArchR);

        // 3. Front Jet Turbine Intake
        Cylinder turbineCowl = new Cylinder(4.2, 4.0);
        turbineCowl.setMaterial(batCharcoalMat);
        turbineCowl.getTransforms().add(new Rotate(90, Rotate.X_AXIS));
        turbineCowl.setTranslateX(0);
        turbineCowl.setTranslateY(-7.5);
        turbineCowl.setTranslateZ(25.5);

        Cylinder turbineCone = new Cylinder(1.6, 5.0);
        turbineCone.setMaterial(batBlackMat);
        turbineCone.getTransforms().add(new Rotate(90, Rotate.X_AXIS));
        turbineCone.setTranslateX(0);
        turbineCone.setTranslateY(-7.5);
        turbineCone.setTranslateZ(26.2);

        Box frontBumperWings = new Box(26, 3.2, 3.5);
        frontBumperWings.setMaterial(batBlackMat);
        frontBumperWings.setTranslateY(-4.2);
        frontBumperWings.setTranslateZ(24.5);

        getChildren().addAll(turbineCowl, turbineCone, frontBumperWings);

        // 4. Rounded Cockpit Canopy
        Box canopyRoof = new Box(17, 6.5, 17);
        canopyRoof.setMaterial(batBlackMat);
        canopyRoof.setTranslateY(-14.0);
        canopyRoof.setTranslateZ(-3.0);

        Box canopyGlass = new Box(15.5, 5.2, 15.0);
        canopyGlass.setMaterial(canopyGlassMat);
        canopyGlass.setTranslateY(-14.4);
        canopyGlass.setTranslateZ(-2.5);

        Box roofScoop = new Box(4.2, 2.0, 5.5);
        roofScoop.setMaterial(batBlackMat);
        roofScoop.setTranslateY(-17.8);
        roofScoop.setTranslateZ(-4.0);

        getChildren().addAll(canopyRoof, canopyGlass, roofScoop);

        // 5. Iconic Sweeping Bat-Wing Tailfins (Left & Right)
        Box finBladeL = new Box(1.6, 11.0, 18.0);
        finBladeL.setMaterial(batBlackMat);
        finBladeL.setTranslateX(-10.6);
        finBladeL.setTranslateY(-13.5);
        finBladeL.setTranslateZ(-16.0);

        Box finTipL = new Box(1.6, 5.0, 6.0);
        finTipL.setMaterial(batBlackMat);
        finTipL.setTranslateX(-10.6);
        finTipL.setTranslateY(-18.0);
        finTipL.setTranslateZ(-21.0);

        Box finBladeR = new Box(1.6, 11.0, 18.0);
        finBladeR.setMaterial(batBlackMat);
        finBladeR.setTranslateX(10.6);
        finBladeR.setTranslateY(-13.5);
        finBladeR.setTranslateZ(-16.0);

        Box finTipR = new Box(1.6, 5.0, 6.0);
        finTipR.setMaterial(batBlackMat);
        finTipR.setTranslateX(10.6);
        finTipR.setTranslateY(-18.0);
        finTipR.setTranslateZ(-21.0);

        getChildren().addAll(finBladeL, finTipL, finBladeR, finTipR);

        // 6. Rear Jet Thruster / Afterburner
        Cylinder jetNozzle = new Cylinder(3.2, 4.0);
        jetNozzle.setMaterial(batCharcoalMat);
        jetNozzle.getTransforms().add(new Rotate(90, Rotate.X_AXIS));
        jetNozzle.setTranslateX(0);
        jetNozzle.setTranslateY(-7.5);
        jetNozzle.setTranslateZ(-25.5);

        Cylinder jetCore = new Cylinder(2.0, 4.2);
        jetCore.setMaterial(jetFlameMat);
        jetCore.getTransforms().add(new Rotate(90, Rotate.X_AXIS));
        jetCore.setTranslateX(0);
        jetCore.setTranslateY(-7.5);
        jetCore.setTranslateZ(-25.8);

        getChildren().addAll(jetNozzle, jetCore);

        // 7. Waist Flank Mechanical Accents
        Box flankL = new Box(2.0, 1.8, 3.5);
        flankL.setMaterial(chromeDetailMat);
        flankL.setTranslateX(-12.2);
        flankL.setTranslateY(-7.5);
        flankL.setTranslateZ(-1.0);

        Box flankR = new Box(2.0, 1.8, 3.5);
        flankR.setMaterial(chromeDetailMat);
        flankR.setTranslateX(12.2);
        flankR.setTranslateY(-7.5);
        flankR.setTranslateZ(-1.0);

        getChildren().addAll(flankL, flankR);

        // 8. 4 Wide Low-Profile Batmobile Wheels
        addWheel(-12.5, -4.8, 14, 5.0, 3.8, tireMat, batCharcoalMat); // Front-Left
        addWheel(12.5, -4.8, 14, 5.0, 3.8, tireMat, batCharcoalMat);  // Front-Right
        addWheel(-12.5, -5.2, -15, 5.4, 4.2, tireMat, batCharcoalMat); // Rear-Left
        addWheel(12.5, -5.2, -15, 5.4, 4.2, tireMat, batCharcoalMat);  // Rear-Right
    }

    /**
     * 2. Green Muscle Car:
     * - Emerald green body with dual bold matte black racing stripes running the length.
     * - Pronounced rectangular hood scoop with front air induction slot.
     * - Classic muscle car coupe fastback roofline and tinted glass.
     * - Aggressive black split grille with quad round headlights (2 per side).
     * - Chrome front/rear bumpers, dual exhaust tips, and sport dish rims.
     */
    private void buildGreenMuscleCarModel() {
        PhongMaterial greenMat = new PhongMaterial(Color.rgb(32, 175, 52));
        greenMat.setSpecularColor(Color.rgb(180, 255, 180));
        greenMat.setSpecularPower(35.0);

        PhongMaterial blackStripeMat = new PhongMaterial(Color.rgb(20, 20, 20));
        PhongMaterial glassMat = new PhongMaterial(Color.rgb(195, 225, 240));
        glassMat.setSpecularColor(Color.WHITE);

        PhongMaterial chromeMat = new PhongMaterial(Color.rgb(220, 225, 230));
        chromeMat.setSpecularColor(Color.WHITE);

        PhongMaterial darkGrilleMat = new PhongMaterial(Color.rgb(32, 34, 38));
        PhongMaterial tireMat = new PhongMaterial(Color.rgb(24, 25, 28));
        PhongMaterial headlightMat = new PhongMaterial(Color.rgb(255, 255, 220));

        // 1. Lower Body & Hood Deck
        Box chassis = new Box(25, 7.5, 48);
        chassis.setMaterial(greenMat);
        chassis.setTranslateY(-8.0);
        getChildren().add(chassis);

        Box hoodDeck = new Box(23, 2.5, 22);
        hoodDeck.setMaterial(greenMat);
        hoodDeck.setTranslateY(-12.0);
        hoodDeck.setTranslateZ(12.0);
        getChildren().add(hoodDeck);

        // 2. Twin Matte Black Racing Stripes (Hood, Roof, and Trunk)
        // Hood Stripes
        Box stripeHoodL = new Box(3.8, 0.4, 23.0);
        stripeHoodL.setMaterial(blackStripeMat);
        stripeHoodL.setTranslateX(-4.5);
        stripeHoodL.setTranslateY(-13.4);
        stripeHoodL.setTranslateZ(12.5);

        Box stripeHoodR = new Box(3.8, 0.4, 23.0);
        stripeHoodR.setMaterial(blackStripeMat);
        stripeHoodR.setTranslateX(4.5);
        stripeHoodR.setTranslateY(-13.4);
        stripeHoodR.setTranslateZ(12.5);

        // Roof Stripes
        Box stripeRoofL = new Box(3.8, 0.4, 22.0);
        stripeRoofL.setMaterial(blackStripeMat);
        stripeRoofL.setTranslateX(-4.5);
        stripeRoofL.setTranslateY(-20.5);
        stripeRoofL.setTranslateZ(-3.5);

        Box stripeRoofR = new Box(3.8, 0.4, 22.0);
        stripeRoofR.setMaterial(blackStripeMat);
        stripeRoofR.setTranslateX(4.5);
        stripeRoofR.setTranslateY(-20.5);
        stripeRoofR.setTranslateZ(-3.5);

        // Trunk Stripes
        Box stripeTrunkL = new Box(3.8, 0.4, 11.0);
        stripeTrunkL.setMaterial(blackStripeMat);
        stripeTrunkL.setTranslateX(-4.5);
        stripeTrunkL.setTranslateY(-12.1);
        stripeTrunkL.setTranslateZ(-19.0);

        Box stripeTrunkR = new Box(3.8, 0.4, 11.0);
        stripeTrunkR.setMaterial(blackStripeMat);
        stripeTrunkR.setTranslateX(4.5);
        stripeTrunkR.setTranslateY(-12.1);
        stripeTrunkR.setTranslateZ(-19.0);

        getChildren().addAll(stripeHoodL, stripeHoodR, stripeRoofL, stripeRoofR, stripeTrunkL, stripeTrunkR);

        // 3. Rectangular Hood Scoop
        Box scoopBody = new Box(9.0, 2.5, 10.0);
        scoopBody.setMaterial(greenMat);
        scoopBody.setTranslateY(-14.4);
        scoopBody.setTranslateZ(11.0);

        Box scoopVent = new Box(7.8, 1.4, 0.6);
        scoopVent.setMaterial(darkGrilleMat);
        scoopVent.setTranslateY(-14.4);
        scoopVent.setTranslateZ(16.1);

        getChildren().addAll(scoopBody, scoopVent);

        // 4. Fastback Coupe Cabin & Windows
        Box cabin = new Box(21, 7.5, 22);
        cabin.setMaterial(greenMat);
        cabin.setTranslateY(-16.5);
        cabin.setTranslateZ(-3.5);

        Box windshield = new Box(18.5, 6.5, 1.2);
        windshield.setMaterial(glassMat);
        windshield.getTransforms().add(new Rotate(28, Rotate.X_AXIS));
        windshield.setTranslateY(-16.5);
        windshield.setTranslateZ(8.0);

        Box rearGlass = new Box(18.5, 6.5, 1.2);
        rearGlass.setMaterial(glassMat);
        rearGlass.getTransforms().add(new Rotate(-32, Rotate.X_AXIS));
        rearGlass.setTranslateY(-16.5);
        rearGlass.setTranslateZ(-15.0);

        Box sideWinL = new Box(0.6, 5.5, 15.0);
        sideWinL.setMaterial(glassMat);
        sideWinL.setTranslateX(-10.6);
        sideWinL.setTranslateY(-16.5);
        sideWinL.setTranslateZ(-3.5);

        Box sideWinR = new Box(0.6, 5.5, 15.0);
        sideWinR.setMaterial(glassMat);
        sideWinR.setTranslateX(10.6);
        sideWinR.setTranslateY(-16.5);
        sideWinR.setTranslateZ(-3.5);

        getChildren().addAll(cabin, windshield, rearGlass, sideWinL, sideWinR);

        // 5. Deep-Set Front Grille & Quad Round Headlights (2 per side)
        Box frontGrille = new Box(22, 5.0, 1.4);
        frontGrille.setMaterial(darkGrilleMat);
        frontGrille.setTranslateY(-8.5);
        frontGrille.setTranslateZ(24.3);

        Box noseBeak = new Box(2.0, 5.2, 2.2);
        noseBeak.setMaterial(greenMat);
        noseBeak.setTranslateY(-8.5);
        noseBeak.setTranslateZ(24.9);

        // Quad Round Headlights
        Sphere hlOuterL = new Sphere(1.2);
        hlOuterL.setMaterial(headlightMat);
        hlOuterL.setTranslateX(-8.5);
        hlOuterL.setTranslateY(-8.5);
        hlOuterL.setTranslateZ(24.8);

        Sphere hlInnerL = new Sphere(1.2);
        hlInnerL.setMaterial(headlightMat);
        hlInnerL.setTranslateX(-5.6);
        hlInnerL.setTranslateY(-8.5);
        hlInnerL.setTranslateZ(24.8);

        Sphere hlInnerR = new Sphere(1.2);
        hlInnerR.setMaterial(headlightMat);
        hlInnerR.setTranslateX(5.6);
        hlInnerR.setTranslateY(-8.5);
        hlInnerR.setTranslateZ(24.8);

        Sphere hlOuterR = new Sphere(1.2);
        hlOuterR.setMaterial(headlightMat);
        hlOuterR.setTranslateX(8.5);
        hlOuterR.setTranslateY(-8.5);
        hlOuterR.setTranslateZ(24.8);

        getChildren().addAll(frontGrille, noseBeak, hlOuterL, hlInnerL, hlInnerR, hlOuterR);

        // 6. Chrome Bumpers & Dual Exhaust Pipes
        Box bumperF = new Box(25, 3.2, 2.0);
        bumperF.setMaterial(chromeMat);
        bumperF.setTranslateY(-5.0);
        bumperF.setTranslateZ(24.8);

        Box bumperR = new Box(25, 3.2, 2.0);
        bumperR.setMaterial(chromeMat);
        bumperR.setTranslateY(-5.0);
        bumperR.setTranslateZ(-24.8);

        Cylinder exhL = new Cylinder(0.9, 3.0);
        exhL.setMaterial(chromeMat);
        exhL.getTransforms().add(new Rotate(90, Rotate.X_AXIS));
        exhL.setTranslateX(-7.0);
        exhL.setTranslateY(-4.0);
        exhL.setTranslateZ(-25.5);

        Cylinder exhR = new Cylinder(0.9, 3.0);
        exhR.setMaterial(chromeMat);
        exhR.getTransforms().add(new Rotate(90, Rotate.X_AXIS));
        exhR.setTranslateX(7.0);
        exhR.setTranslateY(-4.0);
        exhR.setTranslateZ(-25.5);

        getChildren().addAll(bumperF, bumperR, exhL, exhR);

        // 7. 4 Muscle Car Wheels
        addWheel(-13.0, -4.8, 14, 4.8, 3.6, tireMat, darkGrilleMat);
        addWheel(13.0, -4.8, 14, 4.8, 3.6, tireMat, darkGrilleMat);
        addWheel(-13.0, -5.0, -14, 5.0, 4.0, tireMat, darkGrilleMat);
        addWheel(13.0, -5.0, -14, 5.0, 4.0, tireMat, darkGrilleMat);
    }

    /**
     * 3. Red Open-Roof Convertible driven by Velociraptor Dinosaur:
     * - Metallic cherry red convertible body with open cockpit.
     * - Tan/beige leather bucket seats and raked front windshield frame.
     * - Velociraptor Dinosaur sitting upright in the driver's seat:
     *     * Scaled olive/tan reptilian neck & head with fierce yellow eyes.
     *     * Articulated open snarling jaw showing sharp white teeth & pink tongue.
     *     * Raptor arms and sharp black claws actively gripping the steering wheel!
     * - Chrome multi-spoke wheels and sports exhaust.
     */
    private void buildDinoConvertibleModel() {
        PhongMaterial carRedMat = new PhongMaterial(Color.rgb(215, 20, 30));
        carRedMat.setSpecularColor(Color.rgb(255, 200, 200));
        carRedMat.setSpecularPower(40.0);

        PhongMaterial tanLeatherMat = new PhongMaterial(Color.rgb(222, 180, 135));
        PhongMaterial interiorDarkMat = new PhongMaterial(Color.rgb(35, 35, 38));
        PhongMaterial chromeMat = new PhongMaterial(Color.rgb(230, 235, 242));
        chromeMat.setSpecularColor(Color.WHITE);

        PhongMaterial glassMat = new PhongMaterial(Color.rgb(180, 220, 245));
        PhongMaterial tireMat = new PhongMaterial(Color.rgb(24, 24, 26));

        // Velociraptor Dinosaur Materials
        PhongMaterial dinoSkinMat = new PhongMaterial(Color.rgb(138, 148, 98));
        dinoSkinMat.setSpecularColor(Color.rgb(175, 185, 140));

        PhongMaterial dinoSkinDarkMat = new PhongMaterial(Color.rgb(90, 100, 60));
        PhongMaterial dinoMouthMat = new PhongMaterial(Color.rgb(220, 95, 110));
        PhongMaterial dinoTeethMat = new PhongMaterial(Color.WHITE);

        PhongMaterial dinoEyeMat = new PhongMaterial(Color.rgb(255, 170, 25));
        dinoEyeMat.setSpecularColor(Color.YELLOW);

        PhongMaterial clawMat = new PhongMaterial(Color.rgb(30, 30, 30));

        // 1. Sleek Red Convertible Body
        Box body = new Box(25, 7.0, 48);
        body.setMaterial(carRedMat);
        body.setTranslateY(-7.5);
        getChildren().add(body);

        Box frontHood = new Box(23, 2.5, 18);
        frontHood.setMaterial(carRedMat);
        frontHood.setTranslateY(-11.5);
        frontHood.setTranslateZ(14.5);

        Box rearTrunk = new Box(23, 3.0, 14);
        rearTrunk.setMaterial(carRedMat);
        rearTrunk.setTranslateY(-12.0);
        rearTrunk.setTranslateZ(-16.5);

        getChildren().addAll(frontHood, rearTrunk);

        // 2. Open Cockpit Interior & Tan Leather Seats
        Box cockpitFloor = new Box(21, 1.5, 18);
        cockpitFloor.setMaterial(interiorDarkMat);
        cockpitFloor.setTranslateY(-8.0);
        cockpitFloor.setTranslateZ(-1.0);

        // Passenger Seat (Right side)
        Box passSeatBase = new Box(8.0, 3.5, 8.0);
        passSeatBase.setMaterial(tanLeatherMat);
        passSeatBase.setTranslateX(4.8);
        passSeatBase.setTranslateY(-10.0);
        passSeatBase.setTranslateZ(-2.0);

        Box passSeatBack = new Box(8.0, 8.0, 2.5);
        passSeatBack.setMaterial(tanLeatherMat);
        passSeatBack.setTranslateX(4.8);
        passSeatBack.setTranslateY(-15.0);
        passSeatBack.setTranslateZ(-6.5);

        Cylinder passHeadrest = new Cylinder(2.0, 4.0);
        passHeadrest.setMaterial(tanLeatherMat);
        passHeadrest.getTransforms().add(new Rotate(90, Rotate.Z_AXIS));
        passHeadrest.setTranslateX(4.8);
        passHeadrest.setTranslateY(-19.5);
        passHeadrest.setTranslateZ(-6.5);

        // Driver Seat (Left side, where Velociraptor is seated)
        Box driverSeatBack = new Box(8.0, 8.0, 2.5);
        driverSeatBack.setMaterial(tanLeatherMat);
        driverSeatBack.setTranslateX(-4.8);
        driverSeatBack.setTranslateY(-15.0);
        driverSeatBack.setTranslateZ(-6.5);

        Cylinder driverHeadrest = new Cylinder(2.0, 4.0);
        driverHeadrest.setMaterial(tanLeatherMat);
        driverHeadrest.getTransforms().add(new Rotate(90, Rotate.Z_AXIS));
        driverHeadrest.setTranslateX(-4.8);
        driverHeadrest.setTranslateY(-19.5);
        driverHeadrest.setTranslateZ(-6.5);

        getChildren().addAll(cockpitFloor, passSeatBase, passSeatBack, passHeadrest, driverSeatBack, driverHeadrest);

        // 3. Raked Front Windshield Frame
        Box windshield = new Box(22, 6.8, 1.2);
        windshield.setMaterial(glassMat);
        windshield.getTransforms().add(new Rotate(28, Rotate.X_AXIS));
        windshield.setTranslateY(-14.5);
        windshield.setTranslateZ(6.2);

        Box mirrorL = new Box(2.5, 1.5, 1.2);
        mirrorL.setMaterial(carRedMat);
        mirrorL.setTranslateX(-13.0);
        mirrorL.setTranslateY(-12.5);
        mirrorL.setTranslateZ(5.5);

        Box mirrorR = new Box(2.5, 1.5, 1.2);
        mirrorR.setMaterial(carRedMat);
        mirrorR.setTranslateX(13.0);
        mirrorR.setTranslateY(-12.5);
        mirrorR.setTranslateZ(5.5);

        getChildren().addAll(windshield, mirrorL, mirrorR);

        // 4. THE VELOCIRAPTOR DINOSAUR DRIVER (Sitting in Driver Seat)
        Group dinoDriver = new Group();

        // Dinosaur Torso
        Box dinoTorso = new Box(5.0, 7.5, 5.5);
        dinoTorso.setMaterial(dinoSkinMat);
        dinoTorso.setTranslateX(-4.8);
        dinoTorso.setTranslateY(-14.0);
        dinoTorso.setTranslateZ(-1.5);

        Box dinoSpineStripe = new Box(2.0, 7.5, 1.2);
        dinoSpineStripe.setMaterial(dinoSkinDarkMat);
        dinoSpineStripe.setTranslateX(-4.8);
        dinoSpineStripe.setTranslateY(-14.0);
        dinoSpineStripe.setTranslateZ(-4.2);

        // Curved Muscular Raptor Neck
        Cylinder dinoNeck = new Cylinder(2.0, 7.0);
        dinoNeck.setMaterial(dinoSkinMat);
        dinoNeck.getTransforms().add(new Rotate(25, Rotate.X_AXIS));
        dinoNeck.setTranslateX(-4.8);
        dinoNeck.setTranslateY(-19.5);
        dinoNeck.setTranslateZ(0.5);

        // Dinosaur Head & Jaws
        Box cranium = new Box(3.8, 3.2, 6.0);
        cranium.setMaterial(dinoSkinMat);
        cranium.setTranslateX(-4.8);
        cranium.setTranslateY(-23.2);
        cranium.setTranslateZ(2.5);

        Box browRidge = new Box(4.0, 1.0, 3.0);
        browRidge.setMaterial(dinoSkinDarkMat);
        browRidge.setTranslateX(-4.8);
        browRidge.setTranslateY(-24.8);
        browRidge.setTranslateZ(1.5);

        Box snout = new Box(2.8, 2.2, 4.0);
        snout.setMaterial(dinoSkinMat);
        snout.setTranslateX(-4.8);
        snout.setTranslateY(-22.5);
        snout.setTranslateZ(6.2);

        // Open Snarling Lower Jaw
        Box lowerJaw = new Box(2.6, 1.4, 5.8);
        lowerJaw.setMaterial(dinoSkinMat);
        lowerJaw.getTransforms().add(new Rotate(22, Rotate.X_AXIS));
        lowerJaw.setTranslateX(-4.8);
        lowerJaw.setTranslateY(-20.2);
        lowerJaw.setTranslateZ(3.5);

        Box innerTongue = new Box(2.0, 0.8, 4.2);
        innerTongue.setMaterial(dinoMouthMat);
        innerTongue.setTranslateX(-4.8);
        innerTongue.setTranslateY(-20.8);
        innerTongue.setTranslateZ(3.2);

        // White Sharp Teeth
        Box teethUpper = new Box(2.6, 0.6, 4.2);
        teethUpper.setMaterial(dinoTeethMat);
        teethUpper.setTranslateX(-4.8);
        teethUpper.setTranslateY(-21.4);
        teethUpper.setTranslateZ(4.8);

        Box teethLower = new Box(2.4, 0.6, 4.2);
        teethLower.setMaterial(dinoTeethMat);
        teethLower.setTranslateX(-4.8);
        teethLower.setTranslateY(-20.6);
        teethLower.setTranslateZ(4.8);

        // Fierce Yellow Raptor Eyes
        Sphere eyeL = new Sphere(0.85);
        eyeL.setMaterial(dinoEyeMat);
        eyeL.setTranslateX(-6.8);
        eyeL.setTranslateY(-23.4);
        eyeL.setTranslateZ(2.0);

        Sphere eyeR = new Sphere(0.85);
        eyeR.setMaterial(dinoEyeMat);
        eyeR.setTranslateX(-2.8);
        eyeR.setTranslateY(-23.4);
        eyeR.setTranslateZ(2.0);

        // Steering Column & Sports Steering Wheel
        Cylinder steerColumn = new Cylinder(0.9, 5.0);
        steerColumn.setMaterial(interiorDarkMat);
        steerColumn.getTransforms().add(new Rotate(35, Rotate.X_AXIS));
        steerColumn.setTranslateX(-4.8);
        steerColumn.setTranslateY(-12.5);
        steerColumn.setTranslateZ(3.5);

        Cylinder steerWheel = new Cylinder(2.6, 0.7);
        steerWheel.setMaterial(clawMat);
        steerWheel.getTransforms().add(new Rotate(55, Rotate.X_AXIS));
        steerWheel.setTranslateX(-4.8);
        steerWheel.setTranslateY(-15.0);
        steerWheel.setTranslateZ(3.0);

        // Raptor Arms & Claws Gripping Steering Wheel
        Cylinder armL = new Cylinder(0.9, 5.2);
        armL.setMaterial(dinoSkinMat);
        armL.getTransforms().add(new Rotate(45, Rotate.X_AXIS));
        armL.setTranslateX(-7.2);
        armL.setTranslateY(-14.5);
        armL.setTranslateZ(0.5);

        Box clawsL = new Box(1.2, 0.8, 1.5);
        clawsL.setMaterial(clawMat);
        clawsL.setTranslateX(-6.8);
        clawsL.setTranslateY(-15.6);
        clawsL.setTranslateZ(2.8);

        Cylinder armR = new Cylinder(0.9, 5.2);
        armR.setMaterial(dinoSkinMat);
        armR.getTransforms().add(new Rotate(45, Rotate.X_AXIS));
        armR.setTranslateX(-2.4);
        armR.setTranslateY(-14.5);
        armR.setTranslateZ(0.5);

        Box clawsR = new Box(1.2, 0.8, 1.5);
        clawsR.setMaterial(clawMat);
        clawsR.setTranslateX(-2.8);
        clawsR.setTranslateY(-15.6);
        clawsR.setTranslateZ(2.8);

        dinoDriver.getChildren().addAll(dinoTorso, dinoSpineStripe, dinoNeck, cranium, browRidge,
                snout, lowerJaw, innerTongue, teethUpper, teethLower, eyeL, eyeR,
                steerColumn, steerWheel, armL, clawsL, armR, clawsR);
        getChildren().add(dinoDriver);

        // 5. Front/Rear Lights & Chrome Grille
        Box hlL = new Box(4.2, 2.4, 1.2);
        hlL.setMaterial(new PhongMaterial(Color.rgb(255, 255, 220)));
        hlL.setTranslateX(-8.5);
        hlL.setTranslateY(-8.5);
        hlL.setTranslateZ(24.4);

        Box hlR = new Box(4.2, 2.4, 1.2);
        hlR.setMaterial(new PhongMaterial(Color.rgb(255, 255, 220)));
        hlR.setTranslateX(8.5);
        hlR.setTranslateY(-8.5);
        hlR.setTranslateZ(24.4);

        Box grille = new Box(16.0, 4.0, 1.2);
        grille.setMaterial(chromeMat);
        grille.setTranslateY(-8.0);
        grille.setTranslateZ(24.6);

        Box rearLights = new Box(22.0, 2.5, 1.2);
        rearLights.setMaterial(new PhongMaterial(Color.rgb(220, 25, 25)));
        rearLights.setTranslateY(-8.5);
        rearLights.setTranslateZ(-24.5);

        getChildren().addAll(hlL, hlR, grille, rearLights);

        // 6. 4 Multi-Spoke Chrome Wheels
        addWheel(-13.0, -4.8, 14, 4.8, 3.6, tireMat, chromeMat);
        addWheel(13.0, -4.8, 14, 4.8, 3.6, tireMat, chromeMat);
        addWheel(-13.0, -4.8, -14, 4.8, 3.6, tireMat, chromeMat);
        addWheel(13.0, -4.8, -14, 4.8, 3.6, tireMat, chromeMat);
    }

    /**
     * Prehistoric Fossil Chopper / Bone Bike (Matching Reference Design):
     * - Headlight / Cowl: Sculpted dinosaur fossil skull with eye sockets & teeth ridge.
     * - Frame & Tank: Skeletal curved ribcage over a carved wooden log engine core.
     * - Suspension & Fork: Bone stanchions with warm amber-wood collars.
     * - Engine Cylinders: Dual sliced-log tree stump cylinders with golden end-grain caps.
     * - Seat & Tail: Terracotta leather-wrapped cylindrical tail seat behind pelvic bone.
     * - Wheels: Chunky dark rubber tires with slate-stone rims and golden sliced-log wooden hubs.
     */
    private void buildBikeModel() {
        // Palette matching the fossil & wood chopper reference
        PhongMaterial boneMat = new PhongMaterial(Color.rgb(240, 234, 218));
        boneMat.setSpecularColor(Color.rgb(255, 250, 240));
        boneMat.setSpecularPower(20.0);

        PhongMaterial boneDarkMat = new PhongMaterial(Color.rgb(190, 180, 160));
        PhongMaterial woodAmberMat = new PhongMaterial(Color.rgb(228, 142, 38));
        woodAmberMat.setSpecularColor(Color.rgb(255, 200, 120));

        PhongMaterial woodBarkMat = new PhongMaterial(Color.rgb(115, 65, 28));
        PhongMaterial seatLeatherMat = new PhongMaterial(Color.rgb(215, 110, 40));
        PhongMaterial slateRimMat = new PhongMaterial(Color.rgb(140, 155, 172));
        PhongMaterial tireMat = new PhongMaterial(Color.rgb(28, 30, 35));
        PhongMaterial eyeGlowMat = new PhongMaterial(Color.rgb(255, 175, 40));
        eyeGlowMat.setSpecularColor(Color.YELLOW);

        // 1. Front Dinosaur Fossil Skull (Cowl / Head)
        Group skullGroup = new Group();

        // Cranium / Upper Skull
        Box cranium = new Box(7.4, 5.5, 6.8);
        cranium.setMaterial(boneMat);
        cranium.setTranslateY(-15.5);
        cranium.setTranslateZ(8.5);

        // Tapered Snout / Nose
        Box snout = new Box(5.6, 4.4, 6.5);
        snout.setMaterial(boneMat);
        snout.setTranslateY(-14.2);
        snout.setTranslateZ(13.2);

        // Lower Jaw & Teeth Ridge
        Box lowerJaw = new Box(4.8, 1.8, 5.5);
        lowerJaw.setMaterial(boneMat);
        lowerJaw.setTranslateY(-11.5);
        lowerJaw.setTranslateZ(12.5);

        Box teethRidge = new Box(5.0, 1.0, 5.0);
        teethRidge.setMaterial(new PhongMaterial(Color.WHITE));
        teethRidge.setTranslateY(-12.8);
        teethRidge.setTranslateZ(13.0);

        // Deep Eye Sockets with Amber Glowing Orbs
        Box eyeSocketL = new Box(1.4, 2.4, 2.6);
        eyeSocketL.setMaterial(boneDarkMat);
        eyeSocketL.setTranslateX(-3.8);
        eyeSocketL.setTranslateY(-15.2);
        eyeSocketL.setTranslateZ(9.6);

        Sphere eyeL = new Sphere(1.0);
        eyeL.setMaterial(eyeGlowMat);
        eyeL.setTranslateX(-3.6);
        eyeL.setTranslateY(-15.2);
        eyeL.setTranslateZ(9.6);

        Box eyeSocketR = new Box(1.4, 2.4, 2.6);
        eyeSocketR.setMaterial(boneDarkMat);
        eyeSocketR.setTranslateX(3.8);
        eyeSocketR.setTranslateY(-15.2);
        eyeSocketR.setTranslateZ(9.6);

        Sphere eyeR = new Sphere(1.0);
        eyeR.setMaterial(eyeGlowMat);
        eyeR.setTranslateX(3.6);
        eyeR.setTranslateY(-15.2);
        eyeR.setTranslateZ(9.6);

        skullGroup.getChildren().addAll(cranium, snout, lowerJaw, teethRidge, eyeSocketL, eyeL, eyeSocketR, eyeR);
        getChildren().add(skullGroup);

        // 2. Bone Fork with Chunky Amber Wood Suspension Collars
        Cylinder forkL = new Cylinder(1.1, 14.0);
        forkL.setMaterial(boneMat);
        forkL.getTransforms().add(new Rotate(18, Rotate.X_AXIS));
        forkL.setTranslateX(-2.8);
        forkL.setTranslateY(-10.5);
        forkL.setTranslateZ(9.2);

        Cylinder collarL = new Cylinder(2.4, 3.4);
        collarL.setMaterial(woodAmberMat);
        collarL.getTransforms().add(new Rotate(18, Rotate.X_AXIS));
        collarL.setTranslateX(-2.8);
        collarL.setTranslateY(-12.8);
        collarL.setTranslateZ(8.4);

        Cylinder forkR = new Cylinder(1.1, 14.0);
        forkR.setMaterial(boneMat);
        forkR.getTransforms().add(new Rotate(18, Rotate.X_AXIS));
        forkR.setTranslateX(2.8);
        forkR.setTranslateY(-10.5);
        forkR.setTranslateZ(9.2);

        Cylinder collarR = new Cylinder(2.4, 3.4);
        collarR.setMaterial(woodAmberMat);
        collarR.getTransforms().add(new Rotate(18, Rotate.X_AXIS));
        collarR.setTranslateX(2.8);
        collarR.setTranslateY(-12.8);
        collarR.setTranslateZ(8.4);

        getChildren().addAll(forkL, collarL, forkR, collarR);

        // Handlebars (Curved Bone Horns)
        Cylinder handleCross = new Cylinder(0.9, 13.0);
        handleCross.setMaterial(boneMat);
        handleCross.getTransforms().add(new Rotate(90, Rotate.Z_AXIS));
        handleCross.setTranslateY(-18.0);
        handleCross.setTranslateZ(7.5);

        Cylinder gripL = new Cylinder(1.4, 2.5);
        gripL.setMaterial(woodAmberMat);
        gripL.getTransforms().add(new Rotate(90, Rotate.Z_AXIS));
        gripL.setTranslateX(-6.2);
        gripL.setTranslateY(-18.0);
        gripL.setTranslateZ(7.5);

        Cylinder gripR = new Cylinder(1.4, 2.5);
        gripR.setMaterial(woodAmberMat);
        gripR.getTransforms().add(new Rotate(90, Rotate.Z_AXIS));
        gripR.setTranslateX(6.2);
        gripR.setTranslateY(-18.0);
        gripR.setTranslateZ(7.5);

        getChildren().addAll(handleCross, gripL, gripR);

        // 3. Curved Skeletal Ribcage & Vertebrae Spine
        Box spine = new Box(3.2, 2.4, 15.0);
        spine.setMaterial(boneMat);
        spine.setTranslateY(-15.0);
        spine.setTranslateZ(0.5);
        getChildren().add(spine);

        // 5 Pairs of curved lateral ribs
        for (int i = 0; i < 5; i++) {
            double ribZ = 5.0 - (i * 2.6);
            double ribHeight = 7.0 + (i == 2 ? 1.0 : 0.0);

            // Left Rib
            Cylinder ribL = new Cylinder(0.65, ribHeight);
            ribL.setMaterial(boneMat);
            ribL.getTransforms().addAll(
                    new Rotate(28, Rotate.Z_AXIS),
                    new Rotate(8, Rotate.Y_AXIS)
            );
            ribL.setTranslateX(-4.2);
            ribL.setTranslateY(-11.0);
            ribL.setTranslateZ(ribZ);

            // Right Rib
            Cylinder ribR = new Cylinder(0.65, ribHeight);
            ribR.setMaterial(boneMat);
            ribR.getTransforms().addAll(
                    new Rotate(-28, Rotate.Z_AXIS),
                    new Rotate(-8, Rotate.Y_AXIS)
            );
            ribR.setTranslateX(4.2);
            ribR.setTranslateY(-11.0);
            ribR.setTranslateZ(ribZ);

            getChildren().addAll(ribL, ribR);
        }

        // Inner Wooden Log Chassis (Dark bark trunk)
        Cylinder woodLogCore = new Cylinder(3.2, 14.0);
        woodLogCore.setMaterial(woodBarkMat);
        woodLogCore.getTransforms().add(new Rotate(90, Rotate.X_AXIS));
        woodLogCore.setTranslateY(-8.5);
        woodLogCore.setTranslateZ(0.5);
        getChildren().add(woodLogCore);

        // Dual Sliced-Log Engine Cylinders with Golden End-Grain Caps (Right flank)
        Cylinder logCyl1 = new Cylinder(2.4, 4.2);
        logCyl1.setMaterial(woodBarkMat);
        logCyl1.setTranslateX(4.2);
        logCyl1.setTranslateY(-5.0);
        logCyl1.setTranslateZ(1.5);

        Cylinder logCap1 = new Cylinder(2.45, 0.8);
        logCap1.setMaterial(woodAmberMat);
        logCap1.setTranslateX(4.2);
        logCap1.setTranslateY(-7.2);
        logCap1.setTranslateZ(1.5);

        Cylinder logCyl2 = new Cylinder(2.4, 4.2);
        logCyl2.setMaterial(woodBarkMat);
        logCyl2.setTranslateX(4.2);
        logCyl2.setTranslateY(-5.0);
        logCyl2.setTranslateZ(-3.5);

        Cylinder logCap2 = new Cylinder(2.45, 0.8);
        logCap2.setMaterial(woodAmberMat);
        logCap2.setTranslateX(4.2);
        logCap2.setTranslateY(-7.2);
        logCap2.setTranslateZ(-3.5);

        getChildren().addAll(logCyl1, logCap1, logCyl2, logCap2);

        // 4. Pelvis Bone & Rear Swingarm
        Box pelvis = new Box(7.2, 4.8, 5.5);
        pelvis.setMaterial(boneMat);
        pelvis.setTranslateY(-12.8);
        pelvis.setTranslateZ(-7.5);
        getChildren().add(pelvis);

        // Rear Bone Swingarms (Femur bone struts to rear axle)
        Cylinder swingL = new Cylinder(1.1, 11.0);
        swingL.setMaterial(boneMat);
        swingL.getTransforms().add(new Rotate(65, Rotate.X_AXIS));
        swingL.setTranslateX(-2.5);
        swingL.setTranslateY(-8.5);
        swingL.setTranslateZ(-10.0);

        Cylinder swingR = new Cylinder(1.1, 11.0);
        swingR.setMaterial(boneMat);
        swingR.getTransforms().add(new Rotate(65, Rotate.X_AXIS));
        swingR.setTranslateX(2.5);
        swingR.setTranslateY(-8.5);
        swingR.setTranslateZ(-10.0);

        Sphere knuckleL = new Sphere(1.5);
        knuckleL.setMaterial(boneDarkMat);
        knuckleL.setTranslateX(-2.5);
        knuckleL.setTranslateY(-6.0);
        knuckleL.setTranslateZ(-12.0);

        Sphere knuckleR = new Sphere(1.5);
        knuckleR.setMaterial(boneDarkMat);
        knuckleR.setTranslateX(2.5);
        knuckleR.setTranslateY(-6.0);
        knuckleR.setTranslateZ(-12.0);

        // Golden Drive Chain
        Box driveChain = new Box(0.6, 1.2, 9.5);
        driveChain.setMaterial(woodAmberMat);
        driveChain.setTranslateX(-3.4);
        driveChain.setTranslateY(-7.0);
        driveChain.setTranslateZ(-8.5);

        getChildren().addAll(swingL, swingR, knuckleL, knuckleR, driveChain);

        // 5. Terracotta / Amber Wrapped Cylindrical Tail Seat
        Cylinder tailSeat = new Cylinder(2.2, 8.5);
        tailSeat.setMaterial(seatLeatherMat);
        tailSeat.getTransforms().add(new Rotate(78, Rotate.X_AXIS));
        tailSeat.setTranslateY(-15.2);
        tailSeat.setTranslateZ(-12.5);

        Cylinder seatRing = new Cylinder(2.35, 1.2);
        seatRing.setMaterial(woodAmberMat);
        seatRing.getTransforms().add(new Rotate(78, Rotate.X_AXIS));
        seatRing.setTranslateY(-15.6);
        seatRing.setTranslateZ(-11.2);

        getChildren().addAll(tailSeat, seatRing);

        // 6. THE VELOCIRAPTOR DINOSAUR RIDER (Riding the Fossil Chopper!)
        Group dinoRider = new Group();

        PhongMaterial dinoSkinMat = new PhongMaterial(Color.rgb(138, 148, 98));
        dinoSkinMat.setSpecularColor(Color.rgb(175, 185, 140));

        PhongMaterial dinoSkinDarkMat = new PhongMaterial(Color.rgb(90, 100, 60));
        PhongMaterial dinoMouthMat = new PhongMaterial(Color.rgb(220, 95, 110));
        PhongMaterial dinoTeethMat = new PhongMaterial(Color.WHITE);

        PhongMaterial dinoEyeMat = new PhongMaterial(Color.rgb(255, 170, 25));
        dinoEyeMat.setSpecularColor(Color.YELLOW);

        PhongMaterial clawMat = new PhongMaterial(Color.rgb(30, 30, 30));

        // Pelvis & Leaning Torso (Aggressive forward motorcycle riding posture)
        Box dinoPelvis = new Box(5.2, 5.0, 5.5);
        dinoPelvis.setMaterial(dinoSkinMat);
        dinoPelvis.setTranslateY(-16.5);
        dinoPelvis.setTranslateZ(-8.0);

        Box dinoTorso = new Box(4.8, 6.5, 9.0);
        dinoTorso.setMaterial(dinoSkinMat);
        dinoTorso.getTransforms().add(new Rotate(25, Rotate.X_AXIS));
        dinoTorso.setTranslateY(-19.5);
        dinoTorso.setTranslateZ(-2.5);

        Box dinoSpineStripe = new Box(1.8, 6.8, 9.0);
        dinoSpineStripe.setMaterial(dinoSkinDarkMat);
        dinoSpineStripe.getTransforms().add(new Rotate(25, Rotate.X_AXIS));
        dinoSpineStripe.setTranslateY(-20.2);
        dinoSpineStripe.setTranslateZ(-2.5);

        // Thighs & Foot Talons gripping the frame & footpegs
        Cylinder thighL = new Cylinder(1.4, 6.5);
        thighL.setMaterial(dinoSkinMat);
        thighL.getTransforms().addAll(new Rotate(45, Rotate.X_AXIS), new Rotate(15, Rotate.Y_AXIS));
        thighL.setTranslateX(-3.2);
        thighL.setTranslateY(-14.0);
        thighL.setTranslateZ(-5.5);

        Cylinder shinL = new Cylinder(1.0, 5.5);
        shinL.setMaterial(dinoSkinMat);
        shinL.getTransforms().add(new Rotate(-20, Rotate.X_AXIS));
        shinL.setTranslateX(-4.0);
        shinL.setTranslateY(-11.0);
        shinL.setTranslateZ(-2.5);

        Box footL = new Box(1.4, 0.8, 2.5);
        footL.setMaterial(clawMat);
        footL.setTranslateX(-4.2);
        footL.setTranslateY(-8.5);
        footL.setTranslateZ(-1.0);

        Cylinder thighR = new Cylinder(1.4, 6.5);
        thighR.setMaterial(dinoSkinMat);
        thighR.getTransforms().addAll(new Rotate(45, Rotate.X_AXIS), new Rotate(-15, Rotate.Y_AXIS));
        thighR.setTranslateX(3.2);
        thighR.setTranslateY(-14.0);
        thighR.setTranslateZ(-5.5);

        Cylinder shinR = new Cylinder(1.0, 5.5);
        shinR.setMaterial(dinoSkinMat);
        shinR.getTransforms().add(new Rotate(-20, Rotate.X_AXIS));
        shinR.setTranslateX(4.0);
        shinR.setTranslateY(-11.0);
        shinR.setTranslateZ(-2.5);

        Box footR = new Box(1.4, 0.8, 2.5);
        footR.setMaterial(clawMat);
        footR.setTranslateX(4.2);
        footR.setTranslateY(-8.5);
        footR.setTranslateZ(-1.0);

        // Long Sweeping Raptor Tail extending behind over rear wheel
        Cylinder tailBase = new Cylinder(1.6, 9.5);
        tailBase.setMaterial(dinoSkinMat);
        tailBase.getTransforms().add(new Rotate(-35, Rotate.X_AXIS));
        tailBase.setTranslateY(-18.5);
        tailBase.setTranslateZ(-15.0);

        Cylinder tailTip = new Cylinder(1.0, 9.0);
        tailTip.setMaterial(dinoSkinMat);
        tailTip.getTransforms().add(new Rotate(-20, Rotate.X_AXIS));
        tailTip.setTranslateY(-22.5);
        tailTip.setTranslateZ(-22.5);

        Box tailStripe = new Box(0.8, 1.2, 17.0);
        tailStripe.setMaterial(dinoSkinDarkMat);
        tailStripe.getTransforms().add(new Rotate(-28, Rotate.X_AXIS));
        tailStripe.setTranslateY(-21.0);
        tailStripe.setTranslateZ(-18.5);

        // S-Curved Neck & Head
        Cylinder neck = new Cylinder(2.0, 7.5);
        neck.setMaterial(dinoSkinMat);
        neck.getTransforms().add(new Rotate(32, Rotate.X_AXIS));
        neck.setTranslateY(-24.0);
        neck.setTranslateZ(1.0);

        Box riderCranium = new Box(3.8, 3.2, 6.2);
        riderCranium.setMaterial(dinoSkinMat);
        riderCranium.setTranslateY(-28.0);
        riderCranium.setTranslateZ(3.5);

        Box brow = new Box(4.0, 1.0, 3.2);
        brow.setMaterial(dinoSkinDarkMat);
        brow.setTranslateY(-29.6);
        brow.setTranslateZ(2.5);

        Box riderSnout = new Box(2.8, 2.2, 4.2);
        riderSnout.setMaterial(dinoSkinMat);
        riderSnout.setTranslateY(-27.2);
        riderSnout.setTranslateZ(7.5);

        // Open Snarling Lower Jaw
        Box riderLowerJaw = new Box(2.6, 1.4, 5.8);
        riderLowerJaw.setMaterial(dinoSkinMat);
        riderLowerJaw.getTransforms().add(new Rotate(22, Rotate.X_AXIS));
        riderLowerJaw.setTranslateY(-25.0);
        riderLowerJaw.setTranslateZ(4.8);

        Box innerTongue = new Box(2.0, 0.8, 4.2);
        innerTongue.setMaterial(dinoMouthMat);
        innerTongue.setTranslateY(-25.6);
        innerTongue.setTranslateZ(4.5);

        Box teethU = new Box(2.6, 0.6, 4.2);
        teethU.setMaterial(dinoTeethMat);
        teethU.setTranslateY(-26.2);
        teethU.setTranslateZ(6.0);

        Box teethL = new Box(2.4, 0.6, 4.2);
        teethL.setMaterial(dinoTeethMat);
        teethL.setTranslateY(-25.4);
        teethL.setTranslateZ(6.0);

        // Fierce Yellow Eyes
        Sphere riderEyeL = new Sphere(0.85);
        riderEyeL.setMaterial(dinoEyeMat);
        riderEyeL.setTranslateX(-2.0);
        riderEyeL.setTranslateY(-28.2);
        riderEyeL.setTranslateZ(3.2);

        Sphere riderEyeR = new Sphere(0.85);
        riderEyeR.setMaterial(dinoEyeMat);
        riderEyeR.setTranslateX(2.0);
        riderEyeR.setTranslateY(-28.2);
        riderEyeR.setTranslateZ(3.2);

        // Raptor Arms & Claws Gripping the Bone Handlebar Grips
        Cylinder armL = new Cylinder(0.9, 7.5);
        armL.setMaterial(dinoSkinMat);
        armL.getTransforms().addAll(new Rotate(48, Rotate.X_AXIS), new Rotate(-24, Rotate.Z_AXIS));
        armL.setTranslateX(-3.8);
        armL.setTranslateY(-20.5);
        armL.setTranslateZ(3.0);

        Box clawsL = new Box(1.4, 0.9, 1.6);
        clawsL.setMaterial(clawMat);
        clawsL.setTranslateX(-6.2);
        clawsL.setTranslateY(-18.2);
        clawsL.setTranslateZ(7.5);

        Cylinder armR = new Cylinder(0.9, 7.5);
        armR.setMaterial(dinoSkinMat);
        armR.getTransforms().addAll(new Rotate(48, Rotate.X_AXIS), new Rotate(24, Rotate.Z_AXIS));
        armR.setTranslateX(3.8);
        armR.setTranslateY(-20.5);
        armR.setTranslateZ(3.0);

        Box clawsR = new Box(1.4, 0.9, 1.6);
        clawsR.setMaterial(clawMat);
        clawsR.setTranslateX(6.2);
        clawsR.setTranslateY(-18.2);
        clawsR.setTranslateZ(7.5);

        dinoRider.getChildren().addAll(dinoPelvis, dinoTorso, dinoSpineStripe,
                thighL, shinL, footL, thighR, shinR, footR,
                tailBase, tailTip, tailStripe, neck, riderCranium, brow, riderSnout,
                riderLowerJaw, innerTongue, teethU, teethL, riderEyeL, riderEyeR,
                armL, clawsL, armR, clawsR);
        getChildren().add(dinoRider);

        // 7. Chunky Wheels with Slate Stone Rims and Sliced-Log Wood Hubs
        buildFossilWheel(0, -6.4, 11.8, 6.4, 4.0, tireMat, slateRimMat, woodAmberMat);  // Front Wheel
        buildFossilWheel(0, -6.4, -12.0, 6.4, 4.4, tireMat, slateRimMat, woodAmberMat); // Rear Wheel
    }

    /**
     * Builds chunky stylized wheel matching the photo:
     * - Dark rubber tire (outer ring)
     * - Slate-stone blue-gray inner rim disc
     * - Warm golden sliced-log wooden center hub cap
     */
    private void buildFossilWheel(double x, double y, double z, double r, double thickness,
                                 PhongMaterial tireMat, PhongMaterial rimMat, PhongMaterial woodHubMat) {
        // Outer Rubber Tire
        Cylinder tire = new Cylinder(r, thickness);
        tire.setMaterial(tireMat);
        tire.getTransforms().add(new Rotate(90, Rotate.Z_AXIS));
        tire.setTranslateX(x);
        tire.setTranslateY(y);
        tire.setTranslateZ(z);

        // Slate Stone Rim Plate
        Cylinder rim = new Cylinder(r * 0.75, thickness + 0.3);
        rim.setMaterial(rimMat);
        rim.getTransforms().add(new Rotate(90, Rotate.Z_AXIS));
        rim.setTranslateX(x);
        rim.setTranslateY(y);
        rim.setTranslateZ(z);

        // Golden Sliced-Log Wood Center Hub Cap
        Cylinder woodHub = new Cylinder(r * 0.40, thickness + 0.6);
        woodHub.setMaterial(woodHubMat);
        woodHub.getTransforms().add(new Rotate(90, Rotate.Z_AXIS));
        woodHub.setTranslateX(x);
        woodHub.setTranslateY(y);
        woodHub.setTranslateZ(z);

        getChildren().addAll(tire, rim, woodHub);
    }

    /**
     * Optimus Prime Semi-Truck (Peterbilt 379 Custom Long-Nose Tractor):
     * - Peterbilt long-nose square hood with vibrant flame decals (red with blue & orange flames).
     * - Deep royal metallic blue high-rise sleeper cab with side flame licks.
     * - Tall split windshield with sky-blue tint and prominent angled chrome sun visor.
     * - Massive vertical chrome radiator grille with red crest and lower cowcatcher bumper.
     * - Towering dual vertical chrome smokestacks with silencers rising high above the roof.
     * - Polished cylindrical chrome fuel tanks and step boxes along both sides.
     * - Flared flame-red fenders over front and rear tandem dual axles.
     * - Exposed rear catwalk deck with functional 5th-wheel trailer coupling hitch.
     * - 6 heavy-duty wheels with deep-dish polished chrome rims and center hub caps.
     */
    private void buildTruckModel() {
        // Optimus Prime Signature Color Palette
        PhongMaterial blueCabMat = new PhongMaterial(Color.rgb(18, 64, 168));
        blueCabMat.setSpecularColor(Color.rgb(180, 210, 255));
        blueCabMat.setSpecularPower(35.0);

        PhongMaterial redHoodMat = new PhongMaterial(Color.rgb(228, 26, 26));
        redHoodMat.setSpecularColor(Color.rgb(255, 180, 180));
        redHoodMat.setSpecularPower(30.0);

        PhongMaterial flameBlueMat = new PhongMaterial(Color.rgb(24, 78, 205));
        flameBlueMat.setSpecularColor(Color.rgb(150, 190, 255));

        PhongMaterial flameOrangeMat = new PhongMaterial(Color.rgb(252, 135, 15));
        flameOrangeMat.setSpecularColor(Color.rgb(255, 210, 120));

        PhongMaterial flameYellowMat = new PhongMaterial(Color.rgb(255, 210, 30));

        PhongMaterial chromeMat = new PhongMaterial(Color.rgb(232, 238, 245));
        chromeMat.setSpecularColor(Color.WHITE);
        chromeMat.setSpecularPower(75.0);

        PhongMaterial chassisMat = new PhongMaterial(Color.rgb(42, 45, 52));
        PhongMaterial glassMat = new PhongMaterial(Color.rgb(85, 175, 235));
        glassMat.setSpecularColor(Color.WHITE);
        glassMat.setSpecularPower(50.0);

        PhongMaterial tireMat = new PhongMaterial(Color.rgb(24, 25, 28));
        PhongMaterial headlightMat = new PhongMaterial(Color.rgb(255, 255, 230));
        headlightMat.setSpecularColor(Color.WHITE);

        PhongMaterial amberLightMat = new PhongMaterial(Color.rgb(255, 165, 20));
        amberLightMat.setSpecularColor(Color.YELLOW);

        // 1. Heavy Ladder Chassis Frame & Rear Catwalk Deck
        Box chassisRails = new Box(24, 6, 78);
        chassisRails.setMaterial(chassisMat);
        chassisRails.setTranslateY(-7.0);
        chassisRails.setTranslateZ(-3.0);
        getChildren().add(chassisRails);

        // Rear Silver Catwalk Plate (Behind sleeper cab)
        Box rearCatwalk = new Box(21, 1.8, 28);
        rearCatwalk.setMaterial(chromeMat);
        rearCatwalk.setTranslateY(-10.2);
        rearCatwalk.setTranslateZ(-17.0);
        getChildren().add(rearCatwalk);

        // Fifth-Wheel Trailer Hitch (Coupling plate on rear deck)
        Box hitchBase = new Box(12, 2.0, 12);
        hitchBase.setMaterial(chassisMat);
        hitchBase.setTranslateY(-11.2);
        hitchBase.setTranslateZ(-19.0);

        Cylinder fifthWheel = new Cylinder(5.5, 2.2);
        fifthWheel.setMaterial(chassisMat);
        fifthWheel.getTransforms().add(new Rotate(90, Rotate.X_AXIS));
        fifthWheel.setTranslateY(-12.4);
        fifthWheel.setTranslateZ(-19.0);

        Cylinder hitchPin = new Cylinder(1.5, 2.5);
        hitchPin.setMaterial(chromeMat);
        hitchPin.getTransforms().add(new Rotate(90, Rotate.X_AXIS));
        hitchPin.setTranslateY(-12.8);
        hitchPin.setTranslateZ(-19.0);

        getChildren().addAll(hitchBase, fifthWheel, hitchPin);

        // Rear Heavy Cross-Bumper & Taillights
        Box rearBumper = new Box(32, 4.0, 2.0);
        rearBumper.setMaterial(chromeMat);
        rearBumper.setTranslateY(-6.5);
        rearBumper.setTranslateZ(-41.0);

        Box tailLightL = new Box(3.5, 2.0, 1.0);
        tailLightL.setMaterial(redHoodMat);
        tailLightL.setTranslateX(-10.0);
        tailLightL.setTranslateY(-6.5);
        tailLightL.setTranslateZ(-42.2);

        Box tailLightR = new Box(3.5, 2.0, 1.0);
        tailLightR.setMaterial(redHoodMat);
        tailLightR.setTranslateX(10.0);
        tailLightR.setTranslateY(-6.5);
        tailLightR.setTranslateZ(-42.2);

        Box mudFlapL = new Box(5.8, 6.5, 0.8);
        mudFlapL.setMaterial(tireMat);
        mudFlapL.setTranslateX(-13.0);
        mudFlapL.setTranslateY(-3.5);
        mudFlapL.setTranslateZ(-41.5);

        Box mudFlapR = new Box(5.8, 6.5, 0.8);
        mudFlapR.setMaterial(tireMat);
        mudFlapR.setTranslateX(13.0);
        mudFlapR.setTranslateY(-3.5);
        mudFlapR.setTranslateZ(-41.5);

        getChildren().addAll(rearBumper, tailLightL, tailLightR, mudFlapL, mudFlapR);

        // 2. High-Rise Sleeper Cab (Royal Blue with Side Flame Decals)
        Box sleeperCab = new Box(29, 23, 22);
        sleeperCab.setMaterial(blueCabMat);
        sleeperCab.setTranslateY(-20.5);
        sleeperCab.setTranslateZ(2.0);

        Box highRoof = new Box(27, 4.5, 20);
        highRoof.setMaterial(blueCabMat);
        highRoof.setTranslateY(-33.5);
        highRoof.setTranslateZ(2.0);

        // Sleeper small side windows
        Box sleeperWinL = new Box(0.6, 5.0, 4.0);
        sleeperWinL.setMaterial(glassMat);
        sleeperWinL.setTranslateX(-14.8);
        sleeperWinL.setTranslateY(-23.0);
        sleeperWinL.setTranslateZ(-3.0);

        Box sleeperWinR = new Box(0.6, 5.0, 4.0);
        sleeperWinR.setMaterial(glassMat);
        sleeperWinR.setTranslateX(14.8);
        sleeperWinR.setTranslateY(-23.0);
        sleeperWinR.setTranslateZ(-3.0);

        // Side Flame Decals on lower blue sleeper sides (Matching reference photo)
        Box sideFlameRedL = new Box(0.4, 4.5, 13.0);
        sideFlameRedL.setMaterial(redHoodMat);
        sideFlameRedL.setTranslateX(-14.8);
        sideFlameRedL.setTranslateY(-14.5);
        sideFlameRedL.setTranslateZ(3.0);

        Box sideFlameOrangeL = new Box(0.5, 2.5, 9.0);
        sideFlameOrangeL.setMaterial(flameOrangeMat);
        sideFlameOrangeL.setTranslateX(-14.9);
        sideFlameOrangeL.setTranslateY(-15.0);
        sideFlameOrangeL.setTranslateZ(5.0);

        Box sideFlameRedR = new Box(0.4, 4.5, 13.0);
        sideFlameRedR.setMaterial(redHoodMat);
        sideFlameRedR.setTranslateX(14.8);
        sideFlameRedR.setTranslateY(-14.5);
        sideFlameRedR.setTranslateZ(3.0);

        Box sideFlameOrangeR = new Box(0.5, 2.5, 9.0);
        sideFlameOrangeR.setMaterial(flameOrangeMat);
        sideFlameOrangeR.setTranslateX(14.9);
        sideFlameOrangeR.setTranslateY(-15.0);
        sideFlameOrangeR.setTranslateZ(5.0);

        getChildren().addAll(sleeperCab, highRoof, sleeperWinL, sleeperWinR,
                sideFlameRedL, sideFlameOrangeL, sideFlameRedR, sideFlameOrangeR);

        // 3. Driver Cockpit, Windows & Signature Chrome Sun Visor
        Box cockpit = new Box(28.5, 20, 14);
        cockpit.setMaterial(blueCabMat);
        cockpit.setTranslateY(-19.0);
        cockpit.setTranslateZ(16.0);

        // Cockpit side door windows
        Box cabWinL = new Box(0.6, 7.5, 8.5);
        cabWinL.setMaterial(glassMat);
        cabWinL.setTranslateX(-14.6);
        cabWinL.setTranslateY(-23.0);
        cabWinL.setTranslateZ(16.5);

        Box cabWinR = new Box(0.6, 7.5, 8.5);
        cabWinR.setMaterial(glassMat);
        cabWinR.setTranslateX(14.6);
        cabWinR.setTranslateY(-23.0);
        cabWinR.setTranslateZ(16.5);

        // Peterbilt Split Front Windshield (Dual panes with center blue pillar)
        Box windshieldL = new Box(11.2, 7.5, 1.0);
        windshieldL.setMaterial(glassMat);
        windshieldL.setTranslateX(-6.2);
        windshieldL.setTranslateY(-23.0);
        windshieldL.setTranslateZ(23.4);

        Box windshieldR = new Box(11.2, 7.5, 1.0);
        windshieldR.setMaterial(glassMat);
        windshieldR.setTranslateX(6.2);
        windshieldR.setTranslateY(-23.0);
        windshieldR.setTranslateZ(23.4);

        Box centerPillar = new Box(1.5, 8.0, 1.2);
        centerPillar.setMaterial(blueCabMat);
        centerPillar.setTranslateX(0);
        centerPillar.setTranslateY(-23.0);
        centerPillar.setTranslateZ(23.6);

        // Signature Angled Chrome Sun Visor jutting forward over windshield
        Box sunVisor = new Box(28.5, 2.2, 5.0);
        sunVisor.setMaterial(chromeMat);
        sunVisor.getTransforms().add(new Rotate(16, Rotate.X_AXIS));
        sunVisor.setTranslateY(-27.8);
        sunVisor.setTranslateZ(24.2);

        // Roof Amber Clearance Lights (5 lights)
        for (int i = -2; i <= 2; i++) {
            Sphere marker = new Sphere(1.1);
            marker.setMaterial(amberLightMat);
            marker.setTranslateX(i * 5.2);
            marker.setTranslateY(-31.5);
            marker.setTranslateZ(22.5);
            getChildren().add(marker);
        }

        getChildren().addAll(cockpit, cabWinL, cabWinR, windshieldL, windshieldR, centerPillar, sunVisor);

        // 4. Long Front Hood & Flame Decals (Red with Blue & Orange Flames)
        Box hood = new Box(19, 14, 18);
        hood.setMaterial(redHoodMat);
        hood.setTranslateY(-14.0);
        hood.setTranslateZ(31.5);

        // Hood Center Chrome Divider Strip
        Box hoodStrip = new Box(0.6, 0.8, 18);
        hoodStrip.setMaterial(chromeMat);
        hoodStrip.setTranslateX(0);
        hoodStrip.setTranslateY(-21.4);
        hoodStrip.setTranslateZ(31.5);

        // Layered Flame Decals on Hood (Matching Transformers Optimus Prime)
        Box hoodFlameCenter = new Box(8.0, 0.4, 14.0);
        hoodFlameCenter.setMaterial(flameBlueMat);
        hoodFlameCenter.setTranslateX(0);
        hoodFlameCenter.setTranslateY(-21.2);
        hoodFlameCenter.setTranslateZ(31.0);

        Box hoodFlameLeft = new Box(3.5, 0.4, 10.0);
        hoodFlameLeft.setMaterial(flameBlueMat);
        hoodFlameLeft.setTranslateX(-6.2);
        hoodFlameLeft.setTranslateY(-21.2);
        hoodFlameLeft.setTranslateZ(29.5);

        Box hoodFlameRight = new Box(3.5, 0.4, 10.0);
        hoodFlameRight.setMaterial(flameBlueMat);
        hoodFlameRight.setTranslateX(6.2);
        hoodFlameRight.setTranslateY(-21.2);
        hoodFlameRight.setTranslateZ(29.5);

        Box hoodFlameOrange = new Box(5.2, 0.5, 6.0);
        hoodFlameOrange.setMaterial(flameOrangeMat);
        hoodFlameOrange.setTranslateX(0);
        hoodFlameOrange.setTranslateY(-21.3);
        hoodFlameOrange.setTranslateZ(26.5);

        Box hoodFlameYellow = new Box(2.6, 0.6, 3.0);
        hoodFlameYellow.setMaterial(flameYellowMat);
        hoodFlameYellow.setTranslateX(0);
        hoodFlameYellow.setTranslateY(-21.4);
        hoodFlameYellow.setTranslateZ(24.5);

        getChildren().addAll(hood, hoodStrip, hoodFlameCenter, hoodFlameLeft,
                hoodFlameRight, hoodFlameOrange, hoodFlameYellow);

        // 5. Massive Peterbilt Vertical Chrome Radiator Grille & Bumper
        Box grilleSurround = new Box(18.5, 15.5, 2.5);
        grilleSurround.setMaterial(chromeMat);
        grilleSurround.setTranslateY(-14.5);
        grilleSurround.setTranslateZ(41.0);

        Box grilleLouvers = new Box(15.5, 13.0, 0.8);
        grilleLouvers.setMaterial(chromeMat);
        grilleLouvers.setTranslateY(-14.5);
        grilleLouvers.setTranslateZ(42.2);

        // Red Autobot Crest emblem at top of grille
        Box autobotCrest = new Box(2.2, 2.2, 0.6);
        autobotCrest.setMaterial(redHoodMat);
        autobotCrest.setTranslateX(0);
        autobotCrest.setTranslateY(-19.5);
        autobotCrest.setTranslateZ(42.6);

        // Front Heavy Chrome Bumper & Cowcatcher
        Box frontBumper = new Box(33, 5.5, 3.2);
        frontBumper.setMaterial(chromeMat);
        frontBumper.setTranslateY(-6.0);
        frontBumper.setTranslateZ(41.2);

        Box bumperApron = new Box(29, 3.0, 1.8);
        bumperApron.setMaterial(chromeMat);
        bumperApron.setTranslateY(-2.5);
        bumperApron.setTranslateZ(41.2);

        // Dual Chrome Round Headlight Pods
        Cylinder podL = new Cylinder(2.2, 2.6);
        podL.setMaterial(chromeMat);
        podL.getTransforms().add(new Rotate(90, Rotate.X_AXIS));
        podL.setTranslateX(-13.2);
        podL.setTranslateY(-10.5);
        podL.setTranslateZ(39.5);

        Cylinder lensL = new Cylinder(1.8, 0.6);
        lensL.setMaterial(headlightMat);
        lensL.getTransforms().add(new Rotate(90, Rotate.X_AXIS));
        lensL.setTranslateX(-13.2);
        lensL.setTranslateY(-10.5);
        lensL.setTranslateZ(41.0);

        Cylinder podR = new Cylinder(2.2, 2.6);
        podR.setMaterial(chromeMat);
        podR.getTransforms().add(new Rotate(90, Rotate.X_AXIS));
        podR.setTranslateX(13.2);
        podR.setTranslateY(-10.5);
        podR.setTranslateZ(39.5);

        Cylinder lensR = new Cylinder(1.8, 0.6);
        lensR.setMaterial(headlightMat);
        lensR.getTransforms().add(new Rotate(90, Rotate.X_AXIS));
        lensR.setTranslateX(13.2);
        lensR.setTranslateY(-10.5);
        lensR.setTranslateZ(41.0);

        getChildren().addAll(grilleSurround, grilleLouvers, autobotCrest, frontBumper, bumperApron,
                podL, lensL, podR, lensR);

        // 6. Iconic Towering Dual Vertical Chrome Smokestacks (Exhaust Stacks)
        // Left Smokestack
        Cylinder silencerL = new Cylinder(2.0, 16.0);
        silencerL.setMaterial(chromeMat);
        silencerL.setTranslateX(-15.8);
        silencerL.setTranslateY(-19.0);
        silencerL.setTranslateZ(10.0);

        Cylinder pipeL = new Cylinder(1.1, 26.0);
        pipeL.setMaterial(chromeMat);
        pipeL.setTranslateX(-15.8);
        pipeL.setTranslateY(-34.5);
        pipeL.setTranslateZ(10.0);

        Cylinder tipL = new Cylinder(1.15, 2.2);
        tipL.setMaterial(chromeMat);
        tipL.getTransforms().add(new Rotate(15, Rotate.X_AXIS));
        tipL.setTranslateX(-15.8);
        tipL.setTranslateY(-47.0);
        tipL.setTranslateZ(9.8);

        Box bracketL = new Box(2.2, 0.8, 0.8);
        bracketL.setMaterial(chromeMat);
        bracketL.setTranslateX(-14.6);
        bracketL.setTranslateY(-27.0);
        bracketL.setTranslateZ(10.0);

        // Right Smokestack
        Cylinder silencerR = new Cylinder(2.0, 16.0);
        silencerR.setMaterial(chromeMat);
        silencerR.setTranslateX(15.8);
        silencerR.setTranslateY(-19.0);
        silencerR.setTranslateZ(10.0);

        Cylinder pipeR = new Cylinder(1.1, 26.0);
        pipeR.setMaterial(chromeMat);
        pipeR.setTranslateX(15.8);
        pipeR.setTranslateY(-34.5);
        pipeR.setTranslateZ(10.0);

        Cylinder tipR = new Cylinder(1.15, 2.2);
        tipR.setMaterial(chromeMat);
        tipR.getTransforms().add(new Rotate(15, Rotate.X_AXIS));
        tipR.setTranslateX(15.8);
        tipR.setTranslateY(-47.0);
        tipR.setTranslateZ(9.8);

        Box bracketR = new Box(2.2, 0.8, 0.8);
        bracketR.setMaterial(chromeMat);
        bracketR.setTranslateX(14.6);
        bracketR.setTranslateY(-27.0);
        bracketR.setTranslateZ(10.0);

        getChildren().addAll(silencerL, pipeL, tipL, bracketL, silencerR, pipeR, tipR, bracketR);

        // 7. Polished Chrome Cylindrical Fuel Tanks & Driver Steps
        Cylinder tankL = new Cylinder(3.6, 17.0);
        tankL.setMaterial(chromeMat);
        tankL.getTransforms().add(new Rotate(90, Rotate.X_AXIS));
        tankL.setTranslateX(-14.4);
        tankL.setTranslateY(-7.5);
        tankL.setTranslateZ(2.0);

        Box stepL = new Box(4.0, 4.8, 7.5);
        stepL.setMaterial(chromeMat);
        stepL.setTranslateX(-14.5);
        stepL.setTranslateY(-6.8);
        stepL.setTranslateZ(14.5);

        Cylinder tankR = new Cylinder(3.6, 17.0);
        tankR.setMaterial(chromeMat);
        tankR.getTransforms().add(new Rotate(90, Rotate.X_AXIS));
        tankR.setTranslateX(14.4);
        tankR.setTranslateY(-7.5);
        tankR.setTranslateZ(2.0);

        Box stepR = new Box(4.0, 4.8, 7.5);
        stepR.setMaterial(chromeMat);
        stepR.setTranslateX(14.5);
        stepR.setTranslateY(-6.8);
        stepR.setTranslateZ(14.5);

        getChildren().addAll(tankL, stepL, tankR, stepR);

        // 8. Flared Flame Fenders (Front & Rear Tandem)
        Box fenderFrontL = new Box(5.6, 6.0, 16.5);
        fenderFrontL.setMaterial(redHoodMat);
        fenderFrontL.setTranslateX(-13.2);
        fenderFrontL.setTranslateY(-11.5);
        fenderFrontL.setTranslateZ(28.0);

        Box fenderFrontR = new Box(5.6, 6.0, 16.5);
        fenderFrontR.setMaterial(redHoodMat);
        fenderFrontR.setTranslateX(13.2);
        fenderFrontR.setTranslateY(-11.5);
        fenderFrontR.setTranslateZ(28.0);

        // Rear Double/Tandem Fenders with Blue Flame Tops
        Box fenderRearL = new Box(6.0, 6.5, 31.0);
        fenderRearL.setMaterial(redHoodMat);
        fenderRearL.setTranslateX(-14.0);
        fenderRearL.setTranslateY(-11.5);
        fenderRearL.setTranslateZ(-19.5);

        Box rearFlameL = new Box(4.0, 0.5, 20.0);
        rearFlameL.setMaterial(flameBlueMat);
        rearFlameL.setTranslateX(-14.0);
        rearFlameL.setTranslateY(-15.0);
        rearFlameL.setTranslateZ(-19.5);

        Box fenderRearR = new Box(6.0, 6.5, 31.0);
        fenderRearR.setMaterial(redHoodMat);
        fenderRearR.setTranslateX(14.0);
        fenderRearR.setTranslateY(-11.5);
        fenderRearR.setTranslateZ(-19.5);

        Box rearFlameR = new Box(4.0, 0.5, 20.0);
        rearFlameR.setMaterial(flameBlueMat);
        rearFlameR.setTranslateX(14.0);
        rearFlameR.setTranslateY(-15.0);
        rearFlameR.setTranslateZ(-19.5);

        getChildren().addAll(fenderFrontL, fenderFrontR, fenderRearL, rearFlameL, fenderRearR, rearFlameR);

        // 9. 6 Heavy-Duty Wheels with Deep-Dish Polished Chrome Rims
        addOptimusWheel(-14.5, -6.8, 28.0, 6.8, 4.6, true, tireMat, chromeMat);  // Front-Left
        addOptimusWheel(14.5, -6.8, 28.0, 6.8, 4.6, true, tireMat, chromeMat);   // Front-Right
        addOptimusWheel(-14.5, -6.8, -13.0, 6.8, 4.8, false, tireMat, chromeMat); // Tandem Axle 1 Left
        addOptimusWheel(14.5, -6.8, -13.0, 6.8, 4.8, false, tireMat, chromeMat);  // Tandem Axle 1 Right
        addOptimusWheel(-14.5, -6.8, -26.0, 6.8, 4.8, false, tireMat, chromeMat); // Tandem Axle 2 Left
        addOptimusWheel(14.5, -6.8, -26.0, 6.8, 4.8, false, tireMat, chromeMat);  // Tandem Axle 2 Right
    }

    /**
     * Builds heavy-duty truck wheel with deep-dish chrome rims & hub caps.
     */
    private void addOptimusWheel(double x, double y, double z, double r, double w,
                                boolean isFront, PhongMaterial tireMat, PhongMaterial chromeMat) {
        // Outer Rubber Tire
        Cylinder tire = new Cylinder(r, w);
        tire.setMaterial(tireMat);
        tire.getTransforms().add(new Rotate(90, Rotate.Z_AXIS));
        tire.setTranslateX(x);
        tire.setTranslateY(y);
        tire.setTranslateZ(z);

        // Deep-Dish Polished Chrome Rim
        Cylinder rim = new Cylinder(r * 0.72, w + 0.3);
        rim.setMaterial(chromeMat);
        rim.getTransforms().add(new Rotate(90, Rotate.Z_AXIS));
        rim.setTranslateX(x);
        rim.setTranslateY(y);
        rim.setTranslateZ(z);

        // Center Chrome Hub Cap with Lug Nut Ring
        double hubOffset = (x > 0 ? 0.35 : -0.35);
        Cylinder hub = new Cylinder(r * 0.36, w + (isFront ? 1.0 : 0.6));
        hub.setMaterial(chromeMat);
        hub.getTransforms().add(new Rotate(90, Rotate.Z_AXIS));
        hub.setTranslateX(x + hubOffset);
        hub.setTranslateY(y);
        hub.setTranslateZ(z);

        getChildren().addAll(tire, rim, hub);
    }

    private void addWheel(double x, double y, double z, double r, double h, PhongMaterial tireMat, PhongMaterial rimMat) {
        Cylinder tire = new Cylinder(r, h);
        tire.setMaterial(tireMat);
        tire.getTransforms().add(new Rotate(90, Rotate.Z_AXIS));
        tire.setTranslateX(x);
        tire.setTranslateY(y);
        tire.setTranslateZ(z);

        Cylinder rim = new Cylinder(r * 0.55, h + 0.2);
        rim.setMaterial(rimMat);
        rim.getTransforms().add(new Rotate(90, Rotate.Z_AXIS));
        rim.setTranslateX(x);
        rim.setTranslateY(y);
        rim.setTranslateZ(z);

        getChildren().addAll(tire, rim);
    }

    /**
     * Builds floating ID badge plate above the vehicle roof.
     */
    private void buildIdBadge() {
        Group badgeGroup = new Group();

        double badgeHeight = category.equalsIgnoreCase("Truck") ? -52.0 : (category.equalsIgnoreCase("Bike") ? -38.0 : -32.0);

        // Background plate
        Box plate = new Box(26, 8, 1);
        PhongMaterial plateMat = new PhongMaterial(Color.rgb(20, 25, 35, 0.9));
        plate.setMaterial(plateMat);

        // Text display
        Text text = new Text(vehicleId);
        text.setFont(Font.font("Monospaced", FontWeight.BOLD, 7));
        text.setFill(Color.rgb(240, 250, 255));
        text.setTextAlignment(TextAlignment.CENTER);
        text.setTextOrigin(VPos.CENTER);
        text.setTranslateX(-11);

        badgeGroup.getChildren().addAll(plate, text);
        badgeGroup.setTranslateY(badgeHeight);

        // Slight backward tilt so it's readable from overhead camera
        badgeGroup.getTransforms().add(new Rotate(-45, Rotate.X_AXIS));

        getChildren().add(badgeGroup);
    }

    /**
     * Pulsing spotlight aura used when searched by Binary Search.
     */
    private void buildHighlightAura() {
        double radius = category.equalsIgnoreCase("Truck") ? 55 : (category.equalsIgnoreCase("Bike") ? 32 : 42);
        highlightAura = new Box(radius, 2, radius);
        PhongMaterial auraMat = new PhongMaterial(Color.rgb(255, 215, 0, 0.8));
        auraMat.setSpecularColor(Color.YELLOW);
        highlightAura.setMaterial(auraMat);
        highlightAura.setTranslateY(-1);
        highlightAura.setVisible(false);
        getChildren().add(highlightAura);
    }

    /**
     * Updates physics, movement, and yaw angle along the BFS waypoint route.
     * Called every animation frame.
     */
    public boolean updateMovement(double dt) {
        if (path == null || path.isEmpty() || currentTargetIndex >= path.size()) {
            return true; // Path completed
        }

        PathPlanner.Waypoint target = path.get(currentTargetIndex);

        double dx = target.x - currentX;
        double dz = target.z - currentZ;
        double distance = Math.hypot(dx, dz);

        // Calculate target yaw angle
        if (distance > 0.05) {
            double targetYaw;
            // When exiting stall into aisle, keep stall orientation and reverse smoothly
            if (state == State.EXITING && currentTargetIndex <= 1) {
                targetYaw = (assignedCol % 2 == 0) ? -90.0 : 90.0;
                setYaw(targetYaw);
            } else {
                targetYaw = Math.toDegrees(Math.atan2(dx, dz));
                // Smooth yaw rotation
                double diff = normalizeAngle(targetYaw - currentYaw);
                double turnRate = 320.0 * dt; // degrees/sec
                if (Math.abs(diff) < turnRate) {
                    setYaw(targetYaw);
                } else {
                    setYaw(currentYaw + Math.signum(diff) * turnRate);
                }
            }
        }

        // Smooth deceleration when approaching final parking stall or reversing out
        double effectiveSpeed = speed;
        if (state == State.DRIVING_TO_SPOT && currentTargetIndex == path.size() - 1) {
            // Turning into parking bay: slow down to gentle parking speed
            effectiveSpeed = Math.max(45.0, speed * 0.42);
        } else if (state == State.EXITING && currentTargetIndex <= 1) {
            // Reversing out of stall into aisle: gentle reverse speed
            effectiveSpeed = Math.min(65.0, speed * 0.55);
        }

        double step = effectiveSpeed * dt;
        if (distance <= step) {
            // Reached waypoint
            setPosition(target.x, target.y, target.z);
            currentTargetIndex++;

            if (currentTargetIndex >= path.size()) {
                // Final destination reached
                if (state == State.DRIVING_TO_SPOT) {
                    state = State.PARKED;
                    // Snap to parking stall orientation:
                    // Even columns (0, 2, 4) face East (+90 deg), Odd columns face West (-90 deg)
                    if (assignedCol % 2 == 0) {
                        setYaw(-90);
                    } else {
                        setYaw(90);
                    }
                } else if (state == State.EXITING) {
                    state = State.FINISHED;
                }
                return true;
            }
        } else {
            // Move towards waypoint
            double ratio = step / distance;
            setPosition(currentX + dx * ratio, currentY, currentZ + dz * ratio);
        }

        return false;
    }

    private double normalizeAngle(double angle) {
        while (angle > 180) angle -= 360;
        while (angle <= -180) angle += 360;
        return angle;
    }
}
