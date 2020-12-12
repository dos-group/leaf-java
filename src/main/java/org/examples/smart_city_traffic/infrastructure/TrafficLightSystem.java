package org.examples.smart_city_traffic.infrastructure;

import org.examples.smart_city_traffic.application.CctvApplicationGenerator;
import org.cloudbus.cloudsim.core.Simulation;
import org.cloudbus.cloudsim.core.events.SimEvent;
import org.cloudbus.cloudsim.power.models.PowerModelHost;
import org.leaf.application.Application;
import org.leaf.host.HostFactory;
import org.leaf.infrastructure.ComputeNode;
import org.leaf.location.Location;

import java.util.List;

import static org.examples.smart_city_traffic.Settings.RANDOM;
import static org.examples.smart_city_traffic.Settings.TLS_MIPS;
import static org.leaf.LeafTags.START_APPLICATION;

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
    }

    @Override
    protected void startEntity() {
        schedule(this, RANDOM.nextDouble(), START_APPLICATION);
        // Don't call super.startEntity(), the DATACENTER_REGISTRATION_REQUEST event will cause a memory leak
    }

    @Override
    public void processEvent(SimEvent evt) {
        if (evt.getTag() == START_APPLICATION) {
            application = cctvApplicationGenerator.create(this);
            schedule(application, 0, START_APPLICATION);
        }
        super.processEvent(evt);
    }

    @Override
    public Location getLocation() {
        return location;
    }

    public Application getApplication() {
        return application;
    }

}
