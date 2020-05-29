package org.cityexperiment.infrastructure;

import org.leaf.location.Location;
import org.cloudbus.cloudsim.core.Simulation;
import org.cloudbus.cloudsim.power.models.PowerModelHost;
import org.leaf.infrastructure.ComputeNode;
import org.leaf.host.HostFactory;

import java.util.List;

import static org.cityexperiment.Settings.*;

/**
 * A cloud data center.
 */
public class DatacenterCloud extends ComputeNode {

    public DatacenterCloud(Simulation simulation) {
        super(simulation, List.of(HostFactory.createHost(CLOUD_MIPS, new PowerModelHost(CLOUD_WATT_PER_MIPS))));
    }

    @Override
    public Location getLocation() {
        throw new RuntimeException("Cloud data centers do not have a location");
    }
}
