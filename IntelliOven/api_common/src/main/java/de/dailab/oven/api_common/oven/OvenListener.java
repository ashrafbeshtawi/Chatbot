package de.dailab.oven.api_common.oven;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.time.Duration;

/**
 * Listener for oven related events.
 *
 * @author Hendrik Motza
 * @since 20.02
 */
public interface OvenListener {

    /**
     * Called when oven program or target temperature has changed.
     *
     * @param program           identifier of the program, {@code null} disallowed
     * @param targetTemperature target temperature or {@code null} if program does not support any temperature setting
     */
    void onProgramChanged(@Nonnull final String program, @Nullable final Oven.Temperature targetTemperature);

    /**
     * Called when a new measurement for the temperature inside the oven is retrieved.
     *
     * @param currentTemperature current temperature
     * @param unit               unit of the temperature
     */
    void onCurrentTemperatureChanged(final int currentTemperature, @Nonnull final Oven.TemperatureUnit unit);

    /**
     * Called when new data is retrieved from a meat probe sensor.
     *
     * @param sensorId    sensor id
     * @param temperature current temperature or {@code null} if sensor is not connected or not accessible
     * @param unit        unit of the temperature
     */
    void onMeatProbeTemperatureChanged(final int sensorId, final Integer temperature, @Nonnull final Oven.TemperatureUnit unit);

    /**
     * Called when target temperature is reached.
     */
    void onPreheated();

    /*
     * Called when oven gets switched on or off.
     */
    void onSwitchedChange(boolean switchedOn);


    /*
     * Called when lamp inside oven is switched on or off.
     */
    void onLightChange(boolean lightOn);


    /*
     * Called when oven door gets opened or closed.
     */
    void onDoorChange(boolean doorOpened);


    /**
     * Called when timer setting got updated
     *
     * @param duration Remaining time until timer expires or {@code null} if timer is deactivated
     */
    void onTimerUpdated(@Nullable final Duration duration);

    /**
     * Called when timer expired
     */
    void onTimerExpired();

}
