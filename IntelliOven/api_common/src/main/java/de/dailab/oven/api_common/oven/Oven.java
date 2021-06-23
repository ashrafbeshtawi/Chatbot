package de.dailab.oven.api_common.oven;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class Oven {

    public enum TemperatureUnit {
        CELSIUS, FAHRENHEIT, KELVIN
    }

    public static class Temperature {
        private TemperatureUnit temperatureUnit;
        private Integer temp;

        public Temperature(final TemperatureUnit temperatureUnit, final Integer temperature) {
            this.temperatureUnit = temperatureUnit;
            this.temp = temperature;
        }

        private Temperature() {
        }

        public TemperatureUnit getTemperatureUnit() {
            return this.temperatureUnit;
        }

        public Integer getTemp() {
            return this.temp;
        }
    }

    public static class OvenMode {

        @Nonnull
        private final String program;

        @Nonnull
        private final Temperature[] supportedTemperatureValues;

        @Nullable
        private final Temperature defaultTemperature;

        @Nullable
        private Temperature targetTemperature;


        public OvenMode(@Nonnull final String program, @Nonnull final Temperature[] supportedTemperatureValues,
                        @Nullable final Temperature defaultTemperature, @Nullable final Temperature targetTemperature) {
            this.program = program;
            this.supportedTemperatureValues = supportedTemperatureValues;
            this.defaultTemperature = defaultTemperature;
            this.targetTemperature = targetTemperature;
        }

        @Nonnull
        public String getProgram() {
            return this.program;
        }

        @Nonnull
        public Temperature[] getSupportedTemperatureValues() {
            return this.supportedTemperatureValues;
        }

        @Nullable
        public Temperature getDefaultTemperature() {
            return this.defaultTemperature;
        }

        @Nullable
        public Temperature getTargetTemperature() {
            return this.targetTemperature;
        }

        @Nullable
        public void setTargetTemperature(@Nullable final Temperature targetTemperature) {
            this.targetTemperature = targetTemperature;
        }
    }

    public static class ProgramRequest {
        String ovenMode;
        Temperature temperature;

        public ProgramRequest(final String ovenMode, final Temperature temperature) {
            this.ovenMode = ovenMode;
            this.temperature = temperature;
        }

        private ProgramRequest() {
        }

        public String getOvenMode() {
            return this.ovenMode;
        }

        public void setOvenMode(final String ovenMode) {
            this.ovenMode = ovenMode;
        }

        public Temperature getTemperature() {
            return this.temperature;
        }

        public void setTemperature(final Temperature temperature) {
            this.temperature = temperature;
        }
    }

}
