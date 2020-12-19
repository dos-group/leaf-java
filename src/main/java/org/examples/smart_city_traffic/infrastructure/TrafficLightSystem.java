package org.examples.smart_city_traffic.infrastructure;

import org.examples.smart_city_traffic.application.CctvApplicationGenerator;
import org.cloudbus.cloudsim.core.Simulation;
import org.cloudbus.cloudsim.power.models.PowerModelHost;
import org.leaf.application.Application;
import org.leaf.host.HostFactory;
import org.leaf.infrastructure.ComputeNode;
import org.leaf.location.Location;

import java.util.List;

import static org.examples.smart_city_traffic.Settings.TLS_MIPS;

/**
 * Location-aware traffic light system that hosts a CCTV application.
 */
public class TrafficLightSystem extends ComputeNode {

    private Location location;
    private CctvApplicationGenerator cctvApplicationGenerator;

    Application application = Application.NULL;

    public TrafficLightSystem(Simulation simulation, Location location, CctvApplicationGenerator cctvApplicationGenerator) {
        super(simulation, List.of(HostFactory.createHost(TLS_MIPS, PowerModelHost.NULL)));
        this.location = location;
        this.cctvApplicationGenerator = cctvApplicationGenerator;
        this.application = cctvApplicationGenerator.create(this);
    }

    @Override
    protected void startInternal() {
        // Don't call super.startEntity(), the DATACENTER_REGISTRATION_REQUEST event will cause a memory leak
    }

    @Override
    public Location getLocation() {
        return location;
    }

    public Application getApplication() {
        return application;
    }

}
