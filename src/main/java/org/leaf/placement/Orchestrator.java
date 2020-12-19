package org.leaf.placement;

import org.leaf.application.Application;
import org.leaf.infrastructure.InfrastructureGraph;

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
