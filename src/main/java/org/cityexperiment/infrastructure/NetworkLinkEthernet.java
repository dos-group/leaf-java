package org.cityexperiment.infrastructure;

import org.cloudbus.cloudsim.core.SimEntity;
import org.leaf.infrastructure.NetworkLink;
import org.leaf.power.PowerModelNetworkLink;

import static org.cityexperiment.Settings.*;

/**
 * An Ethernet network link.
 */
public class NetworkLinkEthernet extends NetworkLink {

    public NetworkLinkEthernet(SimEntity src, SimEntity dst) {
        super(src, dst);
        setBandwidth(ETHERNET_BANDWIDTH);
        setLatency(ETHERNET_LATENCY);
        setPowerModel(new PowerModelNetworkLink(ETHERNET_WATT_PER_BIT));
    }

}
