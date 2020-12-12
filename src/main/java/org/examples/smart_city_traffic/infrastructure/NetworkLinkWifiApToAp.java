package org.examples.smart_city_traffic.infrastructure;

import org.cloudbus.cloudsim.core.SimEntity;
import org.leaf.power.PowerModelNetworkLink;

import static org.examples.smart_city_traffic.Settings.WIFI_AP_TO_AP_WATT_PER_BIT;

/**
 * A WiFi network link between two access points.
 */
public class NetworkLinkWifiApToAp extends NetworkLinkWifi {

    public NetworkLinkWifiApToAp(SimEntity src, SimEntity dst) {
        super(src, dst);
        setPowerModel(new PowerModelNetworkLink(WIFI_AP_TO_AP_WATT_PER_BIT));
    }

}
