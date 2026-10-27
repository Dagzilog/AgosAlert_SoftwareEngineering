package agosalert.ui;

import agosalert.model.WeatherData;
import agosalert.service.WeatherService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class DashboardFrame extends JFrame {
    private final WeatherService weatherService;
    
    private JLabel tempValLabel;
    private JLabel conditionValLabel;
    private JLabel humidityValLabel;
    private JLabel windValLabel;
    private JLabel regionValLabel;

    public DashboardFrame(WeatherService weatherService) {
        this.weatherService = weatherService;

        setTitle("AgosAlert Weather Dashboard — Metro Manila");
        setSize(950, 520);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        initUI();
    }

    private void initUI() {
        JPanel mainContainer = new JPanel(new GridLayout(1, 2, 15, 15));
        mainContainer.setBackground(new Color(224, 242, 254)); // Sky blue theme
        mainContainer.setBorder(new EmptyBorder(20, 20, 20, 20));

        WeatherData data = weatherService.getWeatherData();

        // --- LEFT PANEL: Weather Data Cards ---
        JPanel leftPanel = new JPanel();
        leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.Y_AXIS));
        leftPanel.setBackground(new Color(224, 242, 254));

        JLabel headerTitle = new JLabel("Today's Forecast");
        headerTitle.setFont(new Font("SansSerif", Font.BOLD, 18));
        headerTitle.setForeground(new Color(15, 23, 42));
        headerTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        leftPanel.add(headerTitle);
        leftPanel.add(Box.createRigidArea(new Dimension(0, 10)));

        regionValLabel = new JLabel(data.getLocation());
        tempValLabel = new JLabel(data.getTemperature() + " °C");
        conditionValLabel = new JLabel(data.getCondition());
        humidityValLabel = new JLabel(data.getHumidity() + "%");
        windValLabel = new JLabel(data.getWindSpeed());

        leftPanel.add(createDataCard("Region", regionValLabel, new Color(2, 132, 199)));
        leftPanel.add(Box.createRigidArea(new Dimension(0, 8)));
        leftPanel.add(createDataCard("Temperature", tempValLabel, new Color(225, 29, 72)));
        leftPanel.add(Box.createRigidArea(new Dimension(0, 8)));
        leftPanel.add(createDataCard("Condition", conditionValLabel, new Color(59, 130, 246)));
        leftPanel.add(Box.createRigidArea(new Dimension(0, 8)));
        leftPanel.add(createDataCard("Humidity", humidityValLabel, new Color(14, 165, 233)));
        leftPanel.add(Box.createRigidArea(new Dimension(0, 8)));
        leftPanel.add(createDataCard("Wind Speed", windValLabel, new Color(71, 85, 105)));
        leftPanel.add(Box.createRigidArea(new Dimension(0, 12)));

        JButton refreshButton = new JButton("Fetch Latest Live Weather");
        refreshButton.setFont(new Font("SansSerif", Font.BOLD, 13));
        refreshButton.setBackground(new Color(2, 132, 199));
        refreshButton.setForeground(Color.WHITE);
        refreshButton.setFocusPainted(false);
        refreshButton.setAlignmentX(Component.LEFT_ALIGNMENT);
        refreshButton.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        
        refreshButton.addActionListener(e -> refreshWeatherData());
        leftPanel.add(refreshButton);

        // --- RIGHT PANEL: Custom 2D Metro Manila Map Showcase ---
        JPanel rightPanel = new JPanel(new BorderLayout());
        rightPanel.setBackground(Color.WHITE);
        rightPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(186, 230, 253), 2),
                new EmptyBorder(15, 15, 15, 15)
        ));

        JLabel regionTitle = new JLabel("Metro Manila Geographic Map View", SwingConstants.CENTER);
        regionTitle.setFont(new Font("SansSerif", Font.BOLD, 15));
        regionTitle.setForeground(new Color(30, 41, 59));

        // Custom Vector Map Panel representing Metro Manila geography
        MetroManilaMapPanel mapPanel = new MetroManilaMapPanel();

        rightPanel.add(regionTitle, BorderLayout.NORTH);
        rightPanel.add(Box.createRigidArea(new Dimension(0, 10)), BorderLayout.CENTER);
        rightPanel.add(mapPanel, BorderLayout.CENTER);

        mainContainer.add(leftPanel);
        mainContainer.add(rightPanel);

        add(mainContainer);
    }

    private JPanel createDataCard(String label, JLabel valueLabel, Color accentColor) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(Color.WHITE);
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 6, 0, 0, accentColor),
                BorderFactory.createEmptyBorder(6, 10, 6, 10)
        ));

        JLabel lblName = new JLabel(label);
        lblName.setFont(new Font("SansSerif", Font.BOLD, 11));
        lblName.setForeground(new Color(100, 116, 139));

        valueLabel.setFont(new Font("SansSerif", Font.BOLD, 13));
        valueLabel.setForeground(new Color(15, 23, 42));

        card.add(lblName, BorderLayout.NORTH);
        card.add(valueLabel, BorderLayout.CENTER);

        return card;
    }

    private void refreshWeatherData() {
        WeatherData newData = weatherService.getWeatherData();
        regionValLabel.setText(newData.getLocation());
        tempValLabel.setText(newData.getTemperature() + " °C");
        conditionValLabel.setText(newData.getCondition());
        humidityValLabel.setText(newData.getHumidity() + "%");
        windValLabel.setText(newData.getWindSpeed());
    }

    // --- Inner Class: Custom 2D Vector Map Canvas of Metro Manila ---
    private static class MetroManilaMapPanel extends JPanel {

        public MetroManilaMapPanel() {
            setBackground(new Color(240, 249, 255)); // Light map water/land backdrop
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g;
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int width = getWidth();
            int height = getHeight();

            // 1. Draw Manila Bay water body on the left side
            g2d.setColor(new Color(186, 230, 253));
            g2d.fillArc(-50, 20, 180, height - 40, 90, -180);

            // 2. Draw Landmass (Metro Manila region polygon shape)
            g2d.setColor(new Color(254, 243, 199)); // Land beige/yellow tint
            int[] xPoints = {80, width - 40, width - 60, 90, 70};
            int[] yPoints = {30, 50, height - 40, height - 30, 100};
            g2d.fillPolygon(xPoints, yPoints, 5);

            g2d.setColor(new Color(217, 119, 6)); // Border outline for land
            g2d.setStroke(new BasicStroke(2f));
            g2d.drawPolygon(xPoints, yPoints, 5);

            // 3. Draw Pasig River flowing through the center
            g2d.setColor(new Color(56, 189, 248));
            g2d.setStroke(new BasicStroke(3f));
            g2d.drawLine(70, height / 2, width - 80, height / 2 + 20);

            // 4. Draw City Markers (Pins)
            drawCityPin(g2d, 140, 90, "Quezon City");
            drawCityPin(g2d, 100, 180, "Manila (Port Area)");
            drawCityPin(g2d, 160, 240, "Taguig / Makati");
            drawCityPin(g2d, 220, 140, "Pasig");

            // 5. Map Legend & Overlay Box
            g2d.setColor(new Color(15, 23, 42, 200));
            g2d.fillRoundRect(15, height - 65, width - 30, 50, 10, 10);

            g2d.setFont(new Font("SansSerif", Font.BOLD, 11));
            g2d.setColor(Color.WHITE);
            g2d.drawString("Target Area: Metro Manila, Philippines", 25, height - 42);
            g2d.setFont(new Font("SansSerif", Font.PLAIN, 10));
            g2d.setColor(new Color(186, 230, 253));
            g2d.drawString("Status: Live Vector Mapping Active", 25, height - 25);
        }

        private void drawCityPin(Graphics2D g2d, int x, int y, String cityName) {
            // Pin dot
            g2d.setColor(new Color(225, 29, 72));
            g2d.fillOval(x - 5, y - 5, 10, 10);
            g2d.setColor(Color.WHITE);
            g2d.setStroke(new BasicStroke(1.5f));
            g2d.drawOval(x - 5, y - 5, 10, 10);

            // City label
            g2d.setFont(new Font("SansSerif", Font.BOLD, 10));
            g2d.setColor(new Color(30, 41, 59));
            g2d.drawString(cityName, x + 12, y + 4);
        }
    }
}