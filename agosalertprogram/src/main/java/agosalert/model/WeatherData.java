package agosalert.model;

public class WeatherData {
private String location;
    private double temperature;
    private String condition;
    private int humidity;
    private String windSpeed;

    public WeatherData(String location, double temperature, String condition, int humidity, String windSpeed) {
        this.location = location;
        this.temperature = temperature;
        this.condition = condition;
        this.humidity = humidity;
        this.windSpeed = windSpeed;
    }

    public String getLocation() { return location; }
    public double getTemperature() { return temperature; }
    public String getCondition() { return condition; }
    public int getHumidity() { return humidity; }
    public String getWindSpeed() { return windSpeed; }
}