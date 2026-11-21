public class Main {
    public static void main(String[] args) {
        // Optional flag to launch the legacy 2D Swing GUI if requested
        if (args.length > 0 && (args[0].equalsIgnoreCase("--2d") || args[0].equalsIgnoreCase("--swing"))) {
            javax.swing.SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    new ParkingGUI();
                }
            });
        } else {
            // Default: Launch the full 3D JavaFX Parking Simulation
            javafx.application.Application.launch(Parking3DApp.class, args);
        }
    }
}
