package agosalert.ui;

import agosalert.service.WeatherService;

// this class just calls the weather view and calls the weather dashboard from the service folder
public class WeatherView {
    private final WeatherService service;

    public WeatherView(WeatherService service) {
        this.service = service;
    }

    public void renderDashboard() {
        System.out.println(service.getFormattedDashboard());
    }
}

