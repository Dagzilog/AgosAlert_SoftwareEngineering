package agosalert.service;

import agosalert.model.WeatherData;
import agosalert.repository.WeatherRepository;

public class WeatherService {
    private static final String OPENWEATHER_API_KEY = "85f4b96c4618592a16717c61de4e459e";
    private final WeatherRepository weatherRepository;

    public WeatherService(WeatherRepository weatherRepository) {
        this.weatherRepository = weatherRepository;
    }

    public String getApiKey() {
        return OPENWEATHER_API_KEY;
    }

    public WeatherData getWeatherData() {
        // Fallback / standard weather data for Metro Manila dashboard
        return new WeatherData("Hulong Duhat(Malabon City), PH", 28.1, "Partly Cloudy", 76, "1.5 km/h");
    }

    public String getFormattedDashboard() {
        WeatherData data = getWeatherData();
        return "Region: " + data.getLocation() + " | Temp: " + data.getTemperature() + "°C | Condition: " + data.getCondition();
    }
}