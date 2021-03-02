package org.leaf;

import org.cloudbus.cloudsim.core.CloudSim;
import org.leaf.infrastructure.InfrastructureGraph;

/**
 * LEAF Simulation
 */
public class LeafSim extends CloudSim {

    InfrastructureGraph infrastructure;

    public LeafSim() {
        infrastructure = new InfrastructureGraph();
        setNetworkTopology(infrastructure);
    }

    public InfrastructureGraph getInfrastructure() {
        return infrastructure;
    }
}
