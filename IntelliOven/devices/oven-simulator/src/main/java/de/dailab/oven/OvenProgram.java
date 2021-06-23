package de.dailab.oven;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;

public final class OvenProgram {

    private static class Range {

        private final int first;
        private final int last;

        public Range(final int first, final int last) {
            if(last<first) {
                throw new IllegalArgumentException("Target index must be equal or greater than start index!");
            }
            this.first = first;
            this.last = last;
        }
    }

    public static final OvenProgram FULL_STEAM = new OvenProgram("FULL_STEAM",15, 359, new Range(0,12));
    public static final OvenProgram STATIC = new OvenProgram("STATIC", 0, 359, new Range(0,64));
    public static final OvenProgram STATIC_FAN = new OvenProgram("STATIC_FAN", 1, 359, new Range(0,64));
    public static final OvenProgram FAN_HEATING = new OvenProgram("FAN_HEATING", 2, 359, new Range(0,64));
    public static final OvenProgram MULTI_COOKING = new OvenProgram("MULTI_COOKING", 3, 359, new Range(0,64));
    public static final OvenProgram PIZZA = new OvenProgram("PIZZA", 4, 359, new Range(2,64));
    public static final OvenProgram FULL_GRILL_FAN = new OvenProgram("FULL_GRILL_FAN", 8, 359, new Range(0,64));
    public static final OvenProgram FULL_GRILL = new OvenProgram("FULL_GRILL", 6, 359, new Range(0,64));
    public static final OvenProgram ECO_GRILL = new OvenProgram("ECO_GRILL", 5, 359, new Range(0,64));
    public static final OvenProgram ECO_FAN_HEATING = new OvenProgram("ECO_FAN_HEATING", 14, 359, new Range(24,31), new Range(42,42));
    public static final OvenProgram BOTTOM_HEATING = new OvenProgram("BOTTOM_HEATING", 10, 359, new Range(0,64));
    public static final OvenProgram LOW_TEMP_COOKING = new OvenProgram("LOW_TEMP_COOKING", 16, 359, new Range(2,22));
    public static final OvenProgram WARM_KEEPING = new OvenProgram("WARM_KEEPING", 13, null, new Range(0,12));
    public static final OvenProgram DEFROST = new OvenProgram("DEFROST", 11, 359);

    private static final int MAX_TEMPERATURE_INDEXES = 57;
    private static final int TEMPERATURE_INDEX_CELSIUS_FACTOR = 5;

    private static final List<OvenProgram> PROGRAMS = new LinkedList<>();

    static {
        PROGRAMS.add(FULL_STEAM);
        PROGRAMS.add(STATIC);
        PROGRAMS.add(STATIC_FAN);
        PROGRAMS.add(FAN_HEATING);
        PROGRAMS.add(MULTI_COOKING);
        PROGRAMS.add(PIZZA);
        PROGRAMS.add(FULL_GRILL_FAN);
        PROGRAMS.add(FULL_GRILL);
        PROGRAMS.add(ECO_GRILL);
        PROGRAMS.add(ECO_FAN_HEATING);
        PROGRAMS.add(BOTTOM_HEATING);
        PROGRAMS.add(LOW_TEMP_COOKING);
        PROGRAMS.add(WARM_KEEPING);
        PROGRAMS.add(DEFROST);
    }

    private final String programName;
    private final int programIndex;
    private final Integer maxDuration; // max timer duration in minutes or null for warm_keeping (is either unlimited or probably timer can't be configured for this program)
    @Nullable
    private final Integer defaultTemperature;
    private final Integer[] validTargetTemperatureValues;

    OvenProgram(@Nonnull final String name, final int index, @Nullable final Integer durationLimit, @Nonnull final Range... tempIndexRanges) {
        this.programName = name;
        this.programIndex = index;
        this.maxDuration = durationLimit;
        final List<Integer> values = new ArrayList<>(MAX_TEMPERATURE_INDEXES);
        for(final Range tempIndexRange : tempIndexRanges) {
            for(int tempIndex=tempIndexRange.first; tempIndex<=tempIndexRange.last; tempIndex++) {
                values.add(tempIndex * TEMPERATURE_INDEX_CELSIUS_FACTOR);
            }
        }
        this.validTargetTemperatureValues = values.toArray(new Integer[0]);
        if(this.validTargetTemperatureValues.length == 0) {
            this.defaultTemperature = null;
        } else {
            this.defaultTemperature = this.validTargetTemperatureValues[this.validTargetTemperatureValues.length/2];
        }
    }

    @Nonnull
    public String getName() {
        return this.programName;
    }

    @Nonnull
    public static List<OvenProgram> getPrograms() {
        return PROGRAMS;
    }

    public int getProgramIndex() {
        return this.programIndex;
    }

    @Nullable
    public Integer getMaxDuration() {
        return this.maxDuration;
    }

    @Nullable
    public Integer getDefaultTemperature() {
        return this.defaultTemperature;
    }

    @Nonnull
    public Integer[] getValidTargetTemperatureValues() {
        return this.validTargetTemperatureValues;
    }

}
