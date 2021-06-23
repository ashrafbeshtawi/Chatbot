package de.dailab.chatbot.aal.weather;

import com.google.common.primitives.Ints;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author sebastian
 */
public final class WeatherReporter {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(WeatherReporter.class);
    
    private static final String WEATHER_URL = "http://www.wttr.in/?AnQT";
    
    private static final Charset ENCODING = StandardCharsets.UTF_8;

    private static String skipLines(final BufferedReader reader, final int linesToSkip) {
        String line = "";
        
        try {
            for(int i = 0; i < linesToSkip; i++){
                    line = reader.readLine();
            }
        } catch(final IOException ioe) {
            LOGGER.error(ioe.getMessage());
        }
        
        return line;
    }

    private static int[] parseTemperatures(final String[] temperatureStrings) {
        final ArrayList<Integer> temperatures = new ArrayList();
        
        for(String temperatureString : temperatureStrings) {
            temperatureString = temperatureString.replaceAll("[\\D\\|\\│]", "");
            
            if(temperatureString.isEmpty()) continue;
            
            temperatures.add(Integer.parseInt(temperatureString));
        }
        
        return Ints.toArray(temperatures);
    }
    
    private WeatherReporter(){}
    
    public static WeatherConditions getConditionsToday() throws IOException {
        final URL url = new URL(WEATHER_URL);
        
        final WeatherConditions conditions;
        
        final BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream(), ENCODING));
        conditions = WeatherReporter.getConditionsToday(reader);

        return conditions;
    }
    
    public static WeatherConditions getConditionsTomorrow() throws IOException {
        final URL url = new URL(WEATHER_URL);
        
        final WeatherConditions conditions;
        
        final BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream(), ENCODING));
        skipLines(reader, 14);
        
        conditions = getConditionsForDayAfterToday(reader);
        
        return conditions;
    }
    
    public static WeatherConditions getConditionsDayAfterTomorrow() throws IOException {
        final URL url = new URL(WEATHER_URL);
        
        final WeatherConditions conditions;
        
        final BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream(), ENCODING));
        skipLines(reader, 24);
        conditions = getConditionsForDayAfterToday(reader);
        
        return conditions;
    }
    
    private static WeatherConditions getConditionsToday(final BufferedReader reader) throws IOException {
        skipLines(reader, 7);
        
        final ImmutablePair<Integer, Integer> temperatures = getTemperatures(reader);
        final int precipitation = getPrecipitation(reader);

        return new WeatherConditions(temperatures.left, temperatures.right, precipitation);
    }
    
    private static WeatherConditions getConditionsForDayAfterToday(final BufferedReader reader) throws IOException {
        skipLines(reader, 3);
        
        final ImmutablePair<Integer, Integer> temperatures = getTemperatures(reader);
        final int precipitation = getPrecipitation(reader);

        return new WeatherConditions(temperatures.left, temperatures.right, precipitation);
    }
    
    private static ImmutablePair<Integer, Integer> getTemperatures(final BufferedReader reader) throws IOException {
        final String line = skipLines(reader, 4);
        
        if(line == null) throw new IOException("Could not parse weather page.");

        final String[] temperatureStrings = line.replaceAll("[\\s,\\°C\\-\\\"\\/\\\\\\(\\)\\_]", "").split("[\\.\\│\\|]");
        final int[] temperatures = parseTemperatures(temperatureStrings);

        final List<Integer> temperatureList = Arrays.asList(ArrayUtils.toObject(temperatures));
        final int high = Collections.max(temperatureList);
        final int low = Collections.min(temperatureList);

        return new ImmutablePair(high, low);
    }
    
    private static int getPrecipitation(final BufferedReader reader) throws IOException {
        final String line = skipLines(reader, 3);
        
        if(line == null) throw new IOException("Could not parse weather page.");
        
        final String[] precipitationStrings = line.split("[\\|]");
        final String s = precipitationStrings[1].replaceAll("[\\|\\s‘m]", "").split("%")[0];
        return Integer.parseInt(s);
    }
}
