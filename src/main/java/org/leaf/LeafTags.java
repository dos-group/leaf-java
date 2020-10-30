package org.leaf;

import org.cloudbus.cloudsim.core.CloudSimTags;

/**
 * Extends the DES tags defined by {@link CloudSimTags}.
 *<b>NOTE:</b> To avoid conflicts with other tags, LEAF reserves numbers between 10000 and 10100.
 *
 * @see CloudSimTags
 */
public class LeafTags {

    /**
     * CloudSim reserves numbers lower than 300 and the number 9600
     */
    public static final int BASE = 10000;

    public static final int START_APPLICATION = BASE + 1;
    public static final int STOP_APPLICATION = BASE + 2;
    public static final int UPDATE_NETWORK_TOPOLOGY = BASE + 3;
    public static final int SHUTDOWN_FOG_NODE = BASE + 4;

    /**
     * Private constructor to avoid class instantiation.
     */
    private LeafTags() {
        throw new UnsupportedOperationException("GreenTags cannot be instantiated");
    }
}
