package org.cityexperiment.infrastructure;

import org.cityexperiment.application.CCTVApplicationFactory;
import org.cloudbus.cloudsim.core.Simulation;
import org.cloudbus.cloudsim.core.events.SimEvent;
import org.cloudbus.cloudsim.power.models.PowerModelHost;
import org.leaf.placement.Orchestrator;
import org.leaf.application.Application;
import org.leaf.infrastructure.ComputeNode;
import org.leaf.host.HostFactory;
import org.leaf.location.Location;

import java.util.List;

import static org.cityexperiment.Settings.*;
import static org.leaf.LeafTags.START_APPLICATION;

/**
 * Location-aware traffic light system that hosts a CCTV application.
 */
public class TrafficLightSystem extends ComputeNode {

    private Location location;
    private Orchestrator broker;

    Application application = Application.NULL;

    public TrafficLightSystem(Simulation simulation, Location location, Orchestrator broker) {
        super(simulation, List.of(HostFactory.createHost(TLS_MIPS, PowerModelHost.NULL)));
        this.location = location;
        this.broker = broker;
    }

    @Override
    protected void startEntity() {
        schedule(this, RANDOM.nextDouble(), START_APPLICATION);
        // Don't call super.startEntity(), the DATACENTER_REGISTRATION_REQUEST event will cause a memory leak
    }

    @Override
    public void processEvent(SimEvent evt) {
        if (evt.getTag() == START_APPLICATION) {
            application = CCTVApplicationFactory.create(this);
            broker.startApplication(application);
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
