package org.leaf.placement;

import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.core.CloudSimEntity;
import org.cloudbus.cloudsim.core.events.SimEvent;
import org.leaf.application.Application;

import static org.leaf.LeafTags.START_APPLICATION;

/**
 * DatacenterBroker for the LEAF infrastructure and application model.
 */
public abstract class Orchestrator extends CloudSimEntity {

    public Orchestrator(CloudSim simulation) {
        super(simulation);
    }

    @Override
    protected void startEntity() {}

    public void startApplication(Application application) {
        placeApplication(application);
        schedule(application, 0, START_APPLICATION);
    }

    @Override
    public void processEvent(final SimEvent evt) {
    }

    public abstract void placeApplication(Application application);

}
