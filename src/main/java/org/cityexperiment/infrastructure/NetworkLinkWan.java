package org.cityexperiment.infrastructure;

import org.cloudbus.cloudsim.core.SimEntity;
import org.leaf.power.PowerModelNetworkLink;
import org.leaf.infrastructure.NetworkLink;

import static org.cityexperiment.Settings.*;

/**
 * A WAN network link.
 */
public class NetworkLinkWan extends NetworkLink {

    public NetworkLinkWan(SimEntity src, SimEntity dst) {
        super(src, dst);
        setBandwidth(WAN_BANDWIDTH);
        setLatency(WAN_LATENCY);
        setPowerModel(new PowerModelNetworkLink(WAN_WATT_PER_BIT));
    }

}
