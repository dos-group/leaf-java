package org.cityexperiment.infrastructure;

import org.cloudbus.cloudsim.core.SimEntity;
import org.leaf.infrastructure.NetworkLink;
import org.leaf.power.PowerModelNetworkLink;

import static org.cityexperiment.Settings.*;

/**
 * A WAN network link.
 */
public class NetworkLinkWanUp extends NetworkLink {

    public NetworkLinkWanUp(SimEntity src, SimEntity dst) {
        super(src, dst);
        setBandwidth(WAN_BANDWIDTH);
        setLatency(WAN_LATENCY);
        setPowerModel(new PowerModelNetworkLink(WAN_WATT_PER_BIT_UP));
    }

}
