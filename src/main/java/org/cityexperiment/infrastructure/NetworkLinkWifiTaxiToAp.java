package org.cityexperiment.infrastructure;

import org.cloudbus.cloudsim.core.SimEntity;
import org.leaf.power.PowerModelNetworkLink;

import static java.lang.String.format;
import static java.util.Objects.requireNonNull;
import static org.cityexperiment.Settings.*;

/**
 * A WiFi network link between a taxi and an access point.
 */
public class NetworkLinkWifiTaxiToAp extends NetworkLinkWifi {

    public NetworkLinkWifiTaxiToAp(SimEntity src, SimEntity dst) {
        super(src, dst);
        setPowerModel(new PowerModelNetworkLink(WIFI_CAR_TO_AP_WATT_PER_BIT));
    }

}
