package agosalert.ui;

import agosalert.service.WeatherService;

public class WeatherView {
    private final WeatherService service;

    public WeatherView(WeatherService service) {
        this.service = service;
    }

    public void renderDashboard() {
        System.out.println(service.getFormattedDashboard());
    }
}

