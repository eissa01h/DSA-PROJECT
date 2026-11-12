import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

public class ParkingGUI extends JFrame {

    private ParkingManager manager;

    // Dashboard value labels for real-time updates
    private JLabel totalValueLbl;
    private JLabel occupiedValueLbl;
    private JLabel availableValueLbl;
    private JLabel waitingValueLbl;

    // Vehicle Entry Components
    private JComboBox<String> categoryCombo;
    private JTextField parkIdField;

    // Vehicle Exit Components
    private JTextField exitIdField;

    // Search Component (Day 4)
    private JTextField searchIdField;

    // Status Message & Stack Activity Display
    private JLabel messageLabel;
    private JLabel stackActivityLabel;

    // 2D Array references for the 30 parking spaces
    private JPanel[][] spotPanels;
    private JLabel[][] spotStatusLabels;

    // Highlighting for searched vehicle (Day 4)
    private int highlightedRow = -1;
    private int highlightedCol = -1;

    public ParkingGUI() {
        manager = new ParkingManager();
        spotPanels = new JPanel[manager.getRows()][manager.getCols()];
        spotStatusLabels = new JLabel[manager.getRows()][manager.getCols()];
        initUI();
    }

    private void initUI() {
        // Window Configuration
        setTitle("Smart Parking Finder");
        setSize(1040, 850);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(15, 12));
        getContentPane().setBackground(new Color(245, 247, 250));

        // 1. Top Section: Header + Dashboard + Legend + Control Toolbar + Activity Banner
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
        topContainer.add(Box.createVerticalStrut(10));

        // Dashboard displaying 4 Statistics Cards
        JPanel dashboardPanel = new JPanel(new GridLayout(1, 4, 12, 0));
        dashboardPanel.setBackground(new Color(245, 247, 250));

        totalValueLbl = new JLabel(String.valueOf(manager.getTotalSpaces()));
        occupiedValueLbl = new JLabel(String.valueOf(manager.getOccupiedSpaces()));
        availableValueLbl = new JLabel(String.valueOf(manager.getAvailableSpaces()));
        waitingValueLbl = new JLabel(String.valueOf(manager.getWaitingQueueCount()));

        dashboardPanel.add(createStatCard("Total Spaces", totalValueLbl, new Color(59, 130, 246)));
        dashboardPanel.add(createStatCard("Occupied Spaces", occupiedValueLbl, new Color(239, 68, 68)));
        dashboardPanel.add(createStatCard("Available Spaces", availableValueLbl, new Color(16, 185, 129)));
        dashboardPanel.add(createStatCard("Waiting Queue", waitingValueLbl, new Color(147, 51, 234)));

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

        // Operation Controls Card (Park, Exit, Search & Selection Sort)
        JPanel operationsCard = createOperationsCard();
        topContainer.add(operationsCard);

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

    // Creates the unified Operations Card with Park, Exit, Search & Sort
    private JPanel createOperationsCard() {
        JPanel card = new JPanel(new BorderLayout(8, 8));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(226, 232, 240), 1, true),
                new EmptyBorder(10, 14, 10, 14)
        ));

        // Top Toolbar: 3 Columns (Park, Exit, Search/Sort)
        JPanel toolbar = new JPanel(new GridLayout(1, 3, 14, 0));
        toolbar.setBackground(Color.WHITE);

        // 1. PARK SECTION
        JPanel parkSection = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        parkSection.setBackground(Color.WHITE);

        JLabel parkTitle = new JLabel("Park:");
        parkTitle.setFont(new Font("SansSerif", Font.BOLD, 12));
        parkTitle.setForeground(new Color(30, 41, 59));

        String[] categories = {"Bike", "Car", "Truck"};
        categoryCombo = new JComboBox<>(categories);
        categoryCombo.setFont(new Font("SansSerif", Font.PLAIN, 12));
        categoryCombo.setBackground(Color.WHITE);

        parkIdField = new JTextField(6);
        parkIdField.setFont(new Font("SansSerif", Font.PLAIN, 12));
        parkIdField.setText("BK-101");

        JButton parkButton = createStyledButton("Park", new Color(37, 99, 235));
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

        parkSection.add(parkTitle);
        parkSection.add(categoryCombo);
        parkSection.add(parkIdField);
        parkSection.add(parkButton);

        // 2. EXIT SECTION
        JPanel exitSection = new JPanel(new FlowLayout(FlowLayout.CENTER, 6, 0));
        exitSection.setBackground(Color.WHITE);

        JLabel exitTitle = new JLabel("Exit:");
        exitTitle.setFont(new Font("SansSerif", Font.BOLD, 12));
        exitTitle.setForeground(new Color(30, 41, 59));

        exitIdField = new JTextField(7);
        exitIdField.setFont(new Font("SansSerif", Font.PLAIN, 12));

        JButton exitButton = createStyledButton("Exit", new Color(220, 38, 38));
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

        exitSection.add(exitTitle);
        exitSection.add(new JLabel("ID:"));
        exitSection.add(exitIdField);
        exitSection.add(exitButton);

        // 3. SEARCH & SELECTION SORT SECTION (Day 4)
        JPanel searchSection = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        searchSection.setBackground(Color.WHITE);

        JLabel searchTitle = new JLabel("Find:");
        searchTitle.setFont(new Font("SansSerif", Font.BOLD, 12));
        searchTitle.setForeground(new Color(30, 41, 59));

        searchIdField = new JTextField(7);
        searchIdField.setFont(new Font("SansSerif", Font.PLAIN, 12));

        JButton searchButton = createStyledButton("Search", new Color(13, 148, 136)); // Teal
        searchButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                handleSearchVehicle();
            }
        });
        searchIdField.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                handleSearchVehicle();
            }
        });

        JButton sortButton = createStyledButton("Sorted List", new Color(79, 70, 229)); // Indigo
        sortButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                handleShowSortedRecords();
            }
        });

        searchSection.add(searchTitle);
        searchSection.add(searchIdField);
        searchSection.add(searchButton);
        searchSection.add(sortButton);

        toolbar.add(parkSection);
        toolbar.add(exitSection);
        toolbar.add(searchSection);

        // Bottom Feedback Bar: Status Message & Stack History
        JPanel feedbackPanel = new JPanel(new GridLayout(2, 1, 2, 2));
        feedbackPanel.setBackground(Color.WHITE);

        messageLabel = new JLabel("Ready. Use Park, Exit, or Search above.");
        messageLabel.setFont(new Font("SansSerif", Font.BOLD, 12));
        messageLabel.setForeground(new Color(71, 85, 105));

        stackActivityLabel = new JLabel("Recent Activity (Stack LIFO): " + manager.getRecentActivity());
        stackActivityLabel.setFont(new Font("SansSerif", Font.PLAIN, 11));
        stackActivityLabel.setForeground(new Color(100, 116, 139));

        feedbackPanel.add(messageLabel);
        feedbackPanel.add(stackActivityLabel);

        card.add(toolbar, BorderLayout.NORTH);
        card.add(feedbackPanel, BorderLayout.SOUTH);

        return card;
    }

    // Button Styling Helper
    private JButton createStyledButton(String text, Color bg) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("SansSerif", Font.BOLD, 11));
        btn.setBackground(bg);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }

    // Handles Vehicle Parking
    private void handleParkVehicle() {
        clearHighlight();
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
            messageLabel.setForeground(new Color(147, 51, 234));
            parkIdField.setText("");
            refreshGUI();
        } else if (result.equals("INVALID")) {
            messageLabel.setText("⚠ Invalid vehicle category selected.");
            messageLabel.setForeground(new Color(220, 38, 38));
        } else {
            int distance = manager.getLastAllocatedDistance();
            messageLabel.setText("✓ Success: " + category + " [" + vehicleId + "] parked at space " + result + " (Manhattan Distance: " + distance + ")");
            messageLabel.setForeground(new Color(21, 128, 61));
            parkIdField.setText("");
            parkIdField.requestFocus();
            refreshGUI();
        }
    }

    // Handles Vehicle Exit
    private void handleExitVehicle() {
        clearHighlight();
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

    // Handles Vehicle Search (Day 4: Binary Search on Sorted Records)
    private void handleSearchVehicle() {
        clearHighlight();
        String searchId = searchIdField.getText().trim();

        if (searchId.isEmpty()) {
            messageLabel.setText("⚠ Please enter a Vehicle ID to search.");
            messageLabel.setForeground(new Color(220, 38, 38));
            return;
        }

        // 1. Search in parked vehicles using Binary Search
        ParkingManager.ParkingRecord record = manager.searchVehicle(searchId);

        if (record != null) {
            highlightedRow = record.getRow();
            highlightedCol = record.getCol();
            updateSpotCard(highlightedRow, highlightedCol);

            messageLabel.setText("✓ Found: " + record.getCategory() + " [" + record.getVehicleId() 
                    + "] is currently parked at space " + record.getSpotId() + " (Row " + (highlightedRow + 1) + ", Col " + (highlightedCol + 1) + ")!");
            messageLabel.setForeground(new Color(13, 148, 136));
            return;
        }

        // 2. Check if vehicle is in the Waiting Queue
        int queuePos = manager.getWaitingPosition(searchId);
        if (queuePos != -1) {
            messageLabel.setText("ⓘ Vehicle [" + searchId + "] is not parked yet — it is currently waiting in the Queue (Position #" + queuePos + ").");
            messageLabel.setForeground(new Color(147, 51, 234));
            return;
        }

        // 3. Not found anywhere
        messageLabel.setText("⚠ Vehicle ID '" + searchId + "' was not found in the parking lot or waiting queue.");
        messageLabel.setForeground(new Color(220, 38, 38));
    }

    // Handles displaying Selection Sorted Records (Day 4)
    private void handleShowSortedRecords() {
        List<ParkingManager.ParkingRecord> sortedList = manager.getSortedOccupiedRecords();

        if (sortedList.isEmpty()) {
            JOptionPane.showMessageDialog(this, 
                    "No vehicles are currently parked in the parking lot.", 
                    "Sorted Records (Selection Sort)", 
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        StringBuilder sb = new StringBuilder();
        sb.append("Occupied Parking Records (Sorted Alphabetically by Vehicle ID):\n");
        sb.append("----------------------------------------------------------------------\n");
        int index = 1;
        for (ParkingManager.ParkingRecord r : sortedList) {
            sb.append(String.format("%2d. Vehicle ID: %-10s | Category: %-6s | Space: %s\n", 
                    index++, r.getVehicleId(), r.getCategory(), r.getSpotId()));
        }
        sb.append("----------------------------------------------------------------------\n");
        sb.append("Total Occupied: ").append(sortedList.size()).append(" spaces.\n");
        sb.append("Sorted using: Selection Sort Algorithm O(N^2)");

        JTextArea textArea = new JTextArea(sb.toString());
        textArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        textArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setPreferredSize(new Dimension(460, 240));

        JOptionPane.showMessageDialog(this, scrollPane, "Sorted Parking Records (Selection Sort)", JOptionPane.PLAIN_MESSAGE);
    }

    // Clears any highlighted search spot
    private void clearHighlight() {
        if (highlightedRow != -1 && highlightedCol != -1) {
            int oldR = highlightedRow;
            int oldC = highlightedCol;
            highlightedRow = -1;
            highlightedCol = -1;
            updateSpotCard(oldR, oldC);
        }
    }

    // Refresh Dashboard counters, Stack activity, and Parking Space cards
    private void refreshGUI() {
        occupiedValueLbl.setText(String.valueOf(manager.getOccupiedSpaces()));
        availableValueLbl.setText(String.valueOf(manager.getAvailableSpaces()));
        waitingValueLbl.setText(String.valueOf(manager.getWaitingQueueCount()));
        stackActivityLabel.setText("Recent Activity (Stack LIFO): " + manager.getRecentActivity());

        for (int r = 0; r < manager.getRows(); r++) {
            for (int c = 0; c < manager.getCols(); c++) {
                updateSpotCard(r, c);
            }
        }
    }

    // Updates a single spot card's display (including search highlight)
    private void updateSpotCard(int row, int col) {
        String status = manager.getSpotStatus(row, col);
        boolean occupied = manager.isSpotOccupied(row, col);
        String zone = manager.getZone(row, col);

        // Highlight searched spot if matched
        if (row == highlightedRow && col == highlightedCol) {
            spotStatusLabels[row][col].setText("[ " + status + " ★ ]");
            spotStatusLabels[row][col].setForeground(new Color(180, 83, 9)); // Amber
            spotPanels[row][col].setBackground(new Color(254, 240, 138));    // Bright yellow highlight
            spotPanels[row][col].setBorder(BorderFactory.createCompoundBorder(
                    new LineBorder(new Color(234, 179, 8), 2, true),
                    new EmptyBorder(7, 7, 7, 7)
            ));
            return;
        }

        if (occupied) {
            spotStatusLabels[row][col].setText("[ " + status + " ]");
            spotStatusLabels[row][col].setForeground(new Color(185, 28, 28)); // Dark red text
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

        JLabel idLabel = new JLabel(spotId, SwingConstants.CENTER);
        idLabel.setFont(new Font("SansSerif", Font.BOLD, 16));
        idLabel.setForeground(headerColor);

        JLabel zoneLabel = new JLabel(zone, SwingConstants.CENTER);
        zoneLabel.setFont(new Font("SansSerif", Font.PLAIN, 11));
        zoneLabel.setForeground(new Color(71, 85, 105));

        JLabel statusLabel = new JLabel("[ " + status + " ]", SwingConstants.CENTER);
        statusLabel.setFont(new Font("SansSerif", Font.BOLD, 12));
        statusLabel.setForeground(new Color(100, 116, 139));

        spotCard.add(idLabel);
        spotCard.add(zoneLabel);
        spotCard.add(statusLabel);

        spotPanels[row][col] = spotCard;
        spotStatusLabels[row][col] = statusLabel;

        return spotCard;
    }
}
