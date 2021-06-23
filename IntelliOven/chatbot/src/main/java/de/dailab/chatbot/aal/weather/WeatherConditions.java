package de.dailab.chatbot.aal.weather;

/**
 *
 * @author sebastian
 */
public class WeatherConditions{
        
    private final int temperatureHigh;

    private final int temperatureLow;

    private final int precipitation;

    public WeatherConditions(final int temperatureHigh, final int temperatureLow, final int precipitation) {
        this.temperatureHigh = temperatureHigh;
        this.temperatureLow = temperatureLow;
        this.precipitation = precipitation;
    }

    public int getTemperatureHigh() {
        return this.temperatureHigh;
    }

    public int getTemperatureLow() {
        return this.temperatureLow;
    }

    public int getPrecipitation() {
        return this.precipitation;
    }
}