import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;

public class ParkingGUI extends JFrame {

    private ParkingManager manager;

    public ParkingGUI() {
        manager = new ParkingManager();
        initUI();
    }

    private void initUI() {
        // Window Configuration
        setTitle("Smart Parking Finder");
        setSize(950, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(15, 15));
        getContentPane().setBackground(new Color(245, 247, 250));

        // 1. Top Section: Header + Dashboard + Legend
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

        dashboardPanel.add(createStatCard("Total Spaces", String.valueOf(manager.getTotalSpaces()), new Color(59, 130, 246)));
        dashboardPanel.add(createStatCard("Occupied Spaces", String.valueOf(manager.getOccupiedSpaces()), new Color(239, 68, 68)));
        dashboardPanel.add(createStatCard("Available Spaces", String.valueOf(manager.getAvailableSpaces()), new Color(16, 185, 129)));

        topContainer.add(dashboardPanel);
        topContainer.add(Box.createVerticalStrut(12));

        // Zone Legend
        JPanel legendPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 0));
        legendPanel.setBackground(new Color(245, 247, 250));
        legendPanel.add(createLegendItem("Bike Zone (Cols 1-2)", new Color(224, 242, 254), new Color(14, 116, 144)));
        legendPanel.add(createLegendItem("Car Zone (Cols 3-4)", new Color(220, 252, 231), new Color(21, 128, 61)));
        legendPanel.add(createLegendItem("Truck Zone (Cols 5-6)", new Color(254, 243, 199), new Color(180, 83, 9)));

        topContainer.add(legendPanel);

        add(topContainer, BorderLayout.NORTH);

        // 2. Center Section: 5x6 Parking Lot Grid (30 Spaces)
        JPanel gridPanel = new JPanel(new GridLayout(manager.getRows(), manager.getCols(), 10, 10));
        gridPanel.setBackground(new Color(245, 247, 250));
        gridPanel.setBorder(new EmptyBorder(10, 20, 20, 20));

        for (int r = 0; r < manager.getRows(); r++) {
            for (int c = 0; c < manager.getCols(); c++) {
                gridPanel.add(createParkingSpaceCard(r, c));
            }
        }

        add(gridPanel, BorderLayout.CENTER);

        setVisible(true);
    }

    // Helper to create a Dashboard Stat Card
    private JPanel createStatCard(String title, String value, Color accentColor) {
        JPanel card = new JPanel(new BorderLayout(5, 5));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(226, 232, 240), 1, true),
                new EmptyBorder(10, 15, 10, 15)
        ));

        JLabel titleLbl = new JLabel(title);
        titleLbl.setFont(new Font("SansSerif", Font.PLAIN, 12));
        titleLbl.setForeground(new Color(100, 116, 139));

        JLabel valueLbl = new JLabel(value);
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

        // Status Label ("EMPTY")
        JLabel statusLabel = new JLabel("[ " + status + " ]", SwingConstants.CENTER);
        statusLabel.setFont(new Font("SansSerif", Font.BOLD, 12));
        statusLabel.setForeground(new Color(100, 116, 139));

        spotCard.add(idLabel);
        spotCard.add(zoneLabel);
        spotCard.add(statusLabel);

        return spotCard;
    }
}

