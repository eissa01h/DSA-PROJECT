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
            buildCarModel();
        }
    }

    /**
     * Realistic Car: Colored chassis, cabin roof, tinted glass windshield/windows,
     * headlights, taillights, and 4 wheels with hubcaps.
     */
    private void buildCarModel() {
        PhongMaterial bodyMat = new PhongMaterial(primaryColor);
        bodyMat.setSpecularColor(Color.WHITE);
        bodyMat.setSpecularPower(25.0);

        PhongMaterial glassMat = new PhongMaterial(Color.rgb(30, 45, 60));
        glassMat.setSpecularColor(Color.rgb(200, 220, 240));

        PhongMaterial tireMat = new PhongMaterial(Color.rgb(25, 25, 25));
        PhongMaterial rimMat = new PhongMaterial(Color.LIGHTGRAY);
        PhongMaterial lightMat = new PhongMaterial(Color.rgb(255, 255, 180));
        PhongMaterial tailMat = new PhongMaterial(Color.rgb(230, 30, 30));

        // 1. Lower Chassis (W: 26, H: 8, D: 48)
        Box chassis = new Box(26, 8, 48);
        chassis.setMaterial(bodyMat);
        chassis.setTranslateY(-9); // sits above ground
        getChildren().add(chassis);

        // 2. Cabin / Roof (W: 22, H: 8, D: 26)
        Box cabin = new Box(22, 8, 26);
        cabin.setMaterial(bodyMat);
        cabin.setTranslateY(-17);
        cabin.setTranslateZ(-2);
        getChildren().add(cabin);

        // 3. Front Windshield (W: 20, H: 7, D: 2)
        Box frontWindshield = new Box(20, 7, 2);
        frontWindshield.setMaterial(glassMat);
        frontWindshield.setTranslateY(-16.5);
        frontWindshield.setTranslateZ(11.5);
        getChildren().add(frontWindshield);

        // 4. Rear Window (W: 20, H: 7, D: 2)
        Box rearWindow = new Box(20, 7, 2);
        rearWindow.setMaterial(glassMat);
        rearWindow.setTranslateY(-16.5);
        rearWindow.setTranslateZ(-15.5);
        getChildren().add(rearWindow);

        // 5. Side Windows
        Box leftWindow = new Box(1.5, 6, 20);
        leftWindow.setMaterial(glassMat);
        leftWindow.setTranslateX(-10.5);
        leftWindow.setTranslateY(-16.5);
        leftWindow.setTranslateZ(-2);

        Box rightWindow = new Box(1.5, 6, 20);
        rightWindow.setMaterial(glassMat);
        rightWindow.setTranslateX(10.5);
        rightWindow.setTranslateY(-16.5);
        rightWindow.setTranslateZ(-2);

        getChildren().addAll(leftWindow, rightWindow);

        // 6. Headlights (front +Z)
        Box hl1 = new Box(4, 3, 1.5);
        hl1.setMaterial(lightMat);
        hl1.setTranslateX(-8);
        hl1.setTranslateY(-9);
        hl1.setTranslateZ(24.5);

        Box hl2 = new Box(4, 3, 1.5);
        hl2.setMaterial(lightMat);
        hl2.setTranslateX(8);
        hl2.setTranslateY(-9);
        hl2.setTranslateZ(24.5);

        // 7. Taillights (back -Z)
        Box tl1 = new Box(5, 3, 1.5);
        tl1.setMaterial(tailMat);
        tl1.setTranslateX(-8);
        tl1.setTranslateY(-9);
        tl1.setTranslateZ(-24.5);

        Box tl2 = new Box(5, 3, 1.5);
        tl2.setMaterial(tailMat);
        tl2.setTranslateX(8);
        tl2.setTranslateY(-9);
        tl2.setTranslateZ(-24.5);

        getChildren().addAll(hl1, hl2, tl1, tl2);

        // 8. Front Grille, Side Mirrors & Bumpers
        Box grille = new Box(16, 4, 1.2);
        grille.setMaterial(rimMat);
        grille.setTranslateY(-9);
        grille.setTranslateZ(24.6);

        Box mirrorL = new Box(3.5, 2.0, 1.5);
        mirrorL.setMaterial(bodyMat);
        mirrorL.setTranslateX(-13.5);
        mirrorL.setTranslateY(-15);
        mirrorL.setTranslateZ(5);

        Box mirrorR = new Box(3.5, 2.0, 1.5);
        mirrorR.setMaterial(bodyMat);
        mirrorR.setTranslateX(13.5);
        mirrorR.setTranslateY(-15);
        mirrorR.setTranslateZ(5);

        Box frontBumper = new Box(28, 4, 2.5);
        frontBumper.setMaterial(tireMat);
        frontBumper.setTranslateY(-5);
        frontBumper.setTranslateZ(24.5);

        Box rearBumper = new Box(28, 4, 2.5);
        rearBumper.setMaterial(tireMat);
        rearBumper.setTranslateY(-5);
        rearBumper.setTranslateZ(-24.5);

        getChildren().addAll(grille, mirrorL, mirrorR, frontBumper, rearBumper);

        // 9. 4 Wheels (radius: 4.8, thickness: 3.5)
        addWheel(-13.5, -4.8, 14, 4.8, 3.5, tireMat, rimMat);  // Front-Left
        addWheel(13.5, -4.8, 14, 4.8, 3.5, tireMat, rimMat);   // Front-Right
        addWheel(-13.5, -4.8, -14, 4.8, 3.5, tireMat, rimMat); // Rear-Left
        addWheel(13.5, -4.8, -14, 4.8, 3.5, tireMat, rimMat);  // Rear-Right
    }

    /**
     * Realistic Motorcycle / Bike: Slim chassis, fuel tank, leather seat,
     * handlebars, front fork, chrome exhaust, and 2 slim wheels with spokes.
     */
    private void buildBikeModel() {
        PhongMaterial bodyMat = new PhongMaterial(primaryColor);
        bodyMat.setSpecularColor(Color.WHITE);

        PhongMaterial seatMat = new PhongMaterial(Color.rgb(30, 30, 30));
        PhongMaterial chromeMat = new PhongMaterial(Color.rgb(210, 215, 225));
        chromeMat.setSpecularColor(Color.WHITE);
        chromeMat.setSpecularPower(40);

        PhongMaterial tireMat = new PhongMaterial(Color.rgb(20, 20, 20));
        PhongMaterial lightMat = new PhongMaterial(Color.rgb(255, 255, 200));

        // 1. Central Engine / Frame (W: 6, H: 8, D: 18)
        Box frame = new Box(6, 8, 18);
        frame.setMaterial(chromeMat);
        frame.setTranslateY(-8);
        getChildren().add(frame);

        // 2. Colored Fuel Tank (W: 8, H: 5, D: 10)
        Box tank = new Box(8, 5, 10);
        tank.setMaterial(bodyMat);
        tank.setTranslateY(-13);
        tank.setTranslateZ(2);
        getChildren().add(tank);

        // 3. Black Seat (W: 7, H: 3, D: 9)
        Box seat = new Box(7, 3, 9);
        seat.setMaterial(seatMat);
        seat.setTranslateY(-13);
        seat.setTranslateZ(-6);
        getChildren().add(seat);

        // 4. Handlebars (cross tube)
        Cylinder handlebar = new Cylinder(1.2, 14);
        handlebar.setMaterial(chromeMat);
        handlebar.getTransforms().add(new Rotate(90, Rotate.Z_AXIS));
        handlebar.setTranslateY(-16.5);
        handlebar.setTranslateZ(7);
        getChildren().add(handlebar);

        // 5. Round Headlight
        Sphere headlight = new Sphere(2.4);
        headlight.setMaterial(lightMat);
        headlight.setTranslateY(-13);
        headlight.setTranslateZ(11);
        getChildren().add(headlight);

        // 6. Chrome Exhaust Pipe along right side
        Cylinder exhaust = new Cylinder(1.2, 14);
        exhaust.setMaterial(chromeMat);
        exhaust.getTransforms().add(new Rotate(90, Rotate.X_AXIS));
        exhaust.setTranslateX(4.2);
        exhaust.setTranslateY(-5);
        exhaust.setTranslateZ(-4);

        // Front Steering Fork
        Cylinder forkL = new Cylinder(0.8, 12);
        forkL.setMaterial(chromeMat);
        forkL.getTransforms().add(new Rotate(15, Rotate.X_AXIS));
        forkL.setTranslateX(-1.5);
        forkL.setTranslateY(-9.5);
        forkL.setTranslateZ(8.5);

        Cylinder forkR = new Cylinder(0.8, 12);
        forkR.setMaterial(chromeMat);
        forkR.getTransforms().add(new Rotate(15, Rotate.X_AXIS));
        forkR.setTranslateX(1.5);
        forkR.setTranslateY(-9.5);
        forkR.setTranslateZ(8.5);

        // Rear Red Taillight
        Box bikeTail = new Box(3, 2, 1);
        bikeTail.setMaterial(new PhongMaterial(Color.rgb(230, 30, 30)));
        bikeTail.setTranslateY(-12);
        bikeTail.setTranslateZ(-11.5);

        getChildren().addAll(exhaust, forkL, forkR, bikeTail);

        // 7. Two Large Slim Wheels (radius: 6.0, thickness: 2.2)
        addWheel(0, -6.0, 11, 6.0, 2.2, tireMat, chromeMat);  // Front
        addWheel(0, -6.0, -11, 6.0, 2.2, tireMat, chromeMat); // Rear
    }

    /**
     * Realistic Truck: Heavy dual chassis, tall driver cabin with tinted windows,
     * large cargo container box, front grille, vertical smokestacks, and 6 heavy-duty wheels.
     */
    private void buildTruckModel() {
        PhongMaterial cabMat = new PhongMaterial(primaryColor);
        cabMat.setSpecularColor(Color.WHITE);

        PhongMaterial cargoMat = new PhongMaterial(Color.rgb(235, 240, 245));
        cargoMat.setSpecularColor(Color.LIGHTGRAY);

        PhongMaterial chassisMat = new PhongMaterial(Color.rgb(45, 45, 50));
        PhongMaterial glassMat = new PhongMaterial(Color.rgb(25, 40, 55));
        PhongMaterial chromeMat = new PhongMaterial(Color.rgb(210, 215, 225));
        PhongMaterial tireMat = new PhongMaterial(Color.rgb(20, 20, 20));
        PhongMaterial lightMat = new PhongMaterial(Color.rgb(255, 255, 180));

        // 1. Long Chassis Bed (W: 30, H: 7, D: 76)
        Box chassis = new Box(30, 7, 76);
        chassis.setMaterial(chassisMat);
        chassis.setTranslateY(-9);
        getChildren().add(chassis);

        // 2. Driver Cab in front (W: 32, H: 22, D: 24)
        Box cab = new Box(32, 22, 24);
        cab.setMaterial(cabMat);
        cab.setTranslateY(-21);
        cab.setTranslateZ(24);
        getChildren().add(cab);

        // 3. Cab Windshield (W: 28, H: 9, D: 2)
        Box windshield = new Box(28, 9, 2);
        windshield.setMaterial(glassMat);
        windshield.setTranslateY(-25);
        windshield.setTranslateZ(36.5);
        getChildren().add(windshield);

        // 4. Large Cargo Container Body (W: 34, H: 26, D: 46)
        Box cargoBox = new Box(34, 26, 46);
        cargoBox.setMaterial(cargoMat);
        cargoBox.setTranslateY(-23);
        cargoBox.setTranslateZ(-12);
        getChildren().add(cargoBox);

        // 5. Front Chrome Grille & Bumpers
        Box grille = new Box(24, 7, 2);
        grille.setMaterial(chromeMat);
        grille.setTranslateY(-11);
        grille.setTranslateZ(36.5);
        getChildren().add(grille);

        // 6. Dual Headlights
        Box hl1 = new Box(4.5, 3.5, 1.5);
        hl1.setMaterial(lightMat);
        hl1.setTranslateX(-11);
        hl1.setTranslateY(-11);
        hl1.setTranslateZ(36.5);

        Box hl2 = new Box(4.5, 3.5, 1.5);
        hl2.setMaterial(lightMat);
        hl2.setTranslateX(11);
        hl2.setTranslateY(-11);
        hl2.setTranslateZ(36.5);
        getChildren().addAll(hl1, hl2);

        // 7. Dual Chrome Smokestacks behind cab
        Cylinder stack1 = new Cylinder(1.4, 26);
        stack1.setMaterial(chromeMat);
        stack1.setTranslateX(-15);
        stack1.setTranslateY(-26);
        stack1.setTranslateZ(11);

        Cylinder stack2 = new Cylinder(1.4, 26);
        stack2.setMaterial(chromeMat);
        stack2.setTranslateX(15);
        stack2.setTranslateY(-26);
        stack2.setTranslateZ(11);

        // Cab Door Side Windows
        Box cabWinL = new Box(1.5, 8, 14);
        cabWinL.setMaterial(glassMat);
        cabWinL.setTranslateX(-16);
        cabWinL.setTranslateY(-24);
        cabWinL.setTranslateZ(24);

        Box cabWinR = new Box(1.5, 8, 14);
        cabWinR.setMaterial(glassMat);
        cabWinR.setTranslateX(16);
        cabWinR.setTranslateY(-24);
        cabWinR.setTranslateZ(24);

        // Cab Roof Amber Clearance Lights
        PhongMaterial amberMat = new PhongMaterial(Color.rgb(245, 158, 11));
        amberMat.setSpecularColor(Color.YELLOW);
        Sphere mark1 = new Sphere(1.5);
        mark1.setMaterial(amberMat);
        mark1.setTranslateX(-9);
        mark1.setTranslateY(-33);
        mark1.setTranslateZ(34);

        Sphere mark2 = new Sphere(1.5);
        mark2.setMaterial(amberMat);
        mark2.setTranslateX(0);
        mark2.setTranslateY(-33);
        mark2.setTranslateZ(34);

        Sphere mark3 = new Sphere(1.5);
        mark3.setMaterial(amberMat);
        mark3.setTranslateX(9);
        mark3.setTranslateY(-33);
        mark3.setTranslateZ(34);

        // Front Heavy-Duty Bumper
        Box truckBumper = new Box(34, 5, 3);
        truckBumper.setMaterial(tireMat);
        truckBumper.setTranslateY(-5);
        truckBumper.setTranslateZ(37);

        getChildren().addAll(stack1, stack2, cabWinL, cabWinR, mark1, mark2, mark3, truckBumper);

        // 8. 6 Heavy Duty Wheels (radius: 6.8, thickness: 4.5)
        addWheel(-16, -6.8, 24, 6.8, 4.5, tireMat, chromeMat);  // Front-Left
        addWheel(16, -6.8, 24, 6.8, 4.5, tireMat, chromeMat);   // Front-Right
        addWheel(-16, -6.8, -12, 6.8, 4.5, tireMat, chromeMat); // Mid-Left
        addWheel(16, -6.8, -12, 6.8, 4.5, tireMat, chromeMat);  // Mid-Right
        addWheel(-16, -6.8, -26, 6.8, 4.5, tireMat, chromeMat); // Rear-Left
        addWheel(16, -6.8, -26, 6.8, 4.5, tireMat, chromeMat);  // Rear-Right
    }

    private void addWheel(double x, double y, double z, double r, double h, PhongMaterial tireMat, PhongMaterial rimMat) {
        Cylinder tire = new Cylinder(r, h);
        tire.setMaterial(tireMat);
        tire.getTransforms().add(new Rotate(90, Rotate.Z_AXIS));
        tire.setTranslateX(x);
        tire.setTranslateY(y);
        tire.setTranslateZ(z);

        // Small silver rim center
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

        double badgeHeight = category.equalsIgnoreCase("Truck") ? -40.0 : (category.equalsIgnoreCase("Bike") ? -22.0 : -27.0);

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
            double targetYaw = Math.toDegrees(Math.atan2(dx, dz));
            // Smooth yaw rotation
            double diff = normalizeAngle(targetYaw - currentYaw);
            double turnRate = 320.0 * dt; // degrees/sec
            if (Math.abs(diff) < turnRate) {
                setYaw(targetYaw);
            } else {
                setYaw(currentYaw + Math.signum(diff) * turnRate);
            }
        }

        // Smooth deceleration when approaching final parking stall
        double effectiveSpeed = speed;
        if (state == State.DRIVING_TO_SPOT && currentTargetIndex == path.size() - 1) {
            // Turning into parking bay: slow down to gentle parking speed
            effectiveSpeed = Math.max(45.0, speed * 0.42);
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
