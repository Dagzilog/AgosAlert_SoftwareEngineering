package agosalert;

import agosalert.repository.WeatherRepository;
import agosalert.service.WeatherService;
import agosalert.ui.DashboardFrame;

import javax.swing.SwingUtilities;

public class App {
    public static void main(String[] args) {
        // this just calls the service and repository class (instantiate)...
        WeatherRepository repository = new WeatherRepository();
        WeatherService service = new WeatherService(repository);

        // fetching the frame from the DashboardFrame class
        SwingUtilities.invokeLater(() -> {
            DashboardFrame frame = new DashboardFrame(service);
            frame.setVisible(true);
        });
    }
}