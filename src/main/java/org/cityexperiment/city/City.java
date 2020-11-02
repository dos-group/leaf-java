package org.cityexperiment.city;

import org.cityexperiment.infrastructure.DatacenterCloud;
import org.cityexperiment.infrastructure.DatacenterFog;
import org.cityexperiment.infrastructure.InfrastructureGraphCity;
import org.cityexperiment.infrastructure.TrafficLightSystem;
import org.cityexperiment.placement.OrchestratorCity;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.datacenters.Datacenter;
import org.jgrapht.Graph;
import org.jgrapht.graph.SimpleGraph;
import org.leaf.location.Location;
import org.leaf.placement.Orchestrator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import static org.cityexperiment.Settings.*;

/**
 * Representing a city center with streets where all crossings are equipped with traffic light systems.
 */
public class City {

    private double width;
    private double height;
    private int streetsPerAxis;

    private DatacenterCloud cloudDc;
    private Orchestrator broker;
    private Graph<Location, Street> streetGraph;
    private InfrastructureGraphCity networkTopology;

    private List<Location> gateLocations = new ArrayList<>();   // Gates are entry/exit points for taxis
    private List<Location> trafficLightLocations = new ArrayList<>();

    public City(CloudSim simulation, double width, double height, int streetsPerAxis) {
        this.width = width;
        this.height = height;
        this.streetsPerAxis = streetsPerAxis;

        this.cloudDc = new DatacenterCloud(simulation);
        this.broker = new OrchestratorCity(simulation, this.cloudDc);
        this.streetGraph = initStreetGraph();
        this.networkTopology = initInfrastructureGraph(simulation);
    }

    public InfrastructureGraphCity getNetworkTopology() {
        return networkTopology;
    }

    public double getWidth() {
        return width;
    }

    public double getHeight() {
        return height;
    }

    public Graph<Location, Street> getStreetGraph() {
        return streetGraph;
    }

    public List<Location> getGateLocations() {
        return gateLocations;
    }

    public List<Location> getTrafficLightLocations() {
        return trafficLightLocations;
    }

    public Datacenter getCloudDc() {
        return cloudDc;
    }

    /**
     * Streets are implemented as graphs.
     */
    private Graph<Location, Street> initStreetGraph() {
        Graph<Location, Street> graph = new SimpleGraph<>(Street.class);
        final int N_POINTS = streetsPerAxis + 2;
        final double STEP_SIZE_X = width / (N_POINTS - 1);
        final double STEP_SIZE_Y = height / (N_POINTS - 1);

        Location[][] locations = new Location[N_POINTS][N_POINTS];
        for (int x = 0; x < N_POINTS; x++) {
            for (int y = 0; y < N_POINTS; y++) {
                Location location = new Location(x * STEP_SIZE_X, y * STEP_SIZE_Y);
                if (x == 0 || x == N_POINTS - 1 || y == 0 || y == N_POINTS - 1) {
                    gateLocations.add(location);
                } else {
                    trafficLightLocations.add(location);
                }
                locations[x][y] = location;
                graph.addVertex(location);
                if (x > 0 && y > 0) {
                    if (y < N_POINTS - 1) graph.addEdge(location, locations[x - 1][y]);
                    if (x < N_POINTS - 1) graph.addEdge(location, locations[x][y - 1]);
                }
            }
        }

        Location[] removeLocations = {
            locations[0][0],
            locations[N_POINTS - 1][0],
            locations[0][N_POINTS - 1],
            locations[N_POINTS - 1][N_POINTS - 1],
        };
        for (Location location : removeLocations) {
            graph.removeVertex(location);
            gateLocations.remove(location);
        }

        return graph;
    }

    /**
     * Initializes the network topology with a cloud and a number of traffic light systems and fog nodes.
     */
    private InfrastructureGraphCity initInfrastructureGraph(CloudSim simulation) {
        InfrastructureGraphCity networkTopology = new InfrastructureGraphCity();
        simulation.setNetworkTopology(networkTopology);

        networkTopology.addCloudDc(cloudDc);

        for (Location trafficLightLocation : trafficLightLocations) {
            TrafficLightSystem tls = new TrafficLightSystem(simulation, trafficLightLocation, broker);
            networkTopology.addTrafficLightSystem(tls);
        }

        // On initialization Fog nodes are attached to random traffic lights
        for (Location fogDcLocation : pickNRandomElements(trafficLightLocations, FOG_DCS)) {
            DatacenterFog fogDc = new DatacenterFog(simulation, fogDcLocation, FOG_SHUTDOWN_DEADLINE);
            networkTopology.addFogDc(fogDc);
        }

        return networkTopology;
    }

    public Orchestrator getBroker() {
        return broker;
    }

    /**
     * Implements the Durstenfeld's algorithm for picking n random elements in a list.
     */
    private static <E> List<E> pickNRandomElements(List<E> list, int n) {
        int length = list.size();
        for (int i = length - 1; i >= length - n; --i) {
            Collections.swap(list, i , new Random(SEED).nextInt(i + 1));
        }
        return list.subList(length - n, length);
    }

}
