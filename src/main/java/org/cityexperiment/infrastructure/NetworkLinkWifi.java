package org.cityexperiment.infrastructure;

import org.cloudbus.cloudsim.core.SimEntity;
import org.leaf.infrastructure.NetworkLink;

import static org.cityexperiment.Settings.*;

/**
 * A WiFi network link.
 */
public abstract class NetworkLinkWifi extends NetworkLink {

    public NetworkLinkWifi(SimEntity src, SimEntity dst) {
        super(src, dst);
        setBandwidth(WIFI_BANDWIDTH);
        setLatency(WIFI_LATENCY);
    }

}
