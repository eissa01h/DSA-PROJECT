public class Main {
    public static void main(String[] args) {
        // Optional flag to launch the legacy 2D Swing GUI if requested
        if (args.length > 0 && (args[0].equalsIgnoreCase("--2d") || args[0].equalsIgnoreCase("--swing"))) {
            launch2D();
            return;
        }

        // Default: Attempt to launch the full 3D JavaFX Parking Simulation
        try {
            Class<?> fxAppClass = Class.forName("javafx.application.Application");
            Class<?> appClass = Class.forName("Parking3DApp");
            java.lang.reflect.Method launchMethod = fxAppClass.getMethod("launch", Class.class, String[].class);
            launchMethod.invoke(null, appClass, args);
        } catch (ClassNotFoundException | NoClassDefFoundError | NoSuchMethodException e) {
            // JavaFX runtime modules were not loaded in the current command line
            System.out.println("===============================================================================");
            System.out.println("  [INFO] JavaFX 3D runtime modules were not detected on the module-path.");
            System.out.println("  -> To launch the full 3D Simulation, run either:");
            System.out.println("       .\\run3d.bat        (in Command Prompt)");
            System.out.println("       .\\run3d.ps1        (in PowerShell)");
            System.out.println("       or click 'Run' in VS Code (preconfigured launch profile)");
            System.out.println("  -> Launching the 2D Swing Interface for you now...");
            System.out.println("===============================================================================");
            launch2D();
        } catch (Exception e) {
            System.err.println("Error launching application: " + e.getMessage());
            e.printStackTrace();
            launch2D();
        }
    }

    private static void launch2D() {
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new ParkingGUI();
            }
        });
    }
}
