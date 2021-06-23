package de.dailab.oven.controller;

import de.dailab.oven.api_common.Sendable;
import de.dailab.oven.api_common.oven.Oven;
import de.dailab.oven.api_common.oven.OvenListener;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

/**
 * Interface to control the oven.
 *
 * @author Hendrik Motza, Leon Haufe
 * @since 20.02
 */
public class OvenController implements Sendable {

    private static OvenController singleInstance = null;

    private boolean isLightOn;
    private boolean isDoorOpened;
    private boolean isOff;
    private Oven.OvenMode program;
    private final List<Oven.OvenMode> programs;
    private Oven.Temperature temperature;
    private final List<OvenListener> ovenListenerList;

    public static OvenController getInstance() {
        if (singleInstance == null)
            singleInstance = new OvenController();

        return singleInstance;
    }

    private OvenController() {
        this.isLightOn = true;
        this.isDoorOpened = false;
        this.isOff = false;
        this.programs = new ArrayList<>();
        this.temperature = new Oven.Temperature(Oven.TemperatureUnit.CELSIUS, 120);
        this.ovenListenerList = new ArrayList<>();

        //create Programs
        final Oven.Temperature[] tempList = new Oven.Temperature[]{new Oven.Temperature(Oven.TemperatureUnit.CELSIUS, 150), new Oven.Temperature(Oven.TemperatureUnit.CELSIUS, 200), new Oven.Temperature(Oven.TemperatureUnit.CELSIUS, 220)};
        final Oven.OvenMode clean = new Oven.OvenMode("clean", new Oven.Temperature[]{new Oven.Temperature(Oven.TemperatureUnit.CELSIUS, 20)}, new Oven.Temperature(Oven.TemperatureUnit.CELSIUS, 150), new Oven.Temperature(Oven.TemperatureUnit.CELSIUS, 150));
        final Oven.OvenMode cook = new Oven.OvenMode("cook", tempList, new Oven.Temperature(Oven.TemperatureUnit.CELSIUS, 150), new Oven.Temperature(Oven.TemperatureUnit.CELSIUS, 150));

        this.program = cook;
		this.programs.add(cook);
		this.programs.add(clean);
    }

    /*
     * sets a Oven Listener
     *
     * @param ovenListener
     */
    public void setOvenListener(final OvenListener ovenListener) {
        this.ovenListenerList.add(ovenListener);
    }

    /**
     * Reports whether oven lamp is on.
     *
     * @return true if lamp is on
     */
    public boolean isLightOn() {
        return this.isLightOn;
    }

    /**
     * Switch the light.
     *
     * @param lightOn {@code true} to switch light on, {@code false} otherwise
     * @throws UnsupportedOperationException Thrown if switching light on/off is not supported by this device.
     */
    public void setLightOn(final boolean lightOn) {
		this.isLightOn = lightOn;
    }

    /**
     * Reports whether oven door is opened.
     *
     * @return true if door is opened
     */
    public boolean isDoorOpened() {
        return this.isDoorOpened;
    }

    /*
     * sets whether oven door is opened.
     *
     * @param doorOpened
     */
    public void setDoorOpened(final boolean doorOpened) {
		this.isDoorOpened = doorOpened;
    }

    /*
     * returns if the oven is off or on
     *
     * @return
     */
    public boolean isOff() {
        return this.isOff;
    }

    /**
     * Switches the device off or on.
     *
     * @throws UnsupportedOperationException Thrown if the oven does not support being switched off remotely.
     */
    public void setOff(final boolean off) {
		this.isOff = off;
    }

    /**
     * Returns the current oven program.
     *
     * @return oven program identifier or can be {@code null} if switched off
     */
    public Oven.OvenMode getProgram() {
        return this.program;
    }

    /**
     * Provides a list of oven programs that are supported by this oven.
     *
     * @return supported oven modes, never {@code null}
     */
    public List<Oven.OvenMode> getPrograms() {
        return this.programs;
    }

    /*
     * Set program and temperature.
     *
     * @param program     program name supported by the oven
     * @param temperature temperature for the programm or {@code null} if program does not support a temperature setting
     */
    public void setProgram(@Nonnull final String program, @Nullable final Oven.Temperature temperature) throws UnknownProgramExeption {
        //find the Program by name
        for (final Oven.OvenMode ovenMode : this.programs) {
            if (ovenMode.getProgram().equalsIgnoreCase(program)) {
                this.program = ovenMode;
                this.program.setTargetTemperature(temperature);
                //access the Listeners
                for (final OvenListener ovenListener : this.ovenListenerList) {
                    ovenListener.onProgramChanged(ovenMode.getProgram(), temperature);
                }
                return;
            }
        }
        throw new UnknownProgramExeption();
    }

    /**
     * gets the current Temperature in the Oven
     *
     * @return Temperature in the Oven
     */
    public Oven.Temperature getTemperature() {
        return this.temperature;
    }

    /*
     * sets the temperature of the Oven (SONSOR ONLY)
     *
     * @param temperature
     */
    public void setTemperature(final Oven.Temperature temperature) {
        this.temperature = temperature;
    }

    /**
     * Sets the timer duration and activates the timer.
     *
     * @param duration after which timer expires.
     * @throws UnsupportedOperationException Thrown if this oven does not support setting a timer.
     */
    void setTimer(@Nullable final Duration duration) { //throws UnsupportedOperationException
        // set the Timer
    }

    /**
     * Retrieves the remaining time until timer expires.
     *
     * @return remaining time or can also be a negative number specifying how long ago timer expired or {@code null} if no timer is set.
     */
    @Nullable
    Duration getRemainingTime() {
        return null;
    }

}
