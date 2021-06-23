package de.dailab.oven;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

public class DummyOven {

    public enum SteamMode {
        OFF(0), LOW(1), MEDIUM(2), HIGH(3);

        private final int id;

        SteamMode(final int identifier) {
            this.id = identifier;
        }

        public int getId() {
            return this.id;
        }

    }

    private static class HeatingTemperatureCurve implements Supplier<Float> {

        private final float sourceTemperature;
        private final float targetTemperature;
        private final float degreePerSecond = 0.27f;
        private final long startTime;
        private final float timeFactor;

        HeatingTemperatureCurve(final float sourceTemperature, final float targetTemperature, final long startTime, final float timeFactor) {
            this.sourceTemperature = sourceTemperature;
            this.targetTemperature = targetTemperature;
            this.startTime = startTime;
            this.timeFactor = timeFactor;
        }

        @Override
        public Float get() {
            final float timeDiff = (System.currentTimeMillis() - this.startTime)/1000f * this.timeFactor;
            final float v = timeDiff* this.degreePerSecond + this.sourceTemperature;
            return v > this.targetTemperature ? this.targetTemperature : v;
        }
    }

    private static class CooldownTemperatureCurve implements Supplier<Float> {

        private final float targetTemperature;
        private final float timeToFullCooldown;
        private final long startTime;
        private final float timeFactor;
        private final float a;

        CooldownTemperatureCurve(final float sourceTemperature, final float targetTemperature, final long startTime, final float timeFactor) {
            this.targetTemperature = targetTemperature;
            this.startTime = startTime;
            this.timeFactor = timeFactor;
            final float cooldownTime = 1200 * (sourceTemperature-AMBIENT_TEMPERATURE)/200;
            this.timeToFullCooldown = cooldownTime == 0 ? 1 : cooldownTime;
            this.a = (sourceTemperature-targetTemperature) / (this.timeToFullCooldown * this.timeToFullCooldown);
        }

        @Override
        public Float get() {
            final float timeDiff = (System.currentTimeMillis() - this.startTime)/1000f * this.timeFactor;
            if(timeDiff > this.timeToFullCooldown) {
                return this.targetTemperature;
            }
            return this.a *(timeDiff- this.timeToFullCooldown)*(timeDiff- this.timeToFullCooldown)+ this.targetTemperature;
        }
    }

    private static final float AMBIENT_TEMPERATURE = 25;

    private final float speedFactor;
    private OvenProgram currentProgram = null;
    private Integer targetTemperature = null;
    private Supplier<Float> currentTemperatureCalculator = new HeatingTemperatureCurve(AMBIENT_TEMPERATURE, AMBIENT_TEMPERATURE, System.currentTimeMillis(), 1);
    private boolean ovenLampOn = false;


    public DummyOven(final float timeFactor) {
        this.speedFactor = timeFactor;
    }

    public DummyOven() {
        this(1);
    }

    @Nonnull
    public List<OvenProgram> getPrograms() {
        return OvenProgram.getPrograms();
    }

    public void switchOff() {
        this.currentProgram = null;
        this.targetTemperature = null;
        this.currentTemperatureCalculator = new CooldownTemperatureCurve(this.currentTemperatureCalculator.get(), AMBIENT_TEMPERATURE, System.currentTimeMillis(), this.speedFactor);
    }

    public boolean isOn() {
        return this.currentProgram != null;
    }

    public void setProgram(@Nonnull final OvenProgram program, @Nullable final Integer targetTemperature, @Nullable final Integer duration, final String lang) {
        boolean isValidTargetTemperature = false;
        for(final Integer tempValue : program.getValidTargetTemperatureValues()) {
            if(Objects.equals(tempValue, targetTemperature)) {
                isValidTargetTemperature = true;
                break;
            }
        }
        if(!isValidTargetTemperature) {
            if("tr".equals(lang)){
                throw new IllegalArgumentException("Söylediğiniz sıcaklık seçilen programla uyumlu değil.");
            }
            else
                throw new IllegalArgumentException("Specified target temperature does not fit to the chosen program.");
        }
        if(duration != null) {
            if(duration <= 0) {
                if("tr".equals(lang)){
                    throw new IllegalArgumentException("Süre 0 dan büyük olmalıdır.");
                }
                else
                    throw new IllegalArgumentException("Duration must be greater than zero.");
            }
            if(duration > program.getMaxDuration()) {
                if("tr".equals(lang)){
                    throw new IllegalArgumentException("Süre belirlenen sınırları aşıyor.");
                }
                else
                    throw new IllegalArgumentException("Duration exceeds limit.");
            }
        }
        this.currentProgram = program;
        this.targetTemperature = targetTemperature;
        final float currentTemp = this.currentTemperatureCalculator.get();
        if(targetTemperature>=currentTemp) {
            this.currentTemperatureCalculator = new HeatingTemperatureCurve(currentTemp, targetTemperature, System.currentTimeMillis(), this.speedFactor);
        } else {
            this.currentTemperatureCalculator = new CooldownTemperatureCurve(currentTemp, targetTemperature, System.currentTimeMillis(), this.speedFactor);
        }
    }
/*
    public void setProgram(@Nonnull final OvenProgram program, @Nullable Integer targetTemperature,@Nullable Integer durationTemp,String lang) {
        setProgram(program, targetTemperature, durationTemp,lang);
    }
*/
    public OvenProgram getProgram() {
        return this.currentProgram;
    }

    public boolean isDoorLocked() {
        return false;
    }

    public boolean isLampOn() {
        return this.ovenLampOn;
    }

    public void switchLampOn() {
        this.ovenLampOn = true;
    }

    public void switchLampOff() {
        this.ovenLampOn = false;
    }

    public boolean isTurboFanTurning() {
        return false;
    }

    public boolean isMeatProbePluggedIn() {
        return false;
    }

    public SteamMode getSteamMode() {
        return SteamMode.OFF;
    }

    public float getCurrentTemperature() {
        return this.currentTemperatureCalculator.get();
    }

    public Integer getTargetTemperature() {
        return this.targetTemperature;
    }

    public float getProbeCurrentTemperature() {
        return 0;
    }

    public Integer getProbeTargetTemperature() {
        return null;
    }

}
