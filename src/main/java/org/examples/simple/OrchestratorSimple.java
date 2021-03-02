package org.examples.simple;

import org.leaf.application.Application;
import org.leaf.application.Task;
import org.leaf.infrastructure.ComputeNode;
import org.leaf.infrastructure.InfrastructureGraph;
import org.leaf.placement.Orchestrator;

public class OrchestratorSimple extends Orchestrator {

    private ComputeNode processorPlacement;

    public OrchestratorSimple(InfrastructureGraph infrastructureGraph, ComputeNode processorPlacement) {
        super(infrastructureGraph);
        this.processorPlacement = processorPlacement;
    }

    @Override
    public void placeApplication(Application application) {
        for (Task task : application.getTasks()) {
            if (!task.isBound()) {
                task.setComputeNode(processorPlacement);
            }
        }
    }

}
