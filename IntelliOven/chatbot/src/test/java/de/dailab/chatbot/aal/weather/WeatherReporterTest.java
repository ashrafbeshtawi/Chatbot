/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.dailab.chatbot.aal.weather;

import java.io.IOException;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Test;

/**
 *
 * @author sebastian
 */
public class WeatherReporterTest {
   
    @Test
    public void testGetConditionsToday() throws IOException{
        WeatherConditions conditions = null;
        
        try {
            conditions = WeatherReporter.getConditionsToday();
        // wttr.in is offline
        } catch(final IOException e) {
           Assume.assumeNoException("Couldn't get a forecast", e);
        }
        
        Assert.assertTrue(conditions.getTemperatureHigh() >= conditions.getTemperatureLow());
    }
    
    @Test
    public void testGetConditionsTomorrow() throws IOException{
        WeatherConditions conditions = null;
        
        try {
            conditions = WeatherReporter.getConditionsTomorrow();
        // wttr.in is offline
        } catch(final IOException e) {
        	Assume.assumeNoException("Couldn't get a forecast", e);
        }
        
        Assert.assertTrue(conditions.getTemperatureHigh() >= conditions.getTemperatureLow());
    }
    
    @Test
    public void testGetConditionsDayAfterTomorrow() throws IOException{
        WeatherConditions conditions = null;
        
        try {
            conditions = WeatherReporter.getConditionsDayAfterTomorrow();
        // wttr.in is offline
        } catch(final IOException e) {
        	Assume.assumeNoException("Couldn't get a forecast", e);
        }
        
        Assert.assertTrue(conditions.getTemperatureHigh() >= conditions.getTemperatureLow());
    }
}
