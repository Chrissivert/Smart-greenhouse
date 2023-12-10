package no.ntnu.greenhouse;

import java.util.Objects;

/**
 * Represents one sensor reading (value).
 */
public class SensorReading {
    private final String type;
    private double value;
    private final String unit;

    /**
     * Create a new sensor reading.
     *
     * @param type  The type of sensor being red
     * @param value The current value of the sensor
     * @param unit  The unit, for example: %, lux
     */
    public SensorReading(String type, double value, String unit) {
        this.type = type;
        this.value = value;
        this.unit = unit;
    }

    /**
     * Get the type of the sensor.
     *
     * @return The type of the sensor
     */

    public String getType() {
        return type;
    }

    /**
     * Get the current value of the sensor.
     *
     * @return The current value of the sensor
     */

    public double getValue() {
        return value;
    }

    /**
     * Get the unit of the sensor.
     *
     * @return The unit of the sensor
     */

    public String getUnit() {
        return unit;
    }

    /**
     * Set the value of the sensor.
     *
     * @param newValue The new value of the sensor
     */

    public void setValue(double newValue) {
        this.value = newValue;
    }

    /**
     * Returns a string of actuator of type being on or off.
     *
     * @return String of actuator of type being on or off.
     */

    @Override
    public String toString() {
        return "{ type=" + type + ", value=" + value + ", unit=" + unit + " }";
    }

    /**
     * Get a human-readable (formatted) version of the current reading, including the unit.
     *
     * @return The sensor reading and the unit
     */
    public String getFormatted() {
        return value + unit;
    }


    /**
     * Compare this sensor reading to another sensor reading.
     *
     * @param o The other sensor reading
     * @return True if the two sensor readings are equal, false otherwise
     */

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        SensorReading that = (SensorReading) o;
        return Double.compare(value, that.value) == 0
                && Objects.equals(type, that.type)
                && Objects.equals(unit, that.unit);
    }

    /**
     * Generate a hash code for this sensor reading.
     *
     * @return The hash code
     */
    @Override
    public int hashCode() {
        return Objects.hash(type, value, unit);
    }
}
