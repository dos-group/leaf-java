package org.examples.smart_city_traffic.mobility;

import static org.examples.smart_city_traffic.Settings.*;

/**
 * Determines how many taxis are being generated at a specific time step
 * in the simulation and how fast they are according to predefined distributions.
 */
public class TaxiDistribution {

    private double stepsPerMinute = 60 / TIME_STEP_INTERVAL;

    /**
     * Returns the average number of taxis that should be generated at this time step.
     * The result will be passed to a Poisson distribution to determine the actual number.
     */
    public double getCount(double time) {
        int minute = (int) (time / stepsPerMinute) % TAXI_COUNT_DISTRIBUTION.size();
        return TAXI_COUNT_DISTRIBUTION.get(minute) / stepsPerMinute * MAX_CARS_PER_MINUTE;
    }

    /**
     * Returns the average speed of taxis at the time of the day.
     */
    public double getSpeed(double time) {
        int minute = (int) (time / stepsPerMinute) % TAXI_SPEED_DISTRIBUTION.size();
        return TAXI_SPEED_DISTRIBUTION.get(minute);
    }
}
