package org.examples.smart_city_traffic.application;

import org.examples.smart_city_traffic.infrastructure.DatacenterCloud;
import org.examples.smart_city_traffic.infrastructure.InfrastructureGraphCity;
import org.examples.smart_city_traffic.infrastructure.TrafficLightSystem;
import org.leaf.application.Application;
import org.leaf.application.Task;
import org.leaf.placement.Orchestrator;

import java.util.List;

import static org.examples.smart_city_traffic.Settings.*;

/**
 * Factory class for constructing CCTV Applications.
 */
public class CctvApplicationGenerator {

    Orchestrator orchestrator;

    public CctvApplicationGenerator(Orchestrator orchestrator) {
        this.orchestrator = orchestrator;
    }

    public Application create(TrafficLightSystem trafficLightSystem) {
        InfrastructureGraphCity networkTopology = (InfrastructureGraphCity) trafficLightSystem.getSimulation().getNetworkTopology();
        List<DatacenterCloud> clouds = networkTopology.getCloudDcs();
        if (clouds.size() != 1) {
            throw new RuntimeException("CCTV application expects exactly 1 cloud data center to exist but there are " + clouds.size() + ".");
        }
        DatacenterCloud cloud = clouds.get(0);

        return new Application(trafficLightSystem.getSimulation(), orchestrator)
            .addSourceTask(new Task(CCTV_OPERATOR_MIPS), CCTV_TO_PROCESSOR_BIT_RATE, trafficLightSystem)
            .addProcessingTask(new Task(IMAGE_ANALYSIS_OPERATOR_MIPS), IMAGE_ANALYSIS_TO_STORAGE_BIT_RATE)
            .addSinkTask(new Task(STORAGE_OPERATOR_MIPS), cloud);
    }

}
