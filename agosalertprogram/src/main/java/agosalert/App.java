package agosalert;

import agosalert.repository.WeatherRepository;
import agosalert.service.WeatherService;
import agosalert.ui.DashboardFrame;

import javax.swing.SwingUtilities;

public class App {
    public static void main(String[] args) {
        WeatherRepository repository = new WeatherRepository();
        WeatherService service = new WeatherService(repository);

        // Launch GUI on the Event Dispatch Thread
        SwingUtilities.invokeLater(() -> {
            DashboardFrame frame = new DashboardFrame(service);
            frame.setVisible(true);
        });
    }
}