import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class ParkingGUI extends JFrame {

    private ParkingManager manager;

    // Dashboard value labels for real-time updates
    private JLabel totalValueLbl;
    private JLabel occupiedValueLbl;
    private JLabel availableValueLbl;

    // Vehicle Allocation input components (Day 2)
    private JComboBox<String> categoryCombo;
    private JTextField vehicleIdField;
    private JLabel messageLabel;

    // 2D Array to hold references to the 30 grid card panels and status labels
    private JPanel[][] spotPanels;
    private JLabel[][] spotStatusLabels;

    public ParkingGUI() {
        manager = new ParkingManager();
        spotPanels = new JPanel[manager.getRows()][manager.getCols()];
        spotStatusLabels = new JLabel[manager.getRows()][manager.getCols()];
        initUI();
    }

    private void initUI() {
        // Window Configuration
        setTitle("Smart Parking Finder");
        setSize(980, 780);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(15, 15));
        getContentPane().setBackground(new Color(245, 247, 250));

        // 1. Top Section: Header + Dashboard + Legend + Allocation Controls
        JPanel topContainer = new JPanel();
        topContainer.setLayout(new BoxLayout(topContainer, BoxLayout.Y_AXIS));
        topContainer.setBackground(new Color(245, 247, 250));
        topContainer.setBorder(new EmptyBorder(15, 20, 5, 20));

        // Title Header
        JLabel titleLabel = new JLabel("Smart Parking Finder");
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 22));
        titleLabel.setForeground(new Color(30, 41, 59));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel subtitleLabel = new JLabel("Parking Lot Simulation System");
        subtitleLabel.setFont(new Font("SansSerif", Font.PLAIN, 13));
        subtitleLabel.setForeground(new Color(100, 116, 139));
        subtitleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        topContainer.add(titleLabel);
        topContainer.add(Box.createVerticalStrut(4));
        topContainer.add(subtitleLabel);
        topContainer.add(Box.createVerticalStrut(15));

        // Dashboard displaying Statistics
        JPanel dashboardPanel = new JPanel(new GridLayout(1, 3, 15, 0));
        dashboardPanel.setBackground(new Color(245, 247, 250));

        totalValueLbl = new JLabel(String.valueOf(manager.getTotalSpaces()));
        occupiedValueLbl = new JLabel(String.valueOf(manager.getOccupiedSpaces()));
        availableValueLbl = new JLabel(String.valueOf(manager.getAvailableSpaces()));

        dashboardPanel.add(createStatCard("Total Spaces", totalValueLbl, new Color(59, 130, 246)));
        dashboardPanel.add(createStatCard("Occupied Spaces", occupiedValueLbl, new Color(239, 68, 68)));
        dashboardPanel.add(createStatCard("Available Spaces", availableValueLbl, new Color(16, 185, 129)));

        topContainer.add(dashboardPanel);
        topContainer.add(Box.createVerticalStrut(10));

        // Zone Legend
        JPanel legendPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 0));
        legendPanel.setBackground(new Color(245, 247, 250));
        legendPanel.add(createLegendItem("Bike Zone (Cols 1-2)", new Color(224, 242, 254), new Color(14, 116, 144)));
        legendPanel.add(createLegendItem("Car Zone (Cols 3-4)", new Color(220, 252, 231), new Color(21, 128, 61)));
        legendPanel.add(createLegendItem("Truck Zone (Cols 5-6)", new Color(254, 243, 199), new Color(180, 83, 9)));

        topContainer.add(legendPanel);
        topContainer.add(Box.createVerticalStrut(12));

        // Vehicle Allocation Control Section (Day 2)
        JPanel allocationCard = createAllocationPanel();
        topContainer.add(allocationCard);

        add(topContainer, BorderLayout.NORTH);

        // 2. Center Section: 5x6 Parking Lot Grid (30 Spaces)
        JPanel gridPanel = new JPanel(new GridLayout(manager.getRows(), manager.getCols(), 10, 10));
        gridPanel.setBackground(new Color(245, 247, 250));
        gridPanel.setBorder(new EmptyBorder(5, 20, 15, 20));

        for (int r = 0; r < manager.getRows(); r++) {
            for (int c = 0; c < manager.getCols(); c++) {
                gridPanel.add(createParkingSpaceCard(r, c));
            }
        }

        add(gridPanel, BorderLayout.CENTER);

        setVisible(true);
    }

    // Creates the Vehicle Allocation Input Form (Day 2)
    private JPanel createAllocationPanel() {
        JPanel card = new JPanel(new BorderLayout(10, 8));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(226, 232, 240), 1, true),
                new EmptyBorder(10, 16, 10, 16)
        ));

        // Input Controls Row
        JPanel controlsRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        controlsRow.setBackground(Color.WHITE);

        JLabel catLabel = new JLabel("Category:");
        catLabel.setFont(new Font("SansSerif", Font.BOLD, 12));
        catLabel.setForeground(new Color(51, 65, 85));

        String[] categories = {"Bike", "Car", "Truck"};
        categoryCombo = new JComboBox<>(categories);
        categoryCombo.setFont(new Font("SansSerif", Font.PLAIN, 12));
        categoryCombo.setBackground(Color.WHITE);

        JLabel idLabel = new JLabel("Vehicle ID:");
        idLabel.setFont(new Font("SansSerif", Font.BOLD, 12));
        idLabel.setForeground(new Color(51, 65, 85));

        vehicleIdField = new JTextField(10);
        vehicleIdField.setFont(new Font("SansSerif", Font.PLAIN, 12));
        vehicleIdField.setText("BK-101");

        JButton parkButton = new JButton("Park Vehicle");
        parkButton.setFont(new Font("SansSerif", Font.BOLD, 12));
        parkButton.setBackground(new Color(37, 99, 235)); // Primary blue
        parkButton.setForeground(Color.WHITE);
        parkButton.setFocusPainted(false);
        parkButton.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // Hook up park button action listener
        parkButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                handleParkVehicle();
            }
        });

        // Also allow pressing ENTER inside text field to park
        vehicleIdField.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                handleParkVehicle();
            }
        });

        controlsRow.add(catLabel);
        controlsRow.add(categoryCombo);
        controlsRow.add(Box.createHorizontalStrut(8));
        controlsRow.add(idLabel);
        controlsRow.add(vehicleIdField);
        controlsRow.add(Box.createHorizontalStrut(8));
        controlsRow.add(parkButton);

        // Feedback / Notification Message Label
        messageLabel = new JLabel("Select a vehicle category and enter an ID, then click 'Park Vehicle'.");
        messageLabel.setFont(new Font("SansSerif", Font.BOLD, 12));
        messageLabel.setForeground(new Color(71, 85, 105));

        card.add(controlsRow, BorderLayout.NORTH);
        card.add(messageLabel, BorderLayout.SOUTH);

        return card;
    }

    // Handles the parking logic when user clicks "Park Vehicle"
    private void handleParkVehicle() {
        String category = (String) categoryCombo.getSelectedItem();
        String vehicleId = vehicleIdField.getText().trim();

        // 1. Validation: Empty ID
        if (vehicleId.isEmpty()) {
            messageLabel.setText("⚠ Please enter a valid Vehicle ID before parking.");
            messageLabel.setForeground(new Color(220, 38, 38));
            return;
        }

        // 2. Validation: Already parked in the lot
        if (manager.isVehicleParked(vehicleId)) {
            messageLabel.setText("⚠ Vehicle ID '" + vehicleId + "' is already parked in the lot!");
            messageLabel.setForeground(new Color(220, 38, 38));
            return;
        }

        // 3. Allocate using Linear Search and Manhattan Distance in ParkingManager
        String allocatedSpotId = manager.allocateVehicle(category, vehicleId);

        if (allocatedSpotId != null) {
            int distance = manager.getLastAllocatedDistance();
            messageLabel.setText("✓ Success: " + category + " [" + vehicleId + "] parked at space " + allocatedSpotId + " (Nearest distance from entrance: " + distance + ")");
            messageLabel.setForeground(new Color(21, 128, 61));

            // Clear input field and set focus
            vehicleIdField.setText("");
            vehicleIdField.requestFocus();

            // Refresh statistics and parking grid display
            refreshGUI();
        } else {
            // Zone full
            messageLabel.setText("⚠ No compatible parking space available! The " + category + " Zone is completely full.");
            messageLabel.setForeground(new Color(220, 38, 38));
        }
    }

    // Refresh Dashboard counters and Parking Space cards
    private void refreshGUI() {
        // Update Dashboard Counters
        occupiedValueLbl.setText(String.valueOf(manager.getOccupiedSpaces()));
        availableValueLbl.setText(String.valueOf(manager.getAvailableSpaces()));

        // Update each of the 30 Grid Space Cards
        for (int r = 0; r < manager.getRows(); r++) {
            for (int c = 0; c < manager.getCols(); c++) {
                updateSpotCard(r, c);
            }
        }
    }

    // Updates a single spot card's display
    private void updateSpotCard(int row, int col) {
        String status = manager.getSpotStatus(row, col);
        boolean occupied = manager.isSpotOccupied(row, col);
        String zone = manager.getZone(row, col);

        if (occupied) {
            spotStatusLabels[row][col].setText("[ " + status + " ]");
            spotStatusLabels[row][col].setForeground(new Color(185, 28, 28)); // Dark red text for occupied
            spotPanels[row][col].setBackground(new Color(254, 226, 226));    // Soft red alert background
            spotPanels[row][col].setBorder(BorderFactory.createCompoundBorder(
                    new LineBorder(new Color(239, 68, 68), 1, true),
                    new EmptyBorder(8, 8, 8, 8)
            ));
        } else {
            spotStatusLabels[row][col].setText("[ EMPTY ]");
            spotStatusLabels[row][col].setForeground(new Color(100, 116, 139));
            spotPanels[row][col].setBorder(BorderFactory.createCompoundBorder(
                    new LineBorder(new Color(203, 213, 225), 1, true),
                    new EmptyBorder(8, 8, 8, 8)
            ));

            // Reset to zone color
            if (zone.contains("Bike")) {
                spotPanels[row][col].setBackground(new Color(224, 242, 254));
            } else if (zone.contains("Car")) {
                spotPanels[row][col].setBackground(new Color(220, 252, 231));
            } else {
                spotPanels[row][col].setBackground(new Color(254, 243, 199));
            }
        }
    }

    // Helper to create a Dashboard Stat Card
    private JPanel createStatCard(String title, JLabel valueLbl, Color accentColor) {
        JPanel card = new JPanel(new BorderLayout(5, 5));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(226, 232, 240), 1, true),
                new EmptyBorder(10, 15, 10, 15)
        ));

        JLabel titleLbl = new JLabel(title);
        titleLbl.setFont(new Font("SansSerif", Font.PLAIN, 12));
        titleLbl.setForeground(new Color(100, 116, 139));

        valueLbl.setFont(new Font("SansSerif", Font.BOLD, 22));
        valueLbl.setForeground(accentColor);

        card.add(titleLbl, BorderLayout.NORTH);
        card.add(valueLbl, BorderLayout.CENTER);

        return card;
    }

    // Helper to create Legend Item
    private JPanel createLegendItem(String text, Color bgColor, Color textColor) {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        panel.setBackground(new Color(245, 247, 250));

        JPanel colorBox = new JPanel();
        colorBox.setPreferredSize(new Dimension(16, 16));
        colorBox.setBackground(bgColor);
        colorBox.setBorder(new LineBorder(textColor, 1));

        JLabel label = new JLabel(text);
        label.setFont(new Font("SansSerif", Font.BOLD, 12));
        label.setForeground(textColor);

        panel.add(colorBox);
        panel.add(label);
        return panel;
    }

    // Helper to create each Parking Space UI Card
    private JPanel createParkingSpaceCard(int row, int col) {
        String zone = manager.getZone(row, col);
        String spotId = manager.getSpotId(row, col);
        String status = manager.getSpotStatus(row, col);

        JPanel spotCard = new JPanel(new GridLayout(3, 1, 2, 2));
        spotCard.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(203, 213, 225), 1, true),
                new EmptyBorder(8, 8, 8, 8)
        ));

        // Colors based on parking zone
        Color bgColor;
        Color headerColor;

        if (zone.contains("Bike")) {
            bgColor = new Color(224, 242, 254);      // Soft sky blue
            headerColor = new Color(14, 116, 144);
        } else if (zone.contains("Car")) {
            bgColor = new Color(220, 252, 231);      // Soft mint green
            headerColor = new Color(21, 128, 61);
        } else {
            bgColor = new Color(254, 243, 199);      // Soft amber
            headerColor = new Color(180, 83, 9);
        }

        spotCard.setBackground(bgColor);

        // Spot ID Label (e.g. B1, C1, T1)
        JLabel idLabel = new JLabel(spotId, SwingConstants.CENTER);
        idLabel.setFont(new Font("SansSerif", Font.BOLD, 16));
        idLabel.setForeground(headerColor);

        // Zone Name Label
        JLabel zoneLabel = new JLabel(zone, SwingConstants.CENTER);
        zoneLabel.setFont(new Font("SansSerif", Font.PLAIN, 11));
        zoneLabel.setForeground(new Color(71, 85, 105));

        // Status Label ("EMPTY" or Vehicle ID)
        JLabel statusLabel = new JLabel("[ " + status + " ]", SwingConstants.CENTER);
        statusLabel.setFont(new Font("SansSerif", Font.BOLD, 12));
        statusLabel.setForeground(new Color(100, 116, 139));

        spotCard.add(idLabel);
        spotCard.add(zoneLabel);
        spotCard.add(statusLabel);

        // Store references in 2D arrays for easy dynamic updates
        spotPanels[row][col] = spotCard;
        spotStatusLabels[row][col] = statusLabel;

        return spotCard;
    }
}
