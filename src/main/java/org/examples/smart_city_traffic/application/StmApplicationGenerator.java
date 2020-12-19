package org.examples.smart_city_traffic.application;

import org.examples.smart_city_traffic.infrastructure.InfrastructureGraphCity;
import org.examples.smart_city_traffic.infrastructure.Taxi;
import org.examples.smart_city_traffic.infrastructure.TrafficLightSystem;
import org.leaf.application.Application;
import org.leaf.application.Task;
import org.leaf.location.Location;
import org.leaf.placement.Orchestrator;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.examples.smart_city_traffic.Settings.*;

/**
 * Factory class for constructing STM Applications.
 */
public class StmApplicationGenerator {

    Orchestrator orchestrator;

    public StmApplicationGenerator(Orchestrator orchestrator) {
        this.orchestrator = orchestrator;
    }

    public Application create(Taxi taxi) {
        Application application = new Application(taxi.getSimulation(), orchestrator)
            .addSourceTask(sourceTask(taxi), CAR_TO_TRAFFIC_MANAGER_BIT_RATE)
            .addProcessingTask(processingTask(), TRAFFIC_MANAGER_TO_TRAFFIC_LIGHT_BIT_RATE);
        // For each traffic light system a sink task is created
        for (TrafficLightSystem tls : getTrafficLightSystemsOnPath(taxi)) {
            application.addSinkTask(sinkTask(tls));
        }
        return application;
    }

    private static Task sourceTask(Taxi taxi) {
        return new Task(CAR_OPERATOR_MIPS, taxi);
    }

    private static Task processingTask() {
        return new Task(TRAFFIC_MANAGER_OPERATOR_MIPS);
    }

    private static Task sinkTask(TrafficLightSystem tls) {
        return new Task(TRAFFIC_LIGHT_OPERATOR_MIPS, tls);
    }

    /**
     * Calculates which traffic light systems are in the path of the taxi.
     */
    private static List<TrafficLightSystem> getTrafficLightSystemsOnPath(Taxi taxi) {
        List<Location> locations = taxi.getMobilityModel().getPath().getVertexList();
        locations.remove(locations.size() - 1);
        locations.remove(0);
        Set<Location> locationSet = new HashSet<>(locations);

        InfrastructureGraphCity topologyExp = (InfrastructureGraphCity) taxi.getSimulation().getNetworkTopology();
        return topologyExp.getTraficLightSystems().stream()
            .filter(tls -> locationSet.contains(tls.getLocation()))
            .collect(Collectors.toList());
    }
}
