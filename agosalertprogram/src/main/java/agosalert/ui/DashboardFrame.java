
package agosalert.ui;

import agosalert.model.WeatherData;
import agosalert.service.WeatherService;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

public class DashboardFrame extends JFrame {

    private final WeatherService weatherService;

    private JLabel tempValLabel;
    private JLabel conditionValLabel;
    private JLabel humidityValLabel;
    private JLabel windValLabel;
    private JLabel regionValLabel;

    private CompositeMapCanvas mapCanvas;

    public DashboardFrame(WeatherService weatherService) {
        this.weatherService = weatherService;

        setTitle("AgosAlert Weather Dashboard - Malabon");
        setSize(980, 540);
        setMinimumSize(new Dimension(850, 480));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        initUI();
    }

    private void initUI() {
        JPanel mainContainer = new JPanel(new GridLayout(1, 2, 15, 15));
        mainContainer.setBackground(new Color(224, 242, 254));
        mainContainer.setBorder(new EmptyBorder(20, 20, 20, 20));

        WeatherData data = weatherService.getWeatherData();

        // LEFT PANEL: WEATHER DASHBOARD
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

        leftPanel.add(createDataCard(
                "Region", regionValLabel, new Color(2, 132, 199)
        ));
        leftPanel.add(Box.createRigidArea(new Dimension(0, 8)));

        leftPanel.add(createDataCard(
                "Temperature", tempValLabel, new Color(225, 29, 72)
        ));
        leftPanel.add(Box.createRigidArea(new Dimension(0, 8)));

        leftPanel.add(createDataCard(
                "Condition", conditionValLabel, new Color(59, 130, 246)
        ));
        leftPanel.add(Box.createRigidArea(new Dimension(0, 8)));

        leftPanel.add(createDataCard(
                "Humidity", humidityValLabel, new Color(14, 165, 233)
        ));
        leftPanel.add(Box.createRigidArea(new Dimension(0, 8)));

        leftPanel.add(createDataCard(
                "Wind Speed", windValLabel, new Color(71, 85, 105)
        ));
        leftPanel.add(Box.createRigidArea(new Dimension(0, 12)));

        JButton refreshButton = new JButton("Fetch Latest Live Weather & Map");
        refreshButton.setFont(new Font("SansSerif", Font.BOLD, 13));
        refreshButton.setBackground(new Color(2, 132, 199));
        refreshButton.setForeground(Color.WHITE);
        refreshButton.setFocusPainted(false);
        refreshButton.setAlignmentX(Component.LEFT_ALIGNMENT);
        refreshButton.setMaximumSize(
                new Dimension(Integer.MAX_VALUE, 38)
        );

        refreshButton.addActionListener(e -> refreshDashboard());
        leftPanel.add(refreshButton);

        // RIGHT PANEL: MAP
        JPanel rightPanel = new JPanel(new BorderLayout(0, 10));
        rightPanel.setBackground(Color.WHITE);
        rightPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(
                        new Color(186, 230, 253), 2
                ),
                new EmptyBorder(12, 12, 12, 12)
        ));

        JLabel regionTitle = new JLabel(
                "Malabon Map - Hulong Duhat",
                SwingConstants.CENTER
        );
        regionTitle.setFont(new Font("SansSerif", Font.BOLD, 14));
        regionTitle.setForeground(new Color(30, 41, 59));

        mapCanvas = new CompositeMapCanvas();

        rightPanel.add(regionTitle, BorderLayout.NORTH);
        rightPanel.add(mapCanvas, BorderLayout.CENTER);

        mainContainer.add(leftPanel);
        mainContainer.add(rightPanel);

        add(mainContainer);
    }

    private JPanel createDataCard(
            String label,
            JLabel valueLabel,
            Color accentColor
    ) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(Color.WHITE);
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));

        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(
                        0, 6, 0, 0, accentColor
                ),
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

    private void refreshDashboard() {
        WeatherData newData = weatherService.getWeatherData();

        regionValLabel.setText(newData.getLocation());
        tempValLabel.setText(newData.getTemperature() + " °C");
        conditionValLabel.setText(newData.getCondition());
        humidityValLabel.setText(newData.getHumidity() + "%");
        windValLabel.setText(newData.getWindSpeed());

        mapCanvas.reloadMap();
    }

    private static class CompositeMapCanvas extends JPanel
            implements ActionListener {

        private static final int ZOOM = 14;
        private static final int TILE_SIZE = 256;
        private static final int GRID_SIZE = 3;

        private static final double TARGET_LATITUDE = 14.6686;
        private static final double TARGET_LONGITUDE = 120.9573;

        private static final ConcurrentHashMap<String, BufferedImage>
                TILE_CACHE = new ConcurrentHashMap<>();

        private BufferedImage baseMapImage;

        private int pulseAlpha = 60;
        private boolean growing = true;

        private SwingWorker<BufferedImage, Void> mapWorker;

        CompositeMapCanvas() {
            setBackground(new Color(226, 238, 242));
            setPreferredSize(new Dimension(400, 400));

            Timer pulseTimer = new Timer(80, this);
            pulseTimer.start();

            reloadMap();
        }

        void reloadMap() {
            if (mapWorker != null && !mapWorker.isDone()) {
                mapWorker.cancel(true);
            }

            baseMapImage = null;
            repaint();

            mapWorker = new SwingWorker<>() {

                @Override
                protected BufferedImage doInBackground() {
                    int centerX = longitudeToTileX(
                            TARGET_LONGITUDE, ZOOM
                    );
                    int centerY = latitudeToTileY(
                            TARGET_LATITUDE, ZOOM
                    );

                    int startX = centerX - GRID_SIZE / 2;
                    int startY = centerY - GRID_SIZE / 2;

                    int imageSize = GRID_SIZE * TILE_SIZE;

                    BufferedImage stitched = new BufferedImage(
                            imageSize,
                            imageSize,
                            BufferedImage.TYPE_INT_RGB
                    );

                    Graphics2D graphics = stitched.createGraphics();
                    ExecutorService executor =
                            Executors.newFixedThreadPool(3);

                    List<Future<TileResult>> requests =
                            new ArrayList<>();

                    try {
                        graphics.setColor(new Color(226, 238, 242));
                        graphics.fillRect(0, 0, imageSize, imageSize);

                        for (int row = 0; row < GRID_SIZE; row++) {
                            for (int column = 0;
                                 column < GRID_SIZE;
                                 column++) {

                                if (isCancelled()) {
                                    return null;
                                }

                                final int tileX = startX + column;
                                final int tileY = startY + row;
                                final int destinationX = column * TILE_SIZE;
                                final int destinationY = row * TILE_SIZE;

                                requests.add(executor.submit(() -> {
                                    BufferedImage tile =
                                            getTile(tileX, tileY);

                                    return new TileResult(
                                            tile,
                                            destinationX,
                                            destinationY
                                    );
                                }));
                            }
                        }

                        for (Future<TileResult> request : requests) {
                            if (isCancelled()) {
                                return null;
                            }

                            try {
                                TileResult result = request.get();

                                if (result.image != null) {
                                    graphics.drawImage(
                                            result.image,
                                            result.x,
                                            result.y,
                                            null
                                    );
                                }
                            } catch (CancellationException e) {
                                return null;
                            } catch (ExecutionException e) {
                                System.err.println(
                                        "Map tile failed: "
                                                + e.getMessage()
                                );
                            } catch (InterruptedException e) {
                                Thread.currentThread().interrupt();
                                return null;
                            }
                        }

                        return isCancelled() ? null : stitched;

                    } finally {
                        graphics.dispose();

                        for (Future<TileResult> request : requests) {
                            request.cancel(true);
                        }

                        executor.shutdownNow();
                    }
                }

                @Override
                protected void done() {
                    if (isCancelled()) {
                        return;
                    }

                    try {
                        baseMapImage = get();
                    } catch (CancellationException ignored) {
                        return;
                    } catch (Exception e) {
                        System.err.println(
                                "Map loading failed: " + e.getMessage()
                        );
                    }

                    repaint();
                }
            };

            mapWorker.execute();
        }

        private static class TileResult {
            final BufferedImage image;
            final int x;
            final int y;

            TileResult(BufferedImage image, int x, int y) {
                this.image = image;
                this.x = x;
                this.y = y;
            }
        }

        private BufferedImage getTile(int x, int y) {
            String key = ZOOM + "/" + x + "/" + y;

            BufferedImage cached = TILE_CACHE.get(key);
            if (cached != null) {
                return cached;
            }

            String address = "https://tile.openstreetmap.org/"
                    + ZOOM + "/" + x + "/" + y + ".png";

            HttpURLConnection connection = null;

            try {
                URL url = URI.create(address).toURL();
                connection = (HttpURLConnection) url.openConnection();

                connection.setRequestProperty(
                        "User-Agent", "AgosAlert/1.0"
                );
                connection.setConnectTimeout(3000);
                connection.setReadTimeout(4000);

                if (connection.getResponseCode()
                        != HttpURLConnection.HTTP_OK) {
                    return null;
                }

                BufferedImage downloaded;

                try (var input = connection.getInputStream()) {
                    downloaded = ImageIO.read(input);
                }

                if (downloaded != null) {
                    TILE_CACHE.putIfAbsent(key, downloaded);
                }

                return downloaded;

            } catch (Exception e) {
                System.err.println(
                        "Tile " + key + " failed: " + e.getMessage()
                );
                return null;

            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
            }
        }

        private static int longitudeToTileX(
                double longitude, int zoom
        ) {
            return (int) Math.floor(
                    (longitude + 180.0) / 360.0
                            * Math.pow(2, zoom)
            );
        }

        private static int latitudeToTileY(
                double latitude, int zoom
        ) {
            double radians = Math.toRadians(latitude);

            return (int) Math.floor(
                    (1.0 - Math.log(
                            Math.tan(radians)
                                    + 1.0 / Math.cos(radians)
                    ) / Math.PI) / 2.0
                            * Math.pow(2, zoom)
            );
        }

        private static double worldPixelX(double longitude) {
            return (longitude + 180.0) / 360.0
                    * Math.pow(2, ZOOM) * TILE_SIZE;
        }

        private static double worldPixelY(double latitude) {
            double radians = Math.toRadians(latitude);

            return (1.0 - Math.log(
                    Math.tan(radians)
                            + 1.0 / Math.cos(radians)
            ) / Math.PI) / 2.0
                    * Math.pow(2, ZOOM) * TILE_SIZE;
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);

            Graphics2D g2 = (Graphics2D) g.create();

            try {
                g2.setRenderingHint(
                        RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON
                );

                int width = getWidth();
                int height = getHeight();

                if (baseMapImage != null) {
                    g2.drawImage(
                            baseMapImage, 0, 0, width, height, null
                    );

                    drawLocationMarker(g2, width, height);
                } else {
                    g2.setColor(new Color(226, 238, 242));
                    g2.fillRect(0, 0, width, height);

                    g2.setColor(new Color(51, 65, 85));
                    g2.setFont(new Font("SansSerif", Font.BOLD, 13));

                    String message = "Loading Hulong Duhat map...";
                    FontMetrics metrics = g2.getFontMetrics();

                    g2.drawString(
                            message,
                            (width - metrics.stringWidth(message)) / 2,
                            height / 2
                    );
                }

                drawAttribution(g2, height);

            } finally {
                g2.dispose();
            }
        }

        private void drawLocationMarker(
                Graphics2D g2,
                int width,
                int height
        ) {
            int centerX = longitudeToTileX(
                    TARGET_LONGITUDE, ZOOM
            );
            int centerY = latitudeToTileY(
                    TARGET_LATITUDE, ZOOM
            );

            double startWorldX =
                    (centerX - GRID_SIZE / 2.0) * TILE_SIZE;
            double startWorldY =
                    (centerY - GRID_SIZE / 2.0) * TILE_SIZE;

            double mapPixelSize = GRID_SIZE * TILE_SIZE;

            int markerX = (int) (
                    (worldPixelX(TARGET_LONGITUDE) - startWorldX)
                            / mapPixelSize * width
            );

            int markerY = (int) (
                    (worldPixelY(TARGET_LATITUDE) - startWorldY)
                            / mapPixelSize * height
            );

            int radius = 25;

            g2.setColor(new Color(239, 68, 68, pulseAlpha));
            g2.fillOval(
                    markerX - radius,
                    markerY - radius,
                    radius * 2,
                    radius * 2
            );

            g2.setColor(new Color(220, 38, 38));
            g2.fillOval(markerX - 5, markerY - 5, 10, 10);

            g2.setColor(Color.WHITE);
            g2.setStroke(new BasicStroke(2));
            g2.drawOval(markerX - 7, markerY - 7, 14, 14);

            drawLocationLabel(g2, markerX, markerY, width, height);
        }

        private void drawLocationLabel(
                Graphics2D g2,
                int markerX,
                int markerY,
                int width,
                int height
        ) {
            int boxWidth = 175;
            int boxHeight = 43;

            int boxX = markerX + 18;
            int boxY = markerY - boxHeight - 12;

            if (boxX + boxWidth > width - 8) {
                boxX = markerX - boxWidth - 18;
            }

            if (boxY < 8) {
                boxY = markerY + 18;
            }

            boxX = Math.max(
                    8, Math.min(boxX, width - boxWidth - 8)
            );
            boxY = Math.max(
                    8, Math.min(boxY, height - boxHeight - 35)
            );

            g2.setColor(new Color(15, 23, 42, 230));
            g2.fillRoundRect(
                    boxX, boxY, boxWidth, boxHeight, 10, 10
            );

            g2.setColor(new Color(56, 189, 248));
            g2.drawRoundRect(
                    boxX, boxY, boxWidth, boxHeight, 10, 10
            );

            g2.setFont(new Font("SansSerif", Font.BOLD, 11));
            g2.setColor(Color.WHITE);
            g2.drawString(
                    "MALABON CITY", boxX + 10, boxY + 17
            );

            g2.setColor(new Color(250, 204, 21));
            g2.drawString(
                    "BRGY. HULONG DUHAT", boxX + 10, boxY + 32
            );
        }

        private void drawAttribution(Graphics2D g2, int height) {
            String text = "© OpenStreetMap contributors";

            g2.setFont(new Font("SansSerif", Font.PLAIN, 10));
            FontMetrics metrics = g2.getFontMetrics();

            int padding = 6;
            int boxWidth = metrics.stringWidth(text) + padding * 2;
            int boxHeight = metrics.getHeight() + padding;

            g2.setColor(new Color(255, 255, 255, 220));
            g2.fillRoundRect(
                    8,
                    height - boxHeight - 8,
                    boxWidth,
                    boxHeight,
                    6,
                    6
            );

            g2.setColor(new Color(51, 65, 85));
            g2.drawString(
                    text,
                    8 + padding,
                    height - padding - 8
            );
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            if (growing) {
                pulseAlpha += 15;

                if (pulseAlpha >= 140) {
                    growing = false;
                }
            } else {
                pulseAlpha -= 15;

                if (pulseAlpha <= 30) {
                    growing = true;
                }
            }

            repaint();
        }
    }
}
