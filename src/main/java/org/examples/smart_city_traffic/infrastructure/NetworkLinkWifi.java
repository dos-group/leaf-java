package org.examples.smart_city_traffic.infrastructure;

import org.cloudbus.cloudsim.core.SimEntity;
import org.leaf.infrastructure.NetworkLink;

import static org.examples.smart_city_traffic.Settings.WIFI_BANDWIDTH;
import static org.examples.smart_city_traffic.Settings.WIFI_LATENCY;

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
