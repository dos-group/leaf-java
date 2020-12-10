package org.leaf.placement;

import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.core.CloudSimEntity;
import org.cloudbus.cloudsim.core.Simulation;
import org.cloudbus.cloudsim.core.events.SimEvent;
import org.leaf.application.Application;
import org.leaf.infrastructure.InfrastructureGraph;

import static org.leaf.LeafTags.START_APPLICATION;

/**
 * DatacenterBroker for the LEAF infrastructure and application model.
 */
public abstract class Orchestrator {

    public static Orchestrator NULL = new Orchestrator(InfrastructureGraph.NULL) {
        @Override
        public void placeApplication(Application application) {/**/}
    };

    InfrastructureGraph infrastructureGraph;

    public Orchestrator(InfrastructureGraph infrastructureGraph) {
        this.infrastructureGraph = infrastructureGraph;
    }

    public abstract void placeApplication(Application application);

    public InfrastructureGraph getInfrastructureGraph() {
        return infrastructureGraph;
    }

}
