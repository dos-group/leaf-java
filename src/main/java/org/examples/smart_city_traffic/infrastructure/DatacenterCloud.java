package org.examples.smart_city_traffic.infrastructure;

import org.cloudbus.cloudsim.core.Simulation;
import org.leaf.host.HostFactory;
import org.leaf.infrastructure.ComputeNode;
import org.leaf.location.Location;
import org.leaf.power.PowerModelHostShared;

import java.util.List;

import static org.examples.smart_city_traffic.Settings.CLOUD_MIPS;
import static org.examples.smart_city_traffic.Settings.CLOUD_WATT_PER_MIPS;

/**
 * A cloud data center.
 */
public class DatacenterCloud extends ComputeNode {

    public DatacenterCloud(Simulation simulation) {
        super(simulation, List.of(HostFactory.createHost(CLOUD_MIPS, new PowerModelHostShared(CLOUD_WATT_PER_MIPS))));
    }

    @Override
    public Location getLocation() {
        throw new RuntimeException("Cloud data centers do not have a location");
    }
}
