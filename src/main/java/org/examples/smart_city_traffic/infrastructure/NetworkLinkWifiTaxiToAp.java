package org.examples.smart_city_traffic.infrastructure;

import org.cloudbus.cloudsim.core.SimEntity;
import org.leaf.power.PowerModelNetworkLink;

import static org.examples.smart_city_traffic.Settings.WIFI_CAR_TO_AP_WATT_PER_BIT;

/**
 * A WiFi network link between a taxi and an access point.
 */
public class NetworkLinkWifiTaxiToAp extends NetworkLinkWifi {

    public NetworkLinkWifiTaxiToAp(SimEntity src, SimEntity dst) {
        super(src, dst);
        setPowerModel(new PowerModelNetworkLink(WIFI_CAR_TO_AP_WATT_PER_BIT));
    }

}
