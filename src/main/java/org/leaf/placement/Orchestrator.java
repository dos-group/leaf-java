package org.leaf.placement;

import org.cloudbus.cloudsim.brokers.DatacenterBrokerAbstract;
import org.cloudbus.cloudsim.cloudlets.Cloudlet;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.core.CloudSimEntity;
import org.cloudbus.cloudsim.core.CloudSimTags;
import org.cloudbus.cloudsim.core.events.SimEvent;
import org.cloudbus.cloudsim.vms.Vm;
import org.leaf.application.Application;

import java.util.List;

import static org.leaf.LeafTags.*;

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
