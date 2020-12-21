package org.examples.smart_city_traffic;

import org.cloudbus.cloudsim.core.CloudSimTags;
import org.leaf.LeafTags;

/**
 * Extends the DES tags defined by {@link CloudSimTags} and {@link LeafTags}.
 */
public class CityTags {

    /**
     * CloudSim reserves numbers lower than 300 and the number 9600
     * Leaf reserves 10000 to 10100
     */
    public static final int BASE = 20000;

    /**
     * Update events
     */
    public static final int CREATE_CARS = BASE + 101;
    public static final int DESTROY_CAR = BASE + 102;

    /**
     * Read events
     */
    public static final int COUNT_CARS = BASE + 103;

    /**
     * Visualization events
     */
    public static final int REPAINT_CHARTS = BASE + 300;

    /**
     * Private constructor to avoid class instantiation.
     */
    private CityTags() {
        throw new UnsupportedOperationException("CityTags cannot be instantiated");
    }
}
