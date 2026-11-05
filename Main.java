import javax.swing.SwingUtilities;

public class Main {
    public static void main(String[] args) {
        // Run the GUI creation on the Event Dispatch Thread (standard Swing practice)
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new ParkingGUI();
            }
        });
    }
}

