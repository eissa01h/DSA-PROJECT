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
    private JLabel waitingValueLbl; // Day 3: Queue display

    // Vehicle Entry Components
    private JComboBox<String> categoryCombo;
    private JTextField parkIdField;

    // Vehicle Exit Components (Day 3)
    private JTextField exitIdField;

    // Status Message & Stack Display (Day 3)
    private JLabel messageLabel;
    private JLabel stackActivityLabel;

    // 2D Array references for the 30 parking spaces
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
        setSize(1020, 820);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(15, 15));
        getContentPane().setBackground(new Color(245, 247, 250));

        // 1. Top Section: Header + Dashboard + Legend + Actions + Activity Banner
        JPanel topContainer = new JPanel();
        topContainer.setLayout(new BoxLayout(topContainer, BoxLayout.Y_AXIS));
        topContainer.setBackground(new Color(245, 247, 250));
        topContainer.setBorder(new EmptyBorder(12, 20, 5, 20));

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
        topContainer.add(Box.createVerticalStrut(3));
        topContainer.add(subtitleLabel);
        topContainer.add(Box.createVerticalStrut(12));

        // Dashboard displaying 4 Statistics Cards (including Waiting Queue)
        JPanel dashboardPanel = new JPanel(new GridLayout(1, 4, 12, 0));
        dashboardPanel.setBackground(new Color(245, 247, 250));

        totalValueLbl = new JLabel(String.valueOf(manager.getTotalSpaces()));
        occupiedValueLbl = new JLabel(String.valueOf(manager.getOccupiedSpaces()));
        availableValueLbl = new JLabel(String.valueOf(manager.getAvailableSpaces()));
        waitingValueLbl = new JLabel(String.valueOf(manager.getWaitingQueueCount()));

        dashboardPanel.add(createStatCard("Total Spaces", totalValueLbl, new Color(59, 130, 246)));
        dashboardPanel.add(createStatCard("Occupied Spaces", occupiedValueLbl, new Color(239, 68, 68)));
        dashboardPanel.add(createStatCard("Available Spaces", availableValueLbl, new Color(16, 185, 129)));
        dashboardPanel.add(createStatCard("Waiting Queue", waitingValueLbl, new Color(147, 51, 234))); // Purple for Queue

        topContainer.add(dashboardPanel);
        topContainer.add(Box.createVerticalStrut(8));

        // Zone Legend
        JPanel legendPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 0));
        legendPanel.setBackground(new Color(245, 247, 250));
        legendPanel.add(createLegendItem("Bike Zone (Cols 1-2)", new Color(224, 242, 254), new Color(14, 116, 144)));
        legendPanel.add(createLegendItem("Car Zone (Cols 3-4)", new Color(220, 252, 231), new Color(21, 128, 61)));
        legendPanel.add(createLegendItem("Truck Zone (Cols 5-6)", new Color(254, 243, 199), new Color(180, 83, 9)));

        topContainer.add(legendPanel);
        topContainer.add(Box.createVerticalStrut(10));

        // Actions & Controls Card (Entry, Exit & Recent Activity Stack)
        JPanel actionCard = createActionCard();
        topContainer.add(actionCard);

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

    // Creates the Controls Card for Parking, Exiting, and Stack Activity (Day 3)
    private JPanel createActionCard() {
        JPanel card = new JPanel(new BorderLayout(8, 8));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(226, 232, 240), 1, true),
                new EmptyBorder(8, 14, 8, 14)
        ));

        // Top Row: Vehicle Entry and Vehicle Exit side-by-side
        JPanel formsRow = new JPanel(new GridLayout(1, 2, 20, 0));
        formsRow.setBackground(Color.WHITE);

        // 1. Vehicle Entry Form
        JPanel entryPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        entryPanel.setBackground(Color.WHITE);

        JLabel entryTitle = new JLabel("Entry:");
        entryTitle.setFont(new Font("SansSerif", Font.BOLD, 12));
        entryTitle.setForeground(new Color(30, 41, 59));

        String[] categories = {"Bike", "Car", "Truck"};
        categoryCombo = new JComboBox<>(categories);
        categoryCombo.setFont(new Font("SansSerif", Font.PLAIN, 12));
        categoryCombo.setBackground(Color.WHITE);

        parkIdField = new JTextField(7);
        parkIdField.setFont(new Font("SansSerif", Font.PLAIN, 12));
        parkIdField.setText("BK-101");

        JButton parkButton = new JButton("Park Vehicle");
        parkButton.setFont(new Font("SansSerif", Font.BOLD, 11));
        parkButton.setBackground(new Color(37, 99, 235));
        parkButton.setForeground(Color.WHITE);
        parkButton.setFocusPainted(false);
        parkButton.setCursor(new Cursor(Cursor.HAND_CURSOR));

        parkButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                handleParkVehicle();
            }
        });

        parkIdField.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                handleParkVehicle();
            }
        });

        entryPanel.add(entryTitle);
        entryPanel.add(categoryCombo);
        entryPanel.add(parkIdField);
        entryPanel.add(parkButton);

        // 2. Vehicle Exit Form (Day 3)
        JPanel exitPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        exitPanel.setBackground(Color.WHITE);

        JLabel exitTitle = new JLabel("Exit:");
        exitTitle.setFont(new Font("SansSerif", Font.BOLD, 12));
        exitTitle.setForeground(new Color(30, 41, 59));

        exitIdField = new JTextField(8);
        exitIdField.setFont(new Font("SansSerif", Font.PLAIN, 12));

        JButton exitButton = new JButton("Exit Vehicle");
        exitButton.setFont(new Font("SansSerif", Font.BOLD, 11));
        exitButton.setBackground(new Color(220, 38, 38)); // Danger red
        exitButton.setForeground(Color.WHITE);
        exitButton.setFocusPainted(false);
        exitButton.setCursor(new Cursor(Cursor.HAND_CURSOR));

        exitButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                handleExitVehicle();
            }
        });

        exitIdField.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                handleExitVehicle();
            }
        });

        exitPanel.add(exitTitle);
        exitPanel.add(new JLabel("ID:"));
        exitPanel.add(exitIdField);
        exitPanel.add(exitButton);

        formsRow.add(entryPanel);
        formsRow.add(exitPanel);

        // Bottom section: Status Feedback and Recent Activity from Stack
        JPanel infoPanel = new JPanel(new GridLayout(2, 1, 2, 2));
        infoPanel.setBackground(Color.WHITE);

        messageLabel = new JLabel("Ready. Enter vehicle information to park or remove vehicles.");
        messageLabel.setFont(new Font("SansSerif", Font.BOLD, 12));
        messageLabel.setForeground(new Color(71, 85, 105));

        stackActivityLabel = new JLabel("Recent Activity (Stack LIFO): " + manager.getRecentActivity());
        stackActivityLabel.setFont(new Font("SansSerif", Font.PLAIN, 11));
        stackActivityLabel.setForeground(new Color(100, 116, 139));

        infoPanel.add(messageLabel);
        infoPanel.add(stackActivityLabel);

        card.add(formsRow, BorderLayout.NORTH);
        card.add(infoPanel, BorderLayout.SOUTH);

        return card;
    }

    // Handles parking vehicle allocation & waiting queue
    private void handleParkVehicle() {
        String category = (String) categoryCombo.getSelectedItem();
        String vehicleId = parkIdField.getText().trim();

        if (vehicleId.isEmpty()) {
            messageLabel.setText("⚠ Please enter a valid Vehicle ID before parking.");
            messageLabel.setForeground(new Color(220, 38, 38));
            return;
        }

        String result = manager.parkVehicle(category, vehicleId);

        if (result.equals("DUPLICATE")) {
            messageLabel.setText("⚠ Vehicle ID '" + vehicleId + "' is already in the parking lot or waiting queue!");
            messageLabel.setForeground(new Color(220, 38, 38));
        } else if (result.equals("QUEUED")) {
            messageLabel.setText("ⓘ " + category + " Zone is full! Vehicle [" + vehicleId + "] added to Waiting Queue (Waiting: " + manager.getWaitingQueueCount() + ").");
            messageLabel.setForeground(new Color(147, 51, 234)); // Purple for Queue
            parkIdField.setText("");
            refreshGUI();
        } else if (result.equals("INVALID")) {
            messageLabel.setText("⚠ Invalid vehicle category selected.");
            messageLabel.setForeground(new Color(220, 38, 38));
        } else {
            // Successfully allocated
            int distance = manager.getLastAllocatedDistance();
            messageLabel.setText("✓ Success: " + category + " [" + vehicleId + "] parked at space " + result + " (Manhattan Distance: " + distance + ")");
            messageLabel.setForeground(new Color(21, 128, 61));
            parkIdField.setText("");
            parkIdField.requestFocus();
            refreshGUI();
        }
    }

    // Handles vehicle exit and queue allocation (Day 3)
    private void handleExitVehicle() {
        String vehicleId = exitIdField.getText().trim();

        if (vehicleId.isEmpty()) {
            messageLabel.setText("⚠ Please enter a Vehicle ID to remove.");
            messageLabel.setForeground(new Color(220, 38, 38));
            return;
        }

        ParkingManager.ExitResult result = manager.exitVehicle(vehicleId);

        if (result.isSuccess()) {
            messageLabel.setText("✓ " + result.getMessage());
            messageLabel.setForeground(new Color(21, 128, 61));
            exitIdField.setText("");
            refreshGUI();
        } else {
            messageLabel.setText("⚠ " + result.getMessage());
            messageLabel.setForeground(new Color(220, 38, 38));
        }
    }

    // Refresh Dashboard counters, Stack activity, and Parking Space cards
    private void refreshGUI() {
        // Update Dashboard Counters
        occupiedValueLbl.setText(String.valueOf(manager.getOccupiedSpaces()));
        availableValueLbl.setText(String.valueOf(manager.getAvailableSpaces()));
        waitingValueLbl.setText(String.valueOf(manager.getWaitingQueueCount()));

        // Update Stack Recent Activity (LIFO)
        stackActivityLabel.setText("Recent Activity (Stack LIFO): " + manager.getRecentActivity());

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
                new EmptyBorder(8, 12, 8, 12)
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
            bgColor = new Color(224, 242, 254);
            headerColor = new Color(14, 116, 144);
        } else if (zone.contains("Car")) {
            bgColor = new Color(220, 252, 231);
            headerColor = new Color(21, 128, 61);
        } else {
            bgColor = new Color(254, 243, 199);
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
