package agosalert.service;

import agosalert.model.WeatherData;
import agosalert.repository.WeatherRepository;

public class WeatherService {
    private final WeatherRepository repository;

    public WeatherService(WeatherRepository repository) {
        this.repository = repository;
    }

    // ADD THIS METHOD:
    public WeatherData getWeatherData() {
        return repository.getTodayWeather();
    }

    // Existing console method (you can keep or remove this)
    public String getFormattedDashboard() {
        WeatherData data = repository.getTodayWeather();
        return String.format("""
                ===================================
                       AGOSALERT WEATHER DASHBOARD
                ===================================
                 Location    : %s
                 Temperature : %.1f °C
                 Condition   : %s
                 Humidity    : %d%%
                 Wind Speed  : %s
                ===================================""",
                data.getLocation(),
                data.getTemperature(),
                data.getCondition(),
                data.getHumidity(),
                data.getWindSpeed()
        );
    }
}