package org.cityexperiment.infrastructure;

import org.leaf.location.Location;
import org.cloudbus.cloudsim.core.Simulation;
import org.cloudbus.cloudsim.power.models.PowerModelHost;
import org.leaf.infrastructure.ComputeNode;
import org.leaf.host.HostFactory;

import java.util.List;

import static org.cityexperiment.Settings.*;

/**
 * A fog data center.
 */
public class DatacenterFog extends ComputeNode {

    private Location location;

    public DatacenterFog(Simulation simulation, Location location, int shutdownDeadline) {
        super(simulation, List.of(HostFactory.createHost(FOG_MIPS,
            new PowerModelHost(FOG_STATIC_POWER, FOG_MAX_POWER), shutdownDeadline)));
        this.location = location;
    }

    @Override
    public Location getLocation() {
        if (location == Location.NULL) {
            throw new RuntimeException("No location has been set for " + this);
        }
        return location;
    }
}
