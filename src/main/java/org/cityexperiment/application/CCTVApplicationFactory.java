package org.cityexperiment.application;

import org.cityexperiment.infrastructure.InfrastructureGraphCity;
import org.cityexperiment.infrastructure.DatacenterCloud;
import org.cityexperiment.infrastructure.TrafficLightSystem;
import org.leaf.application.Application;
import org.leaf.application.Task;
import org.leaf.application.TaskFactory;

import java.util.List;

import static org.cityexperiment.Settings.*;

/**
 * Factory class for constructing CCTV Applications.
 */
public class CCTVApplicationFactory {

    public static Application create(TrafficLightSystem trafficLightSystem) {
        InfrastructureGraphCity networkTopology = (InfrastructureGraphCity) trafficLightSystem.getSimulation().getNetworkTopology();
        List<DatacenterCloud> clouds = networkTopology.getCloudDcs();
        if (clouds.size() != 1) {
            throw new RuntimeException("CCTV application expects exactly 1 cloud data center to exist but there are " + clouds.size() + ".");
        }
        DatacenterCloud cloud = clouds.get(0);

        return new Application(trafficLightSystem.getSimulation())
            .addSourceTask(sourceTask(), trafficLightSystem, CCTV_TO_PROCESSOR_BIT_RATE)
            .addProcessingTask(processingTask(), IMAGE_ANALYSIS_TO_STORAGE_BIT_RATE)
            .addSinkTask(sinkTask(), cloud);
    }

    private static Task sourceTask() {
        return TaskFactory.createTask(CCTV_OPERATOR_MIPS);
    }

    private static Task processingTask() {
        return TaskFactory.createTask(IMAGE_ANALYSIS_OPERATOR_MIPS);
    }

    private static Task sinkTask() {
        return TaskFactory.createTask(STORAGE_OPERATOR_MIPS);
    }

}
