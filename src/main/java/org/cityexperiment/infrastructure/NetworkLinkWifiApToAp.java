package org.cityexperiment.infrastructure;

import org.cloudbus.cloudsim.core.SimEntity;
import org.leaf.power.PowerModelNetworkLink;

import static org.cityexperiment.Settings.*;

/**
 * A WiFi network link between two access points.
 */
public class NetworkLinkWifiApToAp extends NetworkLinkWifi {

    public NetworkLinkWifiApToAp(SimEntity src, SimEntity dst) {
        super(src, dst);
        setPowerModel(new PowerModelNetworkLink(WIFI_AP_TO_AP_WATT_PER_BIT));
    }

}
