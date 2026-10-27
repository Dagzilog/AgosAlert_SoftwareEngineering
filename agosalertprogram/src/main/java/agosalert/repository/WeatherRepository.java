package agosalert.repository;

import agosalert.model.WeatherData;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class WeatherRepository {

    private static final String API_URL = 
        "https://api.open-meteo.com/v1/forecast?latitude=14.5995&longitude=120.9842&current=temperature_2m,relative_humidity_2m,weather_code,wind_speed_10m&timezone=Asia%2FManila";

    public WeatherData getTodayWeather() {
        try {
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(API_URL))
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                String json = response.body();

                double temp = extractJsonValue(json, "temperature_2m");
                int humidity = (int) extractJsonValue(json, "relative_humidity_2m");
                int weatherCode = (int) extractJsonValue(json, "weather_code");
                double windSpeed = extractJsonValue(json, "wind_speed_10m");

                String condition = mapWeatherCode(weatherCode);

                return new WeatherData(
                        "Metro Manila, PH",
                        temp,
                        condition,
                        humidity,
                        windSpeed + " km/h"
                );
            }
        } catch (Exception e) {
            System.err.println("API Request Error: " + e.getMessage());
        }

        return new WeatherData("Metro Manila, PH (Offline)", 30.0, "Unavailable", 75, "0 km/h");
    }

    private double extractJsonValue(String json, String key) {
        Pattern pattern = Pattern.compile("\"" + key + "\"\\s*:\\s*([0-9.]+)");
        Matcher matcher = pattern.matcher(json);
        if (matcher.find()) {
            return Double.parseDouble(matcher.group(1));
        }
        return 0.0;
    }

    private String mapWeatherCode(int code) {
        return switch (code) {
            case 0 -> "Clear Sky";
            case 1, 2, 3 -> "Partly Cloudy";
            case 45, 48 -> "Foggy";
            case 51, 53, 55 -> "Light Drizzle";
            case 61, 63, 65 -> "Rain Showers";
            case 80, 81, 82 -> "Heavy Rain";
            case 95, 96, 99 -> "Thunderstorm";
            default -> "Cloudy (" + code + ")";
        };
    }
}